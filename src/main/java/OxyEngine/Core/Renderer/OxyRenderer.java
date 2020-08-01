package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.PerspectiveCameraComponent;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Sandbox.OxyComponents.ModelMesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.Core.Line.OxyInfoLine;
import OxyEngine.Tools.Ref;

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

    public abstract void render(Mesh mesh, OxyCamera camera);

    public abstract void render(Mesh mesh);

    /**
     * All global meshes should be here!
     */
    public interface MeshSystem {
        Ref<GameObjectMesh> sandBoxMesh = new Ref<>(null);
        Ref<GameObjectMesh> worldGridMesh = new Ref<>(null);
        Ref<ModelMesh> modelMesh = new Ref<>(null);
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
            String s = """
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
                    PerspectiveCameraComponent.zoom
            );
            reset();
            return s;
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
