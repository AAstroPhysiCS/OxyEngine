package OxyEngine.Core.Context;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Camera.SceneCamera;
import OxyEngine.Core.Context.Renderer.Mesh.OpenGLMesh;
import OxyEngine.Core.Context.Renderer.Light.*;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Core.Context.Renderer.Texture.OpenGLHDRTexture;
import OxyEngine.Core.Context.Renderer.Texture.TextureSlot;
import OxyEngine.Core.Context.Scene.*;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.MouseCode;
import OxyEngineEditor.UI.OxySelectHandler;
import OxyEngineEditor.UI.Panels.GUINode;
import OxyEngineEditor.UI.Panels.ScenePanel;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static OxyEngine.Core.Context.OxyRenderer.*;
import static OxyEngine.Core.Context.Scene.SceneRuntime.*;
import static OxyEngine.Core.Layers.EditorLayer.editorCameraEntity;
import static org.lwjgl.opengl.GL30.GL_INT;
import static org.lwjgl.opengl.GL44.glClearTexImage;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public final class SceneRenderer {

    private static SceneRenderer INSTANCE = null;

    Set<OxyEntity> cachedLightEntities, cachedNativeMeshes;
    Set<OxyEntity> cachedCameraComponents;
    public Set<OxyEntity> allModelEntities;

    private static ShadowMapCamera[] cascadedCamArr = null;
    public static boolean cascadeIndicatorToggle;

    public static SceneRenderer getInstance() {
        if (INSTANCE == null) INSTANCE = new SceneRenderer();
        return INSTANCE;
    }

    private SceneRenderer() {
    }

    public void initScene() {

        cachedNativeMeshes = ACTIVE_SCENE.view(OpenGLMesh.class);
        cachedCameraComponents = ACTIVE_SCENE.view(OxyCamera.class);
        updateModelEntities();

        fillPropertyEntries();

        updateCurrentBoundedCamera();
    }

    private void fillPropertyEntries() {
        for (OxyEntity entity : allModelEntities) {
            if (entity instanceof OxyNativeObject) continue;
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
        allModelEntities = ACTIVE_SCENE.view(OpenGLMesh.class);
        updateLightEntities();
    }

    public void updateCameraEntities() {
        cachedCameraComponents = ACTIVE_SCENE.view(OxyCamera.class);
    }

    public void updateCurrentBoundedCamera() {
        for (OxyEntity e : cachedCameraComponents) {
            OxyCamera camera = e.get(OxyCamera.class);
            OxyCamera editorCamera = editorCameraEntity.get(OxyCamera.class);
            if (ACTIVE_SCENE.STATE == SceneState.RUNNING && !camera.equals(editorCamera)) {
                editorCamera.setPrimary(false);
                camera.setPrimary(true);
                currentBoundedCamera = camera;
            } else if (ACTIVE_SCENE.STATE != SceneState.RUNNING) {
                camera.setPrimary(false);
                editorCamera.setPrimary(true);
                currentBoundedCamera = editorCamera;
            }
        }
    }

    public void updateScene(float ts) {
        if (ACTIVE_SCENE == null) return;
        SceneRuntime.onUpdate(ts);

        //Camera
        updateCurrentBoundedCamera();

        if (currentBoundedCamera == null) return;

        //Lights
        int i = 0;
        currentBoundedSkyLightEntity = null;
        for (OxyEntity e : cachedLightEntities) {
            Light l = e.get(Light.class);
            l.update(e, i++);

            SkyLight comp;
            if (((comp = e.get(SkyLight.class)) != null && comp.isPrimary())) {
                currentBoundedSkyLightEntity = (OxyNativeObject) e;
            }
        }

        OxyShader pbrShader = ShaderLibrary.get("OxyPBR");

        //Environment
        int irradianceSlot = TextureSlot.UNUSED.getValue();
        int prefilterSlot = TextureSlot.UNUSED.getValue();
        int bdrfSlot = TextureSlot.UNUSED.getValue();
        int hdrSlot = TextureSlot.UNUSED.getValue();

        pbrShader.begin();
        if (currentBoundedSkyLightEntity != null) {
            irradianceSlot = TextureSlot.IRRADIANCE.getValue();
            prefilterSlot = TextureSlot.PREFILTER.getValue();
            bdrfSlot = TextureSlot.BDRF.getValue();
            hdrSlot = TextureSlot.HDR.getValue();
        }
        pbrShader.setUniform1i("EnvironmentTex.iblMap", irradianceSlot);
        pbrShader.setUniform1i("EnvironmentTex.prefilterMap", prefilterSlot);
        pbrShader.setUniform1i("EnvironmentTex.brdfLUT", bdrfSlot);
        pbrShader.setUniform1i("EnvironmentTex.skyBoxTexture", hdrSlot);
        pbrShader.end();

        if (currentBoundedCamera instanceof SceneCamera s) s.update();
    }

    private void geometryPass() {
        if (ACTIVE_SCENE == null) return;
        if (currentBoundedCamera == null) return;

        {
            if (currentBoundedSkyLightEntity != null) {
                SkyLight skyLightComp = currentBoundedSkyLightEntity.get(SkyLight.class);
                if (skyLightComp != null) {
                    skyLightComp.bind();
                    if (skyLightComp instanceof OpenGLHDREnvironmentMap envMap) {
                        OpenGLHDRTexture hdrTexture = envMap.getHDRTexture();
                        if (hdrTexture != null) {

                            OxyShader skyBoxShader = ShaderLibrary.get("OxySkybox");

                            skyBoxShader.begin(); //first render it to the skybox shader
                            if (envMap.mipLevelStrength[0] > 0)
                                skyBoxShader.setUniform1i("u_skyBoxTexture", TextureSlot.PREFILTER.getValue());
                            else
                                skyBoxShader.setUniform1i("u_skyBoxTexture", TextureSlot.HDR.getValue());
                            skyBoxShader.setUniform1f("u_mipLevel", envMap.mipLevelStrength[0]);
                            skyBoxShader.end();

                            mainFrameBuffer.bind();
                            OxyRenderer.renderSkyLight(skyBoxShader);
                            mainFrameBuffer.unbind();
                        }
                    } else if (skyLightComp instanceof DynamicSky) {

                        OxyShader skyBoxShader = ShaderLibrary.get("OxySkybox");

                        skyBoxShader.begin(); //first render it to the skybox shader
                        skyBoxShader.setUniform1i("u_skyBoxTexture", TextureSlot.HDR.getValue());
                        skyBoxShader.end();

                        mainFrameBuffer.bind();
                        OxyRenderer.renderSkyLight(skyBoxShader);
                        mainFrameBuffer.unbind();
                    }
                }
            }
        }

        {
            OxyRenderPass geometryRenderPass = geometryPipeline.getRenderPass();
            OxyRenderer.beginRenderPass(geometryRenderPass);

            for (OxyEntity e : allModelEntities) {
                if (!e.has(SelectedComponent.class)) continue;
                RenderableComponent renderableComponent = e.get(RenderableComponent.class);
                if (renderableComponent.mode != RenderingMode.Normal) continue;
                OpenGLMesh modelMesh = e.get(OpenGLMesh.class);
                e.update();

                OxyMaterialPool.getMaterial(e).ifPresent((m) -> {
                    OxyShader shader = m.getShader();

                    shader.begin();
                    boolean castShadows = castShadows();
                    shader.setUniform1i("Shadows.castShadows", castShadows ? 1 : 0);
                    if (castShadows) {
                        if (cascadeIndicatorToggle) shader.setUniform1i("Shadows.cascadeIndicatorToggle", 1);
                        else shader.setUniform1i("Shadows.cascadeIndicatorToggle", 0);

                        for (int i = 0; i < NUMBER_CASCADES; i++) {
                            if (ready(i)) {
                                glBindTextureUnit(TextureSlot.CSM.getValue() + i, getShadowMap(i));
                                shader.setUniform1i("Shadows.shadowMap[" + i + "]", TextureSlot.CSM.getValue() + i);
                                shader.setUniformMatrix4fv("lightSpaceMatrix[" + i + "]", getShadowViewMatrix(i));
                                shader.setUniform1f("Shadows.cascadeSplits[" + i + "]", getCascadeSplits(i));
                            }
                        }
                    }
                    shader.end();
                });
                OxyRenderer.renderMesh(geometryPipeline, modelMesh);
            }

            OxyRenderer.endRenderPass();
        }
    }

    private void idPass() {

        int[] clearValue = {-1};

        OxyRenderer.beginRenderPass(pickingRenderPass);

        glClearTexImage(pickingFrameBuffer.getColorAttachmentTexture(1)[0], 0, pickingFrameBuffer.getTextureFormat(1).getStorageFormat(), GL_INT, clearValue);

        OxyShader pbrShader = ShaderLibrary.get("OxyPBR");

        for (OxyEntity e : allModelEntities) {
            if (!e.has(SelectedComponent.class)) continue;
            RenderableComponent renderableComponent = e.get(RenderableComponent.class);
            if (renderableComponent.mode != RenderingMode.Normal) continue;

            pbrShader.begin();
            pbrShader.setUniform1i("Animation.animatedModel", 0);
            if (e.has(AnimationComponent.class)) {
                AnimationComponent animComp = e.get(AnimationComponent.class);
                if (ACTIVE_SCENE.STATE == SceneState.RUNNING) {
                    pbrShader.setUniform1i("Animation.animatedModel", 1);
                    List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                    for (int j = 0; j < matrix4fList.size(); j++) {
                        pbrShader.setUniformMatrix4fv("Animation.finalBonesMatrices[" + j + "]", matrix4fList.get(j));
                    }
                }
            }
            TransformComponent c = e.get(TransformComponent.class);
            pbrShader.setUniformMatrix4fv("Transforms.model", c.transform);
            pbrShader.end();

            OxyRenderer.renderMesh(geometryPipeline, e.get(OpenGLMesh.class));
        }

        OxyRenderer.endRenderPass();
    }

    public void renderScene() {
        OxyRenderer.beginScene();

        geometryPass();
        shadowPass();
        if (Input.isMouseButtonPressed(MouseCode.GLFW_MOUSE_BUTTON_1) && ScenePanel.hoveredWindow
                && SceneRuntime.currentBoundedCamera.equals(editorCameraEntity.get(OxyCamera.class)) && !OxySelectHandler.isOverAnyGizmo()) {
            idPass();
            OxySelectHandler.startPicking();
        }
        OxyRenderer.endScene();
    }

    public OxyPipeline getGeometryPipeline() {
        return geometryPipeline;
    }

    private void shadowPass() {
        for (OxyEntity e : cachedLightEntities) {
            //TODO: Change this, make this dynamic
            if (e.get(Light.class) instanceof DirectionalLight d && d.isCastingShadows()) {
                shadowPass(d);
                break;
            }
        }
    }

    private void shadowPass(DirectionalLight d) {
        //Prepare the camera

        if (d.getDirection() == null) return;

        if (cascadedCamArr == null) {
            cascadedCamArr = new ShadowMapCamera[NUMBER_CASCADES];
            for (int i = 0; i < cascadedCamArr.length; i++) {
                ShadowMapCamera cam = new ShadowMapCamera(0, 0, 0, 0, 0, 0, true);
                cascadedCamArr[i] = cam;
            }
        }

        OxyRenderPass shadowRenderPass = shadowMapPipeline.getRenderPass();

        OxyRenderer.beginRenderPass(shadowRenderPass);

        for (int i = 0; i < cascadeSplit.length; i++) {
            cascadedCamArr[i].setDirectionalLight(d);
            cascadedCamArr[i].prepare((PerspectiveCamera) currentBoundedCamera, cascadeSplit[i]);
            shadowFrameBuffer.flushDepthAttachment(0, i);
        }

        for (OxyEntity e : SceneRenderer.getInstance().allModelEntities) {

            ShadowMapCamera cam = null;
            int camIndex = 0;

            for (int i = 0; i < NUMBER_CASCADES; i++) {
                if (currentBoundedCamera.origin.distance(e.get(TransformComponent.class).position) - 20f < cascadeSplit[i]) {
                    cam = cascadedCamArr[i];
                    cam.update();

                    if (i > 0) cascadedCamArr[i - 1].update();
                    if (i < NUMBER_CASCADES - 1) cascadedCamArr[i + 1].update();

                    camIndex = i;
                    break;
                }
            }
            shadowFrameBuffer.bindDepthAttachment(0, camIndex);

            if (cam == null) {
                OxyRenderer.endRenderPass();
                return;
            }

            OpenGLMesh mesh = e.get(OpenGLMesh.class);

            OxyShader shadowMapDepthShader = ShaderLibrary.get("OxyDepthMap");
            shadowMapDepthShader.begin();
            shadowMapDepthShader.setUniformMatrix4fv("model", e.get(TransformComponent.class).transform);
            shadowMapDepthShader.setUniformMatrix4fv("lightSpaceMatrix", cam.getViewMatrix());
            shadowMapDepthShader.setUniform1i("animatedModel", 0);
            if (e.has(AnimationComponent.class)) {
                shadowMapDepthShader.setUniform1i("animatedModel", 1);
                AnimationComponent animComp = e.get(AnimationComponent.class);
                List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                for (int j = 0; j < matrix4fList.size(); j++) {
                    shadowMapDepthShader.setUniformMatrix4fv("finalBonesMatrices[" + j + "]", matrix4fList.get(j));
                }
            }
            shadowMapDepthShader.end();

            OxyRenderer.renderMesh(shadowMapPipeline, mesh);

            if (camIndex != NUMBER_CASCADES - 1) {
                shadowFrameBuffer.bindDepthAttachment(0, camIndex + 1);
                shadowMapDepthShader.begin();
                shadowMapDepthShader.setUniformMatrix4fv("lightSpaceMatrix", cascadedCamArr[camIndex + 1].getViewMatrix());
                shadowMapDepthShader.end();
                OxyRenderer.renderMesh(shadowMapPipeline, mesh);
            }
            if (camIndex != 0) {
                shadowFrameBuffer.bindDepthAttachment(0, camIndex - 1);
                shadowMapDepthShader.begin();
                shadowMapDepthShader.setUniformMatrix4fv("lightSpaceMatrix", cascadedCamArr[camIndex - 1].getViewMatrix());
                shadowMapDepthShader.end();
                OxyRenderer.renderMesh(shadowMapPipeline, mesh);
            }
        }
        OxyRenderer.endRenderPass();
    }

    private static boolean castShadows() {
        if (cascadedCamArr == null) return false;
        if (!ready(0)) return false;
        return cascadedCamArr[0].getDirectionalLight().isCastingShadows();
    }

    private static boolean ready(int index) {
        return cascadedCamArr[index].getDirectionalLight() != null && cascadedCamArr[index].getViewMatrix() != null;
    }

    protected static int getShadowMap(int index) {
        return shadowFrameBuffer.getColorAttachmentTexture(0)[index];
    }

    private static Matrix4f getShadowViewMatrix(int index) {
        return cascadedCamArr[index].getViewMatrix();
    }

    private static float getCascadeSplits(int index) {
        return cascadeSplit[index];
    }

    public void recompileGeometryShader() {
        OxyShader pbrShader = ShaderLibrary.get("OxyPBR");
        pbrShader.recompile();
    }

    public void clear() {
        cachedLightEntities.clear();
        cachedCameraComponents.clear();
        allModelEntities.clear();
        cachedNativeMeshes.clear();
    }

    public void endFrame() {
        if (ScenePanel.availContentRegionSize.x != mainFrameBuffer.getWidth() || ScenePanel.availContentRegionSize.y != mainFrameBuffer.getHeight()) {
            mainFrameBuffer.setNeedResize(true, (int) ScenePanel.availContentRegionSize.x, (int) ScenePanel.availContentRegionSize.y);
            pickingFrameBuffer.setNeedResize(true, (int) ScenePanel.availContentRegionSize.x, (int) ScenePanel.availContentRegionSize.y);
        }
    }
}
