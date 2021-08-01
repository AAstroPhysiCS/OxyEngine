package OxyEngineEditor.UI;

import OxyEngine.Components.SelectedComponent;
import OxyEngine.Core.Context.SceneRenderer;
import OxyEngine.Core.Context.Scene.OxyEntity;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;

import java.util.Set;

import static OxyEngine.Core.Context.OxyRenderer.getEntityIDByMousePosition;
import static OxyEngine.Core.Context.Scene.SceneRuntime.entityContext;

public final class OxySelectHandler {

    public static int currentGizmoOperation = -1;
    public static final int currentGizmoMode = Mode.LOCAL;
    public static boolean useSnap = false;
    public static float snapValue = 0.5f;
    public static final float[] snapValueBuffer = new float[]{snapValue, snapValue, snapValue};

    private OxySelectHandler(){}

    public static void startPicking() {
        Set<OxyEntity> allModelEntities = SceneRenderer.getInstance().allModelEntities;

        if (allModelEntities.size() == 0) return;

        int id = getEntityIDByMousePosition();
        for (OxyEntity e : allModelEntities) {
            if (e.getObjectId() == id) {
                if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                entityContext = e;
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