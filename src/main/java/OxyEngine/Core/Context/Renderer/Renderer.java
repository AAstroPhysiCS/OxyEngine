package OxyEngine.Core.Context.Renderer;

import OxyEngine.Components.AnimationComponent;
import OxyEngine.Components.EntityComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Camera.Camera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Context.Renderer.Light.*;
import OxyEngine.Core.Context.Renderer.Mesh.*;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.FrameBufferSpecification;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.TextureFormat;
import OxyEngine.Core.Context.Renderer.Texture.*;
import OxyEngine.Core.Context.Scene.Material;
import OxyEngine.Core.Context.Scene.SceneRuntime;
import OxyEngine.Core.Context.Scene.SceneState;
import OxyEngine.Core.Layers.ImGuiLayer;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.MouseCode;
import OxyEngine.OxyEngine;
import OxyEngine.System.FileSystem;
import OxyEngine.TargetPlatform;
import OxyEngineEditor.UI.SelectHandler;
import OxyEngineEditor.UI.Panels.ScenePanel;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static OxyEngine.Core.Context.Renderer.Light.Light.LIGHT_SIZE;
import static OxyEngine.Core.Context.Renderer.RenderCommand.targetPlatform;
import static OxyEngine.Core.Context.Scene.SceneRuntime.*;
import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static OxyEngineEditor.UI.UIAssetManager.DEFAULT_TEXTURE_PARAMETER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
import static org.lwjgl.opengl.GL30.GL_RED_INTEGER;
import static org.lwjgl.opengl.GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;

public final class Renderer {

    private static boolean INIT = false;

    private static final List<DrawCommand> allDrawCommands = new ArrayList<>();
    private static final List<PointLight> allPointLights = new ArrayList<>();
    private static final List<DirectionalLight> allDirectionalLights = new ArrayList<>();
    private static final List<SkyLight> allSkyLights = new ArrayList<>();

    private static UniformBuffer environmentSettingsUniformBuffer;

    private static OpenGLMesh environmentMesh;

    private static Pipeline geometryPipeline, hdrPipeline, gridPipeline, depthPipeline;
    private static OpenGLFrameBuffer mainFrameBuffer, pickingFrameBuffer, depthFrameBuffer;

    private static Material gridMaterial;

    private static RenderPass pickingRenderPass;
    static RenderPass lineRenderPass;

    private static ShadowMapCamera[] cascadedCamArr = null;
    public static boolean cascadeIndicatorToggle;
    static final int NUMBER_CASCADES = 4;
    static final int FRUSTUM_CORNERS = 8;

    static final float[] cascadeSplit = new float[NUMBER_CASCADES];

    static {
        cascadeSplit[0] = 150f;
        cascadeSplit[1] = 300f;
        cascadeSplit[2] = 550f;
        cascadeSplit[3] = 1000f;
    }

    private static OpenGLMesh gridMesh;
    private static boolean enableGrid = false;
    public static boolean showBoundingBoxes;

    public static float FPS = 0;
    public static float FRAME_TIME = 0;
    public static float TS = 0;

    public static Image2DTexture testTexture;

    private Renderer() {
    }

