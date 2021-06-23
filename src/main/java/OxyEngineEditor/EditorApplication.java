package OxyEngineEditor;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Layers.EditorLayer;
import OxyEngine.Core.Layers.Layer;
import OxyEngine.Core.Layers.UILayer;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.KeyCode;
import OxyEngine.Core.Window.OxyEvent;
import OxyEngine.Core.Window.OxyWindow;
import OxyEngine.OxyApplication;
import OxyEngine.OxyEngine;
import OxyEngine.Scene.SceneRuntime;
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
        UILayer uiLayer = UILayer.getInstance();

        uiLayer.addPanel(ProjectPanel.getInstance());
        uiLayer.addPanel(StatsPanel.getInstance());
        uiLayer.addPanel(ToolbarPanel.getInstance());
        uiLayer.addPanel(ScenePanel.getInstance());
        uiLayer.addPanel(SceneRuntime.getPanel());
        uiLayer.addPanel(SceneHierarchyPanel.getInstance());
        uiLayer.addPanel(AnimationPanel.getInstance());
        uiLayer.addPanel(SettingsPanel.getInstance());
        uiLayer.addPanel(PropertiesPanel.getInstance());
//        uiLayer.addPanel(ShadowRenderer.DebugPanel.getInstance());

        layerStack.pushLayer(uiLayer, editorLayer);
        for (Layer l : layerStack.getLayerStack())
            l.build();
    }

    @Override
    protected void update(float ts) {
        for(OxyEvent event : OxyWindow.getEventPool()) {
            for (Layer l : layerStack.getLayerStack())
                l.onEvent(event);
        }

        for (Layer l : layerStack.getLayerStack())
            l.update(ts);
    }

    @Override
    protected void render() {
        OxyRenderer.clearBuffer(); //clearing the buffer for the default framebuffer

        for (Layer l : layerStack.getLayerStack())
            l.render();

        OxyRenderer.swapBuffers();
        OxyRenderer.pollEvents();
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
                update(ts);
                render();

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
        UILayer.uiSystem.dispose();
        SceneRuntime.dispose();
    }
}