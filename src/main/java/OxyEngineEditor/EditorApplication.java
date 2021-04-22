package OxyEngineEditor;

import OxyEngine.Core.Layers.Layer;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Layers.UILayer;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OxyApplication;
import OxyEngine.OxyEngine;
import OxyEngine.Scene.Scene;
import OxyEngine.Scene.SceneRuntime;
import OxyEngine.TargetPlatform;
import OxyEngineEditor.UI.Panels.*;
import org.lwjgl.glfw.GLFW;
import OxyEngine.Core.Renderer.ShadowRender.DebugPanel;

import static OxyEngine.Core.Renderer.Context.OxyRenderCommand.rendererContext;
import static OxyEngine.System.OxyEventSystem.keyEventDispatcher;
import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

public class EditorApplication extends OxyApplication {

    public EditorApplication() {
        windowHandle = new WindowHandle("OxyEngine - Editor", 1366, 768, WindowHandle.WindowMode.WINDOWEDFULLSCREEN);
        oxyEngine = new OxyEngine(this::run, windowHandle, OxyEngine.Antialiasing.ON, false, true, TargetPlatform.OpenGL);
        oxyEngine.start();
    }

    @Override
    public void init() {
        oxyEngine.init();

        scene = new Scene("Test Scene 1");

        SceneRuntime.ACTIVE_SCENE = scene;

        SceneLayer sceneLayer = SceneLayer.getInstance();
        UILayer uiLayer = UILayer.getInstance();

        uiLayer.addPanel(StatsPanel.getInstance());
        uiLayer.addPanel(ToolbarPanel.getInstance());
        uiLayer.addPanel(ProjectPanel.getInstance());
        uiLayer.addPanel(ScenePanel.getInstance());
        uiLayer.addPanel(SceneRuntime.getPanel());
        uiLayer.addPanel(SceneHierarchyPanel.getInstance());
        uiLayer.addPanel(PropertiesPanel.getInstance());
        uiLayer.addPanel(AnimationPanel.getInstance());
        uiLayer.addPanel(DebugPanel.getInstance());

        layerStack.pushLayer(sceneLayer, uiLayer);
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
            double frames = 0;

            while (oxyEngine.getMainThread().isAlive() && !glfwWindowShouldClose(windowHandle.getPointer())) {
                if (keyEventDispatcher.getKeys()[GLFW.GLFW_KEY_ESCAPE]) break;

                final float currentTime = (float) glfwGetTime();
                final float ts = (float) (currentTime - time);
                time = currentTime;
                update(ts);
                render(ts);

                frames++;

                if (System.currentTimeMillis() - timeMillis > 1000) {
                    timeMillis += 1000;
                    FRAME_TIME = (float) (1000 / frames);
                    FPS = (int) frames;
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