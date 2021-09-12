package OxyEngineEditor.UI;

import OxyEngine.Components.SelectedComponent;
import OxyEngine.Core.Context.Renderer.DrawCommand;
import OxyEngine.Core.Context.Renderer.Renderer;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;

import java.util.List;

import static OxyEngine.Core.Context.Renderer.Renderer.getEntityIDByMousePosition;
import static OxyEngine.Core.Context.Scene.SceneRuntime.sceneContext;
import static OxyEngine.Core.Context.Scene.SceneRuntime.entityContext;

public final class SelectHandler {

    public static int currentGizmoOperation = -1;
    public static final int currentGizmoMode = Mode.LOCAL;
    public static boolean useSnap = false;
    public static float snapValue = 0.5f;
    public static final float[] snapValueBuffer = new float[]{snapValue, snapValue, snapValue};

    private SelectHandler(){}

    public static void startPicking() {
        List<DrawCommand> drawCommands = Renderer.getAllDrawCommands();

        if (drawCommands.size() == 0) return;

        int id = getEntityIDByMousePosition();
        for (DrawCommand e : drawCommands) {
            if (e.mesh().getMeshID() == id) {
                if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                entityContext = sceneContext.findEntityByComponent(e.mesh());
                entityContext.get(SelectedComponent.class).selected = true;
                break;
            }
        }
    }

    public static boolean isOverTranslateGizmo() {
        return (ImGuizmo.isOver(Operation.TRANSLATE) && currentGizmoOperation == Operation.TRANSLATE);
    }

    public static boolean isOverRotationGizmo() {
        return (ImGuizmo.isOver(Operation.ROTATE) && currentGizmoOperation == Operation.ROTATE);
    }

    public static boolean isOverScaleGizmo() {
        return (ImGuizmo.isOver(Operation.SCALE) && currentGizmoOperation == Operation.SCALE);
    }

    public static boolean isOverAnyGizmo() {
        return isOverTranslateGizmo() || isOverRotationGizmo() || isOverScaleGizmo();
    }
}