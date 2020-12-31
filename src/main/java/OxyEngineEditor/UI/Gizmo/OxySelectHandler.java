package OxyEngineEditor.UI.Gizmo;

import OxyEngine.Components.RenderingMode;
import OxyEngine.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.UI.Panels.SceneHierarchyPanel;
import OxyEngineEditor.UI.Panels.ScenePanel;
import org.joml.Vector3f;

import static OxyEngine.System.OxyEventSystem.keyEventDispatcher;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;

public class OxySelectHandler {

    private static OxyGizmo3D gizmo;

    public static OxyEntity entityContext;

    private OxySelectHandler() {
    }

    public static void init(OxyGizmo3D gizmo) {
        OxySelectHandler.gizmo = gizmo;
    }

    static boolean switchC = false;

    public static void controlRenderableStates() {
        if (keyEventDispatcher.getKeys()[GLFW_KEY_C] && (ScenePanel.focusedWindow || SceneHierarchyPanel.focusedWindow) && !switchC) {
            if (gizmo.mode == GizmoMode.Translation) {
                gizmo.mode = GizmoMode.Scale;
                gizmo.mode.gizmoComponent.switchRenderableState(RenderingMode.Normal);
                GizmoMode.Translation.gizmoComponent.switchRenderableState(RenderingMode.None);
            } else {
                gizmo.mode = GizmoMode.Translation;
                gizmo.mode.gizmoComponent.switchRenderableState(RenderingMode.Normal);
                GizmoMode.Scale.gizmoComponent.switchRenderableState(RenderingMode.None);
            }
            switchC = true;
        }
        if (!keyEventDispatcher.getKeys()[GLFW_KEY_C]) switchC = false;
        gizmoEntityContextControl(entityContext);
    }

    public static void gizmoEntityContextControl(OxyEntity e) {
        if (e != null) {
            moveGizmos(GizmoMode.Translation.gizmoComponent, e);
            moveGizmos(GizmoMode.Scale.gizmoComponent, e);
        } else {
            gizmo.mode = GizmoMode.Scale;
            GizmoMode.Translation.gizmoComponent.switchRenderableState(RenderingMode.None);
            GizmoMode.Scale.gizmoComponent.switchRenderableState(RenderingMode.None);
        }
    }

    private static void moveGizmos(GizmoMode.GizmoComponent gizmoComponent, OxyEntity e) {

        OxyModel xModel = null, yModel = null, zModel = null;

        if (e == null) return;
        TransformComponent c = e.get(TransformComponent.class);

        if (gizmoComponent instanceof GizmoMode.Translation t) {
            xModel = t.getXModelTranslation();
            yModel = t.getYModelTranslation();
            zModel = t.getZModelTranslation();

        } else if (gizmoComponent instanceof GizmoMode.Scaling s) {
            xModel = s.getXModelScale();
            yModel = s.getYModelScale();
            zModel = s.getZModelScale();

            OxyModel scalingFactor = s.getScalingCube();
            TransformComponent sF = scalingFactor.get(TransformComponent.class);
            sF.position.set(new Vector3f(e.get(TransformComponent.class).position));
            scalingFactor.updateData();
        }

        if (xModel == null || yModel == null || zModel == null) return;

        TransformComponent xC = xModel.get(TransformComponent.class);
        TransformComponent yC = yModel.get(TransformComponent.class);
        TransformComponent zC = zModel.get(TransformComponent.class);

        xC.position.set(new Vector3f(c.position).sub(1.4f, 0, 0f));
        yC.position.set(new Vector3f(c.position).sub(0, 1.4f, 0));
        zC.position.set(new Vector3f(c.position).sub(0f, 0, 1.4f));

        xModel.updateData();
        yModel.updateData();
        zModel.updateData();
    }
}