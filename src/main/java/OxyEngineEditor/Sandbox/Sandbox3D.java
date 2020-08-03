package OxyEngineEditor.Sandbox;

import OxyEngine.Core.Camera.PerspectiveCameraComponent;
import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.OxyRendererType;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Core.Renderer.Texture.OxyTextureCoords;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngine.OxyEngine;
import OxyEngine.System.OxySystem;
import OxyEngine.System.OxyTimestep;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.*;
import OxyEngineEditor.UI.Layers.*;
import OxyEngineEditor.UI.OxyUISystem;
import imgui.ImGui;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static OxyEngine.Core.Renderer.OxyRenderer.MeshSystem.sandBoxMesh;
import static OxyEngine.System.OxySystem.logger;
import static OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh.*;
import static OxyEngineEditor.UI.OxyUISystem.OxyEventSystem.dispatcherThread;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;

public class Sandbox3D {

    private final WindowHandle windowHandle;

    private OxyShader oxyShader;
    private final OxyEngine oxyEngine;

    private Scene scene;

    public static PerspectiveCameraComponent camera;

    private static MainUILayer mainUILayer;

    public static int FPS = 0;

    public static void main(String[] args) {
        new Sandbox3D();
    }

    public Sandbox3D() {
        OxySystem.init();

        windowHandle = new WindowHandle("OxyEngine - Editor", 1366, 768, WindowHandle.WindowMode.WINDOWEDFULLSCREEN);
        oxyEngine = new OxyEngine(this::run, windowHandle, OxyEngine.Antialiasing.ON, false, OxyRendererType.Oxy3D);

        oxyEngine.start();
    }

    private void init() {
        oxyEngine.init();

        oxyShader = new OxyShader("shaders/world.glsl");

        sandBoxMesh.obj = new GameObjectMesh.GameObjectMeshBuilderImpl()
                .setUsage(BufferTemplate.Usage.DYNAMIC)
                .setMode(GL_TRIANGLES)
                .setVerticesBufferAttributes(attributesVert, attributesTXCoords, attributesTXSlot, attributesColors)
                .runOnFrameBuffer(windowHandle, true)
                .create();

        OxyRenderer3D oxyRenderer = (OxyRenderer3D) oxyEngine.getRenderer();
        oxyRenderer.setShader(oxyShader);

        scene = new Scene(windowHandle, oxyRenderer);
        oxyEngine.initLayers(scene);

        OxyTexture texture = OxyTexture.load(1, OxySystem.FileSystem.getResourceByPath("/images/world.png"), OxyTextureCoords.CUBE);

        //TODO: READ MTL FILE... PARSE MULTIPLE MESHES/ENTITIES, RENDER THEM SEPERATELY
        //TODO: CREATE A METHOD FOR MODELS THAT TAKES AN LIST OR ARRAY AND SUMS IN ONE MODELMESH
        camera = new PerspectiveCameraComponent(70, (float) windowHandle.getWidth() / windowHandle.getHeight(), 0.003f, 10000f, 4, true, new Vector3f(0, 0, 0), new Vector3f(5.6f, 2.3f, 0));

        for (int x = -10; x < 10; x++) {
            for (int y = -10; y < 10; y++) {
                for (int z = -10; z < 10; z++) {
                    OxyGameObject cube = scene.createGameObjectEntity();
                    cube.addComponent(camera, sandBoxMesh.obj, new CubeTemplate(), texture, new TransformComponent(new Vector3f(x, y, z)), new SelectedComponent(false));
                    cube.initData();
                }
            }
        }

        mainUILayer = OxyEngine.getMainUIComponent();
        mainUILayer.addUILayers(StatsLayer.getInstance(windowHandle, scene));
        mainUILayer.addUILayers(ToolbarLayer.getInstance(windowHandle, scene));
        mainUILayer.addUILayers(SceneConfigurationLayer.getInstance(windowHandle, scene));
        mainUILayer.addUILayers(SceneLayer.getInstance(windowHandle, scene));
        mainUILayer.preload();

        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;
        oxyShader.enable();
        oxyShader.setUniform1iv("tex", samplers);
        oxyShader.disable();

        testObjects = scene.createModelEntity(ModelFileType.OBJ, "src/main/resources/models/scene1.obj", "src/main/resources/models/scene1.mtl");
        testObjects.addComponent(camera,
                new OxyColor(1.0f, 0.0f, 0.0f, 1.0f),
                new TransformComponent(new Vector3f(-500, -1, 0), new Vector3f((float) Math.toRadians(180), 0, 0), 1),
                new SelectedComponent(false));
        testObjects.updateData();

        scene.build();
    }

    private void update(OxyTimestep ts) {
        scene.update(ts);
    }

    static OxyModel testObjects;

    private void render(OxyTimestep ts) {
        OxyTexture.bindAllTextureSlots();

        scene.render(ts);

        OpenGLRendererAPI.clearBuffer();
        OpenGLRendererAPI.clearColor(41, 41, 41, 1.0f);

        ImGui.newFrame();
        mainUILayer.renderLayer();
        ImGui.render();
        scene.getOxyUISystem().updateImGuiRenderer();

        OpenGLRendererAPI.swapBuffer(windowHandle);
        OpenGLRendererAPI.pollEvents();
        OxyTexture.unbindAllTextureSlots();
    }

    private Runnable run() {
        return () -> {

            init();

            OxyTimestep ts = new OxyTimestep(0);

            double time = 0;
            long timeMillis = System.currentTimeMillis();
            int frames = 0;

            while (oxyEngine.getMainThread().isAlive() && !glfwWindowShouldClose(windowHandle.getPointer())) {
                if (OxyUISystem.OxyEventSystem.keyEventDispatcher.getKeys()[GLFW.GLFW_KEY_ESCAPE]) break;

                windowHandle.update();

                final double currentTime = glfwGetTime();
                final double deltaTime = (time > 0) ? (currentTime - time) : 1f / 60f;
                ts.setDeltaTime(deltaTime);
                ts.setTimestep((float) (currentTime - time));
                time = currentTime;
                update(ts);
                render(ts);

                frames++;

                if (System.currentTimeMillis() - timeMillis > 1000) {
                    timeMillis += 1000;
                    FPS = frames;
                    frames = 0;
                }

                int error = glGetError();
                if (error != GL_NO_ERROR) logger.severe("OpenGL error: " + error);
            }
            dispose();
        };
    }

    public void dispose() {
        oxyEngine.dispose();
        oxyShader.dispose();
        scene.dispose();
        sandBoxMesh.obj.dispose();
        dispatcherThread.joinThread();
    }
}
