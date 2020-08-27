package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.OxyApplication;
import OxyEngineEditor.Components.PerspectiveCamera;

import static org.lwjgl.opengl.GL11.*;

public abstract class OxyRenderer {

    public static OxyCamera currentBoundedCamera;
    protected final WindowHandle windowHandle;
    protected static OxyShader currentShader;

    protected static OxyRendererType type;

    public OxyRenderer(WindowHandle windowHandle) {
        this.windowHandle = windowHandle;
    }

    public abstract void render(float ts, Mesh mesh, OxyCamera camera);

    public abstract void render(float ts, Mesh mesh);

    public static record Stats() {

        public static int drawCalls, totalVertexCount, totalIndicesCount, totalShapeCount;

        private static void reset() {
            drawCalls = 0;
            totalVertexCount = 0;
            totalIndicesCount = 0;
            totalShapeCount = 0;
        }

        public static String getStats() {
            if (currentBoundedCamera == null) return "No Camera";
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
                    currentBoundedCamera.getCameraController().origin.x,
                    currentBoundedCamera.getCameraController().origin.y,
                    currentBoundedCamera.getCameraController().origin.z,
                    currentBoundedCamera.getCameraController().getRotation().x,
                    currentBoundedCamera.getCameraController().getRotation().y,
                    currentBoundedCamera.getCameraController().getRotation().z,
                    PerspectiveCamera.zoom
            );
            reset();
            return s;
        }
    }

    public OxyCamera getCamera() {
        return currentBoundedCamera;
    }

    public static OxyShader getCurrentShader() {
        return currentShader;
    }
}
