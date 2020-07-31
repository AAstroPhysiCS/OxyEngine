package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.OxyGameObject;
import OxyEngineEditor.Sandbox.Scene.OxyModel;
import OxyEngineEditor.Sandbox.Scene.Scene;
import OxyEngineEditor.UI.Layers.SceneLayer;
import OxyEngineEditor.UI.OxyUISystem;
import OxyEngineEditor.UI.Selector.Tools.MouseSelector;
import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_3;

public class OxySelectSystem {

    private static OxyGizmo3D axis;
    private static MouseSelector mSelector;

    private final OxyRenderer3D renderer;

    private static OxySelectSystem INSTANCE;

    public static OxySelectSystem getInstance(Scene scene) {
        if (INSTANCE == null) INSTANCE = new OxySelectSystem(scene);
        return INSTANCE;
    }

    private OxySelectSystem(Scene scene) {
        this.renderer = scene.getRenderer();
        axis = OxyGizmo3D.getInstance(scene);
        mSelector = MouseSelector.getInstance();
    }

    static Vector3f direction = new Vector3f();

    public void start(Set<OxyEntity> entities, OxyCamera camera) {
        if (OxyUISystem.OxyEventSystem.mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_3] && SceneLayer.focusedWindow) {
            ImVec2 mousePos = new ImVec2();
            ImGui.getMousePos(mousePos);
            direction = mSelector.getObjectPosRelativeToCamera(SceneLayer.width, SceneLayer.height, new Vector2f(mousePos.x - SceneLayer.x, mousePos.y - SceneLayer.y), renderer.getCamera());
            OxyEntity e = mSelector.selectObject(entities, camera.getCameraController().origin, direction);
            if (e != null) {

                TransformComponent c = (TransformComponent) e.get(TransformComponent.class);

                OxyModel xModel = axis.getXModel();
                OxyModel yModel = axis.getYModel();
                OxyModel zModel = axis.getZModel();

                TransformComponent xC = (TransformComponent) xModel.get(TransformComponent.class);
                TransformComponent yC = (TransformComponent) yModel.get(TransformComponent.class);
                TransformComponent zC = (TransformComponent) zModel.get(TransformComponent.class);

                xC.position.set(new Vector3f(c.position).add(0, 0, -3));
                yC.position.set(new Vector3f(c.position).add(0, -3, 0));
                zC.position.set(new Vector3f(c.position).add(-3, 0, 0));

                xC.rotation.set(Math.toRadians(180), 0, 0);
                yC.rotation.set(Math.toRadians(-90), Math.toRadians(-180), 0);
                zC.rotation.set(0, Math.toRadians(-90), 0);

                xModel.updateData();
                yModel.updateData();
                zModel.updateData();
                moveGameObject(e);
            }
        }
        axis.render(camera);
    }

    public void moveGameObject(OxyEntity e) {
        OxyGizmoController.setCurrentEntitySelected(e);
    }
}