    public static void init(TargetPlatform targetPlatform, boolean debug) {
        if (INIT) {
            logger.warning("Renderer already initialized!");
            return;
        }
        INIT = true;
        RenderCommand.init(targetPlatform, debug);

        if (debug) ImGuiLayer.getInstance(OxyEngine.getWindowHandle()).addPanel(DebugPanel.getInstance());

        float[] size = getIniViewportSize();
        ScenePanel.windowSize.set(size[0], size[1]);

        environmentSettingsUniformBuffer = UniformBuffer.create(3 * Float.BYTES, 1);

        Shader.createShader("OxyPBR", "src/main/resources/shaders/OxyPBR.glsl");
        Shader.createShader("OxyPreetham", "src/main/resources/shaders/OxyPreetham.glsl");
        Shader.createShader("OxyEquirectangularToCubemap", "src/main/resources/shaders/OxyEquirectangularToCubemap.glsl");
        Shader.createShader("OxySkybox", "src/main/resources/shaders/OxySkybox.glsl");
        Shader.createShader("OxyDepthMap", "src/main/resources/shaders/OxyDepthMap.glsl");
        Shader.createShader("OxyIBL", "src/main/resources/shaders/OxyIBL.glsl");
        Shader.createShader("OxyPrefiltering", "src/main/resources/shaders/OxyPrefiltering.glsl");
        Shader.createShader("OxyBDRF", "src/main/resources/shaders/OxyBDRF.glsl");
        Shader.createShader("OxyLine", "src/main/resources/shaders/OxyLine.glsl");
        Shader.createCompute("OxyTestCompute", "src/main/resources/shaders/compute/test.oxycomp");

        initPipelines();
        initEnvMapMesh();
        initShaderConstants();

        testTexture = Texture.loadImage(TextureSlot.UNUSED, 512, 512,
                TexturePixelType.Float, TextureFormat.RGBA32F, DEFAULT_TEXTURE_PARAMETER);

        testTexture.bind();
        RenderCommand.bindImageTexture(0, testTexture.getTextureId(), 0, false, 0, GL_WRITE_ONLY, TextureFormat.RGBA32F.getInternalFormat());

        Shader computeShaderTest = Renderer.getShader("OxyTestCompute");
        computeShaderTest.begin();

        RenderCommand.dispatchCompute(512, 512, 1);
        RenderCommand.memoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        computeShaderTest.end();

        cascadedCamArr = new ShadowMapCamera[NUMBER_CASCADES];
        for (int i = 0; i < cascadedCamArr.length; i++) cascadedCamArr[i] = new ShadowMapCamera();
    }

    private static void initShaderConstants() {
        //TODO: ? perhaps, you could do the same with materials?
        Shader pbrShader = Renderer.getShader("OxyPBR");
        //RESET ALL THE LIGHT STATES
        for (int i = 0; i < LIGHT_SIZE; i++) {
            pbrShader.begin();
            pbrShader.setUniform1i("p_Light[" + i + "].activeState", 0);
            pbrShader.setUniform1i("d_Light[" + i + "].activeState", 0);
            pbrShader.end();
        }

        pbrShader.begin();
        pbrShader.setUniform1i("EnvironmentTex.iblMap", TextureSlot.UNUSED.getValue());
        pbrShader.setUniform1i("EnvironmentTex.prefilterMap", TextureSlot.UNUSED.getValue());
        pbrShader.setUniform1i("EnvironmentTex.brdfLUT", TextureSlot.UNUSED.getValue());
        pbrShader.setUniform1i("EnvironmentTex.skyBoxTexture", TextureSlot.UNUSED.getValue());
        pbrShader.end();
    }

    private static float[] getIniViewportSize() {
        String content = FileSystem.load(FileSystem.getResourceByPath("/ini/imgui.ini"));

        if (content.contains("Viewport")) {
            int index = content.indexOf("Size", content.indexOf("Viewport"));
            int newLine = content.indexOf("\n", index);
            content = ((String) content.subSequence(index, newLine)).replace("Size=", "");
        }

        String[] size = content.split(",");
        float width = Float.parseFloat(size[0]);
        float height = Float.parseFloat(size[1]);
        return new float[]{width, height};
    }

