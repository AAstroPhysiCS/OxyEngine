package OxyEngineEditor.UI;

import OxyEngine.Components.SelectedComponent;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.SceneRenderer;
import OxyEngineEditor.UI.Panels.ScenePanel;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import org.joml.Vector2f;

import java.util.Set;

import static OxyEngine.Scene.SceneRuntime.entityContext;
import static org.lwjgl.opengl.GL30.*;

public class OxySelectHandler {

    public static int currentGizmoOperation = -1;
    public static final int currentGizmoMode = Mode.LOCAL;
    public static boolean useSnap = false;
    public static float snapValue = 0.5f;
    public static final float[] snapValueBuffer = new float[]{snapValue, snapValue, snapValue};

    public static void startPicking() {
        Set<OxyEntity> allModelEntities = SceneRenderer.getInstance().allModelEntities;

        if (allModelEntities.size() == 0) return;

        int id = getEntityID();
        for (OxyEntity e : allModelEntities) {
            if (e.getObjectId() == id) {
                if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                entityContext = e;
                entityContext.get(SelectedComponent.class).selected = true;
                break;
            }
        }
    }

    private static int getEntityID() {
        var instance = SceneRenderer.getInstance();
        instance.getPickingFrameBuffer().bind();
        Vector2f mousePos = new Vector2f(
                ScenePanel.mousePos.x - ScenePanel.windowPos.x - ScenePanel.offset.x,
                ScenePanel.mousePos.y - ScenePanel.windowPos.y - ScenePanel.offset.y);
        mousePos.y = instance.getMainFrameBuffer().getHeight() - mousePos.y;
        glReadBuffer(GL_COLOR_ATTACHMENT1);
        int[] entityID = new int[1];
        glReadPixels((int) mousePos.x, (int) mousePos.y, 1, 1, GL_RED_INTEGER, GL_INT, entityID);
        instance.getPickingFrameBuffer().unbind();
        return entityID[0];
    }

    public static boolean isOverTranslateGizmo(){
        return (ImGuizmo.isOver(Operation.TRANSLATE) && currentGizmoOperation == Operation.TRANSLATE);
    }

    public static boolean isOverRotationGizmo(){
        return (ImGuizmo.isOver(Operation.ROTATE) && currentGizmoOperation == Operation.ROTATE);
    }

    public static boolean isOverScaleGizmo(){
        return (ImGuizmo.isOver(Operation.SCALE) && currentGizmoOperation == Operation.SCALE);
    }

    public static boolean isOverAnyGizmo(){
        return isOverTranslateGizmo() || isOverRotationGizmo() || isOverScaleGizmo();
    }
}