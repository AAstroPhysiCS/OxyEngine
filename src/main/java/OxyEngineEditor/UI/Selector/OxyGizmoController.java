package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Events.OxyMouseEvent;
import OxyEngine.Events.OxyMouseListener;
import OxyEngineEditor.Sandbox.OxyComponents.BoundingBoxComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Scene;
import OxyEngineEditor.UI.OxyUISystem;
import imgui.flag.ImGuiMouseButton;
import org.joml.Vector2d;

public class OxyGizmoController implements OxyMouseListener {

    OxyEntity hoveredGameObject = null;
    OxyColor standardColor = null;
    boolean init = false;

    Vector2d oldMousePos = new Vector2d();

    final OxyGizmo3D gizmo;
    final Scene scene;

    boolean pressedX, pressedY, pressedZ;

    private static OxyEntity currentEntitySelected;

    OxyGizmoController(Scene scene, OxyGizmo3D gizmo) {
        this.gizmo = gizmo;
        this.scene = scene;
    }

    public static void setCurrentEntitySelected(OxyEntity currentEntitySelected) {
        OxyGizmoController.currentEntitySelected = currentEntitySelected;
    }

    @Override
    public void mouseClicked(OxyEntity selectedEntity, OxyMouseEvent mouseEvent) {
    }

    @Override
    public void mouseDown(OxyEntity selectedEntity, OxyMouseEvent mouseEvent) {
        if (mouseEvent.getButton() == ImGuiMouseButton.Right) {
            OxyModel xAxis = gizmo.getXModel();
            OxyModel yAxis = gizmo.getYModel();
            OxyModel zAxis = gizmo.getZModel();

            TransformComponent xC = xAxis.get(TransformComponent.class);
            TransformComponent yC = yAxis.get(TransformComponent.class);
            TransformComponent zC = zAxis.get(TransformComponent.class);

            BoundingBoxComponent xCB = xAxis.get(BoundingBoxComponent.class);
            BoundingBoxComponent yCB = yAxis.get(BoundingBoxComponent.class);
            BoundingBoxComponent zCB = zAxis.get(BoundingBoxComponent.class);

            TransformComponent currC = currentEntitySelected.get(TransformComponent.class);
            BoundingBoxComponent currCB = currentEntitySelected.get(BoundingBoxComponent.class);

            Vector2d nowMousePos = new Vector2d(OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getXPos(), OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getYPos());
            Vector2d delta = nowMousePos.sub(oldMousePos);
            float mouseSpeed = OxyRenderer.currentBoundedCamera.getCameraController().getMouseSpeed();
            float deltaX = (float) ((delta.x * mouseSpeed) * xC.scale); // xC bcs x,y,z has the same scale, so it does not matter!
            float deltaY = (float) ((delta.y * mouseSpeed) * xC.scale);
            System.out.println("X: " + deltaX);
            System.out.println("Y: " + deltaY);

            if (deltaX <= -1f || deltaX >= 1f) deltaX = 0; // for safety reasons
            if (deltaY <= -1f || deltaY >= 1f) deltaY = 0;

            if (selectedEntity == zAxis) {
                pressedZ = true;
                xC.position.add(0, 0, -deltaX);
                yC.position.add(0, 0, -deltaX);
                zC.position.add(0, 0, -deltaX);
                xCB.pos().add(0, 0, -deltaX);
                yCB.pos().add(0, 0, -deltaX);
                zCB.pos().add(0, 0, -deltaX);
                currC.position.add(0, 0, -deltaX);
                currCB.pos().add(0, 0, -deltaX);
            } else if (selectedEntity == yAxis) {
                pressedY = true;
                xC.position.add(0, deltaY, 0);
                yC.position.add(0, deltaY, 0);
                zC.position.add(0, deltaY, 0);
                xCB.pos().add(0, deltaY, 0);
                yCB.pos().add(0, deltaY, 0);
                zCB.pos().add(0, deltaY, 0);
                currC.position.add(0, deltaY, 0);
                currCB.pos().add(0, deltaY, 0);
            } else if (selectedEntity == xAxis) {
                pressedX = true;
                xC.position.add(deltaX, 0, 0);
                yC.position.add(deltaX, 0, 0);
                zC.position.add(deltaX, 0, 0);
                xCB.pos().add(deltaX, 0, 0);
                yCB.pos().add(deltaX, 0, 0);
                zCB.pos().add(deltaX, 0, 0);
                currC.position.add(deltaX, 0, 0);
                currCB.pos().add(deltaX, 0, 0);
            }
            if (currentEntitySelected != null) currentEntitySelected.updateData();
            xAxis.updateData();
            yAxis.updateData();
            zAxis.updateData();
        }
    }

    @Override
    public void mouseHovered(OxyEntity hoveredEntity, OxyMouseEvent mouseEvent) {
        if (hoveredEntity instanceof OxyModel) {
            if (!init) {
                OxyColor color = hoveredEntity.get(OxyColor.class);
                try {
                    standardColor = (OxyColor) color.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                init = true;
            }
            hoveredGameObject = hoveredEntity;
            OxyColor hoveredColor = hoveredGameObject.get(OxyColor.class);
            hoveredColor.setColorRGBA(new float[]{1.0f, 1.0f, 0.0f, 1.0f});
            hoveredGameObject.updateData();
        }
    }

    @Override
    public void mouseDragged(OxyEntity selectedEntity, OxyMouseEvent mouseEvent) {
        oldMousePos = new Vector2d(OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getXPos(), OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getYPos());
    }

    @Override
    public void mouseNoAction() {
        if (hoveredGameObject != null) {
            OxyColor hoveredColor = hoveredGameObject.get(OxyColor.class);
            hoveredColor.setColorRGBA(standardColor.getNumbers());
            hoveredGameObject.updateData();
        }
    }
}
