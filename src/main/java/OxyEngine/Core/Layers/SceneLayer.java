package OxyEngine.Core.Layers;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Light.SkyLight;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Shader.ShaderLibrary;
import OxyEngine.Core.Renderer.Texture.HDRTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyMaterialPool;
import OxyEngine.Scene.Objects.Model.OxyModel;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;
import OxyEngine.Scene.Objects.WorldGrid;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.SceneRuntime;
import OxyEngine.Scene.SceneState;
import OxyEngine.TextureSlot;
import OxyEngineEditor.UI.Panels.GUINode;
import OxyEngineEditor.UI.Panels.Panel;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static OxyEngine.Core.Renderer.Context.OxyRenderCommand.rendererAPI;
import static OxyEngine.Core.Renderer.Light.Light.LIGHT_SIZE;
import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngine.Scene.SceneRuntime.currentBoundedSkyLight;
import static OxyEngineEditor.EditorApplication.editorCameraEntity;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;

public class SceneLayer extends Layer {

    public Set<OxyEntity> cachedLightEntities, cachedNativeMeshes;
    public Set<OxyEntity> cachedCameraComponents;
    public Set<OxyEntity> allModelEntities;

    //    private static final OxyShader outlineShader = new OxyShader("shaders/OxyOutline.glsl");
    private static final OxyShader cubemapShader = OxyShader.createShader("SkyboxShader", "shaders/OxySkybox.glsl");

    private OxyCamera mainCamera;

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
        cachedCameraComponents = ACTIVE_SCENE.view(OxyCamera.class);
        updateModelEntities();

