package OxyEngine.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Context.CullMode;
import OxyEngine.Core.Context.OxyRenderPass;
import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Buffer.*;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.FrameBufferSpecification;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.TextureFormat;
import OxyEngine.Core.Context.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Context.Renderer.Light.Light;
import OxyEngine.Core.Context.Renderer.Light.SkyLight;
import OxyEngine.Core.Context.Renderer.Mesh.MeshRenderMode;
import OxyEngine.Core.Context.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Context.Renderer.Mesh.NativeMeshOpenGL;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderType;
import OxyEngine.Core.Context.Renderer.ShadowRenderer;
import OxyEngine.Core.Context.Renderer.Texture.HDRTexture;
import OxyEngine.Core.Context.Renderer.Texture.OxyColor;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.MouseCode;
import OxyEngineEditor.UI.OxySelectHandler;
import OxyEngineEditor.UI.Panels.GUINode;
import OxyEngineEditor.UI.Panels.ScenePanel;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static OxyEngine.Core.Context.Renderer.Light.Light.LIGHT_SIZE;
import static OxyEngine.Scene.SceneRuntime.*;
import static OxyEngineEditor.UI.Panels.ScenePanel.editorCameraEntity;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.opengl.GL44.glClearTexImage;

public final class SceneRenderer {

    private static SceneRenderer INSTANCE = null;

    public Set<OxyEntity> cachedLightEntities, cachedNativeMeshes;
    public Set<OxyEntity> cachedCameraComponents;
    public Set<OxyEntity> allModelEntities;

    public static SceneRenderer getInstance() {
        if (INSTANCE == null) INSTANCE = new SceneRenderer();
        return INSTANCE;
    }

    private OxyPipeline geometryPipeline, hdrPipeline;

    private OpenGLFrameBuffer mainFrameBuffer;

    static OpenGLFrameBuffer pickingFrameBuffer;
    static OxyRenderPass pickingRenderPass;

    private SceneRenderer() {
    }

    private void initShaders() {
        OxyShader.createShader("OxyPBR", "src/main/resources/shaders/OxyPBR.glsl");
        OxyShader.createShader("OxyHDR", "src/main/resources/shaders/OxyHDR.glsl");
        OxyShader.createShader("OxySkybox", "src/main/resources/shaders/OxySkybox.glsl");
        OxyShader.createShader("OxyDepthMap", "src/main/resources/shaders/OxyDepthMap.glsl");
        OxyShader.createShader("OxyIBL", "src/main/resources/shaders/OxyIBL.glsl");
        OxyShader.createShader("OxyPrefiltering", "src/main/resources/shaders/OxyPrefiltering.glsl");
        OxyShader.createShader("OxyBDRF", "src/main/resources/shaders/OxyBDRF.glsl");
        OxyShader.createShader("OxyGrid", "src/main/resources/shaders/OxyGrid.glsl");
    }

    public void initPipelines() {
        initShaders();

        int width = (int) ScenePanel.windowSize.x;
        int height = (int) ScenePanel.windowSize.y;

        RenderBuffer mainRenderBuffer = RenderBuffer.create(TextureFormat.DEPTH24STENCIL8, width, height);
        mainFrameBuffer = FrameBuffer.create(width, height, new OxyColor(0f, 0f, 0f, 1f),
                FrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setTextureCount(1)
                        .setAttachmentIndex(0)
                        .setMultiSampled(true)
                        .setFormat(TextureFormat.RGBA8)
                        .useRenderBuffer(mainRenderBuffer));

        mainFrameBuffer.createBlittingFrameBuffer(FrameBuffer.createNewSpec(FrameBufferSpecification.class)
                .setTextureCount(1)
                .setAttachmentIndex(0)
                .setFormat(TextureFormat.RGBA8)
                .setFilter(GL_LINEAR, GL_LINEAR));

        OxyShader pbrShader = ShaderLibrary.get("OxyPBR");

        OxyRenderPass geometryRenderPass = OxyRenderPass.createBuilder(mainFrameBuffer)
                .renderingMode(MeshRenderMode.TRIANGLES)
                .setCullFace(CullMode.DISABLED)
                .enableBlending()
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

        OxyRenderPass hdrRenderPass = OxyRenderPass.createBuilder(mainFrameBuffer)
                .renderingMode(MeshRenderMode.TRIANGLES)
                .setCullFace(CullMode.FRONT)
                .create();

        OxyShader hdrShader = ShaderLibrary.get("OxyHDR");
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

        pickingFrameBuffer = FrameBuffer.create(mainFrameBuffer.getWidth(), mainFrameBuffer.getHeight(), new OxyColor(0f, 0f, 0f, 1.0f),
                OpenGLFrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setTextureCount(1)
                        .setAttachmentIndex(0)
                        .setFormat(TextureFormat.RGBA8)
                        .setFilter(GL_LINEAR, GL_LINEAR),
                OpenGLFrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setTextureCount(1)
                        .setAttachmentIndex(1)
                        .setFormat(TextureFormat.R32I)
                        .setFilter(GL_NEAREST, GL_NEAREST),
                OpenGLFrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setTextureCount(1)
                        .setStorage(true, 1));
        pickingFrameBuffer.drawBuffers(0, 1);
        pickingRenderPass = OxyRenderPass.createBuilder(pickingFrameBuffer).create();

        ShadowRenderer.initPipeline();
    }

