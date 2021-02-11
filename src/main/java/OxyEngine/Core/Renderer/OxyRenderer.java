package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OxyApplication;

import static OxyEngine.Scene.SceneRuntime.currentBoundedCamera;
import static org.lwjgl.opengl.GL11.*;

public abstract class OxyRenderer {

    protected final WindowHandle windowHandle;

    protected static OxyRendererType type;

    public OxyRenderer(WindowHandle windowHandle) {
        this.windowHandle = windowHandle;
    }

    public abstract void render(float ts, OpenGLMesh mesh, OxyCamera camera, OxyShader shader);

    public abstract void renderWithCurrentBoundedCamera(float ts, OpenGLMesh mesh, OxyShader shader);

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
                    FPS: %s
                    Draw Calls: %s
                    Total Shapes: %s
                    Total Vertices: %s
                    Total Indices: %s
                                        
                    Renderer: %s %s
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
                    """.formatted(
                    OxyApplication.FPS,
                    drawCalls,
                    totalShapeCount,
                    totalVertexCount,
                    totalIndicesCount,
                    type == OxyRendererType.Oxy3D ? "3D" : "2D",
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
