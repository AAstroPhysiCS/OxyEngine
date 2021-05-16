package OxyEngine.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Layers.UILayer;
import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Buffer.Platform.FrameBufferSpecification;
import OxyEngine.Core.Renderer.Buffer.Platform.FrameBufferTextureFormat;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.CullMode;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Light.SkyLight;
import OxyEngine.Core.Renderer.Mesh.MeshRenderMode;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Renderer.Mesh.NativeMeshOpenGL;
import OxyEngine.Core.Renderer.OxyRenderPass;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Core.Renderer.Pipeline.ShaderType;
import OxyEngine.Core.Renderer.ShadowRenderer;
import OxyEngine.Core.Renderer.Texture.HDRTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.OxyEngine;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyMaterialPool;
import OxyEngine.Scene.Objects.Model.OxyNativeObject;
import OxyEngine.Scene.Objects.WorldGrid;
import OxyEngine.Core.Renderer.Texture.TextureSlot;
import OxyEngineEditor.UI.Gizmo.OxySelectHandler;
import OxyEngineEditor.UI.Panels.GUINode;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static OxyEngine.Core.Renderer.Light.Light.LIGHT_SIZE;
import static OxyEngine.Core.Renderer.ShadowRenderer.NUMBER_CASCADES;
import static OxyEngine.Scene.SceneRuntime.*;
import static OxyEngineEditor.UI.Panels.ScenePanel.editorCameraEntity;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public final class SceneRenderer {

    private static SceneRenderer INSTANCE = null;

    public Set<OxyEntity> cachedLightEntities, cachedNativeMeshes;
    public Set<OxyEntity> cachedCameraComponents;
    public Set<OxyEntity> allModelEntities;

    public OxyCamera mainCamera;

    public static SceneRenderer getInstance() {
        if (INSTANCE == null) INSTANCE = new SceneRenderer();
        return INSTANCE;
    }

    private OxyPipeline geometryPipeline, hdrPipeline;

    private OpenGLFrameBuffer frameBuffer, blittedFrameBuffer;

    private SceneRenderer() {
    }

    public void initPipelines() {

        frameBuffer = FrameBuffer.create(OxyEngine.getWindowHandle().getWidth(), OxyEngine.getWindowHandle().getHeight(),
                FrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setTextureCount(1)
                        .setAttachmentIndex(0)
                        .setMultiSampled(true)
                        .setFormats(FrameBufferTextureFormat.RGBA8, FrameBufferTextureFormat.DEPTH24STENCIL8)
                        .useRenderBuffer(true));

        blittedFrameBuffer = FrameBuffer.create(frameBuffer.getWidth(), frameBuffer.getHeight(),
                FrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setTextureCount(1)
                        .setAttachmentIndex(0)
                        .setFormats(FrameBufferTextureFormat.RGBA8)
                        .setFilter(GL_LINEAR, GL_LINEAR));

        OxyShader pbrShader = OxyShader.createShader("OxyPBRAnimation", "shaders/OxyPBRAnimation.glsl");

        OxyRenderPass geometryRenderPass = OxyRenderPass.createBuilder(frameBuffer)
                .renderingMode(MeshRenderMode.TRIANGLES)
                .setCullFace(CullMode.DISABLED)
                .create();

        geometryPipeline = OxyPipeline.createNewPipeline(OxyPipeline.createNewSpecification()
                .setDebugName("Geometry Pipeline")
                .setRenderPass(geometryRenderPass)
                .createLayout(OxyPipeline.createNewPipelineLayout() //vertex buffer
                        .targetBuffer(VertexBuffer.class)
                        .set(OxyShader.VERTICES, ShaderType.Float3)
                        .set(OxyShader.OBJECT_ID, ShaderType.Float1)
                        .set(OxyShader.BONEIDS, ShaderType.Float4)
                        .set(OxyShader.WEIGHTS, ShaderType.Float4)
                )
                .createLayout(OxyPipeline.createNewPipelineLayout() //index Buffer
                        .targetBuffer(IndexBuffer.class)
                )
                .createLayout(OxyPipeline.createNewPipelineLayout() //texture buffer
                        .targetBuffer(TextureBuffer.class)
                        .set(OxyShader.TEXTURE_COORDS, ShaderType.Float2)
                )
                .createLayout(OxyPipeline.createNewPipelineLayout() //normals buffer
                        .targetBuffer(NormalsBuffer.class)
                        .set(OxyShader.NORMALS, ShaderType.Float3)
                )
                .createLayout(OxyPipeline.createNewPipelineLayout() //tangents buffer
                        .targetBuffer(TangentBuffer.class)
                        .set(OxyShader.TANGENT, ShaderType.Float3)
                        .set(OxyShader.BITANGENT, ShaderType.Float3)
                )
                .setShader(pbrShader));

        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;

        pbrShader.begin();
        pbrShader.setUniform1iv("tex", samplers);
        pbrShader.end();

        OxyRenderPass hdrRenderPass = OxyRenderPass.createBuilder(frameBuffer)
                .renderingMode(MeshRenderMode.TRIANGLES)
                .setCullFace(CullMode.FRONT)
                .create();

        OxyShader hdrShader = OxyShader.createShader("OxyHDR", "shaders/OxyHDR.glsl");
        hdrPipeline = OxyPipeline.createNewPipeline(OxyPipeline.createNewSpecification()
                .setDebugName("HDR Pipeline")
                .setRenderPass(hdrRenderPass)
                .createLayout(OxyPipeline.createNewPipelineLayout()
                        .targetBuffer(VertexBuffer.class)
                        .set(OxyShader.VERTICES, ShaderType.Float3)
                )
                .createLayout(OxyPipeline.createNewPipelineLayout()
                        .targetBuffer(IndexBuffer.class)
                )
                .setShader(hdrShader));

        OxySelectHandler.init(frameBuffer.getWidth(), frameBuffer.getHeight());
        ShadowRenderer.initPipeline();
    }

    public void initScene() {

        cachedNativeMeshes = ACTIVE_SCENE.view(NativeMeshOpenGL.class);
        cachedCameraComponents = ACTIVE_SCENE.view(OxyCamera.class);
        updateModelEntities();

        fillPropertyEntries();

        updateCurrentBoundedCamera(0);

        new WorldGrid(ACTIVE_SCENE, 10);
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
        allModelEntities = ACTIVE_SCENE.view(ModelMeshOpenGL.class);
        updateLightEntities();
    }

    public void updateCameraEntities() {
        cachedCameraComponents = ACTIVE_SCENE.view(OxyCamera.class);
    }

    public void updateNativeEntities() {
        cachedNativeMeshes = ACTIVE_SCENE.view(NativeMeshOpenGL.class);
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

    public void updateScene(float ts) {
        if (ACTIVE_SCENE == null) return;
        SceneRuntime.onUpdate(ts);

        //Camera
        updateCurrentBoundedCamera(ts);

        if (currentBoundedCamera == null) return;

        //RESET ALL THE LIGHT STATES
        OxyShader pbrShader = ShaderLibrary.get("OxyPBRAnimation");
        for (int i = 0; i < LIGHT_SIZE; i++) {
            pbrShader.begin();
            pbrShader.setUniform1i("p_Light[" + i + "].activeState", 0);
            pbrShader.setUniform1i("d_Light[" + i + "].activeState", 0);
            pbrShader.end();
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

    private void geometryPass(float ts) {
        if (ACTIVE_SCENE == null) return;
        if (currentBoundedCamera == null) return;

        HDRTexture hdrTexture = null;
        SkyLight skyLightComp = null;
        if (currentBoundedSkyLight != null) {
            skyLightComp = currentBoundedSkyLight.get(SkyLight.class);
            if (skyLightComp != null) hdrTexture = skyLightComp.getHDRTexture();
        }

        OpenGLFrameBuffer.blit(frameBuffer, blittedFrameBuffer);
        frameBuffer.flush();

        {
            OxyShader pbrShader = geometryPipeline.getShader();
            OxyRenderPass geometryRenderPass = geometryPipeline.getRenderPass();
            geometryRenderPass.beginRenderPass();

            for (OxyEntity e : allModelEntities) {
                if (!e.has(SelectedComponent.class)) continue;
                RenderableComponent renderableComponent = e.get(RenderableComponent.class);
                if (renderableComponent.mode != RenderingMode.Normal) continue;
                ModelMeshOpenGL modelMesh = e.get(ModelMeshOpenGL.class);
                OxyMaterial material = null;
                if (e.has(OxyMaterialIndex.class)) material = OxyMaterialPool.getMaterial(e);
                TransformComponent c = e.get(TransformComponent.class);

                pbrShader.begin();

                //ANIMATION UPDATE
                pbrShader.setUniform1i("animatedModel", 0);
                if (e.has(AnimationComponent.class)) {
                    AnimationComponent animComp = e.get(AnimationComponent.class);
                    if (ACTIVE_SCENE.STATE == SceneState.RUNNING) {
                        pbrShader.setUniform1i("animatedModel", 1);
                        animComp.updateAnimation(ts);
                        List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                        for (int j = 0; j < matrix4fList.size(); j++) {
                            pbrShader.setUniformMatrix4fv("finalBonesMatrices[" + j + "]", matrix4fList.get(j), false);
                        }
                    } else animComp.setTime(0);
                }

                pbrShader.setUniformMatrix4fv("model", c.transform, false);
                int iblSlot = TextureSlot.UNUSED.getValue(), prefilterSlot = TextureSlot.UNUSED.getValue(), brdfLUTSlot = TextureSlot.UNUSED.getValue();
                if (hdrTexture != null) {
                    iblSlot = hdrTexture.getIBLSlot();
                    prefilterSlot = hdrTexture.getPrefilterSlot();
                    brdfLUTSlot = hdrTexture.getBDRFSlot();
                    hdrTexture.bindAll();
                }
                pbrShader.setUniform1i("iblMap", iblSlot);
                pbrShader.setUniform1i("prefilterMap", prefilterSlot);
                pbrShader.setUniform1i("brdfLUT", brdfLUTSlot);
                if (skyLightComp != null) {
                    pbrShader.setUniform1f("hdrIntensity", skyLightComp.intensity[0]);
                    pbrShader.setUniform1f("gamma", skyLightComp.gammaStrength[0]);
                    pbrShader.setUniform1f("exposure", skyLightComp.exposure[0]);
                } else {
                    pbrShader.setUniform1f("hdrIntensity", 1.0f);
                    pbrShader.setUniform1f("gamma", 2.2f);
                    pbrShader.setUniform1f("exposure", 1.0f);
                }

                boolean castShadows = ShadowRenderer.castShadows();
                pbrShader.setUniform1i("castShadows", castShadows ? 1 : 0);
                if (castShadows) {
                    if (ShadowRenderer.cascadeIndicatorToggle)
                        pbrShader.setUniform1i("cascadeIndicatorToggle", 1);
                    else pbrShader.setUniform1i("cascadeIndicatorToggle", 0);

                    for (int i = 0; i < NUMBER_CASCADES; i++) {
                        if (ShadowRenderer.ready(i)) {
                            glBindTextureUnit(TextureSlot.CSM.getValue() + i, ShadowRenderer.getShadowMap(i));
                            pbrShader.setUniform1i("shadowMap[" + i + "]", TextureSlot.CSM.getValue() + i);
                            pbrShader.setUniformMatrix4fv("lightSpaceMatrix[" + i + "]", ShadowRenderer.getShadowViewMatrix(i), false);
                            pbrShader.setUniform1f("cascadeSplits[" + i + "]", ShadowRenderer.getCascadeSplits(i));
                        }
                    }
                }

                pbrShader.end();
                if (material != null) material.push(pbrShader);
                OxyRenderer.renderMesh(geometryPipeline, modelMesh, mainCamera);
            }
            /*for (OxyEntity e : cachedNativeMeshes) {
                OpenGLMesh mesh = e.get(OpenGLMesh.class);
                if (e.has(SkyLight.class)) continue; //DO NOT RENDER THE SKYLIGHT HERE

                if (e.has(OxyMaterialIndex.class)) {
                    OxyMaterial m = OxyMaterialPool.getMaterial(e);
                    if (m != null) {
                        m.push(pbrShader);
                        OxyRenderer.renderMesh(geometryPipeline, mesh, mainCamera);
                    }
                }
            }*/
            geometryRenderPass.endRenderPass();
        }

        {
            if (WorldGrid.grid != null) {
                OxyPipeline gridPipeline = WorldGrid.getPipeline();
                OxyRenderPass gridRenderPass = gridPipeline.getRenderPass();
                gridRenderPass.beginRenderPass();
                OxyShader gridShader = gridPipeline.getShader();
                gridShader.begin();
                gridShader.setUniformMatrix4fv("v_Matrix", mainCamera.getViewMatrix(), mainCamera.isTranspose());
                gridShader.end();
                OxyRenderer.renderMesh(gridPipeline, WorldGrid.grid.get(OpenGLMesh.class), mainCamera);
                gridRenderPass.endRenderPass();
            }
        }

        {
            if (currentBoundedSkyLight != null) {
                if (hdrTexture != null) {

                    OxyRenderPass hdrRenderPass = hdrPipeline.getRenderPass();
                    hdrRenderPass.beginRenderPass();

                    hdrTexture.bindAll();
                    glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);

                    OxyShader skyBoxShader = ShaderLibrary.get("OxySkybox");

                    skyBoxShader.begin(); //first render it to the skybox shader
                    if (skyLightComp.mipLevelStrength[0] > 0)
                        skyBoxShader.setUniform1i("u_skyBoxTexture", hdrTexture.getPrefilterSlot());
                    else skyBoxShader.setUniform1i("u_skyBoxTexture", hdrTexture.getHDRSlot());
                    skyBoxShader.setUniform1f("u_mipLevel", skyLightComp.mipLevelStrength[0]);
                    skyBoxShader.setUniform1f("u_exposure", skyLightComp.exposure[0]);
                    skyBoxShader.setUniform1f("u_gamma", skyLightComp.gammaStrength[0]);
                    skyBoxShader.end();

                    OxyRenderer.renderMesh(hdrPipeline, currentBoundedSkyLight.get(OpenGLMesh.class), mainCamera, skyBoxShader);

                    glDisable(GL_TEXTURE_CUBE_MAP_SEAMLESS);

                    hdrRenderPass.endRenderPass();
                }
            }
        }

    }

    public void renderScene(float ts) {
        geometryPass(ts);
        shadowPass();

        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        OxyTexture.unbindAllTextures();

        UILayer.uiSystem.dispatchNativeEvents();
        OxyRenderer.Stats.totalShapeCount = ACTIVE_SCENE.getShapeCount();
    }

    public void shadowPass() {
        for (OxyEntity e : cachedLightEntities) {
            //TODO: Change this, make this dynamic
            if (e.get(Light.class) instanceof DirectionalLight d && d.isCastingShadows()) {
                ShadowRenderer.shadowPass(d);
                break;
            }
        }
    }

    public void recompileGeometryShader() {
        OxyShader pbrShaderOld = ShaderLibrary.get("OxyPBRAnimation");
        pbrShaderOld.dispose();
        OxyShader pbrShader = OxyShader.createShader("OxyPBRAnimation", "shaders/OxyPBRAnimation.glsl");

        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;

        pbrShader.begin();
        pbrShader.setUniform1iv("tex", samplers);
        pbrShader.end();
    }

    public void clear() {
        cachedLightEntities.clear();
        cachedCameraComponents.clear();
        allModelEntities.clear();
    }

    public OpenGLFrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public OpenGLFrameBuffer getBlittedFrameBuffer() {
        return blittedFrameBuffer;
    }

    public OxyPipeline getGeometryPipeline() {
        return geometryPipeline;
    }

    public OxyPipeline getHDRPipeline() {
        return hdrPipeline;
    }
}
