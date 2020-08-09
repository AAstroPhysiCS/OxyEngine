package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngineEditor.Sandbox.OxyComponents.InternObjectMesh;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.Core.Line.OxyInfoLine;
import OxyEngine.Tools.Ref;
import OxyEngineEditor.Sandbox.OxyComponents.PerspectiveCamera;
import OxyEngineEditor.Sandbox.Sandbox3D;

import static org.lwjgl.opengl.GL11.*;

public abstract class OxyRenderer implements OxyInfoLine<String> {

    public static OxyCamera currentBoundedCamera;
    protected OxyShader shader;
    protected final WindowHandle windowHandle;

    protected static OxyRendererType type;

    public OxyRenderer(WindowHandle windowHandle) {
        this.windowHandle = windowHandle;
    }

    public void setShader(OxyShader shader) {
        this.shader = shader;
    }

    public abstract void render(float ts, Mesh mesh, OxyCamera camera);

    public abstract void render(float ts, Mesh mesh);

    /**
     * All global meshes should be here!
     */
    public interface MeshSystem {
        Ref<InternObjectMesh> sandBoxMesh = new Ref<>(null);
        Ref<InternObjectMesh> worldGridMesh = new Ref<>(null);
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
            if(currentBoundedCamera != null) {
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
                        Sandbox3D.FPS,
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
            return "No Camera";
        }
    }

    @Override
    public String info() {
        return Stats.getStats();
    }

    public OxyCamera getCamera() {
        return currentBoundedCamera;
    }

    public OxyShader getShader() {
        return shader;
    }
}