        fillPropertyEntries();
    }

    @Override
    public void rebuild() {
        updateModelEntities();

        //Prep
        {
            List<OxyEntity> cachedConverted = new ArrayList<>(allModelEntities);
            if (cachedConverted.size() == 0) return;
            ModelMeshOpenGL mesh = cachedConverted.get(cachedConverted.size() - 1).get(ModelMeshOpenGL.class);
            mesh.addToBuffer();
        }
    }

    private void fillPropertyEntries() {
        for (OxyEntity entity : SceneLayer.getInstance().allModelEntities) {
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

    public void updateLightEntities() {
        cachedLightEntities = ACTIVE_SCENE.view(Light.class);
    }

    public void updateModelEntities() {
        allModelEntities = ACTIVE_SCENE.view(ModelMeshOpenGL.class);
        updateLightEntities();
    }

    public void updateCameraEntities() {
        cachedCameraComponents = ACTIVE_SCENE.view(OxyCamera.class);
    }

    public void updateNativeEntities() {
        cachedNativeMeshes = ACTIVE_SCENE.view(NativeObjectMeshOpenGL.class);
    }

    @Override
    public void update(float ts) {
        if (ACTIVE_SCENE == null) return;
        SceneRuntime.onUpdate(ts);

        //Camera
        {
            for (OxyEntity e : cachedCameraComponents) {
                OxyCamera camera = e.get(OxyCamera.class);
                OxyCamera editorCamera = editorCameraEntity.get(OxyCamera.class);
                if (ACTIVE_SCENE.STATE == SceneState.RUNNING && !camera.equals(editorCamera)) {
                    editorCamera.setPrimary(false);
                    camera.setPrimary(true);
                    mainCamera = camera;
                    SceneRuntime.currentBoundedCamera = mainCamera;
                } else if (ACTIVE_SCENE.STATE != SceneState.RUNNING) {
                    if (mainCamera != null) mainCamera.setPrimary(false);
                    editorCamera.setPrimary(true);
                    mainCamera = editorCamera;
                    SceneRuntime.currentBoundedCamera = mainCamera;
                }
            }
        }

        if (mainCamera != null) {
            mainCamera.finalizeCamera(ts);
            if (mainCamera instanceof PerspectiveCamera c) c.setViewMatrixNoTranslation();
        }

        //RESET ALL THE LIGHT STATES
        OxyShader pbrShader = ShaderLibrary.get("OxyPBRAnimation");
        for (int i = 0; i < LIGHT_SIZE; i++) {
            pbrShader.enable();
            pbrShader.setUniform1i("p_Light[" + i + "].activeState", 0);
            pbrShader.setUniform1i("d_Light[" + i + "].activeState", 0);
            pbrShader.disable();
        }

        //Lights
        int i = 0;
        currentBoundedSkyLight = null;
        for (OxyEntity e : cachedLightEntities) {
            Light l = e.get(Light.class);
            l.update(e, i++);
            SkyLight comp;
            if (((comp = e.get(SkyLight.class)) != null && comp.isPrimary())) {
                currentBoundedSkyLight = (OxyNativeObject) e;
            }
        }
    }

    @Override
    public void render(float ts) {
        if (ACTIVE_SCENE == null) return;

        HDRTexture hdrTexture = null;
        OxyNativeObject skyLightEntity = currentBoundedSkyLight;
        SkyLight skyLightComp = null;
        if (skyLightEntity != null) {
            skyLightComp = skyLightEntity.get(SkyLight.class);
            if (skyLightComp != null) hdrTexture = skyLightComp.getHDRTexture();
        }

        OpenGLFrameBuffer frameBuffer = ACTIVE_SCENE.getFrameBuffer();
        OpenGLFrameBuffer destFrameBuffer = ACTIVE_SCENE.getBlittedFrameBuffer();

        OpenGLFrameBuffer.blit(frameBuffer, destFrameBuffer);
        frameBuffer.bind();
        rendererAPI.clearBuffer();
        rendererAPI.clearColor(Panel.bgC[0], Panel.bgC[1], Panel.bgC[2], 1.0f);

        //Rendering
        {
            for (OxyEntity e : allModelEntities) {
                if (!e.has(SelectedComponent.class)) continue;
                RenderableComponent renderableComponent = e.get(RenderableComponent.class);
                if (renderableComponent.mode != RenderingMode.Normal) continue;
                ModelMeshOpenGL modelMesh = e.get(ModelMeshOpenGL.class);
                OxyMaterial material = null;
                if (e.has(OxyMaterialIndex.class)) material = OxyMaterialPool.getMaterial(e);
                TransformComponent c = e.get(TransformComponent.class);

                OxyShader shader = e.get(OxyShader.class);
                shader.enable();

                //ANIMATION UPDATE
                if (e.has(AnimationComponent.class)) {
                    AnimationComponent animComp = e.get(AnimationComponent.class);
                    animComp.updateAnimation(ts);
                    List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                    for (int j = 0; j < matrix4fList.size(); j++) {
                        shader.setUniformMatrix4fv("finalBonesMatrices[" + j + "]", matrix4fList.get(j), false);
                    }
                }

                shader.setUniformMatrix4fv("model", c.transform, false);
                int iblSlot = TextureSlot.UNUSED.getValue(), prefilterSlot = TextureSlot.UNUSED.getValue(), brdfLUTSlot = TextureSlot.UNUSED.getValue();
                if (hdrTexture != null) {
                    iblSlot = hdrTexture.getIBLSlot();
                    prefilterSlot = hdrTexture.getPrefilterSlot();
                    brdfLUTSlot = hdrTexture.getBDRFSlot();
                    hdrTexture.bindAll();
                }
                shader.setUniform1i("iblMap", iblSlot);
                shader.setUniform1i("prefilterMap", prefilterSlot);
                shader.setUniform1i("brdfLUT", brdfLUTSlot);
                if (skyLightComp != null) {
                    shader.setUniform1f("hdrIntensity", skyLightComp.intensity[0]);
                    shader.setUniform1f("gamma", skyLightComp.gammaStrength[0]);
                    shader.setUniform1f("exposure", skyLightComp.exposure[0]);
                } else {
                    shader.setUniform1f("hdrIntensity", 1.0f);
                    shader.setUniform1f("gamma", 2.2f);
                    shader.setUniform1f("exposure", 1.0f);
                }
                shader.disable();
                if (material != null) material.push(shader);
                render(modelMesh, mainCamera, shader);
            }

            for (OxyEntity e : cachedNativeMeshes) {
                OpenGLMesh mesh = e.get(OpenGLMesh.class);
                if (!e.has(OxyShader.class) || e.has(SkyLight.class)) continue; //DO NOT RENDER THE SKYLIGHT HERE

                if (e.has(OxyMaterialIndex.class)) {
                    OxyMaterial m = OxyMaterialPool.getMaterial(e);
                    if (m != null) {
                        m.push(e.get(OxyShader.class));
                        render(mesh, mainCamera, e.get(OxyShader.class));
                    }
                }

                if (e.get(OxyShader.class).equals(WorldGrid.shader)) {
                    render(mesh, mainCamera, WorldGrid.shader);
                }
            }

            //SKY LIGHT RENDER
            if (skyLightEntity != null) {
                if (hdrTexture != null) {
                    hdrTexture.bindAll();
                    glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
                    cubemapShader.enable();
                    if (skyLightComp.mipLevelStrength[0] > 0)
                        cubemapShader.setUniform1i("skyBoxTexture", hdrTexture.getPrefilterSlot());
                    else cubemapShader.setUniform1i("skyBoxTexture", hdrTexture.getTextureSlot());
                    cubemapShader.setUniform1f("mipLevel", skyLightComp.mipLevelStrength[0]);
                    cubemapShader.setUniform1f("exposure", skyLightComp.exposure[0]);
                    cubemapShader.setUniform1f("gamma", skyLightComp.gammaStrength[0]);
                    cubemapShader.disable();
                    render(skyLightEntity.get(OpenGLMesh.class), mainCamera, cubemapShader);
                    glDisable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
                }
            }
        }

        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        frameBuffer.unbind();


        OxyTexture.unbindAllTextures();

        UILayer.uiSystem.dispatchNativeEvents();
    }

    public void recompileShader() {
        OxyShader pbrShaderOld = ShaderLibrary.get("OxyPBRAnimation");
        pbrShaderOld.dispose();
        OxyShader pbrShader = OxyShader.createShader("OxyPBRAnimation", "shaders/OxyPBRAnimation.glsl");
        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;
        pbrShader.enable();
        pbrShader.setUniform1iv("tex", samplers);
        pbrShader.disable();
        for (OxyEntity m : allModelEntities) m.addComponent(pbrShader);
        for (OxyEntity m : cachedLightEntities) m.addComponent(pbrShader);
    }

    private void render(OpenGLMesh mesh, OxyCamera camera, OxyShader shader) {
        OxyRenderer.render(mesh, camera, shader);
        OxyRenderer.Stats.totalShapeCount = ACTIVE_SCENE.getShapeCount();
    }

    public void clear() {
        cachedLightEntities.clear();
        cachedCameraComponents.clear();
        allModelEntities.clear();
    }

}