    public void initScene() {

        cachedNativeMeshes = ACTIVE_SCENE.view(NativeMeshOpenGL.class);
        cachedCameraComponents = ACTIVE_SCENE.view(OxyCamera.class);
        updateModelEntities();

        fillPropertyEntries();

        updateCurrentBoundedCamera();

        if (WorldGrid.grid == null) new WorldGrid(ACTIVE_SCENE, 10);
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

        //RESET ALL THE LIGHT STATES
        OxyShader pbrShader = ShaderLibrary.get("OxyPBR");
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

    private void geometryPass() {
        if (ACTIVE_SCENE == null) return;
        if (currentBoundedCamera == null) return;
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);

        {
            OxyRenderPass geometryRenderPass = geometryPipeline.getRenderPass();
            OxyRenderer.beginRenderPass(geometryRenderPass);

            for (OxyEntity e : allModelEntities) {
                if (!e.has(SelectedComponent.class)) continue;
                RenderableComponent renderableComponent = e.get(RenderableComponent.class);
                if (renderableComponent.mode != RenderingMode.Normal) continue;
                OpenGLMesh modelMesh = e.get(OpenGLMesh.class);
                e.update();
                OxyRenderer.renderMesh(geometryPipeline, modelMesh);
            }

            OxyRenderer.endRenderPass();
        }

        {
            if (WorldGrid.grid != null) {
                OxyPipeline gridPipeline = WorldGrid.getPipeline();

                OxyRenderPass gridPass = gridPipeline.getRenderPass();
                OxyRenderer.beginRenderPass(gridPass);
                OxyRenderer.renderMesh(gridPipeline, WorldGrid.grid.get(OpenGLMesh.class));
                OxyRenderer.endRenderPass();
            }
        }

        {
            if (currentBoundedSkyLight != null) {

                HDRTexture hdrTexture = null;
                SkyLight skyLightComp = currentBoundedSkyLight.get(SkyLight.class);
                if (skyLightComp != null) hdrTexture = skyLightComp.getHDRTexture();

                if (hdrTexture != null) {

                    OxyRenderPass hdrRenderPass = hdrPipeline.getRenderPass();
                    OxyRenderer.beginRenderPass(hdrRenderPass);

                    hdrTexture.bindAll();

                    OxyShader skyBoxShader = ShaderLibrary.get("OxySkybox");

                    skyBoxShader.begin(); //first render it to the skybox shader
                    if (skyLightComp.mipLevelStrength[0] > 0)
                        skyBoxShader.setUniform1i("u_skyBoxTexture", hdrTexture.getPrefilterSlot());
                    else skyBoxShader.setUniform1i("u_skyBoxTexture", hdrTexture.getHDRSlot());
                    skyBoxShader.setUniform1f("u_mipLevel", skyLightComp.mipLevelStrength[0]);
                    skyBoxShader.end();

                    OxyRenderer.renderMesh(hdrPipeline, currentBoundedSkyLight.get(OpenGLMesh.class), skyBoxShader);

                    OxyRenderer.endRenderPass();
                }
            }
        }
        glDisable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
    }

    private void idPass() {

        int[] clearValue = {-1};

        OxyRenderer.beginRenderPass(pickingRenderPass);

        OxyPipeline geometryPipeline = SceneRenderer.getInstance().getGeometryPipeline();
        OxyShader pbrShader = geometryPipeline.getShader();

        glClearTexImage(pickingFrameBuffer.getColorAttachmentTexture(1)[0], 0, pickingFrameBuffer.getTextureFormat(1).getStorageFormat(), GL_INT, clearValue);

        for (OxyEntity e : allModelEntities) {
            if (!e.has(SelectedComponent.class)) continue;
            RenderableComponent renderableComponent = e.get(RenderableComponent.class);
            if (renderableComponent.mode != RenderingMode.Normal) continue;
            pbrShader.begin();
            pbrShader.setUniform1i("Shadows.animatedModel", 0);
            if (e.has(AnimationComponent.class)) {
                pbrShader.setUniform1i("Shadows.animatedModel", 1);
                AnimationComponent animComp = e.get(AnimationComponent.class);
                List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                for (int j = 0; j < matrix4fList.size(); j++) {
                    pbrShader.setUniformMatrix4fv("Shadows.finalBonesMatrices[" + j + "]", matrix4fList.get(j), false);
                }
            }
            pbrShader.setUniformMatrix4fv("Transforms.model", e.get(TransformComponent.class).transform, false);
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

    private void shadowPass() {
        for (OxyEntity e : cachedLightEntities) {
            //TODO: Change this, make this dynamic
            if (e.get(Light.class) instanceof DirectionalLight d && d.isCastingShadows()) {
                ShadowRenderer.shadowPass(d);
                break;
            }
        }
    }

    public void recompileGeometryShader() {
        OxyShader pbrShader = ShaderLibrary.get("OxyPBR");
        pbrShader.recompile();
    }

    public void clear() {
        cachedLightEntities.clear();
        cachedCameraComponents.clear();
        allModelEntities.clear();
    }

    public OpenGLFrameBuffer getMainFrameBuffer() {
        return mainFrameBuffer;
    }

    public OpenGLFrameBuffer getPickingFrameBuffer() {
        return pickingFrameBuffer;
    }

    public OxyPipeline getGeometryPipeline() {
        return geometryPipeline;
    }

    public OxyPipeline getHDRPipeline() {
        return hdrPipeline;
    }
}
