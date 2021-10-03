package OxyEngineEditor;

import OxyEngine.Core.Layers.EditorLayer;
import OxyEngine.Core.Layers.ImGuiLayer;
import OxyEngine.Core.Layers.Layer;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Scene.SceneRuntime;
import OxyEngine.Core.Window.Event;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.KeyCode;
import OxyEngine.Core.Window.Window;
import OxyEngine.OxyApplication;
import OxyEngine.OxyEngine;
import OxyEngine.TargetPlatform;
import OxyEngineEditor.UI.Panels.*;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

public final class EditorApplication extends OxyApplication {

    private EditorLayer editorLayer;
    private ImGuiLayer imGuiLayer;

    public EditorApplication() {
    }

    @Override
    public void start() {
        window = new Window("OxyEngine - Editor", 1366, 768, Window.WindowMode.WINDOWEDFULLSCREEN);
        oxyEngine = new OxyEngine(this::run, window, OxyEngine.Antialiasing.ON, false, true, TargetPlatform.OpenGL);
        oxyEngine.start();
    }

    @Override
    protected void init() {

        oxyEngine.init();

        editorLayer = EditorLayer.getInstance();
        imGuiLayer = ImGuiLayer.getInstance(window);

        imGuiLayer.addPanel(ProjectPanel.getInstance());
        imGuiLayer.addPanel(StatsPanel.getInstance());
        imGuiLayer.addPanel(ToolbarPanel.getInstance());
        imGuiLayer.addPanel(SceneRuntime.getPanel());
        imGuiLayer.addPanel(SceneHierarchyPanel.getInstance());
        imGuiLayer.addPanel(AnimationPanel.getInstance());
        imGuiLayer.addPanel(SettingsPanel.getInstance());
        imGuiLayer.addPanel(PropertiesPanel.getInstance());
        imGuiLayer.addPanel(ScenePanel.getInstance());
        imGuiLayer.addPanel(MaterialEditorPanel.getInstance());

        layerStack.pushLayer(imGuiLayer, editorLayer);
    }

    @Override
    protected void update(float ts) {
        window.pollEvents();
        for (Event event : Window.getEventPool()) {
            for (Layer l : layerStack.getLayerStack())
                l.onEvent(event);
        }
        window.update();

        for (Layer l : layerStack.getLayerStack()) {
            l.update(ts);
        }
    }

    protected Runnable run() {
        return () -> {

            init();

            double time = 0;
            long timeMillis = System.currentTimeMillis();
            double frames = 0;

            while (Thread.currentThread().isAlive() && !glfwWindowShouldClose(window.getPointer())) {
                if (Input.isKeyPressed(KeyCode.GLFW_KEY_ESCAPE)) break;

                final float currentTime = (float) glfwGetTime();
                final float ts = (float) (currentTime - time);
                time = currentTime;
                update(ts);

                frames++;

                if (System.currentTimeMillis() - timeMillis > 1000) {
                    timeMillis += 1000;
                    Renderer.FRAME_TIME = (float) (1000 / frames);
                    Renderer.FPS = (int) frames;
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
        editorLayer.dispose();
        imGuiLayer.dispose();
    }
}