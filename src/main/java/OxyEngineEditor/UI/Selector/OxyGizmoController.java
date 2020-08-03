package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Events.OxyMouseEvent;
import OxyEngine.Events.OxyMouseListener;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.OxyModel;
import OxyEngineEditor.Sandbox.Scene.Scene;
import OxyEngineEditor.UI.OxyUISystem;
import imgui.flag.ImGuiMouseButton;
import org.joml.Vector2d;

/*
 * TODO: REFACTOR IT!
 */
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

            TransformComponent xC = (TransformComponent) xAxis.get(TransformComponent.class);
            TransformComponent yC = (TransformComponent) yAxis.get(TransformComponent.class);
            TransformComponent zC = (TransformComponent) zAxis.get(TransformComponent.class);
            TransformComponent currC = (TransformComponent) currentEntitySelected.get(TransformComponent.class);
            if (selectedEntity == xAxis) {
                if (directionVector.x > 0) {
                    xC.position.add(0, 0, -0.15f);
                    yC.position.add(0, 0, -0.15f);
                    zC.position.add(0, 0, -0.15f);
                    currC.position.add(0, 0, -0.15f);
                } else if (directionVector.x < 0) {
                    xC.position.add(0, 0, 0.15f);
                    yC.position.add(0, 0, 0.15f);
                    zC.position.add(0, 0, 0.15f);
                    currC.position.add(0, 0, 0.15f);
                }
            } else if (selectedEntity == yAxis) {
                if (directionVector.y > 0) {
                    xC.position.add(0, 0.15f, 0);
                    yC.position.add(0, 0.15f, 0);
                    zC.position.add(0, 0.15f, 0);
                    currC.position.add(0, 0.15f, 0);
                } else if (directionVector.y < 0) {
                    xC.position.add(0, -0.15f, 0);
                    yC.position.add(0, -0.15f, 0);
                    zC.position.add(0, -0.15f, 0);
                    currC.position.add(0, -0.15f, 0);
                }
            } else if (selectedEntity == zAxis) {
                if (directionVector.x > 0) {
                    xC.position.add(0.15f, 0, 0);
                    yC.position.add(0.15f, 0, 0);
                    zC.position.add(0.15f, 0, 0);
                    currC.position.add(0.15f, 0, 0);
                } else if (directionVector.x < 0) {
                    xC.position.add(-0.15f, 0, 0);
                    yC.position.add(-0.15f, 0, 0);
                    zC.position.add(-0.15f, 0, 0);
                    currC.position.add(-0.15f, 0, 0);
                }
            }
            currentEntitySelected.updateData();
            ((Mesh) currentEntitySelected.get(Mesh.class)).updateSingleEntityData(scene, currentEntitySelected);
            xAxis.updateData();
            yAxis.updateData();
            zAxis.updateData();
        }
    }

    @Override
    public void mouseHovered(OxyEntity hoveredEntity, OxyMouseEvent mouseEvent) {
        if (hoveredEntity instanceof OxyModel) {
            if (!init) {
                OxyColor color = (OxyColor) hoveredEntity.get(OxyColor.class);
                try {
                    standardColor = (OxyColor) color.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                init = true;
            }
            hoveredGameObject = hoveredEntity;
            OxyColor hoveredColor = (OxyColor) hoveredGameObject.get(OxyColor.class);
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
            OxyColor hoveredColor = (OxyColor) hoveredGameObject.get(OxyColor.class);
            hoveredColor.setColorRGBA(standardColor.getNumbers());
            hoveredGameObject.updateData();
        }
    }
}
