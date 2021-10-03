package OxyEngine.Core.Layers;

import OxyEngine.Core.Camera.EditorCamera;
import OxyEngine.Core.Camera.Camera;
import OxyEngine.Core.Camera.SceneCamera;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Scene.Entity;
import OxyEngine.Core.Scene.Scene;
import OxyEngine.Core.Scene.SceneRuntime;
import OxyEngine.Core.Scene.SceneState;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.KeyCode;
import OxyEngine.Core.Window.Event;
import OxyEngine.Core.Window.KeyEvent;
import OxyEngine.PhysX.OxyPhysX;
import OxyEngine.PhysX.PhysXComponent;
import OxyEngine.Scripting.ScriptEngine;
import OxyEngine.System.Disposable;
import OxyEngineEditor.UI.Panels.ScenePanel;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Operation;
import org.joml.Vector3f;

import java.io.File;

import static OxyEngine.Core.Scene.Scene.*;
import static OxyEngine.Core.Scene.SceneRuntime.*;
import static OxyEngine.System.FileSystem.deleteDir;
import static OxyEngineEditor.UI.SelectHandler.currentGizmoOperation;
import static OxyEngineEditor.UI.SelectHandler.useSnap;

public final class EditorLayer extends Layer implements Disposable {

    private static EditorLayer INSTANCE = null;

    private final EditorCamera editorCamera;

    public static EditorLayer getInstance() {
        if (INSTANCE == null) INSTANCE = new EditorLayer();
        return INSTANCE;
    }

    private EditorLayer() {
        sceneContext = new Scene("Empty Scene", null);

        OxyPhysX.init();

        //WTF WITH CAMERA???
        editorCamera = new EditorCamera(new Vector3f(0), new Vector3f(-0.35f, -0.77f, 0.0f), true, 45f, ScenePanel.windowPos.x / ScenePanel.windowSize.y, 0.1f, 1000f, false);
        //just first frame update
        editorCamera.update();
        cameraContext = editorCamera;
    }

    @Override
    public void onEvent(Event event) {
        eventDispatcher.dispatch(KeyEvent.Press.class, event, this::onKeyPressed);
        eventDispatcher.dispatch(KeyEvent.Release.class, event, this::onKeyReleased);

        if (cameraContext != null)
            cameraContext.onEvent(event);
    }

    public void onKeyPressed(KeyEvent event) {
        KeyCode key = event.getKeyCode();
        if (key == null) return;

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
                    Renderer.recompileGeometryShader();
            }

            case GLFW_KEY_DELETE -> {
                if (entityContext != null) {
                    SceneRuntime.runtimeStop();
                    SceneRuntime.sceneContext.removeEntity(entityContext);
                    entityContext = null;
                    System.gc();
                    sceneContext.setState(SceneState.STOP);
                }
            }

            case GLFW_KEY_C -> {
                if (control) {
                    sceneContext.copyEntity(entityContext);
                    System.gc();
                    sceneContext.setState(SceneState.STOP);
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

    public void onKeyReleased(KeyEvent event) {
        //nothing for now
    }

    private void queryCamera() {
        for (Entity e : sceneContext.view(Camera.class)) {
            Camera camera = e.get(Camera.class);
            if (sceneContext.getState() == SceneState.PLAY && !camera.equals(editorCamera)) {
                editorCamera.setPrimary(false);
                camera.setPrimary(true);
                cameraContext = camera;
            } else if (sceneContext.getState() != SceneState.PLAY) {
                camera.setPrimary(false);
                editorCamera.setPrimary(true);
                cameraContext = editorCamera;
            }
            if (cameraContext instanceof SceneCamera s) s.update();
        }
    }

    private void onUpdatePlay(float ts) {
        queryCamera();
        Renderer.beginScene(cameraContext, ts);
        for (Entity e : sceneContext.view(PhysXComponent.class))
            e.updateTransform();
        ScriptEngine.onUpdate();
        OxyPhysX.simulate();
        Renderer.endScene();
    }

    private void onUpdateStop(float ts) {
        queryCamera();
        Renderer.beginScene(cameraContext, ts);
        Renderer.endScene();
    }

    @Override
    public void update(float ts) {
        switch (sceneContext.getState()) {
            case PLAY -> onUpdatePlay(ts);
            case STOP -> onUpdateStop(ts);
        }
        Renderer.swapBuffers();
    }

    @Override
    public void dispose() {
        ScriptEngine.dispose();
        OxyPhysX.dispose();
        sceneContext.setState(SceneState.TERMINATED);
        sceneContext.dispose();

        //Deleting the script class directory
        deleteDir(new File(System.getProperty("user.dir") + "\\target\\classes\\Scripts"));
    }
}