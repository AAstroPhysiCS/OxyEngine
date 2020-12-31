package OxyEngineEditor;

import OxyEngine.Components.PerspectiveCamera;
import OxyEngine.Components.TagComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Layers.*;
import OxyEngine.Core.Renderer.Buffer.Platform.BufferProducer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.OxyRendererPlatform;
import OxyEngine.Core.Renderer.OxyRendererType;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OxyApplication;
import OxyEngine.OxyEngine;
import OxyEngine.OxyEngineSpecs;
import OxyEngine.System.OxyEventSystem;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.Scene.SceneRuntime;
import OxyEngineEditor.UI.Panels.*;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static OxyEngine.Core.Renderer.Context.OxyRenderCommand.rendererContext;
import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

public class EditorApplication extends OxyApplication {

    public EditorApplication() {
        windowHandle = new WindowHandle("OxyEngine - Editor", 1366, 768, WindowHandle.WindowMode.WINDOWEDFULLSCREEN);
        oxyEngine = new OxyEngine(this::run, windowHandle, OxyEngine.Antialiasing.ON, false, true, new OxyEngineSpecs(OxyRendererPlatform.OpenGL, OxyRendererType.Oxy3D));
        oxyEngine.start();
    }

    public static OxyShader oxyShader;

    @Override
    public void init() {
        oxyEngine.init();

        oxyShader = new OxyShader("shaders/OxyPBR.glsl");
        OxyRenderer3D oxyRenderer = (OxyRenderer3D) oxyEngine.getRenderer();
        scene = new Scene("Test Scene 1", oxyRenderer, BufferProducer.createFrameBuffer(windowHandle.getWidth(), windowHandle.getHeight()));

        OxyNativeObject editorCameraEntity = scene.createNativeObjectEntity();
        PerspectiveCamera camera = new PerspectiveCamera(true, Math.toRadians(50), (float) windowHandle.getWidth() / windowHandle.getHeight(), 1f, 10000f, true, new Vector3f(0, 0, 0), new Vector3f(3.7f, 5.4f, 0));
        editorCameraEntity.addComponent(camera, new TagComponent("Editor Camera"));

        OxyModel m = scene.createEmptyModel(oxyShader);
        m.addComponent(new TransformComponent(new Vector3f(0, -20, 0), 0.5f), new TagComponent("Point Light"), new OxyMaterial(1.0f, 1.0f, 1.0f, 1.0f));

        OxyModel m2 = scene.createEmptyModel(oxyShader);
        m2.addComponent(new TransformComponent(new Vector3f(0, -30, 0), 0.5f), new TagComponent("Point Light 2"), new OxyMaterial(1.0f, 1.0f, 1.0f, 1.0f));

        OxyModel directionalLightEntity = scene.createEmptyModel(oxyShader);
        directionalLightEntity.addComponent(oxyShader, new TagComponent("Directional Light"), new TransformComponent(new Vector3f(-12.5f, -7.5f, 14.9f)));

        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;
        oxyShader.enable();
        oxyShader.setUniform1iv("tex", samplers);
        oxyShader.disable();

        //order matters!
        SceneRuntime.ACTIVE_SCENE = scene;
        SceneLayer sceneLayer = SceneLayer.getInstance();
        GizmoLayer gizmoLayer = GizmoLayer.getInstance();
        UILayer uiLayer = new UILayer(windowHandle);

        uiLayer.addPanel(StatsPanel.getInstance());
        uiLayer.addPanel(ToolbarPanel.getInstance());
        uiLayer.addPanel(ProjectPanel.getInstance());
        uiLayer.addPanel(PropertiesPanel.getInstance(sceneLayer));
        uiLayer.addPanel(ScenePanel.getInstance(sceneLayer));
        uiLayer.addPanel(EnvironmentPanel.getInstance(sceneLayer));
        uiLayer.addPanel(SceneHierarchyPanel.getInstance(sceneLayer, oxyShader));
        uiLayer.addPanel(SceneRuntime.getPanel());

        layerStack.pushLayer(sceneLayer, gizmoLayer, uiLayer);
        for (Layer l : layerStack.getLayerStack())
            l.build();
    }

    @Override
    public void update(float ts) {
        for (Layer l : layerStack.getLayerStack())
            l.update(ts);
    }

    @Override
    public void render(float ts) {
        for (Layer l : layerStack.getLayerStack())
            l.render(ts);

        rendererContext.swapBuffer(windowHandle);
        rendererContext.pollEvents();
    }

    protected Runnable run() {
        return () -> {

            init();

            double time = 0;
            long timeMillis = System.currentTimeMillis();
            int frames = 0;

            while (oxyEngine.getMainThread().isAlive() && !glfwWindowShouldClose(windowHandle.getPointer())) {
                if (OxyEventSystem.keyEventDispatcher.getKeys()[GLFW.GLFW_KEY_ESCAPE]) break;

                final float currentTime = (float) glfwGetTime();
                final float ts = (float) (currentTime - time);
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

    @Override
    public void dispose() {
        oxyEngine.dispose();
        UILayer.uiSystem.dispose();
        SceneRuntime.dispose();
        scene.dispose();
    }
}
