package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Renderer.Pipeline.OxyShader;
import OxyEngine.OxyApplication;
import OxyEngine.OxyEngine;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static OxyEngine.Scene.SceneRuntime.currentBoundedCamera;
import static OxyEngine.System.OxyEventSystem.keyEventDispatcher;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;

public final class OxyRenderer {

    private static final Thread renderThread = new Thread(run(), "Oxy Renderer - OpenGL Thread");
    static {
        renderThread.start();
    }

    private OxyRenderer(){}

    public static void renderMesh(OxyPipeline pipeline, OpenGLMesh mesh, OxyCamera camera){
        pipeline.updatePipelineShader();
        pipeline.getShader().begin();
        pipeline.setCameraUniforms(camera);
        if (mesh.empty())
            mesh.load(pipeline);
        mesh.render();
        pipeline.getShader().end();
    }

    public static void renderMesh(OxyPipeline pipeline, OpenGLMesh mesh, OxyCamera camera, OxyShader shader){
        pipeline.updatePipelineShader();
        shader.begin();
        pipeline.setCameraUniforms(shader, camera);
        if (mesh.empty())
            mesh.load(pipeline);
        mesh.render();
        shader.end();
    }

    public static void renderMesh(OxyPipeline pipeline, OpenGLMesh mesh){
        pipeline.updatePipelineShader();
        pipeline.getShader().begin();
        pipeline.setCameraUniforms(currentBoundedCamera);
        if (mesh.empty())
            mesh.load(pipeline);
        mesh.render();
        pipeline.getShader().end();
    }

    public static Runnable run(){
        return () -> {
            while(!glfwWindowShouldClose(OxyEngine.getWindowHandle().getPointer())){
                if (keyEventDispatcher.getKeys()[GLFW.GLFW_KEY_ESCAPE]) break;

                RenderQueue.runQueue();
            }
        };
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
            if (currentBoundedCamera == null) return "FPS: %s, No Camera".formatted(OxyApplication.FPS);
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
                    """.formatted(OxyApplication.FRAME_TIME,
                    OxyApplication.FPS,
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
