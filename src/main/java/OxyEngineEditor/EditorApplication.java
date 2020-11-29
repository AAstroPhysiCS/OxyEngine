package OxyEngineEditor;

import OxyEngine.Components.PerspectiveCamera;
import OxyEngine.Components.SelectedComponent;
import OxyEngine.Components.TagComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Layers.GizmoLayer;
import OxyEngine.Core.Layers.Layer;
import OxyEngine.Core.Layers.OverlayPanelLayer;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.OxyRendererType;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngine.OxyApplication;
import OxyEngine.OxyEngine;
import OxyEngine.System.OxyEventSystem;
import OxyEngine.System.OxyUISystem;
import OxyEngineEditor.Scene.Objects.Model.ModelType;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.Scene.SceneRuntime;
import OxyEngineEditor.UI.Panels.*;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

public class EditorApplication extends OxyApplication {

    public EditorApplication() {
        windowHandle = new WindowHandle("OxyEngine - Editor", 1366, 768, WindowHandle.WindowMode.WINDOWEDFULLSCREEN);
        oxyEngine = new OxyEngine(this::run, windowHandle, OxyEngine.Antialiasing.ON, false, OxyRendererType.Oxy3D);
        oxyEngine.start();
    }

    OxyShader oxyShader;

    @Override
    public void init() {
        oxyEngine.init();

        oxyShader = new OxyShader("shaders/OxyPBR.glsl");
        OxyRenderer3D oxyRenderer = (OxyRenderer3D) oxyEngine.getRenderer();
        scene = new Scene("Test Scene 1", oxyRenderer, new FrameBuffer(windowHandle.getWidth(), windowHandle.getHeight()));

        OxyNativeObject editorCameraEntity = scene.createNativeObjectEntity();
        PerspectiveCamera camera = new PerspectiveCamera(true, Math.toRadians(50), (float) windowHandle.getWidth() / windowHandle.getHeight(), 0.003f, 10000f, true, new Vector3f(0, 0, 0), new Vector3f(3.7f, 5.4f, 0));
        editorCameraEntity.addComponent(camera, new TagComponent("Editor Camera"));

        OxyModel m = scene.createModelEntity(ModelType.Sphere, oxyShader);
        Light pointLightComponent = new PointLight(new Vector3f(2f, 2f, 2f), new Vector3f(1f, 1f, 1f), 1.0f, 0.027f, 0.0028f);
        m.addComponent(pointLightComponent, new TransformComponent(new Vector3f(0, -20, 0), 0.5f), new SelectedComponent(false), new TagComponent("Light Cube"), new OxyMaterial(1.0f, 1.0f, 1.0f, 1.0f));
        m.constructData();

        OxyModel m2 = scene.createModelEntity(ModelType.Sphere, oxyShader);
        Light pointLightComponent2 = new PointLight(new Vector3f(2f, 2f, 2f), new Vector3f(1f, 1f, 1f), 1.0f, 0.027f, 0.0028f);
        m2.addComponent(pointLightComponent2, new TransformComponent(new Vector3f(0, -30, 0), 0.5f), new SelectedComponent(false), new TagComponent("Light Cube 2"), new OxyMaterial(1.0f, 1.0f, 1.0f, 1.0f));
        m2.constructData();

        OxyModel directionalLightEntity = scene.createModelEntity(ModelType.Cone, oxyShader);
        Light directionalLightComponent = new DirectionalLight(new Vector3f(2f, 2f, 2f), new Vector3f(1f, 1f, 1f));
        directionalLightEntity.addComponent(oxyShader, directionalLightComponent, new TransformComponent(new Vector3f(-12.5f, -7.5f, 14.9f), new Vector3f(42.4f, -20.9f, -37.9f)), new SelectedComponent(false));
        directionalLightEntity.constructData();

        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;
        oxyShader.enable();
        oxyShader.setUniform1iv("tex", samplers);
        oxyShader.disable();

        //order matters!
        scene.setUISystem(new OxyUISystem(windowHandle));
        SceneRuntime.ACTIVE_SCENE = scene;
        SceneLayer sceneLayer = new SceneLayer();
        GizmoLayer gizmoLayer = new GizmoLayer();
        OverlayPanelLayer overlayPanelLayer = new OverlayPanelLayer(windowHandle);

        overlayPanelLayer.addPanel(StatsPanel.getInstance());
        overlayPanelLayer.addPanel(ToolbarPanel.getInstance(sceneLayer, gizmoLayer, oxyShader));
        overlayPanelLayer.addPanel(ProjectPanel.getInstance());
        overlayPanelLayer.addPanel(PropertiesPanel.getInstance(sceneLayer));
        overlayPanelLayer.addPanel(ScenePanel.getInstance(sceneLayer));
        overlayPanelLayer.addPanel(EnvironmentPanel.getInstance(sceneLayer));
        overlayPanelLayer.addPanel(SceneHierarchyPanel.getInstance(sceneLayer, oxyShader));
        overlayPanelLayer.addPanel(SceneRuntime.getPanel());

        layerStack.pushLayer(sceneLayer, gizmoLayer, overlayPanelLayer);
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
        OxyTexture.bindAllTextureSlots();

        for (Layer l : layerStack.getLayerStack())
            l.render(ts);

        OpenGLRendererAPI.swapBuffer(windowHandle);
        OpenGLRendererAPI.pollEvents();
        OxyTexture.unbindAllTextureSlots();
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
        scene.getOxyUISystem().dispose();
        SceneRuntime.dispose();
        scene.dispose();
    }
}
