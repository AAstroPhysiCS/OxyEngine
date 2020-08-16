package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngineEditor.Sandbox.OxyComponents.BoundingBoxComponent;
import OxyEngineEditor.Sandbox.OxyComponents.IsRenderable;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Scene;
import OxyEngineEditor.UI.Layers.SceneLayer;
import OxyEngineEditor.UI.OxyUISystem;
import OxyEngineEditor.UI.Selector.Tools.MouseSelector;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class OxySelectSystem {

    private static OxyGizmo3D gizmo;
    private static MouseSelector mSelector;

    private final OxyRenderer3D renderer;

    private static OxySelectSystem INSTANCE;

    public static OxySelectSystem getInstance(Scene scene) {
        if (INSTANCE == null) INSTANCE = new OxySelectSystem(scene);
        return INSTANCE;
    }

    private OxySelectSystem(Scene scene) {
        this.renderer = scene.getRenderer();
        gizmo = OxyGizmo3D.getInstance(scene);
        mSelector = MouseSelector.getInstance();
    }

    static Vector3f direction = new Vector3f();

    public void start(Set<OxyEntity> entities, OxyCamera camera) {
        if (OxyUISystem.OxyEventSystem.mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_LEFT] && SceneLayer.focusedWindow) {
            direction = mSelector.getObjectPosRelativeToCamera(SceneLayer.windowSize.x - SceneLayer.offset.x, SceneLayer.windowSize.y - SceneLayer.offset.y, new Vector2f(SceneLayer.mousePos.x - SceneLayer.windowPos.x - SceneLayer.offset.x, SceneLayer.mousePos.y - SceneLayer.windowPos.y - SceneLayer.offset.y), renderer.getCamera());
            OxyEntity e = mSelector.selectObject(entities, camera.getCameraController().origin, direction);
            OxyModel xModel = gizmo.getXModelTranslation();
            OxyModel yModel = gizmo.getYModelTranslation();
            OxyModel zModel = gizmo.getZModelTranslation();

            if (e != null) {
                BoundingBoxComponent c = e.get(BoundingBoxComponent.class);

                TransformComponent xC = xModel.get(TransformComponent.class);
                TransformComponent yC = yModel.get(TransformComponent.class);
                TransformComponent zC = zModel.get(TransformComponent.class);

                xC.position.set(new Vector3f(c.pos()));
                yC.position.set(new Vector3f(c.pos()));
                zC.position.set(new Vector3f(c.pos()));

                //recalculate bounding box, but it is being done in the camera class
            }

            xModel.get(IsRenderable.class).renderable = e != null;
            yModel.get(IsRenderable.class).renderable = e != null;
            zModel.get(IsRenderable.class).renderable = e != null;

            xModel.updateData();
            yModel.updateData();
            zModel.updateData();
            moveEntity(e);
        }
    }

    public void moveEntity(OxyEntity e) {
        OxyGizmoController.setCurrentEntitySelected(e);
    }
}