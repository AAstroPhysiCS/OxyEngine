package OxyEngine.Core.Layers;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.HDRTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.SceneRuntime;
import OxyEngineEditor.UI.Gizmo.OxySelectHandler;
import OxyEngineEditor.UI.Panels.EnvironmentPanel;
import OxyEngineEditor.UI.Panels.GUINode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static OxyEngine.Core.Renderer.Context.OxyRenderCommand.rendererAPI;
import static OxyEngineEditor.Scene.SceneRuntime.ACTIVE_SCENE;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.opengl.GL44.glClearTexImage;

public class SceneLayer extends Layer {

    private Set<OxyEntity> cachedLightEntities, cachedNativeMeshes;
    private Set<OxyCamera> cachedCameraComponents;
    private Set<OxyEntity> allModelEntities;

    static final OxyShader outlineShader = new OxyShader("shaders/OxyOutline.glsl");
    public static HDRTexture hdrTexture;

    private PerspectiveCamera mainCamera;

    private static SceneLayer INSTANCE = null;

    public static SceneLayer getInstance() {
        if (INSTANCE == null) INSTANCE = new SceneLayer();
        return INSTANCE;
    }

    private SceneLayer() {
    }

    @Override
    public void build() {
        cachedNativeMeshes = ACTIVE_SCENE.view(NativeObjectMeshOpenGL.class);
        for (OxyEntity e : cachedNativeMeshes) {
            e.get(NativeObjectMeshOpenGL.class).initList();
        }

        cachedCameraComponents = ACTIVE_SCENE.distinct(OxyCamera.class);
        cachedLightEntities = ACTIVE_SCENE.view(Light.class);
        allModelEntities = ACTIVE_SCENE.view(ModelMeshOpenGL.class);

        fillPropertyEntries();
    }

    @Override
    public void rebuild() {
        updateAllEntities();
//        cachedNativeMeshes = scene.view(NativeObjectMesh.class);

        //Prep
        {
            List<OxyEntity> cachedConverted = new ArrayList<>(allModelEntities);
            if (cachedConverted.size() == 0) return;
            ModelMeshOpenGL mesh = cachedConverted.get(cachedConverted.size() - 1).get(ModelMeshOpenGL.class);
            mesh.initList();
        }
    }

