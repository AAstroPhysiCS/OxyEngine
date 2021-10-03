package OxyEngine.Core.Renderer;

import OxyEngine.Components.AnimationComponent;
import OxyEngine.Components.EntityComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Camera.Camera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Layers.ImGuiLayer;
import OxyEngine.Core.Renderer.Light.*;
import OxyEngine.Core.Renderer.Mesh.*;
import OxyEngine.Core.Renderer.Mesh.Platform.FrameBufferSpecification;
import OxyEngine.Core.Renderer.Mesh.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Mesh.Platform.TextureFormat;
import OxyEngine.Core.Renderer.Texture.Color;
import OxyEngine.Core.Renderer.Texture.EnvironmentTexture;
import OxyEngine.Core.Renderer.Texture.TextureSlot;
import OxyEngine.Core.Scene.Material;
import OxyEngine.Core.Scene.SceneRuntime;
import OxyEngine.Core.Scene.SceneState;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.MouseCode;
import OxyEngine.OxyEngine;
import OxyEngine.System.FileSystem;
import OxyEngine.TargetPlatform;
import OxyEngineEditor.UI.Panels.ScenePanel;
import OxyEngineEditor.UI.SelectHandler;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static OxyEngine.Core.Renderer.RenderCommand.targetPlatform;
import static OxyEngine.Core.Renderer.RendererLightBufferSpecification.MAX_SUPPORTED_LIGHTS;
import static OxyEngine.Core.Renderer.RendererLightBufferSpecification.workGroupsX;
import static OxyEngine.Core.Scene.SceneRuntime.*;
import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
import static org.lwjgl.opengl.GL30.GL_RED_INTEGER;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

public final class Renderer {

    private static boolean INIT = false;

    private static final List<DrawCommand> meshDrawCommands = new ArrayList<>();
    private static final List<DrawCommand> colliderMeshDrawCommands = new ArrayList<>();
    private static final List<PointLight> pointLightCommand = new ArrayList<>();
    private static final List<DirectionalLight> directionalLightCommand = new ArrayList<>();
    private static final List<SkyLight> skyLightsCommand = new ArrayList<>();

    private static final List<RenderCommandQueue> renderCommandQueues = new CopyOnWriteArrayList<>();

    private static RendererLightBufferSpecification rendererLightBufferSpecification;

    private static UniformBuffer environmentSettingsUniformBuffer;
    private static OpenGLMesh environmentMesh;

    private static Pipeline geometryPipeline, hdrPipeline, gridPipeline, shadowDepthPipeline, colliderPipeline;
    private static FrameBuffer pickingFrameBuffer;

    private static Material gridMaterial;

    private static RenderPass pickingRenderPass;
    static RenderPass lineRenderPass, depthPrePassRenderPass;

    private static OpenGLMesh gridMesh;
    private static boolean enableGrid = false;
    public static boolean showBoundingBoxes;

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

    public static boolean showPixelComplexity;

    public static float FPS = 0;
    public static float FRAME_TIME = 0;
    public static float TS = 0;

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
        Shader.createShader("OxyDepthPrePass", "src/main/resources/shaders/OxyDepthPrePass.glsl");
        Shader.createCompute("OxyLightCulling", "src/main/resources/shaders/compute/OxyLightCulling.oxycomp");

        rendererLightBufferSpecification = new RendererLightBufferSpecification();

        initPipelines();
        initEnvMapMesh();

        cascadedCamArr = new ShadowMapCamera[NUMBER_CASCADES];
        for (int i = 0; i < cascadedCamArr.length; i++) cascadedCamArr[i] = new ShadowMapCamera();

