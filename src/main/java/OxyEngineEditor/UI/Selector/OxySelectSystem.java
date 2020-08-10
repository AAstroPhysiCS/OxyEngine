package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
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

    private static OxyGizmo3D gizmo;
    private static MouseSelector mSelector;

    private final OxyRenderer3D renderer;

    private static OxySelectSystem INSTANCE;

    public static OxySelectSystem getInstance(Scene scene, OxyShader shader) {
        if (INSTANCE == null) INSTANCE = new OxySelectSystem(scene, shader);
        return INSTANCE;
    }

    private OxySelectSystem(Scene scene, OxyShader shader) {
        this.renderer = scene.getRenderer();
        gizmo = OxyGizmo3D.getInstance(scene, shader);
        mSelector = MouseSelector.getInstance();
    }

    static Vector3f direction = new Vector3f();

    public void start(Set<OxyEntity> entities, OxyCamera camera) {
        if (OxyUISystem.OxyEventSystem.mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_3] && SceneLayer.focusedWindow) {
            ImVec2 mousePos = new ImVec2();
            ImGui.getMousePos(mousePos);
            direction = mSelector.getObjectPosRelativeToCamera(SceneLayer.width, SceneLayer.height, new Vector2f(mousePos.x - SceneLayer.x, mousePos.y - SceneLayer.y), renderer.getCamera());
            OxyEntity e = mSelector.selectObject(entities, camera.getCameraController().origin, direction);

            if (e != null && !((SelectedComponent)e.get(SelectedComponent.class)).fixedValue) {

                TransformComponent c = (TransformComponent) e.get(TransformComponent.class);

                OxyModel xModel = gizmo.getXModel();
                OxyModel yModel = gizmo.getYModel();
                OxyModel zModel = gizmo.getZModel();

                TransformComponent xC = (TransformComponent) xModel.get(TransformComponent.class);
                TransformComponent yC = (TransformComponent) yModel.get(TransformComponent.class);
                TransformComponent zC = (TransformComponent) zModel.get(TransformComponent.class);

                xC.position.set(new Vector3f(c.position));
                yC.position.set(new Vector3f(c.position));
                zC.position.set(new Vector3f(c.position));

                xModel.updateData();
                yModel.updateData();
                zModel.updateData();
                moveEntity(e);
            }
        }
    }

    public void moveEntity(OxyEntity e) {
        OxyGizmoController.setCurrentEntitySelected(e);
    }
}