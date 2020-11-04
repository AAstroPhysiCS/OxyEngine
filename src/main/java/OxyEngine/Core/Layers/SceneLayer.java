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
import OxyEngineEditor.UI.Panels.EnvironmentPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static OxyEngineEditor.Scene.SceneRuntime.ACTIVE_SCENE;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;

public class SceneLayer extends Layer {

    private Set<OxyEntity> cachedLightEntities, cachedNativeMeshes;
    private Set<OxyCamera> cachedCameraComponents;
    private Set<OxyEntity> allModelEntities;

    static final OxyShader outlineShader = new OxyShader("shaders/OxyOutline.glsl");
    public static HDRTexture hdrTexture;

    @Override
    public void build() {
        if (hdrTexture == null)
            hdrTexture = OxyTexture.loadHDRTexture(OxySystem.FileSystem.getResourceByPath("/hdr/birchwood_4k.hdr"), ACTIVE_SCENE);

        if (cachedNativeMeshes == null) {
            cachedNativeMeshes = ACTIVE_SCENE.view(NativeObjectMesh.class);
            for (OxyEntity e : cachedNativeMeshes) {
                e.get(NativeObjectMesh.class).initList();
            }
        }

        cachedCameraComponents = ACTIVE_SCENE.distinct(OxyCamera.class);
        cachedLightEntities = ACTIVE_SCENE.view(Light.class);
        allModelEntities = ACTIVE_SCENE.view(ModelMesh.class);
    }

    @Override
    public void rebuild() {
        allModelEntities = ACTIVE_SCENE.view(ModelMesh.class);
//        cachedNativeMeshes = scene.view(NativeObjectMesh.class);

        //Prep
        {
            List<OxyEntity> cachedConverted = new ArrayList<>(allModelEntities);
            if (cachedConverted.size() == 0) return;
            ModelMesh mesh = cachedConverted.get(cachedConverted.size() - 1).get(ModelMesh.class);
            mesh.initList();
        }
    }

    public void updateAllModelEntities() {
        allModelEntities = ACTIVE_SCENE.view(ModelMesh.class);
        cachedLightEntities = ACTIVE_SCENE.view(Light.class);
    }

    @Override
    public void update(float ts) {
        if (ACTIVE_SCENE == null) return;
        ACTIVE_SCENE.getOxyUISystem().dispatchNativeEvents();
        ACTIVE_SCENE.getOxyUISystem().updateImGuiContext(ts);

        int i = 0;
        for (OxyEntity e : cachedLightEntities) {
            Light l = e.get(Light.class);
            l.update(e, i);
            i++;
        }
    }

    public static boolean initHdrTexture = false;
    public static final OxyShader cubemapShader = new OxyShader("shaders/OxySkybox.glsl");

    @Override
    public void render(float ts) {
        if (ACTIVE_SCENE == null) return;
        ACTIVE_SCENE.getFrameBuffer().blit();
        if (!initHdrTexture && OxyRenderer.currentBoundedCamera != null) {
            hdrTexture.captureFaces(ts);
            cachedNativeMeshes = ACTIVE_SCENE.view(NativeObjectMesh.class);
            initHdrTexture = true;
        }

        ACTIVE_SCENE.getFrameBuffer().bind();
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
                if (mesh.equals(hdrTexture.getMesh())) {
                    glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
                    cubemapShader.enable();
                    cubemapShader.setUniform1i("skyBoxTexture", hdrTexture.getTextureSlot());
                    cubemapShader.setUniform1f("mipLevel", EnvironmentPanel.mipLevelStrength[0]);
                    cubemapShader.setUniform1f("exposure", EnvironmentPanel.exposure[0]);
                    cubemapShader.disable();
                    render(ts, mesh, mainCamera, cubemapShader);
                    glDisable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
                } else {
                    render(ts, mesh, mainCamera);
                }
            }
            for (OxyEntity e : allModelEntities) {
                if (!e.has(SelectedComponent.class)) return;
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
                modelMesh.getShader().setUniform1f("gamma", EnvironmentPanel.gammaStrength[0]);
                modelMesh.getShader().disable();
                material.push(modelMesh.getShader());

                glEnable(GL_CULL_FACE);
                glCullFace(GL_BACK);
                /*if (s.selected) {
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
                }*/
                render(ts, modelMesh, mainCamera);
                glDisable(GL_CULL_FACE);
            }
        }
        ACTIVE_SCENE.getFrameBuffer().unbind();
    }

    public void loadHDRTextureToScene(String path) {
        if (SceneLayer.hdrTexture != null) SceneLayer.hdrTexture.dispose();
        SceneLayer.hdrTexture = OxyTexture.loadHDRTexture(path, ACTIVE_SCENE);
        SceneLayer.hdrTexture.captureFaces(0);
    }

    private void render(float ts, Mesh mesh, OxyCamera camera) {
        ACTIVE_SCENE.getRenderer().render(ts, mesh, camera);
        OxyRenderer.Stats.totalShapeCount = ACTIVE_SCENE.getShapeCount();
    }

    private void render(float ts, Mesh mesh, OxyCamera camera, OxyShader shader) {
        ACTIVE_SCENE.getRenderer().render(ts, mesh, camera, shader);
        OxyRenderer.Stats.totalShapeCount = ACTIVE_SCENE.getShapeCount();
    }

    private void render(float ts, Mesh mesh) {
        ACTIVE_SCENE.getRenderer().render(ts, mesh);
        OxyRenderer.Stats.totalShapeCount = ACTIVE_SCENE.getShapeCount();
    }

    public void clear() {
        cachedLightEntities.clear();
        cachedCameraComponents.clear();
        allModelEntities.clear();
    }
}
