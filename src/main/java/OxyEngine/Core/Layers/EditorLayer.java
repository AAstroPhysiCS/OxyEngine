package OxyEngine.Core.Layers;

import OxyEngine.Components.TagComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Camera.EditorCamera;
import OxyEngine.Core.Context.Scene.*;
import OxyEngine.Core.Context.SceneRenderer;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.KeyCode;
import OxyEngine.Core.Window.OxyEvent;
import OxyEngine.Core.Window.OxyKeyEvent;
import OxyEngine.OxyEngine;
import OxyEngine.PhysX.OxyPhysX;
import OxyEngine.Scripting.ScriptEngine;
import OxyEngine.System.OxySystem;
import OxyEngine.System.OxyUISystem;
import OxyEngineEditor.UI.Panels.Panel;
import OxyEngineEditor.UI.Panels.ScenePanel;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.*;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.Core.Context.Scene.Scene.*;
import static OxyEngine.Core.Context.Scene.SceneRuntime.*;
import static OxyEngineEditor.UI.OxySelectHandler.currentGizmoOperation;
import static OxyEngineEditor.UI.OxySelectHandler.useSnap;

public class EditorLayer extends Layer {

    private static EditorLayer INSTANCE = null;

    private final OxyPhysX oxyPhysics = OxyPhysX.getInstance();

    private final SceneRenderer sceneRenderer = SceneRenderer.getInstance();
    public static OxyUISystem uiSystem;

    public static OxyNativeObject editorCameraEntity;

    private final List<Panel> panelList = new ArrayList<>();

    public static EditorLayer getInstance() {
        if (INSTANCE == null) INSTANCE = new EditorLayer();
        return INSTANCE;
    }

    private EditorLayer() {
        ACTIVE_SCENE = new Scene("Empty Scene", null);
        uiSystem = new OxyUISystem(OxyEngine.getWindowHandle());
    }

    public void addPanel(Panel panel) {
        panelList.add(panel);
    }

    @Override
    public void build() {

        oxyPhysics.init();
        sceneRenderer.initScene();

        editorCameraEntity = ACTIVE_SCENE.createNativeObjectEntity(null, null);
        EditorCamera editorCamera = new EditorCamera(true, 45f, ScenePanel.windowPos.x / ScenePanel.windowSize.y, 1f, 10000f, true);
        editorCameraEntity.addComponent(new TransformComponent(new Vector3f(0), new Vector3f(-0.35f, -0.77f, 0.0f)), editorCamera, new TagComponent("Editor Camera"));
        //just first frame update
        editorCamera.calcViewMatrixNoTranslation();
        editorCamera.update();
        currentBoundedCamera = editorCamera;

        for (Panel panel : panelList) {
            panel.preload();
        }
    }

    @Override
    public void onEvent(OxyEvent event) {
        eventDispatcher.dispatch(OxyKeyEvent.Press.class, event, this::onKeyPressed);
        eventDispatcher.dispatch(OxyKeyEvent.Release.class, event, this::onKeyReleased);

        if (currentBoundedCamera != null)
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
                    SceneRuntime.onStop();
                    SceneRuntime.ACTIVE_SCENE.removeEntity(entityContext);
                    var instance = SceneRenderer.getInstance();
                    instance.updateModelEntities();
                    instance.updateCameraEntities();
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
    public void run(float ts) {
        sceneRenderer.updateScene(ts);
        ScriptEngine.run();
        oxyPhysics.simulate();
        sceneRenderer.renderScene();
    }

    @Override
    public void endFrame() {
        sceneRenderer.endFrame();
    }

    @Override
    public void onImGuiRender() {
        uiSystem.newFrameGLFW();
        ImGui.newFrame();
        ImGuizmo.beginFrame();

        final ImGuiViewport viewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(viewport.getWorkPosX(), viewport.getWorkPosY(), ImGuiCond.Always);
        ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY(), ImGuiCond.Always);

        ImGui.pushFont(OxySystem.Font.allFonts.get(0));

        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.begin("Main", ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoNavFocus |
                ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus |
                ImGuiWindowFlags.NoDecoration);
        int id = ImGui.getID("MyDockSpace");
        ImGui.dockSpace(id, 0, 0, ImGuiDockNodeFlags.PassthruCentralNode);
        ImGui.end();
        ImGui.popStyleVar(3);

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4, 8);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 3);
        ImGui.pushStyleColor(ImGuiCol.TableHeaderBg, Panel.childCardBgC[0], Panel.childCardBgC[1], Panel.childCardBgC[2], Panel.childCardBgC[3]);
        ImGui.pushStyleColor(ImGuiCol.TableBorderLight, Panel.frameBgC[0], Panel.frameBgC[1], Panel.frameBgC[2], Panel.frameBgC[3]);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, Panel.frameBgC[0], Panel.frameBgC[1], Panel.frameBgC[2], Panel.frameBgC[3]);
        for (Panel panel : panelList)
            panel.renderPanel();

        ImGui.popStyleVar(2);
        ImGui.popStyleColor(3);

        ImGui.popFont();

        uiSystem.updateImGuiContext(SceneRuntime.TS);
        ImGui.render();
        uiSystem.renderDrawData();
    }
}