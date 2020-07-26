package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Events.OxyMouseEvent;
import OxyEngine.Events.OxyMouseListener;
import OxyEngine.Core.OxyObjects.Model.OxyModel;
import OxyEngine.Core.OxyObjects.OxyEntity;
import OxyEngineEditor.UI.OxyUISystem;
import imgui.flag.ImGuiMouseButton;
import org.joml.Vector2d;

import static OxyEngine.Core.Renderer.OxyRenderer.MeshSystem.sandBoxMesh;

/*
 * TODO: REFACTOR IT!
 */
public class OxyGizmoController implements OxyMouseListener {

    OxyModel hoveredGameObject = null;
    OxyColor standardColor = null;
    boolean init = false;

    Vector2d oldMousePos = new Vector2d();
    final OxyModel xAxis;
    final OxyModel yAxis;
    final OxyModel zAxis;

    private static OxyEntity currentEntitySelected;

    OxyGizmoController(OxyModel xAxis, OxyModel yAxis, OxyModel zAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;
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

            if (selectedEntity == xAxis) {
                if (directionVector.x > 0) {
                    xAxis.getPosition().add(0, 0, -0.15f);
                    yAxis.getPosition().add(0, 0, -0.15f);
                    zAxis.getPosition().add(0, 0, -0.15f);
                    currentEntitySelected.getPosition().add(0, 0, -0.15f);
                } else if (directionVector.x < 0) {
                    xAxis.getPosition().add(0, 0, 0.15f);
                    yAxis.getPosition().add(0, 0, 0.15f);
                    zAxis.getPosition().add(0, 0, 0.15f);
                    currentEntitySelected.getPosition().add(0, 0, 0.15f);
                }
            } else if (selectedEntity == yAxis) {
                if (directionVector.y > 0) {
                    xAxis.getPosition().add(0, 0.15f, 0);
                    yAxis.getPosition().add(0, 0.15f, 0);
                    zAxis.getPosition().add(0, 0.15f, 0);
                    currentEntitySelected.getPosition().add(0, 0.15f, 0);
                } else if (directionVector.y < 0) {
                    xAxis.getPosition().add(0, -0.15f, 0);
                    yAxis.getPosition().add(0, -0.15f, 0);
                    zAxis.getPosition().add(0, -0.15f, 0);
                    currentEntitySelected.getPosition().add(0, -0.15f, 0);
                }
            } else if (selectedEntity == zAxis) {
                if (directionVector.x > 0) {
                    xAxis.getPosition().add(0.15f, 0, 0);
                    yAxis.getPosition().add(0.15f, 0, 0);
                    zAxis.getPosition().add(0.15f, 0, 0);
                    currentEntitySelected.getPosition().add(0.15f, 0, 0);
                } else if (directionVector.x < 0) {
                    xAxis.getPosition().add(-0.15f, 0, 0);
                    yAxis.getPosition().add(-0.15f, 0, 0);
                    zAxis.getPosition().add(-0.15f, 0, 0);
                    currentEntitySelected.getPosition().add(-0.15f, 0, 0);
                }
            }
            currentEntitySelected.updateData();
            sandBoxMesh.obj.updateSingleEntityData(currentEntitySelected);
            xAxis.updateData();
            yAxis.updateData();
            zAxis.updateData();
        }
    }

    @Override
    public void mouseHovered(OxyEntity hoveredEntity, OxyMouseEvent mouseEvent) {
        if (hoveredEntity instanceof OxyModel g) {
            if (!init) {
                try {
                    standardColor = (OxyColor) g.getColor().clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                init = true;
            }
            hoveredGameObject = g;
            hoveredGameObject.getColor().setColorRGBA(new float[]{1.0f, 1.0f, 0.0f, 1.0f});
        }
    }

    @Override
    public void mouseDragged(OxyEntity selectedEntity, OxyMouseEvent mouseEvent) {
    }

    @Override
    public void mouseNoAction() {
        if (hoveredGameObject != null) {
            hoveredGameObject.getColor().setColorRGBA(standardColor.getNumbers());
        }
    }
}
