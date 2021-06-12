package OxyEngine.Core.Context;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Context.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Context.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Texture.OxyColor;
import OxyEngine.OxyEngine;
import OxyEngine.Scene.SceneRuntime;
import OxyEngine.TargetPlatform;

import static OxyEngine.Core.Context.OxyRenderCommand.targetPlatform;
import static OxyEngine.Scene.SceneRuntime.*;
import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.opengl.GL11.*;

public final class OxyRenderer {

    private static boolean INIT = false;

    private static OxyRenderCommand renderCommand;

    private OxyRenderer() {
    }

    public static void renderMesh(OxyPipeline pipeline, OpenGLMesh mesh, OxyCamera camera) {
        if(RendererAPI.onStackRenderPass == null) throw new IllegalStateException("RenderPass not bound!");
        pipeline.updatePipelineShader();
        pipeline.getShader().begin();
        pipeline.setCameraUniforms(camera);
        if (mesh.empty())
            mesh.load(pipeline);
        mesh.render();
        pipeline.getShader().end();
    }

    public static void renderMesh(OxyPipeline pipeline, OpenGLMesh mesh, OxyCamera camera, OxyShader shader) {
        if(RendererAPI.onStackRenderPass == null) throw new IllegalStateException("RenderPass not bound!");
        pipeline.updatePipelineShader();
        shader.begin();
        pipeline.setCameraUniforms(shader, camera);
        if (mesh.empty())
            mesh.load(pipeline);
        mesh.render();
        shader.end();
    }

    public static void renderMesh(OxyPipeline pipeline, OpenGLMesh mesh) {
        if(RendererAPI.onStackRenderPass == null) throw new IllegalStateException("RenderPass not bound!");
        pipeline.updatePipelineShader();
        pipeline.getShader().begin();
        pipeline.setCameraUniforms(currentBoundedCamera);
        if (mesh.empty())
            mesh.load(pipeline);
        mesh.render();
        pipeline.getShader().end();
    }

    public static void init(TargetPlatform targetPlatform, boolean debug) {
        if (INIT) {
            logger.warning("Renderer already initiated!");
            return;
        }
        INIT = true;
        renderCommand = OxyRenderCommand.getInstance(targetPlatform);
        renderCommand.init(debug);
    }

    public static void clearBuffer() {
        renderCommand.getRendererAPI().clearBuffer();
    }

    public static void clearColor(OxyColor color) {
        renderCommand.getRendererAPI().clearColor(color);
    }

    public static void beginRenderPass(OxyRenderPass renderPass) {
        if (RendererAPI.onStackRenderPass != null) throw new IllegalStateException("RenderPass already on stack. Did you forget to call endRenderPass?");
        FrameBuffer frameBuffer = renderPass.getFrameBuffer();
        if (frameBuffer.needResize()) {
            frameBuffer.resize(frameBuffer.getWidth(), frameBuffer.getHeight());
            if (SceneRuntime.currentBoundedCamera instanceof PerspectiveCamera p)
                p.setAspect((float) frameBuffer.getWidth() / frameBuffer.getHeight());
        }
        renderCommand.getRendererAPI().beginRenderPass(renderPass);
    }

    public static void endRenderPass() {
        if (RendererAPI.onStackRenderPass == null) throw new IllegalStateException("RenderPass not on stack. Did you forget to call beginRenderPass?");
        renderCommand.getRendererAPI().endRenderPass();
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
