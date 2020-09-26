package OxyEngine.Core.Layers;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.RenderingMode;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.HDRTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Components.*;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.UI.Panels.PropertiesPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;

public class SceneLayer extends Layer {

    private Set<OxyEntity> cachedLightEntities, cachedNativeMeshes;
    private Set<EntityComponent> cachedCameraComponents;
    private Set<OxyEntity> allModelEntities;

    public SceneLayer(Scene scene) {
        super(scene);
    }

    static final OxyShader outlineShader = new OxyShader("D:\\programming\\Java\\OxyEngine\\shaders\\OxyOutline.glsl");
    static HDRTexture hdrTexture;

    @Override
    public void build() {
        hdrTexture = OxyTexture.loadHDRTexture(OxySystem.FileSystem.getResourceByPath("/hdr/fireplace_4k.hdr"), scene);

        cachedNativeMeshes = scene.view(NativeObjectMesh.class);
        cachedCameraComponents = scene.distinct(OxyCamera.class);

        cachedLightEntities = scene.view(Light.class);
        allModelEntities = scene.view(ModelMesh.class);

        //Prep
        {
            for (OxyEntity e : cachedNativeMeshes) {
                e.get(NativeObjectMesh.class).initList();
            }
            for (OxyEntity e : allModelEntities) {
                e.get(ModelMesh.class).initList();
            }
        }
    }

    @Override
    public void rebuild() {
        allModelEntities = scene.view(ModelMesh.class);

        //Prep
        {
            List<OxyEntity> cachedConverted = new ArrayList<>(allModelEntities);
            if (cachedConverted.size() == 0) return;
            ModelMesh mesh = cachedConverted.get(cachedConverted.size() - 1).get(ModelMesh.class);
            mesh.initList();
        }
    }

    public void updateAllModelEntities() {
        allModelEntities = scene.view(ModelMesh.class);
        cachedLightEntities = scene.view(Light.class);
    }

    @Override
    public void update(float ts, float deltaTime) {
        scene.getFrameBuffer().blit();

        scene.getOxyUISystem().dispatchNativeEvents();
        scene.getOxyUISystem().updateImGuiContext(deltaTime);

        int i = 0;
        for (OxyEntity e : cachedLightEntities) {
            if (!e.has(EmittingComponent.class)) continue;
            Light l = e.get(Light.class);
            EmittingComponent emittingComponent = e.get(EmittingComponent.class);
            l.setAmbient(emittingComponent.ambient());
            l.setDiffuse(emittingComponent.diffuse());
            l.setSpecular(emittingComponent.specular());
            l.setPosition(emittingComponent.position());
            l.setDirection(emittingComponent.direction());
            OxyShader shader = e.get(OxyShader.class);
            l.update(shader, i);
            i++;
        }
    }

    static boolean initHdrTexture = false;
    public static final OxyShader cubemapShader = new OxyShader("shaders/OxySkybox.glsl");

    @Override
    public void render(float ts, float deltaTime) {
        if (!initHdrTexture && OxyRenderer.currentBoundedCamera != null) {
            hdrTexture.captureFaces(ts);
            cachedNativeMeshes = scene.view(NativeObjectMesh.class);
            initHdrTexture = true;
        }

        scene.getFrameBuffer().bind();
        OpenGLRendererAPI.clearBuffer();

        //Camera
        PerspectiveCamera mainCamera = null;
        {
            for (EntityComponent camera : cachedCameraComponents) {
                if (camera instanceof PerspectiveCamera p) {
                    if (p.isPrimary()) {
                        mainCamera = p;
                        OxyRenderer.currentBoundedCamera = mainCamera;
                        break;
                    }
                }
            }
        }

        //Rendering
        {

            glDepthFunc(GL_LEQUAL);
            for (OxyEntity e : cachedNativeMeshes) {
                Mesh mesh = e.get(Mesh.class);
                if (e.has(OxyMaterial.class)) {
                    OxyMaterial m = e.get(OxyMaterial.class);
                    m.push(mesh.getShader());
                }
                if(mesh.equals(hdrTexture.getMesh())){
                    glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
                    cubemapShader.enable();
                    cubemapShader.setUniform1i("skyBoxTexture", hdrTexture.getTextureSlot());
                    cubemapShader.setUniform1f("mipLevel", PropertiesPanel.mipLevelStrength[0]);
                    cubemapShader.setUniform1f("exposure", PropertiesPanel.exposure[0]);
                    cubemapShader.disable();
                    render(ts, mesh, mainCamera, cubemapShader);
                    glDisable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
                } else {
                    render(ts, mesh, mainCamera);
                }
            }
            for (OxyEntity e : allModelEntities) {
                RenderableComponent renderableComponent = e.get(RenderableComponent.class);
                if (renderableComponent.mode != RenderingMode.Normal) continue;
                ModelMesh modelMesh = e.get(ModelMesh.class);
                OxyMaterial material = e.get(OxyMaterial.class);
                SelectedComponent s = e.get(SelectedComponent.class);
                TransformComponent c = e.get(TransformComponent.class);
                modelMesh.getShader().enable();
                if (cachedLightEntities.size() == 0) modelMesh.getShader().setUniform1f("currentLightIndex", -1f);
                modelMesh.getShader().setUniformMatrix4fv("model", c.transform, false);
                modelMesh.getShader().setUniform1i("irradianceMap", hdrTexture.getIrradianceSlot());
                modelMesh.getShader().setUniform1i("prefilterMap", hdrTexture.getPrefilterSlot());
                modelMesh.getShader().setUniform1i("brdfLUT", hdrTexture.getBDRFSlot());
                modelMesh.getShader().disable();
                material.push(modelMesh.getShader());

                glEnable(GL_CULL_FACE);
                glCullFace(GL_BACK);
                if (s.selected) {
                    glEnable(GL_STENCIL_TEST);
                    glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
                    glStencilMask(0xFF);
                    glClear(GL_STENCIL_BUFFER_BIT);
                    glStencilFunc(GL_ALWAYS, 1, 0xFF);

                    render(ts, modelMesh, mainCamera);
                    outlineShader.enable();
                    e.get(TransformComponent.class).scale.mul(1.10f, 1.10f, 1.10f);
                    e.updateData();

                    glStencilFunc(GL_NOTEQUAL, 1, 0xFF);
                    glStencilMask(0x00);

                    render(ts, modelMesh, mainCamera, outlineShader); // draw with the outline shader
                    outlineShader.disable();
                    e.get(TransformComponent.class).scale.div(1.10f, 1.10f, 1.10f);
                    e.updateData();

                    glDisable(GL_STENCIL_TEST);
                } else {
                    render(ts, modelMesh, mainCamera);
                }
                glDisable(GL_CULL_FACE);
            }
        }
        scene.getFrameBuffer().unbind();
    }

    private void render(float ts, Mesh mesh, OxyCamera camera) {
        scene.getRenderer().render(ts, mesh, camera);
        OxyRenderer.Stats.totalShapeCount = scene.getShapeCount();
    }

    private void render(float ts, Mesh mesh, OxyCamera camera, OxyShader shader) {
        scene.getRenderer().render(ts, mesh, camera, shader);
        OxyRenderer.Stats.totalShapeCount = scene.getShapeCount();
    }

    private void render(float ts, Mesh mesh) {
        scene.getRenderer().render(ts, mesh);
        OxyRenderer.Stats.totalShapeCount = scene.getShapeCount();
    }
}
