package OxyEngine.Core.Context;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Context.Renderer.Mesh.*;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.FrameBufferSpecification;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.TextureFormat;
import OxyEngine.Core.Context.Renderer.Light.SkyLight;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderType;
import OxyEngine.Core.Context.Renderer.Texture.OxyColor;
import OxyEngine.Core.Context.Renderer.Texture.TextureSlot;
import OxyEngine.Core.Context.Scene.SceneRuntime;
import OxyEngine.Core.Layers.EditorLayer;
import OxyEngine.OxyEngine;
import OxyEngine.System.OxyFileSystem;
import OxyEngine.TargetPlatform;
import OxyEngineEditor.UI.Panels.ScenePanel;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static OxyEngine.Core.Context.OxyRenderCommand.targetPlatform;
import static OxyEngine.Core.Context.Renderer.Light.Light.LIGHT_SIZE;
import static OxyEngine.Core.Context.Scene.SceneRuntime.*;
import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
import static org.lwjgl.opengl.GL30.GL_RED_INTEGER;

public final class OxyRenderer {

    private static boolean INIT = false;

    private static OxyRenderCommand renderCommand;

    private static UniformBuffer environmentSettingsUniformBuffer;

    private static OpenGLMesh environmentMesh;

    static OxyPipeline geometryPipeline;
    static OxyRenderPass pickingRenderPass;
    static OpenGLFrameBuffer mainFrameBuffer, pickingFrameBuffer, shadowFrameBuffer;
    private static OxyPipeline gridPipeline;

    static OxyPipeline shadowMapPipeline;
    static final int NUMBER_CASCADES = 4;
    static final int FRUSTUM_CORNERS = 8;

    static final float[] cascadeSplit = new float[NUMBER_CASCADES];

    static {
        cascadeSplit[0] = 100f;
        cascadeSplit[1] = 300f;
        cascadeSplit[2] = 600f;
        cascadeSplit[3] = 10000f;
    }

    private static boolean enableGrid = true;
    private static OpenGLMesh gridMesh;

    private OxyRenderer() {
    }

    public static void init(TargetPlatform targetPlatform, boolean debug) {
        if (INIT) {
            logger.warning("Renderer already initiated!");
            return;
        }
        INIT = true;
        renderCommand = OxyRenderCommand.getInstance(targetPlatform);
        renderCommand.init(debug);

        if (debug) EditorLayer.getInstance().addPanel(DebugPanel.getInstance());

        float[] size = getIniViewportSize();
        ScenePanel.windowSize.set(size[0], size[1]);

        environmentSettingsUniformBuffer = UniformBuffer.create(3 * Float.BYTES, 1);

        OxyShader.createShader("OxyPBR", "src/main/resources/shaders/OxyPBR.glsl");
        OxyShader.createShader("OxyPreetham", "src/main/resources/shaders/OxyPreetham.glsl");
        OxyShader.createShader("OxyEquirectangularToCubemap", "src/main/resources/shaders/OxyEquirectangularToCubemap.glsl");
        OxyShader.createShader("OxySkybox", "src/main/resources/shaders/OxySkybox.glsl");
        OxyShader.createShader("OxyDepthMap", "src/main/resources/shaders/OxyDepthMap.glsl");
        OxyShader.createShader("OxyIBL", "src/main/resources/shaders/OxyIBL.glsl");
        OxyShader.createShader("OxyPrefiltering", "src/main/resources/shaders/OxyPrefiltering.glsl");
        OxyShader.createShader("OxyBDRF", "src/main/resources/shaders/OxyBDRF.glsl");
        OxyShader.createShader("OxyGrid", "src/main/resources/shaders/OxyGrid.glsl");

        initPipelines();
        initEnvMapMesh();
        initShaderConstants();
    }

    private static void initShaderConstants() {
        //TODO: ? perhaps, you could do the same with materials?
        OxyShader pbrShader = ShaderLibrary.get("OxyPBR");
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
        String content = OxyFileSystem.load(OxyFileSystem.getResourceByPath("/ini/imgui.ini"));

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

        OxyRenderPass gridRenderPass = OxyRenderPass.createBuilder(mainFrameBuffer)
                .renderingMode(MeshRenderMode.LINES)
                .setCullFace(CullMode.BACK)
                .create();

        gridPipeline = OxyPipeline.createNewPipeline(OxyPipeline.createNewSpecification()
                .setDebugName("Grid Pipeline")
                .setRenderPass(gridRenderPass)
                .createLayout(OxyPipeline.createNewPipelineLayout()
                        .targetBuffer(VertexBuffer.class)
                        .set(OxyShader.VERTICES, ShaderType.Float3)
                )
                .createLayout(OxyPipeline.createNewPipelineLayout()
                        .targetBuffer(IndexBuffer.class)
                )
                .setShader(ShaderLibrary.get("OxyGrid")));

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

        shadowFrameBuffer = FrameBuffer.create(512, 512, new OxyColor(1f, 0f, 0f, 1.0f),
                OpenGLFrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setAttachmentIndex(0)
                        .setTextureCount(NUMBER_CASCADES)
                        .setSizeForTextures(0, 2048, 2048)
                        .setSizeForTextures(1, 2048, 2048)
                        .setSizeForTextures(2, 1024, 1024)
                        .setSizeForTextures(3, 512, 512)
                        .setFormat(TextureFormat.DEPTHCOMPONENT32COMPONENT)
                        .setFilter(GL_NEAREST, GL_NEAREST)
                        .wrapSTR(GL_REPEAT, GL_REPEAT, -1)
                        .disableReadWriteBuffer(true)
        );

        OxyShader shadowMapDepthShader = ShaderLibrary.get("OxyDepthMap");
        shadowMapPipeline = OxyPipeline.createNewPipeline(OxyPipeline.createNewSpecification()
                .setRenderPass(OxyRenderPass.createBuilder(shadowFrameBuffer)
                        .setCullFace(CullMode.BACK)
                        .create())
                .setDebugName("Shadow Map Rendering Pipeline")
                .setShader(shadowMapDepthShader));
    }

