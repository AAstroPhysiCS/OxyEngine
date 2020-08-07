package OxyEngineEditor.Sandbox;

import OxyEngine.Core.Camera.PerspectiveCameraComponent;
import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.OxyRendererType;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Core.Renderer.Texture.OxyTextureCoords;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngine.OxyEngine;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.*;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.UI.Layers.*;
import OxyEngineEditor.UI.OxyUISystem;
import imgui.ImGui;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.List;

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
                .create();

        OxyRenderer3D oxyRenderer = (OxyRenderer3D) oxyEngine.getRenderer();
        oxyRenderer.setShader(oxyShader);

        scene = new Scene(windowHandle, oxyRenderer, new FrameBuffer(windowHandle.getWidth(), windowHandle.getHeight()));
        oxyEngine.initLayers(scene);

        OxyTexture texture = OxyTexture.load(OxySystem.FileSystem.getResourceByPath("/images/world.png"), OxyTextureCoords.CUBE.getTcs());

        camera = new PerspectiveCameraComponent(70, (float) windowHandle.getWidth() / windowHandle.getHeight(), 0.003f, 10000f, 4, true, new Vector3f(0, 0, 0), new Vector3f(5.6f, 2.3f, 0));

        OxyModel cube = scene.createModelEntity(ModelType.Cube).get(0);
        cube.addComponent(camera, texture, new TransformComponent(new Vector3f(-30, 0, 0)), new SelectedComponent(false));
        cube.updateData();

        /*for (int x = -10; x < 10; x++) {
            for (int y = -10; y < 10; y++) {
                for (int z = -10; z < 10; z++) {
                    OxyModel cube = scene.createModelEntity(ModelType.Cube).get(0);
                    cube.addComponent(camera, sandBoxMesh.obj, new CubeFactory(), texture, new TransformComponent(new Vector3f(x + 25, y, z)), new SelectedComponent(false));
                    cube.updateData();
                }
            }
        }*/

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

        testObjects = scene.createModelEntity(OxySystem.FileSystem.getResourceByPath("/models/scene2.obj"));

        //TEMP
        for(OxyModel obj : testObjects) {
            obj.addComponent(camera, new SelectedComponent(false));
            obj.updateData();
        }

        scene.build();
    }

    private void update(float ts, float deltaTime) {
        scene.update(ts, deltaTime);
    }

    static List<OxyModel> testObjects;

    private void render(float ts, float deltaTime) {
        OxyTexture.bindAllTextureSlots();

        scene.render(ts, deltaTime);

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

            double time = 0;
            long timeMillis = System.currentTimeMillis();
            int frames = 0;

            while (oxyEngine.getMainThread().isAlive() && !glfwWindowShouldClose(windowHandle.getPointer())) {
                if (OxyUISystem.OxyEventSystem.keyEventDispatcher.getKeys()[GLFW.GLFW_KEY_ESCAPE]) break;

                windowHandle.update();

                final float currentTime = (float) glfwGetTime();
                final float deltaTime = (time > 0) ? (float) (currentTime - time) : 1f / 60f;
                final float ts = (float) (currentTime - time);
                time = currentTime;
                update(ts, deltaTime);
                render(ts, deltaTime);

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