        RenderCommand.bindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, rendererLightBufferSpecification.getLightBuffer().getBufferId());
        RenderCommand.bindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, rendererLightBufferSpecification.getVisibleLightIndicesBuffer().getBufferId());
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
        OpenGLFrameBuffer mainFrameBuffer = FrameBuffer.create(width, height, new Color(0f, 0f, 0f, 1f),
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

        FrameBuffer depthPrePassFrameBuffer = FrameBuffer.create(width, height, new FrameBufferSpecification()
                .setAttachmentIndex(0)
                .setTextureCount(1)
                .setTexelDataType(GL_FLOAT)
                .setFilter(GL_NEAREST, GL_NEAREST)
                .setFormat(TextureFormat.DEPTHCOMPONENT)
                .wrapSTR(GL_CLAMP_TO_BORDER, GL_CLAMP_TO_BORDER, -1)
                .disableReadWriteBuffer(true)
        );

        depthPrePassRenderPass = RenderPass.createBuilder(depthPrePassFrameBuffer).create();

        RenderPass colliderRenderPass = RenderPass.createBuilder(mainFrameBuffer)
                .renderingMode(RenderMode.TRIANGLES)
//                .polygonMode(PolygonMode.LINE)
                .setCullFace(CullMode.DISABLED)
                .create();

        colliderPipeline = Pipeline.createNewPipeline(Pipeline.createNewSpecification()
                .setDebugName("Collider Pipeline")
                .setRenderPass(colliderRenderPass)
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
        if (pickingFrameBuffer instanceof OpenGLFrameBuffer p) p.drawBuffers(0, 1);
        pickingRenderPass = RenderPass.createBuilder(pickingFrameBuffer).create();

        FrameBuffer shadowDepthFrameBuffer = FrameBuffer.create(new Color(1f, 0f, 0f, 1.0f),
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

        shadowDepthPipeline = Pipeline.createNewPipeline(Pipeline.createNewSpecification()
                .setRenderPass(RenderPass.createBuilder(shadowDepthFrameBuffer)
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

        RenderPass hdrRenderPass = RenderPass.createBuilder(geometryPipeline.getRenderPass().getFrameBuffer())
                .renderingMode(RenderMode.TRIANGLES)
                .setCullFace(CullMode.FRONT)
                .create();

        hdrPipeline = Pipeline.createNewPipeline(Pipeline.createNewSpecification()
                .setDebugName("HDR Pipeline")
                .setRenderPass(hdrRenderPass)
                .setVertexBufferLayout(Pipeline.createNewVertexBufferLayout()
                        .addShaderType(Shader.VERTICES, ShaderType.Float3)
                ));

        environmentMesh = new OpenGLMesh(hdrPipeline, BufferUsage.STATIC);

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
        int[] entityID = new int[1];
        pickingFrameBuffer.bind();
        Vector2f mousePos = new Vector2f(
                ScenePanel.mousePos.x - ScenePanel.windowPos.x - ScenePanel.offset.x,
                ScenePanel.mousePos.y - ScenePanel.windowPos.y - ScenePanel.offset.y);

        mousePos.y = ScenePanel.windowSize.y - mousePos.y;
        RenderCommand.readBuffer(GL_COLOR_ATTACHMENT1);
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

        geometryPipeline.getRenderPass().getFrameBuffer().blit();

        environmentSettingsUniformBuffer.setData(0, sceneContext.gammaStrength);
        environmentSettingsUniformBuffer.setData(4, sceneContext.exposure);
        environmentSettingsUniformBuffer.setData(8, new float[]{1.0f});

        if (skyLightEntityContext != null && sceneContext.isValid(skyLightEntityContext)) {
            SkyLight skyLightComp = skyLightEntityContext.get(SkyLight.class);
            if (skyLightComp != null) {
                environmentSettingsUniformBuffer.setData(8, skyLightComp.intensity);
            }
        }

        //RESET ALL THE LIGHT STATES
        Shader pbrShader = Renderer.getShader("OxyPBR");
        pbrShader.begin();
        pbrShader.setUniform1i("d_Light.activeState", 0);
        pbrShader.end();

        //Update SkyLight
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

        Renderer2D.beginScene();
    }

    public static void endScene() {
        assert sceneContext != null : oxyAssert("Active scene is somehow null!");

        for (DirectionalLight light : directionalLightCommand) {
            var entity = sceneContext.findEntityByComponent(light);
            if (entity != null) light.update(entity);
        }

        for (PointLight light : pointLightCommand) {
            var entity = sceneContext.findEntityByComponent(light);
            if (entity != null) light.update(entity);
        }

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

        Shader pbrShader = Renderer.getShader("OxyPBR");
        pbrShader.begin();
        pbrShader.setUniform1i("numberOfTilesX", workGroupsX);
        pbrShader.setUniform1i("totalLightCount", pointLightCommand.size());
        pbrShader.setUniform1i("showPixelComplexity", Renderer.showPixelComplexity ? 1 : 0);
        pbrShader.end();

        depthPrePass();
        rendererLightBufferSpecification.update();
        rendererLightBufferSpecification.dispatch();

        geometryPass();
        shadowPass();
        if (Input.isMouseButtonPressed(MouseCode.GLFW_MOUSE_BUTTON_1) && ScenePanel.hoveredWindow
                && sceneContext.getState() == SceneState.STOP && !SelectHandler.isOverAnyGizmo()) {
            idPass();
            SelectHandler.startPicking();
        }

        Renderer2D.endScene();
        renderCommandQueues.clear();

        FrameBuffer mainFrameBuffer = geometryPipeline.getRenderPass().getFrameBuffer();

        mainFrameBuffer.resetFlush();
        pickingFrameBuffer.resetFlush();
        shadowDepthPipeline.getRenderPass().getFrameBuffer().resetFlush();
        depthPrePassRenderPass.getFrameBuffer().resetFlush();

        if (ScenePanel.availContentRegionSize.x != mainFrameBuffer.getWidth() || ScenePanel.availContentRegionSize.y != mainFrameBuffer.getHeight()) {
            mainFrameBuffer.setNeedResize(true, (int) ScenePanel.availContentRegionSize.x, (int) ScenePanel.availContentRegionSize.y);
            pickingFrameBuffer.setNeedResize(true, (int) ScenePanel.availContentRegionSize.x, (int) ScenePanel.availContentRegionSize.y);
        }

        Stats.totalShapeCount = sceneContext.getShapeCount();
    }

    private static void depthPrePass() {

        RenderPass renderPass = depthPrePassRenderPass;
        Shader depthShader = Renderer.getShader("OxyDepthPrePass");

        depthShader.begin();
        Renderer.beginRenderPass(renderPass);

        for (DrawCommand drawCommand : meshDrawCommands) {
            OpenGLMesh mesh = drawCommand.mesh();
            TransformComponent transform = drawCommand.transform();

            if (mesh.getRenderMode() == RenderMode.NONE) continue;

            if (mesh.empty())
                mesh.loadGL();

            mesh.bind();
            for (OpenGLMesh.Submesh submesh : mesh.getSubmeshes()) {
                depthShader.setUniformMatrix4fv("model", new Matrix4f(transform.transform).mul(submesh.t().transform));
                RenderCommand.drawElementsIndexed(renderPass.getRenderMode().getModeID(), submesh.indexCount(),
                        GL_UNSIGNED_INT, submesh.baseIndex(), submesh.baseVertex());
            }
            mesh.unbind();
        }
        depthShader.end();
        Renderer.endRenderPass();
    }

    private static void geometryPass() {

        for (SkyLight skyLight : skyLightsCommand) {
            if (skyLight.isPrimary()) {
                skyLightEntityContext = sceneContext.findEntityByComponent(skyLight);
                skyLight.bind();

                Shader skyBoxShader = Renderer.getShader("OxySkybox");
                FrameBuffer mainFrameBuffer = geometryPipeline.getRenderPass().getFrameBuffer();

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

        for (DrawCommand drawCommand : meshDrawCommands) {
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
            if (mesh.empty())
                mesh.loadGL();
        }
    }

    private static void shadowPass() {

        //Prepare the camera
        if (directionalLightCommand.size() == 0) return;

        DirectionalLight d = directionalLightCommand.get(0);

        if (d.getDirection() == null || !d.isCastingShadows()) return;

        RenderPass depthRenderPass = shadowDepthPipeline.getRenderPass();
        Shader depthShader = Renderer.getShader("OxyDepthMap");
        FrameBuffer shadowDepthFrameBuffer = depthRenderPass.getFrameBuffer();

        Renderer.beginRenderPass(depthRenderPass);

        for (int i = 0; i < cascadedCamArr.length; i++) {
            cascadedCamArr[i].setDirectionalLight(d);
            cascadedCamArr[i].prepare((PerspectiveCamera) cameraContext, cascadeSplit[i]);
            if (shadowDepthFrameBuffer instanceof OpenGLFrameBuffer p)
                p.flushDepthAttachment(0, i);
        }

        for (DrawCommand drawCommand : meshDrawCommands) {

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

            for (int i = 0; i < cascadedCamArr.length; i++) {
                ShadowMapCamera camera = cascadedCamArr[i];

                shadowDepthFrameBuffer.bindDepthAttachment(0, i);
                depthShader.setUniformMatrix4fv("lightSpaceMatrix", camera.getViewMatrix());
                mesh.bind();
                for (OpenGLMesh.Submesh submesh : mesh.getSubmeshes()) {
                    depthShader.setUniformMatrix4fv("model", new Matrix4f(t.transform).mul(submesh.t().transform));
                    RenderCommand.drawElementsIndexed(depthRenderPass.getRenderMode().getModeID(), submesh.indexCount(),
                            GL_UNSIGNED_INT, submesh.baseIndex(), submesh.baseVertex());
                }
                mesh.unbind();
            }
            depthShader.end();
        }
        Renderer.endRenderPass();
    }

    private static void idPass() {

        if (pickingFrameBuffer instanceof OpenGLFrameBuffer p) {

            int[] clearValue = {-1};

            Renderer.beginRenderPass(pickingRenderPass);
            RenderCommand.clearTexImage(Objects.requireNonNull(p.getColorAttachmentTexture(1))[0], 0, p.getTextureFormat(1).getStorageFormat(), GL_INT, clearValue);

            Shader pbrShader = Renderer.getShader("OxyPBR");

            for (DrawCommand drawCommand : meshDrawCommands) {

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
    }

    public static void endRenderPass() {
        if (RendererAPI.onStackRenderPass == null)
            throw new IllegalStateException("RenderPass not on stack. Did you forget to call beginRenderPass?");
        RenderCommand.endRenderPass();
    }

    public static Pipeline getGeometryPipeline() {
        return geometryPipeline;
    }

    public static Pipeline getColliderPipeline() {
        return colliderPipeline;
    }

    static List<PointLight> getPointLightCommand() {
        return pointLightCommand;
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
        return Objects.requireNonNull(((OpenGLFrameBuffer) shadowDepthPipeline.getRenderPass().getFrameBuffer()).getColorAttachmentTexture(0))[index];
    }

    static Matrix4f getShadowViewMatrix(int index) {
        return cascadedCamArr[index].getViewMatrix();
    }

    static float getCascadeSplits(int index) {
        return cascadeSplit[index];
    }

    public static void enableGrid(boolean enableGrid) {
        Renderer.enableGrid = enableGrid;
    }

    public static void swapBuffers() {
        RenderCommand.swapBuffer(OxyEngine.getWindowHandle());
    }

    public static TargetPlatform getCurrentTargetPlatform() {
        return targetPlatform;
    }

    public static FrameBuffer getMainFrameBuffer() {
        return geometryPipeline.getRenderPass().getFrameBuffer();
    }

    public static List<DrawCommand> getMeshDrawCommands() {
        return meshDrawCommands;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public static boolean removeFromCommand(EntityComponent... components) {
        boolean successRemove = false;
        outer:
        for (EntityComponent o : components) {
            successRemove |= skyLightsCommand.remove(o);
            successRemove |= pointLightCommand.remove(o);
            successRemove |= directionalLightCommand.remove(o);
            if (!successRemove) { // still not removed
                for (DrawCommand drawCommand : meshDrawCommands) {
                    var mesh = drawCommand.mesh();
                    var anim = drawCommand.animation();
                    if ((mesh != null) && mesh.equals(o)) {
                        successRemove = meshDrawCommands.remove(drawCommand);
                        break outer;
                    } else if ((anim != null) && anim.equals(o)) {
                        successRemove = meshDrawCommands.remove(drawCommand);
                        break outer;
                    }
                }
            }
        }
        return successRemove;
    }

    public static void flushList() {
        pointLightCommand.clear();
        directionalLightCommand.clear();
        skyLightsCommand.clear();
        meshDrawCommands.clear();
    }

    public static void submit(RenderCommandQueue queue) {
        renderCommandQueues.add(queue);
    }

    public static void submitMesh(OpenGLMesh mesh, TransformComponent transformComponent, AnimationComponent animation) {
        meshDrawCommands.add(new DrawCommand(mesh, transformComponent, animation));
    }

    public static void submitColliderMesh(OpenGLMesh colliderMesh, TransformComponent transformComponent, AnimationComponent animation) {
        colliderMeshDrawCommands.add(new DrawCommand(colliderMesh, transformComponent, animation));
    }

    public static void submitSkyLight(SkyLight skyLight) {
        if (skyLightsCommand.contains(skyLight)) return;
        skyLightsCommand.add(skyLight);
    }

    public static void submitPointLight(PointLight pointLight) {
        if (pointLightCommand.contains(pointLight)) return;
        pointLightCommand.add(pointLight);
    }

    public static void submitDirectionalLight(DirectionalLight directionalLight) {
        if (directionalLightCommand.contains(directionalLight)) return;
        directionalLightCommand.add(directionalLight);
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

        OpenGLMesh mesh = new OpenGLMesh(gridPipeline, BufferUsage.STATIC);
        mesh.pushVertices(vertices);
        mesh.pushIndices(indices);
        mesh.loadGL();
        return mesh;
    }
}