    private static void initEnvMapMesh() {

        OxyRenderPass hdrRenderPass = OxyRenderPass.createBuilder(mainFrameBuffer)
                .renderingMode(MeshRenderMode.TRIANGLES)
                .setCullFace(CullMode.FRONT)
                .create();

        OxyShader equirectangularShader = ShaderLibrary.get("OxyEquirectangularToCubemap");
        OxyPipeline hdrPipeline = OxyPipeline.createNewPipeline(OxyPipeline.createNewSpecification()
                .setDebugName("HDR Pipeline")
                .setRenderPass(hdrRenderPass)
                .createLayout(OxyPipeline.createNewPipelineLayout()
                        .targetBuffer(VertexBuffer.class)
                        .set(OxyShader.VERTICES, ShaderType.Float3)
                )
                .createLayout(OxyPipeline.createNewPipelineLayout()
                        .targetBuffer(IndexBuffer.class)
                )
                .setShader(equirectangularShader));

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
        int[] skyBoxIndices = new int[skyboxVertices.length];
        for (int i = 0; i < skyBoxIndices.length; i++) {
            skyBoxIndices[i] = i;
        }
        environmentMesh.pushVertices(skyboxVertices);
        environmentMesh.pushIndices(skyBoxIndices);
    }

    public static int getEntityIDByMousePosition() {
        pickingFrameBuffer.bind();
        Vector2f mousePos = new Vector2f(
                ScenePanel.mousePos.x - ScenePanel.windowPos.x - ScenePanel.offset.x,
                ScenePanel.mousePos.y - ScenePanel.windowPos.y - ScenePanel.offset.y);
        mousePos.y = mainFrameBuffer.getHeight() - mousePos.y;
        glReadBuffer(GL_COLOR_ATTACHMENT1);
        int[] entityID = new int[1];
        glReadPixels((int) mousePos.x, (int) mousePos.y, 1, 1, GL_RED_INTEGER, GL_INT, entityID);
        pickingFrameBuffer.unbind();
        return entityID[0];
    }

    public static void renderMesh(OxyPipeline pipeline, OpenGLMesh mesh) {
        if (RendererAPI.onStackRenderPass == null) throw new IllegalStateException("RenderPass not bound!");
        pipeline.updatePipelineShader();
        pipeline.getShader().begin();
        if (mesh.empty())
            mesh.load();
        mesh.render();
        pipeline.getShader().end();
    }

    /*
     * Overriding the pipeline shader with a given shader
     */
    public static void renderMesh(OxyPipeline pipeline, OpenGLMesh mesh, OxyShader shader) {
        if (RendererAPI.onStackRenderPass == null) throw new IllegalStateException("RenderPass not bound!");
        pipeline.updatePipelineShader();
        shader.begin();
        if (mesh.empty())
            mesh.load();
        mesh.render();
        shader.end();
    }

    public static void renderSkyLight(OxyShader shader) {
        shader.begin();
        if (environmentMesh.empty())
            environmentMesh.load();
        environmentMesh.render();
        shader.end();
    }

    public static void clearBuffer() {
        renderCommand.getRendererAPI().clearBuffer();
    }

    public static void clearColor(OxyColor color) {
        renderCommand.getRendererAPI().clearColor(color);
    }

    public static void beginRenderPass(OxyRenderPass renderPass) {
        if (RendererAPI.onStackRenderPass != null)
            throw new IllegalStateException("RenderPass already on stack. Did you forget to call endRenderPass?");
        FrameBuffer frameBuffer = renderPass.getFrameBuffer();
        if (frameBuffer.needResize()) {
            frameBuffer.resize(frameBuffer.getWidth(), frameBuffer.getHeight());
            if (SceneRuntime.currentBoundedCamera instanceof PerspectiveCamera p) {
                p.setAspect((float) frameBuffer.getWidth() / frameBuffer.getHeight());
                p.update();
            }
        }
        renderCommand.getRendererAPI().beginRenderPass(renderPass);
    }

