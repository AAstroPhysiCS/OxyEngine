package OxyEngineEditor;

import OxyEngine.Core.Layers.*;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
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
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Components.*;
import OxyEngineEditor.Scene.Model.ModelType;
import OxyEngineEditor.Scene.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.OxyEntitySystem;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.UI.OxyEventSystem;
import OxyEngineEditor.UI.OxyUISystem;
import OxyEngineEditor.UI.Panels.*;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

public class EditorApplication extends OxyApplication {

    private final WindowHandle windowHandle;

    public EditorApplication() {
        windowHandle = new WindowHandle("OxyEngine - Editor", 1366, 768, WindowHandle.WindowMode.WINDOWEDFULLSCREEN);
        oxyEngine = new OxyEngine(this::run, windowHandle, OxyEngine.Antialiasing.ON, false, OxyRendererType.Oxy3D);
        layerStack = new LayerStack();
        oxyEngine.start();
    }

    @Override
    public void init() {
        oxyEngine.init();

        OxyShader oxyShader = new OxyShader("shaders/world.glsl");
        OxyRenderer3D oxyRenderer = (OxyRenderer3D) oxyEngine.getRenderer();
        scene = new Scene("Test Scene 1", oxyRenderer, new FrameBuffer(windowHandle.getWidth(), windowHandle.getHeight()));

        OxyEntity cameraEntity = scene.createNativeObjectEntity();
        PerspectiveCamera camera = new PerspectiveCamera(true, 70, (float) windowHandle.getWidth() / windowHandle.getHeight(), 0.003f, 10000f, true, new Vector3f(0, 0, 0), new Vector3f(3.7f, 5.4f, 0));
        cameraEntity.addComponent(camera);

        OxyEntity pointLightEntity = scene.createNativeObjectEntity();
        Light pointLightComponent = new PointLight(1.0f, 0.027f, 0.0028f);
        pointLightEntity.addComponent(oxyShader, pointLightComponent, new EmittingComponent(
                new Vector3f(0, -2, 0),
                null,
                new Vector3f(0.1f, 0.1f, 0.1f),
                new Vector3f(5f, 5f, 5f),
                new Vector3f(1f, 1f, 1f)));

        OxyEntity m = scene.createModelEntity(ModelType.Sphere, oxyShader);
        m.addComponent(new TransformComponent(new Vector3f(0, -10, 0), 1f), new SelectedComponent(false), new TagComponent("Light Cube"), new OxyMaterial(1.0f, 1.0f, 1.0f, 1.0f));
        m.constructData();

        class Mover implements OxyEntitySystem {

            private final Vector3f positionLight, positionEntity;

            public Mover(Vector3f positionLight, Vector3f positionEntity) {
                this.positionLight = positionLight;
                this.positionEntity = positionEntity;
            }

            @Override
            public void run() {
                positionLight.set(positionEntity);
            }
        }

        m.addSystem(new Mover(pointLightEntity.get(EmittingComponent.class).position(), m.get(TransformComponent.class).position));

        /*OxyEntity directionalLightEntity = scene.createNativeObjectEntity();
        Light directionalLightComponent = new DirectionalLight();
        directionalLightEntity.addComponent(oxyShader, directionalLightComponent, new EmittingComponent(
                null,
                new Vector3f(152, -9.8f, -0.14f),
                new Vector3f(0.5f, 0.5f, 0.5f),
                new Vector3f(5.0f, 5.0f, 5.0f),
                new Vector3f(0f, 0f, 0f)));*/

        List<OxyEntity> testObjects = scene.createModelEntities(OxySystem.FileSystem.getResourceByPath("/models/scene3.fbx"), oxyShader);
        for (OxyEntity obj : testObjects) {
            obj.addComponent(new SelectedComponent(false), new TransformComponent(new Vector3f(0, 0, 0), 2f));
            obj.constructData();
        }

        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;
        oxyShader.enable();
        oxyShader.setUniform1iv("tex", samplers);
        oxyShader.disable();

        //order matters!
        scene.setUISystem(new OxyUISystem(windowHandle));
        SceneLayer sceneLayer = new SceneLayer(scene);
        GizmoLayer gizmoLayer = new GizmoLayer(scene, windowHandle);
        OverlayPanelLayer overlayPanelLayer = new OverlayPanelLayer(windowHandle, scene);

        overlayPanelLayer.addPanel(StatsPanel.getInstance());
        overlayPanelLayer.addPanel(ToolbarPanel.getInstance());
        overlayPanelLayer.addPanel(SceneHierarchyPanel.getInstance(sceneLayer, oxyShader));
        overlayPanelLayer.addPanel(PropertiesPanel.getInstance(sceneLayer));
        overlayPanelLayer.addPanel(ScenePanel.getInstance(sceneLayer, oxyShader));

        layerStack.pushLayer(sceneLayer, gizmoLayer, overlayPanelLayer);
        for (Layer l : layerStack.getLayerStack())
            l.build();
    }

    @Override
    public void update(float ts, float deltaTime) {
        for (Layer l : layerStack.getLayerStack())
            l.update(ts, deltaTime);
    }

    @Override
    public void render(float ts, float deltaTime) {
        OxyTexture.bindAllTextureSlots();

        for (Layer l : layerStack.getLayerStack())
            l.render(ts, deltaTime);

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

    @Override
    public void dispose() {
        oxyEngine.dispose();
        scene.dispose();
    }
}
