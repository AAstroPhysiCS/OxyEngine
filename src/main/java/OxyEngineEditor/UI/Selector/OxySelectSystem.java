package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.RenderingMode;
import OxyEngineEditor.Components.SelectedComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Model.OxyModel;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.UI.Panels.SceneHierarchyPanel;
import OxyEngineEditor.UI.Panels.ScenePanel;
import OxyEngineEditor.UI.Selector.Tools.MouseSelector;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static OxyEngineEditor.UI.OxyEventSystem.keyEventDispatcher;
import static OxyEngineEditor.UI.OxyEventSystem.mouseButtonDispatcher;
import static org.lwjgl.glfw.GLFW.*;

public class OxySelectSystem {

    private static OxyGizmo3D gizmo;
    private static MouseSelector mSelector;

    private static OxyRenderer3D renderer;
    private static Scene scene;

    public static OxyEntity entityContext;

    public OxySelectSystem(Scene scene, OxyGizmo3D gizmo) {
        OxySelectSystem.gizmo = gizmo;
        OxySelectSystem.renderer = scene.getRenderer();
        OxySelectSystem.scene = scene;
        mSelector = MouseSelector.getInstance();
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
        if (mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_LEFT] && ScenePanel.hoveredWindow) {
            Vector3f direction = mSelector.getObjectPosRelativeToCamera(
                    ScenePanel.windowSize.x - ScenePanel.offset.x,
                    ScenePanel.windowSize.y - ScenePanel.offset.y,
                    new Vector2f(
                            ScenePanel.mousePos.x - ScenePanel.windowPos.x - ScenePanel.offset.x,
                            ScenePanel.mousePos.y - ScenePanel.windowPos.y - ScenePanel.offset.y),
                    renderer.getCamera()
            );
            //if a entity is selected in SceneHierarchyPanel then do it to false
            if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
            //if user selected a empty entity then don't show them, instead set it to null and don't draw anything
            entityContext = mSelector.selectObject(scene.getEntities(), OxyRenderer.currentBoundedCamera.getCameraController().origin, direction);
            //if something was selected then show it
            if (entityContext != null) entityContext.get(SelectedComponent.class).selected = true;
        }
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

        if (gizmoComponent instanceof GizmoMode.Translation t) {
            xModel = t.getXModelTranslation();
            yModel = t.getYModelTranslation();
            zModel = t.getZModelTranslation();
        } else if (gizmoComponent instanceof GizmoMode.Scaling s) {
            xModel = s.getXModelScale();
            yModel = s.getYModelScale();
            zModel = s.getZModelScale();
            OxyModel scalingFactor = s.getScalingCube();
            if (e != null) {
                TransformComponent sF = scalingFactor.get(TransformComponent.class);
                sF.position.set(new Vector3f(scalingFactor.originPos).mul(sF.scale).add(e.get(TransformComponent.class).position));
                scalingFactor.updateData();
            }
        }

        if (xModel == null || yModel == null || zModel == null || e == null) return;

        TransformComponent c = e.get(TransformComponent.class);

        TransformComponent xC = xModel.get(TransformComponent.class);
        TransformComponent yC = yModel.get(TransformComponent.class);
        TransformComponent zC = zModel.get(TransformComponent.class);

        xC.position.set(new Vector3f(xModel.originPos).mul(xC.scale).add(c.position));
        yC.position.set(new Vector3f(yModel.originPos).mul(yC.scale).add(c.position));
        zC.position.set(new Vector3f(zModel.originPos).mul(zC.scale).add(c.position));

        xModel.updateData();
        yModel.updateData();
        zModel.updateData();
    }
}