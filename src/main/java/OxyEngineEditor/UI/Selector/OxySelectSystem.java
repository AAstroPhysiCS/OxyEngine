package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Components.RenderableComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Model.OxyModel;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.UI.Panels.ScenePanel;
import OxyEngineEditor.UI.OxyUISystem;
import OxyEngineEditor.UI.Selector.Tools.MouseSelector;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class OxySelectSystem {

    private static OxyGizmo3D gizmo;
    private static MouseSelector mSelector;

    private final OxyRenderer3D renderer;

    private static OxySelectSystem INSTANCE;

    public static OxySelectSystem getInstance(WindowHandle windowHandle, Scene scene) {
        if (INSTANCE == null) INSTANCE = new OxySelectSystem(windowHandle, scene);
        return INSTANCE;
    }

    private OxySelectSystem(WindowHandle windowHandle, Scene scene) {
        this.renderer = scene.getRenderer();
        gizmo = OxyGizmo3D.getInstance(windowHandle, scene);
        mSelector = MouseSelector.getInstance();
    }

    static boolean switchC = false;

    public void start(Set<OxyEntity> entities, OxyCamera camera) {
        if (OxyUISystem.OxyEventSystem.keyEventDispatcher.getKeys()[GLFW_KEY_C] && ScenePanel.focusedWindow && !switchC) {
            if (gizmo.mode == OxyGizmo3D.GizmoMode.Translation) {
                gizmo.mode = OxyGizmo3D.GizmoMode.Scale;
                gizmo.mode.component.switchRenderableState(true);
                OxyGizmo3D.GizmoMode.Translation.component.switchRenderableState(false);
            } else {
                gizmo.mode = OxyGizmo3D.GizmoMode.Translation;
                gizmo.mode.component.switchRenderableState(true);
                OxyGizmo3D.GizmoMode.Scale.component.switchRenderableState(false);
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

            OxyEntity e = mSelector.selectObject(entities, camera.getCameraController().origin, direction);
            nStart(OxyGizmo3D.GizmoMode.Translation.component, e);
            nStart(OxyGizmo3D.GizmoMode.Scale.component, e);
            moveEntity(e);
        }
    }

    private void nStart(OxyGizmo3D.Component component, OxyEntity e) {

        OxyModel xModel = null, yModel = null, zModel = null;

        if (component instanceof OxyGizmo3D.Translation t) {
            xModel = t.getXModelTranslation();
            yModel = t.getYModelTranslation();
            zModel = t.getZModelTranslation();
            for (OxyModel m : component.models)
                m.get(RenderableComponent.class).renderable = e != null && gizmo.mode == OxyGizmo3D.GizmoMode.Translation;
        } else if (component instanceof OxyGizmo3D.Scaling s) {
            xModel = s.getXModelScale();
            yModel = s.getYModelScale();
            zModel = s.getZModelScale();
            for (OxyModel m : component.models)
                m.get(RenderableComponent.class).renderable = e != null && gizmo.mode == OxyGizmo3D.GizmoMode.Scale;
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