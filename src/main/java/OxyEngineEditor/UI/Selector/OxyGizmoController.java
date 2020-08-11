package OxyEngineEditor.UI.Selector;

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
    final OxyModel xAxis;
    final OxyModel yAxis;
    final OxyModel zAxis;

    final Scene scene;

    private static OxyEntity currentEntitySelected;

    OxyGizmoController(Scene scene, OxyModel xAxis, OxyModel yAxis, OxyModel zAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;
        this.scene = scene;
    }

    public static void setCurrentEntitySelected(OxyEntity currentEntitySelected) {
        OxyGizmoController.currentEntitySelected = currentEntitySelected;
    }

    @Override
    public void mouseClicked(OxyEntity selectedEntity, OxyMouseEvent mouseEvent) {
        oldMousePos = new Vector2d(OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getXPos(), OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getYPos());
    }

    @Override
    public void mouseDown(OxyEntity selectedEntity, OxyMouseEvent mouseEvent) {
        if (mouseEvent.getButton() == ImGuiMouseButton.Right && currentEntitySelected != null) {
            Vector2d nowMousePos = new Vector2d(OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getXPos(), OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getYPos());
            Vector2d directionVector = nowMousePos.sub(oldMousePos);

            TransformComponent xC = xAxis.get(TransformComponent.class);
            TransformComponent yC = yAxis.get(TransformComponent.class);
            TransformComponent zC = zAxis.get(TransformComponent.class);

            BoundingBoxComponent xCB = xAxis.get(BoundingBoxComponent.class);
            BoundingBoxComponent yCB = yAxis.get(BoundingBoxComponent.class);
            BoundingBoxComponent zCB = zAxis.get(BoundingBoxComponent.class);

            TransformComponent currC = currentEntitySelected.get(TransformComponent.class);
            BoundingBoxComponent currCB = currentEntitySelected.get(BoundingBoxComponent.class);

            if(xCB != null) {
                if (selectedEntity == xAxis) {
                    if (directionVector.x > 0) {
                        xC.position.add(0, 0, -0.15f);
                        yC.position.add(0, 0, -0.15f);
                        zC.position.add(0, 0, -0.15f);
                        xCB.pos().add(0, 0, -0.15f);
                        yCB.pos().add(0, 0, -0.15f);
                        zCB.pos().add(0, 0, -0.15f);
                        currC.position.add(0, 0, -0.15f);
                        currCB.pos().add(0, 0, -0.15f);
                    } else if (directionVector.x < 0) {
                        xC.position.add(0, 0, 0.15f);
                        yC.position.add(0, 0, 0.15f);
                        zC.position.add(0, 0, 0.15f);
                        xCB.pos().add(0, 0, 0.15f);
                        yCB.pos().add(0, 0, 0.15f);
                        zCB.pos().add(0, 0, 0.15f);
                        currC.position.add(0, 0, 0.15f);
                        currCB.pos().add(0, 0, 0.15f);
                    }
                } else if (selectedEntity == yAxis) {
                    if (directionVector.y > 0) {
                        xC.position.add(0, 0.15f, 0);
                        yC.position.add(0, 0.15f, 0);
                        zC.position.add(0, 0.15f, 0);
                        xCB.pos().add(0, 0.15f, 0);
                        yCB.pos().add(0, 0.15f, 0);
                        zCB.pos().add(0, 0.15f, 0);
                        currC.position.add(0, 0.15f, 0);
                        currCB.pos().add(0, 0.15f, 0);
                    } else if (directionVector.y < 0) {
                        xC.position.add(0, -0.15f, 0);
                        yC.position.add(0, -0.15f, 0);
                        zC.position.add(0, -0.15f, 0);
                        xCB.pos().add(0, -0.15f, 0);
                        yCB.pos().add(0, -0.15f, 0);
                        zCB.pos().add(0, -0.15f, 0);
                        currC.position.add(0, -0.15f, 0);
                        currCB.pos().add(0, -0.15f, 0);
                    }
                } else if (selectedEntity == zAxis) {
                    if (directionVector.x > 0) {
                        xC.position.add(0.15f, 0, 0);
                        yC.position.add(0.15f, 0, 0);
                        zC.position.add(0.15f, 0, 0);
                        xCB.pos().add(0.15f, 0, 0);
                        yCB.pos().add(0.15f, 0, 0);
                        zCB.pos().add(0.15f, 0, 0);
                        currC.position.add(0.15f, 0, 0);
                        currCB.pos().add(0.15f, 0, 0);
                    } else if (directionVector.x < 0) {
                        xC.position.add(-0.15f, 0, 0);
                        yC.position.add(-0.15f, 0, 0);
                        zC.position.add(-0.15f, 0, 0);
                        xCB.pos().add(-0.15f, 0, 0);
                        yCB.pos().add(-0.15f, 0, 0);
                        zCB.pos().add(-0.15f, 0, 0);
                        currC.position.add(-0.15f, 0, 0);
                        currCB.pos().add(-0.15f, 0, 0);
                    }
                }
            }
            currentEntitySelected.updateData();
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
