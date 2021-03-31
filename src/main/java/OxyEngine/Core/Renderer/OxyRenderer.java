package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.OxyApplication;

import static OxyEngine.Scene.SceneRuntime.currentBoundedCamera;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.*;

public class OxyRenderer {

    private OxyRenderer(){}

    public static void render(OpenGLMesh mesh, OxyCamera camera, OxyShader shader) {
        assert shader != null : oxyAssert("Shader is not instantiated.");
        shader.enable();
        shader.setUniformVec3("cameraPos", camera.origin);
        shader.setCamera(camera);
        if (mesh.empty())
            mesh.load();
        mesh.render();
        shader.disable();
    }

    public static void render(OpenGLMesh mesh, OxyShader shader){
        assert shader != null : oxyAssert("Shader is not instantiated.");
        shader.enable();
        if (mesh.empty())
            mesh.load();
        mesh.render();
        shader.disable();
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