    private void fillPropertyEntries() {
        for (OxyEntity entity : SceneRuntime.ACTIVE_SCENE.getEntities()) {
            if (entity instanceof OxyNativeObject) continue;
            if (((OxyModel) entity).factory == null) continue;
            for (Class<? extends EntityComponent> component : EntityComponent.allEntityComponentChildClasses) {
                if (component == null) continue;
                if (!entity.has(component)) continue;
                //overhead, i know. getDeclaredField() method throw an exception if the given field isn't declared... => no checking for the field
                //so you have to manually do the checking by getting all the fields that the class has.
                Field[] allFields = component.getDeclaredFields();
                Field guiNodeField = null;
                for (Field f : allFields) {
                    if (f.getName().equals("guiNode")) {
                        f.setAccessible(true);
                        guiNodeField = f;
                        break;
                    }
                }
                if (guiNodeField == null) continue;
                try {
                    GUINode entry = (GUINode) guiNodeField.get(component);
                    if (!entity.getGUINodes().contains(entry)) entity.getGUINodes().add(entry);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateAllEntities() {
        allModelEntities = ACTIVE_SCENE.view(ModelMeshOpenGL.class);
        cachedLightEntities = ACTIVE_SCENE.view(Light.class);
    }

    @Override
    public void update(float ts) {
        if (ACTIVE_SCENE == null) return;
        SceneRuntime.onUpdate(ts);

        //Camera
        {
            for (EntityComponent camera : cachedCameraComponents) {
                if (camera instanceof PerspectiveCamera p) {
                    if (p.isPrimary()) {
                        mainCamera = p;
                        SceneRuntime.currentBoundedCamera = mainCamera;
                        break;
                    }
                }
            }
        }

        if (mainCamera != null) mainCamera.finalizeCamera(ts);

        //Lights
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

        OpenGLFrameBuffer frameBuffer = ACTIVE_SCENE.getFrameBuffer();

        frameBuffer.blit();
        if (!initHdrTexture && hdrTexture != null && SceneRuntime.currentBoundedCamera != null) {
            hdrTexture.captureFaces(ts);
            cachedNativeMeshes = ACTIVE_SCENE.view(NativeObjectMeshOpenGL.class);
            initHdrTexture = true;
        }

        frameBuffer.bind();
        rendererAPI.clearBuffer();
        rendererAPI.clearColor(32, 32, 32, 1.0f);

        //Rendering
        {
            glDepthFunc(GL_LEQUAL);
            for (OxyEntity e : cachedNativeMeshes) {
                OpenGLMesh mesh = e.get(OpenGLMesh.class);
                if (e.has(OxyMaterial.class)) {
                    OxyMaterial m = e.get(OxyMaterial.class);
                    m.push(mesh.getShader());
                }

                if (hdrTexture == null) render(ts, mesh, mainCamera);

                if (hdrTexture != null) {
                    hdrTexture.bindAll();
                    if (mesh.equals(hdrTexture.getMesh())) {
                        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
                        cubemapShader.enable();
                        if (EnvironmentPanel.mipLevelStrength[0] > 0) cubemapShader.setUniform1i("skyBoxTexture", hdrTexture.getPrefilterSlot());
                        else cubemapShader.setUniform1i("skyBoxTexture", hdrTexture.getTextureSlot());
                        cubemapShader.setUniform1f("mipLevel", EnvironmentPanel.mipLevelStrength[0]);
                        cubemapShader.setUniform1f("exposure", EnvironmentPanel.exposure[0]);
                        cubemapShader.setUniform1f("gamma", EnvironmentPanel.gammaStrength[0]);
                        cubemapShader.disable();
                        render(ts, mesh, mainCamera, cubemapShader);
                        glDisable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
                    } else {
                        render(ts, mesh, mainCamera);
                    }
                }
            }
            for (OxyEntity e : allModelEntities) {
                if (!e.has(SelectedComponent.class)) continue;
                RenderableComponent renderableComponent = e.get(RenderableComponent.class);
                if (renderableComponent.mode != RenderingMode.Normal) continue;
                ModelMeshOpenGL modelMesh = e.get(ModelMeshOpenGL.class);
                OxyMaterial material = e.get(OxyMaterial.class);
                TransformComponent c = e.get(TransformComponent.class);
                modelMesh.getShader().enable();
                modelMesh.getShader().setUniformMatrix4fv("model", c.transform, false);
                int irradianceSlot = 0, prefilterSlot = 0, brdfLUTSlot = 0;
                if (hdrTexture != null) {
                    irradianceSlot = hdrTexture.getIrradianceSlot();
                    prefilterSlot = hdrTexture.getPrefilterSlot();
                    brdfLUTSlot = hdrTexture.getBDRFSlot();
                    hdrTexture.bindAll();
                }
                modelMesh.getShader().setUniform1i("irradianceMap", irradianceSlot);
                modelMesh.getShader().setUniform1i("prefilterMap", prefilterSlot);
                modelMesh.getShader().setUniform1i("brdfLUT", brdfLUTSlot);
                modelMesh.getShader().setUniform1f("gamma", EnvironmentPanel.gammaStrength[0]);
                modelMesh.getShader().setUniform1f("exposure", EnvironmentPanel.exposure[0]);
                modelMesh.getShader().disable();

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
                    render(ts, modelMeshglmainCamera);
                }*/
                material.push(modelMesh.getShader());
                render(ts, modelMesh, mainCamera);
            }
        }

        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        ACTIVE_SCENE.getFrameBuffer().unbind();
        OxyTexture.unbindAllTextures();

        UILayer.uiSystem.dispatchNativeEvents();
    }

    public void loadHDRTextureToScene(String path) {
        if (path == null) return;
        if (SceneLayer.hdrTexture != null) SceneLayer.hdrTexture.dispose();
        SceneLayer.hdrTexture = OxyTexture.loadHDRTexture(path, ACTIVE_SCENE);
        SceneLayer.hdrTexture.captureFaces(SceneRuntime.TS);
    }

    private void render(float ts, OpenGLMesh mesh, OxyCamera camera) {
        ACTIVE_SCENE.getRenderer().render(ts, mesh, camera);
        OxyRenderer.Stats.totalShapeCount = ACTIVE_SCENE.getShapeCount();
    }

    private void render(float ts, OpenGLMesh mesh, OxyCamera camera, OxyShader shader) {
        ACTIVE_SCENE.getRenderer().render(ts, mesh, camera, shader);
        OxyRenderer.Stats.totalShapeCount = ACTIVE_SCENE.getShapeCount();
    }

    private void render(float ts, OpenGLMesh mesh) {
        ACTIVE_SCENE.getRenderer().render(ts, mesh);
        OxyRenderer.Stats.totalShapeCount = ACTIVE_SCENE.getShapeCount();
    }

    public void clear() {
        cachedLightEntities.clear();
        cachedCameraComponents.clear();
        allModelEntities.clear();
    }

    public void startPicking() {
        if (allModelEntities.size() == 0) return;
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        OpenGLFrameBuffer frameBuffer = ACTIVE_SCENE.getFrameBuffer();
        //ID RENDER PASS
        if (frameBuffer.getIdAttachmentFBO() != 0) {
            int[] clearValue = {-1};
            frameBuffer.bindPicking();
            rendererAPI.clearBuffer();
            rendererAPI.clearColor(32, 32, 32, 1.0f);
            glClearTexImage(frameBuffer.getIdAttachment(), 0, GL_RED_INTEGER, GL_INT, clearValue);
            for (OxyEntity e : allModelEntities) render(0, e.get(ModelMeshOpenGL.class), mainCamera);
        }
        int id = frameBuffer.getEntityID();
        if(id == -1) OxySelectHandler.entityContext = null;
        else {
            for (OxyEntity e : allModelEntities) {
                if (e.getObjectId() == id) {
                    OxySelectHandler.entityContext = e;
                    break;
                }
            }
        }
        frameBuffer.unbind();
    }
}