    private static void initPipelines() {

        int width = (int) ScenePanel.windowSize.x;
        int height = (int) ScenePanel.windowSize.y;

        RenderBuffer mainRenderBuffer = RenderBuffer.create(TextureFormat.DEPTH24STENCIL8, width, height);
        mainFrameBuffer = FrameBuffer.create(width, height, new Color(0f, 0f, 0f, 1f),
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

        Shader pbrShader = Renderer.getShader("OxyPBR");

        RenderPass geometryRenderPass = RenderPass.createBuilder(mainFrameBuffer)
                .renderingMode(RenderMode.TRIANGLES)
                .setCullFace(CullMode.DISABLED)
                .enableBlending()
                .create();

        //order matters
        geometryPipeline = Pipeline.createNewPipeline(Pipeline.createNewSpecification()
                .setDebugName("Geometry Pipeline")
                .setRenderPass(geometryRenderPass)
                .setVertexBufferLayout(Pipeline.createNewVertexBufferLayout()
                        .addShaderType(Shader.VERTICES, ShaderType.Float3)
                        .addShaderType(Shader.OBJECT_ID, ShaderType.Float1)
                        .addShaderType(Shader.BONEIDS, ShaderType.Float4)
                        .addShaderType(Shader.WEIGHTS, ShaderType.Float4)
                        .addShaderType(Shader.NORMALS, ShaderType.Float3)
                        .addShaderType(Shader.TEXTURE_COORDS, ShaderType.Float2)
                        .addShaderType(Shader.TANGENT, ShaderType.Float3)
                        .addShaderType(Shader.BITANGENT, ShaderType.Float3)
                ));

        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;

        pbrShader.begin();
        pbrShader.setUniform1iv("tex", samplers);
        pbrShader.end();

        pickingFrameBuffer = FrameBuffer.create(mainFrameBuffer.getWidth(), mainFrameBuffer.getHeight(), new Color(0f, 0f, 0f, 1.0f),
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
        pickingRenderPass = RenderPass.createBuilder(pickingFrameBuffer).create();

        depthFrameBuffer = FrameBuffer.create(512, 512, new Color(1f, 0f, 0f, 1.0f),
                OpenGLFrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setAttachmentIndex(0)
                        .setTextureCount(NUMBER_CASCADES)
                        .setSizeForTextures(0, 4096, 4096)
                        .setSizeForTextures(1, 2048, 2048)
                        .setSizeForTextures(2, 1024, 1024)
                        .setSizeForTextures(3, 512, 512)
                        .setFormat(TextureFormat.DEPTHCOMPONENT32COMPONENT)
                        .setFilter(GL_NEAREST, GL_NEAREST)
                        .wrapSTR(GL_REPEAT, GL_REPEAT, -1)
                        .disableReadWriteBuffer(true)
        );

        depthPipeline = Pipeline.createNewPipeline(Pipeline.createNewSpecification()
                .setRenderPass(RenderPass.createBuilder(depthFrameBuffer)
                        .setCullFace(CullMode.BACK)
                        .create())
                .setDebugName("Shadow Map Rendering Pipeline"));

        lineRenderPass = RenderPass.createBuilder(mainFrameBuffer)
                .renderingMode(RenderMode.LINES)
                .setCullFace(CullMode.BACK)
                .create();

        gridPipeline = Pipeline.createNewPipeline(Pipeline.createNewSpecification()
                .setDebugName("Grid Pipeline")
                .setRenderPass(lineRenderPass)
                .setVertexBufferLayout(Pipeline.createNewVertexBufferLayout()
                        .addShaderType(Shader.VERTICES, ShaderType.Float3)
                ));

        gridMaterial = Material.create(0, Renderer.getShader("OxyLine"));
        gridMaterial.albedoColor = new Color(1.0f, 1.0f, 1.0f, 0.2f);

        Renderer2D.initPipelines();
    }

    private static void initEnvMapMesh() {

        RenderPass hdrRenderPass = RenderPass.createBuilder(mainFrameBuffer)
                .renderingMode(RenderMode.TRIANGLES)
                .setCullFace(CullMode.FRONT)
                .create();

        hdrPipeline = Pipeline.createNewPipeline(Pipeline.createNewSpecification()
                .setDebugName("HDR Pipeline")
                .setRenderPass(hdrRenderPass)
                .setVertexBufferLayout(Pipeline.createNewVertexBufferLayout()
                        .addShaderType(Shader.VERTICES, ShaderType.Float3)
                ));

        environmentMesh = new OpenGLMesh(hdrPipeline, MeshUsage.STATIC);

        float[] skyboxVertices = {
                -1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                // front face
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                // left face
                -1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                // right face
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                // bottom face
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
                // top face
                -1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
        };

        environmentMesh.pushVertices(skyboxVertices);
        environmentMesh.loadGL();
    }

    public static int getEntityIDByMousePosition() {
        pickingFrameBuffer.bind();
        Vector2f mousePos = new Vector2f(
                ScenePanel.mousePos.x - ScenePanel.windowPos.x - ScenePanel.offset.x,
                ScenePanel.mousePos.y - ScenePanel.windowPos.y - ScenePanel.offset.y);
        mousePos.y = mainFrameBuffer.getHeight() - mousePos.y;
        RenderCommand.readBuffer(GL_COLOR_ATTACHMENT1);
        int[] entityID = new int[1];
        RenderCommand.readPixels((int) mousePos.x, (int) mousePos.y, 1, 1, GL_RED_INTEGER, GL_INT, entityID);
        pickingFrameBuffer.unbind();
        return entityID[0];
    }

    public static void renderSkyLight(Shader shader) {
        shader.begin();
        environmentMesh.bind();
        RenderCommand.drawArrays(hdrPipeline.getRenderPass().getRenderMode().getModeID(), 0, environmentMesh.getVertices().length);
        environmentMesh.unbind();
        shader.end();
    }

    public static Shader getShader(String shaderName) {
        return ShaderLibrary.get(shaderName);
    }

    public static void recompileGeometryShader() {
        Shader pbrShader = Renderer.getShader("OxyPBR");
        pbrShader.recompile();
    }

    public static void clearBuffer() {
        RenderCommand.clearBuffer();
    }

    public static void clearColor(Color color) {
        RenderCommand.clearColor(color);
    }

    public static void beginRenderPass(RenderPass renderPass) {
        if (RendererAPI.onStackRenderPass != null)
            throw new IllegalStateException("RenderPass already on stack. Did you forget to call endRenderPass?");
        FrameBuffer frameBuffer = renderPass.getFrameBuffer();
        if (frameBuffer.needResize()) {
            frameBuffer.resize(frameBuffer.getWidth(), frameBuffer.getHeight());
            if (SceneRuntime.cameraContext instanceof PerspectiveCamera p) {
                p.setAspect((float) frameBuffer.getWidth() / frameBuffer.getHeight());
                p.update();
            }
        }
        RenderCommand.beginRenderPass(renderPass);
    }

    public static void beginScene(Camera camera, float ts) {
        assert sceneContext != null : oxyAssert("Active scene is somehow null!");
        TS = ts;
        cameraContext = camera;
        mainFrameBuffer.blit();

        environmentSettingsUniformBuffer.setData(0, sceneContext.gammaStrength);
        environmentSettingsUniformBuffer.setData(4, sceneContext.exposure);
        environmentSettingsUniformBuffer.setData(8, new float[]{1.0f});

        if (skyLightEntityContext != null && sceneContext.isValid(skyLightEntityContext)) {
            SkyLight skyLightComp = skyLightEntityContext.get(SkyLight.class);
            if (skyLightComp != null) {
                environmentSettingsUniformBuffer.setData(8, skyLightComp.intensity);
            }
        }

        //Update SkyLight
        Shader pbrShader = Renderer.getShader("OxyPBR");

        int irradianceSlot = TextureSlot.UNUSED.getValue();
        int prefilterSlot = TextureSlot.UNUSED.getValue();
        int bdrfSlot = TextureSlot.UNUSED.getValue();
        int hdrSlot = TextureSlot.UNUSED.getValue();

        pbrShader.begin();
        if (skyLightEntityContext != null) {
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

        //Grid
        if (enableGrid) {
            if (gridMesh == null) gridMesh = buildGrid(10);
            RenderPass gridPass = gridPipeline.getRenderPass();

            Renderer.beginRenderPass(gridPass);
            gridMesh.bind();
            gridMaterial.bindMaterial();
            RenderCommand.drawElements(gridPass.getRenderMode().getModeID(), gridMesh.getIndices().length, GL_UNSIGNED_INT, 0);
            gridMaterial.unbindMaterial();
            gridMesh.unbind();
            Renderer.endRenderPass();
        }

        Renderer2D.beginScene();
    }

    public static void endScene() {
        assert sceneContext != null : oxyAssert("Active scene is somehow null!");

        int i = 0;
        for (Light light : allDirectionalLights) {
            var entity = sceneContext.findEntityByComponent(light);
            if (entity != null) light.update(entity, i++);
        }

        i = 0;
        for (Light light : allPointLights) {
            var entity = sceneContext.findEntityByComponent(light);
            if (entity != null) light.update(entity, i++);
        }

        geometryPass();
        shadowPass();
        if (Input.isMouseButtonPressed(MouseCode.GLFW_MOUSE_BUTTON_1) && ScenePanel.hoveredWindow
                && sceneContext.getState() == SceneState.STOP && !SelectHandler.isOverAnyGizmo()) {
            idPass();
            SelectHandler.startPicking();
        }
        Renderer2D.endScene();

        mainFrameBuffer.resetFlush();
        pickingFrameBuffer.resetFlush();
        depthFrameBuffer.resetFlush();

        if (ScenePanel.availContentRegionSize.x != mainFrameBuffer.getWidth() || ScenePanel.availContentRegionSize.y != mainFrameBuffer.getHeight()) {
            mainFrameBuffer.setNeedResize(true, (int) ScenePanel.availContentRegionSize.x, (int) ScenePanel.availContentRegionSize.y);
            pickingFrameBuffer.setNeedResize(true, (int) ScenePanel.availContentRegionSize.x, (int) ScenePanel.availContentRegionSize.y);
        }

        Stats.totalShapeCount = sceneContext.getShapeCount();
    }

    private static void shadowPass() {
        //Prepare the camera
        if (allDirectionalLights.size() == 0) return;

        DirectionalLight d = allDirectionalLights.get(0);

        if (d.getDirection() == null || !d.isCastingShadows()) return;

        Shader depthShader = Renderer.getShader("OxyDepthMap");
        RenderPass depthRenderPass = depthPipeline.getRenderPass();

        Renderer.beginRenderPass(depthRenderPass);

        for (int i = 0; i < cascadedCamArr.length; i++) {
            cascadedCamArr[i].setDirectionalLight(d);
            cascadedCamArr[i].prepare((PerspectiveCamera) cameraContext, cascadeSplit[i]);
            depthFrameBuffer.flushDepthAttachment(0, i);
        }

        for (DrawCommand drawCommand : allDrawCommands) {

            TransformComponent t = drawCommand.transform();
            OpenGLMesh mesh = drawCommand.mesh();
            AnimationComponent animComp = drawCommand.animation();

            if (mesh.getRenderMode() == RenderMode.NONE) continue;

            for (ShadowMapCamera camera : cascadedCamArr)
                camera.update();

            depthShader.begin();
            depthShader.setUniform1i("animatedModel", 0);
            if (animComp != null) {
                depthShader.setUniform1i("animatedModel", 1);
                List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                for (int j = 0; j < matrix4fList.size(); j++) {
                    depthShader.setUniformMatrix4fv("finalBonesMatrices[" + j + "]", matrix4fList.get(j));
                }
            }
            depthShader.end();

            for (int i = 0; i < cascadedCamArr.length; i++) {
                ShadowMapCamera camera = cascadedCamArr[i];

                depthFrameBuffer.bindDepthAttachment(0, i);
                depthShader.begin();
                depthShader.setUniformMatrix4fv("lightSpaceMatrix", camera.getViewMatrix());
                depthShader.end();
                mesh.bind();
                depthShader.begin();
                for (OpenGLMesh.Submesh submesh : mesh.getSubmeshes()) {
                    depthShader.setUniformMatrix4fv("model", new Matrix4f(t.transform).mul(submesh.t().transform));
                    RenderCommand.drawElementsIndexed(depthPipeline.getRenderPass().getRenderMode().getModeID(), submesh.indexCount(),
                            GL_UNSIGNED_INT, submesh.baseIndex(), submesh.baseVertex());
                }
                depthShader.end();
                mesh.unbind();
            }
        }
        Renderer.endRenderPass();
    }

    private static void geometryPass() {

        for (SkyLight skyLight : allSkyLights) {
            if (skyLight.isPrimary()) {
                skyLightEntityContext = sceneContext.findEntityByComponent(skyLight);
                skyLight.bind();

                Shader skyBoxShader = Renderer.getShader("OxySkybox");

                if (skyLight instanceof HDREnvironmentMap envMap) {
                    EnvironmentTexture environmentTexture = envMap.getEnvironmentTexture();
                    if (environmentTexture != null) {

                        skyBoxShader.begin(); //render it to the skybox shader
                        if (envMap.mipLevelStrength[0] > 0)
                            skyBoxShader.setUniform1i("u_skyBoxTexture", TextureSlot.PREFILTER.getValue());
                        else
                            skyBoxShader.setUniform1i("u_skyBoxTexture", TextureSlot.HDR.getValue());
                        skyBoxShader.setUniform1f("u_mipLevel", envMap.mipLevelStrength[0]);
                        skyBoxShader.end();

                        mainFrameBuffer.bind();
                        Renderer.renderSkyLight(skyBoxShader);
                        mainFrameBuffer.unbind();
                    }
                } else if (skyLight instanceof DynamicSky) {

                    skyBoxShader.begin(); //render it to the skybox shader
                    skyBoxShader.setUniform1i("u_skyBoxTexture", TextureSlot.HDR.getValue());
                    skyBoxShader.end();

                    mainFrameBuffer.bind();
                    Renderer.renderSkyLight(skyBoxShader);
                    mainFrameBuffer.unbind();
                }
            }
        }

        for (DrawCommand drawCommand : allDrawCommands) {
            OpenGLMesh mesh = drawCommand.mesh();
            TransformComponent transform = drawCommand.transform();
            AnimationComponent animComp = drawCommand.animation();

            if (mesh.getRenderMode() == RenderMode.NONE) continue;

            Pipeline pipeline = mesh.getPipeline();
            RenderPass renderPass = pipeline.getRenderPass();

            if (mesh.empty())
                mesh.loadGL();

            Renderer.beginRenderPass(renderPass);
            mesh.bind();

            for (OpenGLMesh.Submesh submesh : mesh.getSubmeshes()) {

                mesh.getMaterial(submesh.assimpMaterialIndex()).ifPresent((m) -> {

                    Shader shader = m.getShader();

                    m.bindMaterial();

                    boolean castShadows = castShadows();
                    shader.setUniform1i("Shadows.castShadows", castShadows ? 1 : 0);
                    if (castShadows) {
                        if (cascadeIndicatorToggle) shader.setUniform1i("Shadows.cascadeIndicatorToggle", 1);
                        else shader.setUniform1i("Shadows.cascadeIndicatorToggle", 0);

                        for (int i = 0; i < NUMBER_CASCADES; i++) {
                            if (shadowsReady(i)) {
                                RenderCommand.bindTextureUnit(TextureSlot.CSM.getValue() + i, getShadowMap(i));
                                shader.setUniform1i("Shadows.shadowMap[" + i + "]", TextureSlot.CSM.getValue() + i);
                                shader.setUniformMatrix4fv("lightSpaceMatrix[" + i + "]", getShadowViewMatrix(i));
                                shader.setUniform1f("Shadows.cascadeSplits[" + i + "]", getCascadeSplits(i));
                            }
                        }
                    }

                    shader.setUniform1i("Animation.animatedModel", 0);
                    if (animComp != null) {
                        if (sceneContext.getState() == SceneState.PLAY) {
                            shader.setUniform1i("Animation.animatedModel", 1);
                            animComp.updateAnimation(Renderer.TS);
                            List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                            for (int j = 0; j < matrix4fList.size(); j++) {
                                shader.setUniformMatrix4fv("Animation.finalBonesMatrices[" + j + "]", matrix4fList.get(j));
                            }
                        } else animComp.setTime(0);
                    }

                    shader.setUniformMatrix4fv("Transforms.model", new Matrix4f(transform.transform).mul(submesh.t().transform));

                    RenderCommand.drawElementsIndexed(pipeline.getRenderPass().getRenderMode().getModeID(), submesh.indexCount(),
                            GL_UNSIGNED_INT, submesh.baseIndex(), submesh.baseVertex());
                    m.unbindMaterial();
                });
            }

            mesh.unbind();

            Renderer.endRenderPass();
        }

        if (showBoundingBoxes && entityContext != null && entityContext.has(OpenGLMesh.class)) {
            OpenGLMesh mesh = entityContext.get(OpenGLMesh.class);
            Matrix4f transform = entityContext.getTransform();
            Renderer2D.submitAABB(mesh.getAABB(), new Matrix4f(transform));
        }
    }

    private static void idPass() {

        int[] clearValue = {-1};

        Renderer.beginRenderPass(pickingRenderPass);

        RenderCommand.clearTexImage(Objects.requireNonNull(pickingFrameBuffer.getColorAttachmentTexture(1))[0], 0, pickingFrameBuffer.getTextureFormat(1).getStorageFormat(), GL_INT, clearValue);

        Shader pbrShader = Renderer.getShader("OxyPBR");

        for (DrawCommand drawCommand : allDrawCommands) {

            OpenGLMesh mesh = drawCommand.mesh();
            TransformComponent c = drawCommand.transform();
            AnimationComponent animComp = drawCommand.animation();

            if (mesh.getRenderMode() == RenderMode.NONE) continue;

            mesh.bind();
            pbrShader.begin();
            pbrShader.setUniform1i("Animation.animatedModel", 0);
            if (animComp != null) {
                if (sceneContext.getState() == SceneState.PLAY) {
                    pbrShader.setUniform1i("Animation.animatedModel", 1);
                    List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                    for (int j = 0; j < matrix4fList.size(); j++) {
                        pbrShader.setUniformMatrix4fv("Animation.finalBonesMatrices[" + j + "]", matrix4fList.get(j));
                    }
                }
            }

            for (OpenGLMesh.Submesh submesh : mesh.getSubmeshes()) {
                pbrShader.setUniformMatrix4fv("Transforms.model", new Matrix4f(c.transform).mul(submesh.t().transform));
                RenderCommand.drawElementsIndexed(geometryPipeline.getRenderPass().getRenderMode().getModeID(), submesh.indexCount(),
                        GL_UNSIGNED_INT, submesh.baseIndex(), submesh.baseVertex());
            }
            pbrShader.end();
            mesh.unbind();
        }

        Renderer.endRenderPass();
    }

    public static void endRenderPass() {
        if (RendererAPI.onStackRenderPass == null)
            throw new IllegalStateException("RenderPass not on stack. Did you forget to call beginRenderPass?");
        RenderCommand.endRenderPass();
    }

    static boolean castShadows() {
        if (cascadedCamArr == null) return false;
        if (!shadowsReady(0)) return false;
        return cascadedCamArr[0].getDirectionalLight().isCastingShadows();
    }

    static boolean shadowsReady(int index) {
        return cascadedCamArr[index].getDirectionalLight() != null && cascadedCamArr[index].getViewMatrix() != null;
    }

    static int getShadowMap(int index) {
        return Objects.requireNonNull(depthFrameBuffer.getColorAttachmentTexture(0))[index];
    }

    static Matrix4f getShadowViewMatrix(int index) {
        return cascadedCamArr[index].getViewMatrix();
    }

    static float getCascadeSplits(int index) {
        return cascadeSplit[index];
    }

    public static Pipeline getGeometryPipeline() {
        return geometryPipeline;
    }

    public static void enableGrid(boolean enableGrid) {
        Renderer.enableGrid = enableGrid;
    }

    public static void pollEvents() {
        RenderCommand.pollEvents();
    }

    public static void swapBuffers() {
        RenderCommand.swapBuffer(OxyEngine.getWindowHandle());
    }

    public static TargetPlatform getCurrentTargetPlatform() {
        return targetPlatform;
    }

    public static OpenGLFrameBuffer getMainFrameBuffer() {
        return mainFrameBuffer;
    }

    public static List<DrawCommand> getAllDrawCommands() {
        return allDrawCommands;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public static boolean removeFromCommand(EntityComponent... components) {
        boolean successRemove = false;
        outer:
        for (EntityComponent o : components) {
            successRemove |= allSkyLights.remove(o);
            successRemove |= allPointLights.remove(o);
            successRemove |= allDirectionalLights.remove(o);
            if (!successRemove) { // still not removed
                for (DrawCommand drawCommand : allDrawCommands) {
                    var mesh = drawCommand.mesh();
                    var anim = drawCommand.animation();
                    if ((mesh != null) && mesh.equals(o)) {
                        successRemove = allDrawCommands.remove(drawCommand);
                        break outer;
                    } else if ((anim != null) && anim.equals(o)) {
                        successRemove = allDrawCommands.remove(drawCommand);
                        break outer;
                    }
                }
            }
        }
        return successRemove;
    }

    public static void flushList() {
        allPointLights.clear();
        allDirectionalLights.clear();
        allSkyLights.clear();
        allDrawCommands.clear();
    }

    public static void submitMesh(OpenGLMesh mesh, TransformComponent transformComponent, AnimationComponent animation) {
        allDrawCommands.add(new DrawCommand(mesh, transformComponent, animation));
    }

    public static void submitSkyLight(SkyLight skyLight) {
        if (allSkyLights.contains(skyLight)) return;
        allSkyLights.add(skyLight);
    }

    public static void submitPointLight(PointLight pointLight) {
        if (allPointLights.contains(pointLight)) return;
        allPointLights.add(pointLight);
    }

    public static void submitDirectionalLight(DirectionalLight directionalLight) {
        if (allDirectionalLights.contains(directionalLight)) return;
        allDirectionalLights.add(directionalLight);
    }

    public static record Stats() {

        public static int drawCalls, totalVertexCount, totalIndicesCount, totalShapeCount;

        private static void reset() {
            drawCalls = 0;
            totalVertexCount = 0;
            totalIndicesCount = 0;
            totalShapeCount = 0;
        }

        public static String getStats() {
            if (cameraContext == null) return "FPS: %s, No Camera".formatted(FPS);
            String s = """
                    Frame Time: %.02f ms
                    FPS: %s
                    Draw Calls: %s
                    Total Shapes: %s
                    Total Vertices: %s
                    Total Indices: %s
                                        
                    Renderer: %s
                    OpenGL version: %s
                    Graphics Card Vendor: %s
                    OpenGL Context running on %s Thread
                    Zoom: %s
                    """.formatted(FRAME_TIME,
                    FPS,
                    drawCalls,
                    totalShapeCount,
                    totalVertexCount,
                    totalIndicesCount,
                    glGetString(GL_RENDERER),
                    glGetString(GL_VERSION),
                    glGetString(GL_VENDOR),
                    Thread.currentThread().getName(),
                    PerspectiveCamera.getZoom()
            );
            reset();
            return s;
        }

    }

    private static OpenGLMesh buildGrid(int gridSize) {

        final float[] vertexPos = new float[]{
                -0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
        };

        final int vertexSize = 32;

        int indicesPtr = 0;
        int indicesX = 0, indicesY = 0, indicesZ = 0;

        final int finalSize = gridSize * gridSize * 4;
        Matrix4f[] components = new Matrix4f[finalSize];

        int index = 0;
        for (int x = -gridSize; x < gridSize; x++) {
            for (int z = -gridSize; z < gridSize; z++) {
                components[index] = new Matrix4f()
                        .scale(2f)
                        .translate(x, 0, z);
                index++;
            }
        }

        float[] vertices = new float[finalSize * vertexSize];
        int[] indices = new int[6 * gridSize * components.length];

        index = 0;
        for (Matrix4f c : components) {
            for (int i = 0; i < vertexPos.length; ) {
                Vector4f transformed = new Vector4f(vertexPos[i++], vertexPos[i++], vertexPos[i++], 1.0f).mul(c);
                vertices[index++] = transformed.x;
                vertices[index++] = transformed.y;
                vertices[index++] = transformed.z;
            }
            int[] indicesTemplate = new int[]{
                    indicesX, 1 + indicesY, 3 + indicesZ,
                    3 + indicesX, indicesY, 2 + indicesZ,
            };
            for (int i : indicesTemplate) {
                indices[indicesPtr++] = i;
            }
            indicesX += 4;
            indicesY += 4;
            indicesZ += 4;
        }

        OpenGLMesh mesh = new OpenGLMesh(gridPipeline, MeshUsage.STATIC);
        mesh.pushVertices(vertices);
        mesh.pushIndices(indices);
        mesh.loadGL();
        return mesh;
    }
}
