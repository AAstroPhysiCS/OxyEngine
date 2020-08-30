package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.RenderingMode;
import OxyEngineEditor.Components.RenderableComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Model.OxyModel;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.UI.OxyUISystem;
import OxyEngineEditor.UI.Panels.ScenePanel;
import OxyEngineEditor.UI.Selector.Tools.MouseSelector;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class OxySelectSystem {

    private static OxyGizmo3D gizmo;
    private static MouseSelector mSelector;

    private final OxyRenderer3D renderer;
    private final Scene scene;

    private static OxySelectSystem INSTANCE;

    public static OxySelectSystem getInstance(Scene scene, OxyGizmo3D gizmo) {
        if (INSTANCE == null) INSTANCE = new OxySelectSystem(scene, gizmo);
        return INSTANCE;
    }

    private OxySelectSystem(Scene scene, OxyGizmo3D gizmo) {
        OxySelectSystem.gizmo = gizmo;
        this.renderer = scene.getRenderer();
        this.scene = scene;
        mSelector = MouseSelector.getInstance();
    }

    static boolean switchC = false;

    public void controlRenderableStates() {
        if (OxyUISystem.OxyEventSystem.keyEventDispatcher.getKeys()[GLFW_KEY_C] && ScenePanel.focusedWindow && !switchC) {
            if (gizmo.mode == OxyGizmo3D.GizmoMode.Translation) {
                gizmo.mode = OxyGizmo3D.GizmoMode.Scale;
                gizmo.mode.gizmoComponent.switchRenderableState(RenderingMode.Normal);
                OxyGizmo3D.GizmoMode.Translation.gizmoComponent.switchRenderableState(RenderingMode.None);
            } else {
                gizmo.mode = OxyGizmo3D.GizmoMode.Translation;
                gizmo.mode.gizmoComponent.switchRenderableState(RenderingMode.Normal);
                OxyGizmo3D.GizmoMode.Scale.gizmoComponent.switchRenderableState(RenderingMode.None);
            }
            switchC = true;
        }
        if (!OxyUISystem.OxyEventSystem.keyEventDispatcher.getKeys()[GLFW_KEY_C]) {
            switchC = false;
        }
        if (OxyUISystem.OxyEventSystem.mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_LEFT] && ScenePanel.focusedWindow) {
            Vector3f direction = mSelector.getObjectPosRelativeToCamera(
                    ScenePanel.windowSize.x - ScenePanel.offset.x,
                    ScenePanel.windowSize.y - ScenePanel.offset.y,
                    new Vector2f(
                            ScenePanel.mousePos.x - ScenePanel.windowPos.x - ScenePanel.offset.x,
                            ScenePanel.mousePos.y - ScenePanel.windowPos.y - ScenePanel.offset.y),
                    renderer.getCamera()
            );

            OxyEntity e = mSelector.selectObject(scene.getEntities(), OxyRenderer.currentBoundedCamera.getCameraController().origin, direction);
            nStart(OxyGizmo3D.GizmoMode.Translation.gizmoComponent, e);
            nStart(OxyGizmo3D.GizmoMode.Scale.gizmoComponent, e);
            moveEntity(e);
        }
    }

    private void nStart(OxyGizmo3D.GizmoComponent gizmoComponent, OxyEntity e) {

        OxyModel xModel = null, yModel = null, zModel = null;

        if (gizmoComponent instanceof OxyGizmo3D.Translation t) {
            xModel = t.getXModelTranslation();
            yModel = t.getYModelTranslation();
            zModel = t.getZModelTranslation();
            for (OxyModel m : gizmoComponent.models) {
                if (e != null && OxySelectSystem.gizmo.mode == OxyGizmo3D.GizmoMode.Translation) {
                    m.get(RenderableComponent.class).mode = RenderingMode.Normal;
                } else {
                    m.get(RenderableComponent.class).mode = RenderingMode.None;
                }
            }
        } else if (gizmoComponent instanceof OxyGizmo3D.Scaling s) {
            xModel = s.getXModelScale();
            yModel = s.getYModelScale();
            zModel = s.getZModelScale();
            for (OxyModel m : gizmoComponent.models) {
                if (e != null && OxySelectSystem.gizmo.mode == OxyGizmo3D.GizmoMode.Scale) {
                    m.get(RenderableComponent.class).mode = RenderingMode.Normal;
                } else {
                    m.get(RenderableComponent.class).mode = RenderingMode.None;
                }
            }
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

    public void moveEntity(OxyEntity e) {
        OxyGizmoController.setCurrentEntitySelected(e);
    }
}