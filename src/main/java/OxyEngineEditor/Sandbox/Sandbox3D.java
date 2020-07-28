package OxyEngineEditor.Sandbox;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.ScenePerspectiveCamera;
import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
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
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.OxyObjects.CubeTemplate;
import OxyEngineEditor.Sandbox.OxyObjects.GameObjectType;
import OxyEngineEditor.Sandbox.OxyObjects.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Scene;
import OxyEngineEditor.UI.Layers.*;
import OxyEngineEditor.UI.OxyUISystem;
import imgui.ImGui;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
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
    private OxyUISystem oxyUISystem;
    private final OxyEngine oxyEngine;

    private Scene scene;

    private OxyCamera camera;

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
                .setVerticesBufferAttributes(attributesVert, attributesTXCoords, attributesTXSlots)
                .setGameObjectType(GameObjectType.Cube)
                .runOnFrameBuffer(windowHandle) //optional (single use)
                .create();

        OxyRenderer3D oxyRenderer = (OxyRenderer3D) oxyEngine.getRenderer();
        oxyRenderer.setShader(oxyShader);

        scene = new Scene(oxyRenderer);
        oxyEngine.initLayers(scene);

        final List<OxyEntity> fuck = new ArrayList<>(8000);
        OxyTexture texture = new OxyTexture(1, OxySystem.FileSystem.getResourceByPath("/images/world.png"), OxyTextureCoords.CUBE);

        //TODO: CUBE TAKES TOO MUCH VERTICES... ABOUT 144... REDUCE IT TO SOMETHING => TEXTURE..., COLOR..., BUT NOT BOTH AT THE SAME TIME!!!!!, PROBABLY THAT'S THE CAUSE OF BOTTLENECK
        //TODO: PROBABLY USE A TEXTURE BUFFER INSTEAD OF SHOVING ALL OF THEM IN TO THE VERTEXBUFFER

        for (int x = -10; x < 10; x++) {
            for (int y = -10; y < 10; y++) {
                for (int z = -10; z < 10; z++) {
                    OxyEntity e = scene.createEntity(new CubeTemplate());
                    e.addComponent(texture, new TransformComponent(new Vector3f(x, y, z), new Vector3f((float) Math.toRadians(0), 0, 0)));
                    e.initData(sandBoxMesh.obj);
                    fuck.add(e);
                }
            }
        }
        sandBoxMesh.obj.add(fuck);

        camera = new ScenePerspectiveCamera(70, (float) windowHandle.getWidth() / windowHandle.getHeight(), 0.003f, 10000f, 4, true, new Vector3f(0, 0, 0), new Vector3f(5.6f, 2.3f, 0));

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

        oxyUISystem = new OxyUISystem(scene, windowHandle);
    }

    private void update(float deltaTime) {
        oxyUISystem.updateImGuiContext(deltaTime);
    }

    private void render() {
        OxyTexture.bindAllTextureSlots();

        /*c.position.add(0.01f, 0f, 0f);
        testEntity.updateData();
        sandBoxMesh.obj.updateSingleEntityData(scene, testEntity);*/

        sandBoxMesh.obj.getFrameBuffer().bind();
        OpenGLRendererAPI.clearBuffer();
        scene.render(sandBoxMesh.obj, camera);
        oxyUISystem.render(scene.getEntities(), camera);
        sandBoxMesh.obj.getFrameBuffer().unbind();
        OpenGLRendererAPI.clearBuffer();
        OpenGLRendererAPI.clearColor(41, 41, 41, 1.0f);

        ImGui.newFrame();
        mainUILayer.renderLayer();
        ImGui.render();
        oxyUISystem.updateImGuiRenderer();

        OpenGLRendererAPI.swapBuffer(windowHandle);
        OpenGLRendererAPI.pollEvents();
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

                final double currentTime = glfwGetTime();
                final double deltaTime = (time > 0) ? (currentTime - time) : 1f / 60f;
                time = currentTime;
                update((float) deltaTime);
                render();

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
        sandBoxMesh.obj.dispose();
        oxyUISystem.dispose();
        dispatcherThread.joinThread();
    }
}
