package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.OxyObjects.Model.OxyModel;
import OxyEngine.Core.OxyObjects.OxyEntity;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngineEditor.UI.Layers.SceneLayer;
import OxyEngineEditor.UI.OxyUISystem;
import OxyEngineEditor.UI.Selector.Tools.MouseSelector;
import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_3;

public class OxySelectSystem {

    private static OxyGizmo3D axis;
    private static MouseSelector mSelector;

    private final OxyRenderer3D renderer;

    private static OxySelectSystem INSTANCE;

    public static OxySelectSystem getInstance(OxyRenderer3D renderer) {
        if (INSTANCE == null) INSTANCE = new OxySelectSystem(renderer);
        return INSTANCE;
    }

    private OxySelectSystem(OxyRenderer3D renderer) {
        this.renderer = renderer;
        axis = OxyGizmo3D.getInstance(renderer);
        mSelector = MouseSelector.getInstance();
    }

    static Vector3f direction = new Vector3f();

    public void start(List<OxyEntity> entities, OxyCamera camera) {
        if (OxyUISystem.OxyEventSystem.mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_3] && SceneLayer.focusedWindow) {
            ImVec2 mousePos = new ImVec2();
            ImGui.getMousePos(mousePos);
            direction = mSelector.getObjectPosRelativeToCamera(SceneLayer.width, SceneLayer.height, new Vector2f(mousePos.x - SceneLayer.x, mousePos.y - SceneLayer.y), renderer.getCamera());
            OxyEntity e = mSelector.selectObject(entities, camera.getCameraController().origin, direction);
            if (e != null) {
                Vector3f ePos = new Vector3f(e.getPosition());

                OxyModel xModel = axis.getXModel();
                OxyModel yModel = axis.getYModel();
                OxyModel zModel = axis.getZModel();

                xModel.getPosition().set(new Vector3f(ePos).add(0, 0, -3));
                yModel.getPosition().set(new Vector3f(ePos).add(0, -3, 0));
                zModel.getPosition().set(new Vector3f(ePos).add(-3, 0, 0));

                xModel.getRotation().set(180, 0, 0);
                yModel.getRotation().set(-90, -180, 0);
                zModel.getRotation().set(0, -90, 0);

                xModel.updateData();
                yModel.updateData();
                zModel.updateData();
                moveController(e);
            }
        }
        axis.render(camera);
    }

    public void moveController(OxyEntity e){
        OxyGizmoController.setCurrentEntitySelected(e);
    }
}