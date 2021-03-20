package OxyEngineEditor;

import OxyEngine.Components.TagComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Camera.EditorCamera;
import OxyEngine.Core.Layers.GizmoLayer;
import OxyEngine.Core.Layers.Layer;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Layers.UILayer;
import OxyEngine.Core.Renderer.Buffer.Platform.BufferProducer;
import OxyEngine.Core.Renderer.Buffer.Platform.FrameBufferSpecification;
import OxyEngine.Core.Renderer.Buffer.Platform.FrameBufferTextureFormat;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.OxyRendererPlatform;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OxyApplication;
import OxyEngine.OxyEngine;
import OxyEngine.OxyEngineSpecs;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;
import OxyEngine.Scene.Scene;
import OxyEngine.Scene.SceneRuntime;
import OxyEngineEditor.UI.Panels.*;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

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
        oxyEngine = new OxyEngine(this::run, windowHandle, OxyEngine.Antialiasing.ON, false, true, new OxyEngineSpecs(OxyRendererPlatform.OpenGL));
        oxyEngine.start();
    }

    public static OxyShader oxyShader;
    public static OxyNativeObject editorCameraEntity;

    @Override
    public void init() {
        oxyEngine.init();

        oxyShader = new OxyShader("shaders/OxyPBRAnimation.glsl");
        OxyRenderer3D oxyRenderer = (OxyRenderer3D) oxyEngine.getRenderer();

        scene = new Scene("Test Scene 1", oxyRenderer,
                BufferProducer.createFrameBuffer(windowHandle.getWidth(), windowHandle.getHeight(),
                        OpenGLFrameBuffer.createNewSpec(FrameBufferSpecification.class)
                                .setAttachmentIndex(0)
                                .setMultiSampled(true)
                                .setFormats(FrameBufferTextureFormat.RGBA8, FrameBufferTextureFormat.DEPTH24STENCIL8)
                                .useRenderBuffer(true)));

        //Editor Camera should be native.
        editorCameraEntity = scene.createNativeObjectEntity();
        EditorCamera editorCamera = new EditorCamera(true, 50, (float) windowHandle.getWidth() / windowHandle.getHeight(), 1f, 10000f, true);
        editorCameraEntity.addComponent(new TransformComponent(new Vector3f(0), new Vector3f(-0.35f, -0.77f, 0.0f)), editorCamera, new TagComponent("Editor Camera"));

        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;
        oxyShader.enable();
        oxyShader.setUniform1iv("tex", samplers);
        oxyShader.disable();

        SceneRuntime.ACTIVE_SCENE = scene;

        SceneLayer sceneLayer = SceneLayer.getInstance();
        GizmoLayer gizmoLayer = GizmoLayer.getInstance();
        UILayer uiLayer = UILayer.getInstance();

        uiLayer.addPanel(StatsPanel.getInstance());
        uiLayer.addPanel(ToolbarPanel.getInstance());
        uiLayer.addPanel(ProjectPanel.getInstance());
        uiLayer.addPanel(ScenePanel.getInstance());
        uiLayer.addPanel(SceneRuntime.getPanel());
        uiLayer.addPanel(SceneHierarchyPanel.getInstance());
        uiLayer.addPanel(PropertiesPanel.getInstance());

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
                if (keyEventDispatcher.getKeys()[GLFW.GLFW_KEY_ESCAPE]) break;

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
