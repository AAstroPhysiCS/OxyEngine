package OxyEngine.Core.Layers;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.CubemapTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Components.*;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.lwjgl.opengl.GL11.*;

public class SceneLayer extends Layer {

    private Set<OxyEntity> cachedLightEntities;
    private Set<EntityComponent> cachedNativeMeshes, cachedModelMeshes, cachedCameraComponents/*, cachedModelMeshesMasked*/;

    public SceneLayer(Scene scene) {
        super(scene);
    }

    private CubemapTexture cubemapTexture; //skyboxtexture
//    static OxyShader outlineShader = new OxyShader("D:\\programming\\Java\\OxyEngine\\shaders\\outline.glsl");

    @Override
    public void build() {
        Set<EntityComponent> cachedShaders = scene.distinct(OxyShader.class);
        cubemapTexture = OxyTexture.loadCubemap(OxySystem.FileSystem.getResourceByPath("/images/skybox/skyboxNature1"), scene);
        cubemapTexture.init(cachedShaders);
        cachedNativeMeshes = scene.distinct(NativeObjectMesh.class);
        cachedModelMeshes = scene.distinct(ModelMesh.class);
        cachedCameraComponents = scene.distinct(OxyCamera.class);
        cachedLightEntities = scene.view(Light.class);

        //Prep
        {
            for (EntityComponent e : cachedNativeMeshes) {
                ((Mesh) e).initList();
            }
            for (EntityComponent model : cachedModelMeshes) {
                ((ModelMesh) model).initList();
            }
        }
    }

    @Override
    public void rebuild() {
        cachedModelMeshes = scene.distinct(ModelMesh.class);
        //Prep
        {
            List<EntityComponent> cachedConverted = new ArrayList<>(cachedModelMeshes);
            ModelMesh mesh = (ModelMesh) cachedConverted.get(cachedConverted.size() - 1);
            mesh.initList();
        }
    }

    @Override
    public void update(float ts, float deltaTime) {
        scene.getOxyUISystem().updateImGuiContext(deltaTime);

        for (OxyEntity e : cachedLightEntities) {
            if (!e.has(EmittingComponent.class)) continue;
            Light l = e.get(Light.class);
            EmittingComponent emittingComponent = e.get(EmittingComponent.class);
            l.setAmbient(emittingComponent.ambient());
            l.setDiffuse(emittingComponent.diffuse());
            l.setSpecular(emittingComponent.specular());
            l.setPosition(emittingComponent.position());
            l.setDirection(emittingComponent.direction());
            l.update(e.get(OxyShader.class));
        }
    }

    @Override
    public void render(float ts, float deltaTime) {

        if (scene.getFrameBuffer() != null) scene.getFrameBuffer().bind();
        OpenGLRendererAPI.clearBuffer();

        //Camera
        PerspectiveCamera mainCamera = null;
        {
            for (EntityComponent camera : cachedCameraComponents) {
                if (camera instanceof PerspectiveCamera p) {
                    if (p.isPrimary()) {
                        mainCamera = p;
                        break;
                    }
                }
            }
        }

        //Rendering
        {
            for (EntityComponent c : cachedNativeMeshes) {
                Mesh mesh = (Mesh) c;
                RenderableComponent rC = mesh.renderableComponent;
                if (mesh.getShader().equals(cubemapTexture.getCube().get(OxyShader.class)) && rC.renderable) {
                    //skybox
                    glDepthMask(false);
                    render(ts, mesh, mainCamera);
                    glDepthMask(true);
                    continue;
                }
                if (rC.renderable)
                    render(ts, mesh, mainCamera);
            }
            for (EntityComponent c : cachedModelMeshes) {
                Mesh mesh = (Mesh) c;
                RenderableComponent rC = mesh.renderableComponent;
                if (rC.renderable && !rC.noZBufferRendering) render(ts, mesh, mainCamera);
                if (rC.renderable && rC.noZBufferRendering){
                    glDisable(GL_DEPTH_TEST);
                    render(ts, mesh, mainCamera);
                    glEnable(GL_DEPTH_TEST);
                }
                /*glEnable(GL_STENCIL_TEST);
                glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

                glStencilMask(0xFF);
                glClear(GL_STENCIL_BUFFER_BIT);

                glStencilFunc(GL_ALWAYS, 1, 0xFF);
                if (rC.renderable) render(ts, mesh, mainCamera);
                outlineShader.enable();
                mesh.scaleUp(1.01f);
                glStencilFunc(GL_NOTEQUAL, 1, 0xFF);
                glStencilMask(0x00);
                render(ts, mesh, mainCamera, outlineShader); // draw with the outline shader
                mesh.finalizeScaleUp();
                glDisable(GL_STENCIL_TEST);*/
            }
        }
        if (scene.getFrameBuffer() != null) scene.getFrameBuffer().unbind();
        scene.getOxyUISystem().start(scene.getEntities(), OxyRenderer.currentBoundedCamera);
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
