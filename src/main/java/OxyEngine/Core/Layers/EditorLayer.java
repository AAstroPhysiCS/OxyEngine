package OxyEngine.Core.Layers;

import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.KeyCode;
import OxyEngine.Core.Window.OxyEvent;
import OxyEngine.Core.Window.OxyKeyEvent;
import OxyEngine.PhysX.OxyPhysX;
import OxyEngine.Scene.OxyModel;
import OxyEngine.Scene.Scene;
import OxyEngine.Scene.SceneRenderer;
import OxyEngine.Scene.SceneRuntime;
import OxyEngine.Scene.SceneState;
import OxyEngine.Scripting.ScriptEngine;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Operation;

import static OxyEngine.Scene.Scene.*;
import static OxyEngine.Scene.SceneRuntime.*;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.currentGizmoOperation;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.useSnap;

public class EditorLayer extends Layer {

    private static EditorLayer INSTANCE = null;

    private final OxyPhysX oxyPhysics = OxyPhysX.getInstance();

    private final SceneRenderer sceneRenderer = SceneRenderer.getInstance();

    public static EditorLayer getInstance() {
        if (INSTANCE == null) INSTANCE = new EditorLayer();
        return INSTANCE;
    }

    private EditorLayer() {
        SceneRuntime.ACTIVE_SCENE = new Scene("Empty Scene", null);
    }

    @Override
    public void build() {
        oxyPhysics.init();
        sceneRenderer.initPipelines();
        sceneRenderer.initScene();
    }

    @Override
    public void onEvent(OxyEvent event) {
        eventDispatcher.dispatch(OxyKeyEvent.Press.class, event, this::onKeyPressed);
        eventDispatcher.dispatch(OxyKeyEvent.Release.class, event, this::onKeyReleased);

        if(currentBoundedCamera != null)
            currentBoundedCamera.onEvent(event);
    }

    public void onKeyPressed(OxyKeyEvent event) {
        KeyCode key = event.getKeyCode();

        boolean control = Input.isKeyPressed(KeyCode.GLFW_KEY_LEFT_CONTROL) || Input.isKeyPressed(KeyCode.GLFW_KEY_RIGHT_CONTROL);

        switch (key) {
            case GLFW_KEY_N -> {
                if (control) {
                    newScene();
                    System.gc();
                }
            }
            case GLFW_KEY_O -> {
                if (control) {
                    openScene();
                    System.gc();
                }
            }

            case GLFW_KEY_G -> {
                if (control)
                    SceneRenderer.getInstance().recompileGeometryShader();
            }

            case GLFW_KEY_DELETE -> {
                if (entityContext != null) {
                    SceneRuntime.stop();
                    SceneRuntime.ACTIVE_SCENE.removeEntity(entityContext);
                    var instance = SceneRenderer.getInstance();
                    instance.updateModelEntities();
                    instance.updateCameraEntities();
                    instance.updateNativeEntities();
                    entityContext = null;
                    System.gc();
                    ACTIVE_SCENE.STATE = SceneState.IDLE;
                }
            }

            case GLFW_KEY_C -> {
                if (control && entityContext instanceof OxyModel m) {
                    m.copyMe();
                    SceneRenderer.getInstance().updateModelEntities();
                    System.gc();
                    ACTIVE_SCENE.STATE = SceneState.IDLE;
                }
            }

            case GLFW_KEY_T -> {
                if (!ImGuizmo.isUsing()) currentGizmoOperation = Operation.TRANSLATE;
            }
            case GLFW_KEY_R -> {
                if (!ImGuizmo.isUsing()) currentGizmoOperation = Operation.ROTATE;
            }
            case GLFW_KEY_S -> {
                if (!ImGuizmo.isUsing()) currentGizmoOperation = Operation.SCALE;
                if (control)
                    saveScene();
            }
            case GLFW_KEY_Q -> {
                if (!ImGuizmo.isUsing()) currentGizmoOperation = -1;
            }

            case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> useSnap = !useSnap;
        }
    }

    public void onKeyReleased(OxyKeyEvent event) {
        //nothing for now
    }

    @Override
    public void update(float ts) {
        sceneRenderer.updateScene(ts);
        ScriptEngine.run();
        oxyPhysics.simulate();
    }

    @Override
    public void render() {
        sceneRenderer.renderScene();
    }
}