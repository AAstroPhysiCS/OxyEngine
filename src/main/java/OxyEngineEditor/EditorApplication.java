package OxyEngineEditor;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Layers.EditorLayer;
import OxyEngine.Core.Layers.Layer;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.KeyCode;
import OxyEngine.Core.Window.OxyEvent;
import OxyEngine.Core.Window.OxyWindow;
import OxyEngine.OxyApplication;
import OxyEngine.OxyEngine;
import OxyEngine.Core.Context.Scene.SceneRuntime;
import OxyEngine.TargetPlatform;
import OxyEngineEditor.UI.Panels.*;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

public class EditorApplication extends OxyApplication {

    public EditorApplication() {
    }

    @Override
    public void start() {
        oxyWindow = new OxyWindow("OxyEngine - Editor", 1366, 768, OxyWindow.WindowMode.WINDOWEDFULLSCREEN);
        oxyEngine = new OxyEngine(this::run, oxyWindow, OxyEngine.Antialiasing.ON, false, true, TargetPlatform.OpenGL);
        oxyEngine.start();
    }

    @Override
    protected void init() {

        oxyEngine.init();

        EditorLayer editorLayer = EditorLayer.getInstance();
        editorLayer.addPanel(ProjectPanel.getInstance());
        editorLayer.addPanel(StatsPanel.getInstance());
        editorLayer.addPanel(ToolbarPanel.getInstance());
        editorLayer.addPanel(SceneRuntime.getPanel());
        editorLayer.addPanel(SceneHierarchyPanel.getInstance());
        editorLayer.addPanel(AnimationPanel.getInstance());
        editorLayer.addPanel(SettingsPanel.getInstance());
        editorLayer.addPanel(PropertiesPanel.getInstance());
        editorLayer.addPanel(ScenePanel.getInstance());

        layerStack.pushLayer(editorLayer);
        for (Layer l : layerStack.getLayerStack())
            l.build();
    }

    @Override
    protected void update() {
        OxyRenderer.pollEvents();
        for (OxyEvent event : OxyWindow.getEventPool()) {
            for (Layer l : layerStack.getLayerStack())
                l.onEvent(event);
        }
        oxyWindow.update();
    }

    @Override
    protected void render(float ts) {
        for (Layer l : layerStack.getLayerStack()) {
            l.onImGuiRender();
            l.run(ts);
            l.endFrame();
        }

        OxyRenderer.swapBuffers();
    }

    protected Runnable run() {
        return () -> {

            init();

            double time = 0;
            long timeMillis = System.currentTimeMillis();
            double frames = 0;

            while (Thread.currentThread().isAlive() && !glfwWindowShouldClose(oxyWindow.getPointer())) {
                if (Input.isKeyPressed(KeyCode.GLFW_KEY_ESCAPE)) break;

                final float currentTime = (float) glfwGetTime();
                final float ts = (float) (currentTime - time);
                time = currentTime;
                update();
                render(ts);

                frames++;

                if (System.currentTimeMillis() - timeMillis > 1000) {
                    timeMillis += 1000;
                    SceneRuntime.FRAME_TIME = (float) (1000 / frames);
                    SceneRuntime.FPS = (int) frames;
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
        EditorLayer.uiSystem.dispose();
        SceneRuntime.dispose();
    }
}