    public static void beginScene() {
        assert ACTIVE_SCENE != null : oxyAssert("Active scene is somehow null!");
        if (currentBoundedCamera == null) return;
        mainFrameBuffer.blit();

        environmentSettingsUniformBuffer.setData(0, ACTIVE_SCENE.gammaStrength);
        environmentSettingsUniformBuffer.setData(4, ACTIVE_SCENE.exposure);
        environmentSettingsUniformBuffer.setData(8, new float[]{1.0f});

        if (currentBoundedSkyLightEntity != null) {
            SkyLight skyLightComp = currentBoundedSkyLightEntity.get(SkyLight.class);
            if (skyLightComp != null) {
                environmentSettingsUniformBuffer.setData(8, skyLightComp.intensity);
            }
        }

        if (enableGrid) {
            if (gridMesh == null) gridMesh = buildGrid(10);
            OxyRenderPass gridPass = gridPipeline.getRenderPass();
            OxyRenderer.beginRenderPass(gridPass);
            OxyRenderer.renderMesh(gridPipeline, gridMesh);
            OxyRenderer.endRenderPass();
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
        TransformComponent[] components = new TransformComponent[finalSize];

        int index = 0;
        for (int x = -gridSize; x < gridSize; x++) {
            for (int z = -gridSize; z < gridSize; z++) {
                components[index] = new TransformComponent(new Vector3f(x, 0, z), 2f);
                components[index].transform = new Matrix4f()
                        .scale(components[index].scale)
                        .translate(components[index].position)
                        .rotateX(components[index].rotation.x)
                        .rotateY(components[index].rotation.y)
                        .rotateZ(components[index].rotation.z);
                index++;
            }
        }

        float[] vertices = new float[finalSize * vertexSize];
        int[] indices = new int[6 * gridSize * components.length];

        index = 0;
        for (TransformComponent component : components) {
            Matrix4f c = component.transform;
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
        return mesh;
    }

    public static void endScene() {
        assert ACTIVE_SCENE != null : oxyAssert("Active scene is somehow null!");

        mainFrameBuffer.resetFlush();
        pickingFrameBuffer.resetFlush();
        shadowFrameBuffer.resetFlush();

        OxyRenderer.Stats.totalShapeCount = ACTIVE_SCENE.getShapeCount();
    }

    public static void endRenderPass() {
        if (RendererAPI.onStackRenderPass == null)
            throw new IllegalStateException("RenderPass not on stack. Did you forget to call beginRenderPass?");
        renderCommand.getRendererAPI().endRenderPass();
    }

    public static void setEnableGrid(boolean enableGrid) {
        OxyRenderer.enableGrid = enableGrid;
    }

    public static void pollEvents() {
        renderCommand.getRendererContext().pollEvents();
    }

    public static void swapBuffers() {
        renderCommand.getRendererContext().swapBuffer(OxyEngine.getWindowHandle());
    }

    public static TargetPlatform getCurrentTargetPlatform() {
        return targetPlatform;
    }

    public static OpenGLFrameBuffer getMainFrameBuffer() {
        return mainFrameBuffer;
    }

    /*private static final record RenderQueue() {

        private static final List<RenderFunc> renderFuncs = new ArrayList<>();

        private static void submit(RenderFunc func) {
            renderFuncs.add(func);
        }

        private static void runQueue() {
            for (RenderFunc f : renderFuncs) {
                f.func();
            }
        }

        private static void flush() {
            renderFuncs.clear();
        }
    }

    public static synchronized void submit(RenderFunc func) {
        RenderQueue.submit(func);
    }

    public static synchronized void run() {
        RenderQueue.runQueue();
        RenderQueue.flush();
    }*/

    public static record Stats() {

        public static int drawCalls, totalVertexCount, totalIndicesCount, totalShapeCount;

        private static void reset() {
            drawCalls = 0;
            totalVertexCount = 0;
            totalIndicesCount = 0;
            totalShapeCount = 0;
        }

        public static String getStats() {
            if (currentBoundedCamera == null) return "FPS: %s, No Camera".formatted(FPS);
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
                    Current Position:
                        X: %s,
                        Y: %s,
                        Z: %s
                    Current Origin:
                        X: %s,
                        Y: %s,
                        Z: %s
                    Current Rotation:
                        X: %s,
                        Y: %s,
                        Z: %s
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
                    currentBoundedCamera.getPosition().x,
                    currentBoundedCamera.getPosition().y,
                    currentBoundedCamera.getPosition().z,
                    currentBoundedCamera.origin.x,
                    currentBoundedCamera.origin.y,
                    currentBoundedCamera.origin.z,
                    currentBoundedCamera.getRotation().x,
                    currentBoundedCamera.getRotation().y,
                    currentBoundedCamera.getRotation().z,
                    PerspectiveCamera.zoom
            );
            reset();
            return s;
        }
    }
}
