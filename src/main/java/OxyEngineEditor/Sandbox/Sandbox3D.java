package OxyEngineEditor.Sandbox;

import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.OxyRendererType;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngine.OxyEngine;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Sandbox.OxyComponents.PerspectiveCamera;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Scene;
import OxyEngineEditor.UI.Layers.*;
import OxyEngineEditor.UI.OxyUISystem;
import imgui.ImGui;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngineEditor.UI.OxyUISystem.OxyEventSystem.dispatcherThread;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

public class Sandbox3D {

    private final WindowHandle windowHandle;

    private final OxyEngine oxyEngine;

    private Scene scene;

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

        OxyShader oxyShader = new OxyShader("shaders/world.glsl");
        OxyRenderer3D oxyRenderer = (OxyRenderer3D) oxyEngine.getRenderer();
        scene = new Scene("Test Scene 1", oxyRenderer, new FrameBuffer(windowHandle.getWidth(), windowHandle.getHeight()));
        scene.setUISystem(new OxyUISystem(scene, windowHandle));

        OxyEntity cameraEntity = scene.createInternObjectEntity();
        PerspectiveCamera camera = new PerspectiveCamera(true, 70, (float) windowHandle.getWidth() / windowHandle.getHeight(), 0.003f, 10000f, true, new Vector3f(0, 0, 0), new Vector3f(5.6f, 2.3f, 0));
        cameraEntity.addComponent(camera);

        /*OxyEntity pointLightEntity = scene.createInternObjectEntity();
        Light pointLightComponent = new PointLight(1.0f, 0.027f, 0.0028f);
        pointLightEntity.addComponent(oxyShader, pointLightComponent, new EmittingComponent(
                new Vector3f(-5, -25, 0),
                null,
                new Vector3f(0.2f, 0.2f, 0.2f),
                new Vector3f(10f, 10f, 10f),
                new Vector3f(1.0f, 1.0f, 1.0f)));*/

        /*OxyEntity directionalLightEntity = scene.createInternObjectEntity();
        Light directionalLightComponent = new DirectionalLight();
        directionalLightEntity.addComponent(oxyShader, directionalLightComponent, new EmittingComponent(
                null,
                new Vector3f(33, -17, -32),
                new Vector3f(0.5f, 0.5f, 0.5f),
                new Vector3f(5.0f, 5.0f, 5.0f),
                new Vector3f(0f, 0f, 0f)));*/

        windowHandle.addLayer(StatsLayer.getInstance(windowHandle, scene));
        windowHandle.addLayer(ToolbarLayer.getInstance(windowHandle, scene));
        windowHandle.addLayer(ConfigurationLayer.getInstance(windowHandle, scene));
        windowHandle.addLayer(SceneLayer.getInstance(windowHandle, scene, oxyShader));
        windowHandle.addLayer(PropertiesLayer.getInstance(windowHandle, scene));
        windowHandle.preloadAllLayers();

        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;
        oxyShader.enable();
        oxyShader.setUniform1iv("tex", samplers);
        oxyShader.disable();

        List<OxyModel> testObjects = scene.createModelEntities(OxySystem.FileSystem.getResourceByPath("/models/scene2.obj"), oxyShader);
//        List<OxyModel> gizmoRotate = scene.createModelEntities(OxySystem.FileSystem.getResourceByPath("/models/scene2.obj"), oxyShader);

        for (OxyModel obj : testObjects) {
            obj.addComponent(new SelectedComponent(false), new TransformComponent(new Vector3f(20, 0, 0), 5f));
            obj.updateData();
        }
        /*for (OxyModel obj : gizmoRotate) {
            obj.addComponent(new SelectedComponent(false), new TransformComponent(new Vector3f(0, -35, 0), 5f));
            obj.updateData();
        }*/
        scene.build();
    }

    private void update(float ts, float deltaTime) {
        scene.update(ts, deltaTime);
    }

    private void render(float ts, float deltaTime) {
        OxyTexture.bindAllTextureSlots();

        scene.render(ts, deltaTime);

        OpenGLRendererAPI.clearBuffer();
        OpenGLRendererAPI.clearColor(41, 41, 41, 1.0f);

        ImGui.newFrame();
        windowHandle.renderAllLayers();
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
        scene.dispose();
        dispatcherThread.joinThread();
    }
}
