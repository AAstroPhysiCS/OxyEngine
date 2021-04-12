package OxyEngine.Core.Layers;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Light.SkyLight;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Passes.ShadowRenderPass;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
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
import static OxyEngine.Core.Renderer.Passes.ShadowRenderPass.NUMBER_CASCADES;
import static OxyEngine.Scene.Objects.WorldGrid.gridPipeline;
import static OxyEngine.Scene.SceneRuntime.*;
import static OxyEngineEditor.UI.Panels.ScenePanel.editorCameraEntity;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public class SceneLayer extends Layer {

    public Set<OxyEntity> cachedLightEntities, cachedNativeMeshes;
    public Set<OxyEntity> cachedCameraComponents;
    public Set<OxyEntity> allModelEntities;

    public OxyCamera mainCamera;

    private OxyPipeline geometryPipeline;
    private OxyPipeline hdrPipeline;

    private static SceneLayer INSTANCE = null;

    public static SceneLayer getInstance() {
        if (INSTANCE == null) INSTANCE = new SceneLayer();
        return INSTANCE;
    }

    public OxyPipeline getGeometryPipeline() {
        return geometryPipeline;
    }

    public OxyPipeline getHDRPipeline() {
        return hdrPipeline;
    }

    private SceneLayer() {
    }

    private final static int[] samplers = new int[32];

    static {
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;
    }

    @Override
    public void build() {
        OxyShader pbrShader = OxyShader.createShader("OxyPBRAnimation", "shaders/OxyPBRAnimation.glsl");
        geometryPipeline = OxyPipeline.createNewPipeline(OxyPipeline.createNewSpecification()
                .setDebugName("Main Geometry Pipeline")
                .setShader(pbrShader));

        OxyShader hdrShader = OxyShader.createShader("OxyHDR", "shaders/OxyHDR.glsl");
        hdrPipeline = OxyPipeline.createNewPipeline(OxyPipeline.createNewSpecification()
                .setDebugName("HDR Pipeline")
                .setShader(hdrShader));

        geometryPipeline.begin();
        geometryPipeline.setUniform1iv("tex", samplers);
        geometryPipeline.end();

        cachedNativeMeshes = ACTIVE_SCENE.view(NativeObjectMeshOpenGL.class);
        cachedCameraComponents = ACTIVE_SCENE.view(OxyCamera.class);
        updateModelEntities();

        fillPropertyEntries();

        updateCurrentBoundedCamera(0);

        new WorldGrid(ACTIVE_SCENE, 10);
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

    public void updateCurrentBoundedCamera(float ts) {
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

        if (mainCamera != null) {
            mainCamera.finalizeCamera(ts);
            if (mainCamera instanceof PerspectiveCamera c) c.setViewMatrixNoTranslation();
        }
    }

    @Override
    public void update(float ts) {
        if (ACTIVE_SCENE == null) return;
        SceneRuntime.onUpdate(ts);

        //Camera
        updateCurrentBoundedCamera(ts);

        if (currentBoundedCamera == null) return;

        //RESET ALL THE LIGHT STATES
        for (int i = 0; i < LIGHT_SIZE; i++) {
            geometryPipeline.begin();
            geometryPipeline.setUniform1i("p_Light[" + i + "].activeState", 0);
            geometryPipeline.setUniform1i("d_Light[" + i + "].activeState", 0);
            geometryPipeline.end();
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

    public static HDRTexture hdrTexture;

    @Override
    public void render(float ts) {
        if (ACTIVE_SCENE == null) return;
        if (currentBoundedCamera == null) return;

        shadowPass();

        hdrTexture = null;
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

                geometryPipeline.begin();

                //ANIMATION UPDATE
                geometryPipeline.setUniform1i("animatedModel", 0);
                if (e.has(AnimationComponent.class)) {
                    geometryPipeline.setUniform1i("animatedModel", 1);
                    AnimationComponent animComp = e.get(AnimationComponent.class);
                    animComp.updateAnimation(ts);
                    List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                    for (int j = 0; j < matrix4fList.size(); j++) {
                        geometryPipeline.setUniformMatrix4fv("finalBonesMatrices[" + j + "]", matrix4fList.get(j), false);
                    }
                }

                geometryPipeline.setUniformMatrix4fv("model", c.transform, false);
                int iblSlot = TextureSlot.UNUSED.getValue(), prefilterSlot = TextureSlot.UNUSED.getValue(), brdfLUTSlot = TextureSlot.UNUSED.getValue();
                if (hdrTexture != null) {
                    iblSlot = hdrTexture.getIBLSlot();
                    prefilterSlot = hdrTexture.getPrefilterSlot();
                    brdfLUTSlot = hdrTexture.getBDRFSlot();
                    hdrTexture.bindAll();
                }
                geometryPipeline.setUniform1i("iblMap", iblSlot);
                geometryPipeline.setUniform1i("prefilterMap", prefilterSlot);
                geometryPipeline.setUniform1i("brdfLUT", brdfLUTSlot);
                if (skyLightComp != null) {
                    geometryPipeline.setUniform1f("hdrIntensity", skyLightComp.intensity[0]);
                    geometryPipeline.setUniform1f("gamma", skyLightComp.gammaStrength[0]);
                    geometryPipeline.setUniform1f("exposure", skyLightComp.exposure[0]);
                } else {
                    geometryPipeline.setUniform1f("hdrIntensity", 1.0f);
                    geometryPipeline.setUniform1f("gamma", 2.2f);
                    geometryPipeline.setUniform1f("exposure", 1.0f);
                }

                boolean castShadows = ShadowRenderPass.castShadows();
                geometryPipeline.setUniform1i("castShadows", castShadows ? 1 : 0);
                if (castShadows) {
                    for (int i = 0; i < NUMBER_CASCADES; i++) {
                        if (ShadowRenderPass.ready(i)) {
                            glBindTextureUnit(TextureSlot.CSM.getValue() + i, ShadowRenderPass.getShadowMap(i));
                            geometryPipeline.setUniform1i("shadowMap[" + i + "]", TextureSlot.CSM.getValue() + i);
                            geometryPipeline.setUniformMatrix4fv("lightSpaceMatrix[" + i + "]", ShadowRenderPass.getShadowViewMatrix(i), false);
                            geometryPipeline.setUniform1f("cascadeSplits[" + i + "]", ShadowRenderPass.getCascadeSplits(i));
                        }
                    }
                }

                geometryPipeline.end();
                if (material != null) material.push(geometryPipeline);
                OxyRenderer.renderMesh(geometryPipeline, modelMesh, mainCamera);
            }

            for (OxyEntity e : cachedNativeMeshes) {
                OpenGLMesh mesh = e.get(OpenGLMesh.class);
                if (e.has(SkyLight.class)) continue; //DO NOT RENDER THE SKYLIGHT HERE

                if (e.has(OxyMaterialIndex.class)) {
                    OxyMaterial m = OxyMaterialPool.getMaterial(e);
                    if (m != null) {
                        m.push(geometryPipeline);
                        OxyRenderer.renderMesh(geometryPipeline, mesh, mainCamera);
                    }
                }

                if (e.equals(WorldGrid.grid)) {
                    gridPipeline.begin();
                    gridPipeline.setUniformMatrix4fv("v_Matrix", mainCamera.getViewMatrix(), mainCamera.isTranspose());
                    gridPipeline.end();
                    OxyRenderer.renderMesh(gridPipeline, mesh, mainCamera);
                }
            }

            //SKY LIGHT RENDER
            if (skyLightEntity != null) {
                if (hdrTexture != null) {
                    hdrTexture.bindAll();
                    glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);

                    hdrPipeline.setShader(ShaderLibrary.get("OxySkybox"));

                    hdrPipeline.begin(); //first render it to the cubemap shader
                    if (skyLightComp.mipLevelStrength[0] > 0)
                        hdrPipeline.setUniform1i("u_skyBoxTexture", hdrTexture.getPrefilterSlot());
                    else hdrPipeline.setUniform1i("u_skyBoxTexture", hdrTexture.getTextureSlot());
                    hdrPipeline.setUniform1f("u_mipLevel", skyLightComp.mipLevelStrength[0]);
                    hdrPipeline.setUniform1f("u_exposure", skyLightComp.exposure[0]);
                    hdrPipeline.setUniform1f("u_gamma", skyLightComp.gammaStrength[0]);
                    hdrPipeline.setUniformMatrix4fv("v_Matrix_NoTransform", mainCamera.getViewMatrixNoTranslation(), mainCamera.isTranspose());
                    hdrPipeline.end();

                    OxyRenderer.renderMesh(hdrPipeline, skyLightEntity.get(OpenGLMesh.class), mainCamera);

                    hdrPipeline.setShader(ShaderLibrary.get("OxyHDR")); // reset to the hdr shader in case the user wants to change the hdr texture

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
        OxyRenderer.Stats.totalShapeCount = ACTIVE_SCENE.getShapeCount();
    }

    public void shadowPass() {
        for (OxyEntity e : cachedLightEntities) {
            //TODO: Change this, make this dynamic
            if (e.get(Light.class) instanceof DirectionalLight d && d.isCastingShadows()) {
                ShadowRenderPass.shadowPass(d);
                break;
            }
        }
    }

    public void recompileGeometryShader() {
        OxyShader pbrShaderOld = ShaderLibrary.get("OxyPBRAnimation");
        pbrShaderOld.dispose();
        OxyShader pbrShader = OxyShader.createShader("OxyPBRAnimation", "shaders/OxyPBRAnimation.glsl");
        geometryPipeline.setShader(pbrShader);
        geometryPipeline.begin();
        geometryPipeline.setUniform1iv("tex", samplers);
        geometryPipeline.end();
    }

    public void clear() {
        cachedLightEntities.clear();
        cachedCameraComponents.clear();
        allModelEntities.clear();
    }

}