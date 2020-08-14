package OxyEngine.Events;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.UI.Layers.SceneLayer;
import OxyEngineEditor.UI.OxyUISystem;
import OxyEngineEditor.UI.Selector.Tools.MouseSelector;
import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Vector2f;
import org.joml.Vector3f;

public interface OxyMouseListener extends OxyEventListener {

    void mouseClicked(OxyEntity selectedEntity, OxyMouseEvent mouseEvent);

    void mouseDragged(OxyEntity selectedEntity, OxyMouseEvent mouseEvent);

    void mouseHovered(OxyEntity hoveredEntity, OxyMouseEvent mouseEvent);

    void mouseDown(OxyEntity selectedEntity, OxyMouseEvent mouseEvent);

    void mouseNoAction();

    OxyMouseEvent mouseEvent = new OxyMouseEvent();

    default void dispatch(OxyEntity entity) {
        ImVec2 mousePos = new ImVec2();
        ImGui.getMousePos(mousePos);
        Vector3f direction = MouseSelector.getInstance().getObjectPosRelativeToCamera(SceneLayer.width, SceneLayer.height, new Vector2f(mousePos.x - SceneLayer.x, mousePos.y - SceneLayer.y), OxyRenderer.currentBoundedCamera);
        OxyEntity e = MouseSelector.getInstance().selectObject(entity, OxyRenderer.currentBoundedCamera.getCameraController().origin, direction);
        if (e == null || !SceneLayer.focusedWindow) {
            mouseNoAction();
            return;
        }
        mouseEvent.getLastRayPosition().set(MouseSelector.getInstance().nearFar);
        if (e.equals(entity)) dispatchMethods(e);
    }

    private void dispatchMethods(OxyEntity e) {
        if (ImGui.isAnyMouseDown())
            mouseDown(e, mouseEvent);
        for (int i = 0; i < 3; i++) { //goes through the imgui supported buttons (0 to 2)
            if (OxyUISystem.OxyEventSystem.mouseButtonDispatcher.getButtons()[i]) {
                if (ImGui.isMouseClicked(i))
                    mouseClicked(e, mouseEvent);
                if (ImGui.isMouseDragging(i))
                    mouseDragged(e, mouseEvent);
                mouseEvent.buttonId = i;
            }
        }
        mouseHovered(e, mouseEvent);
    }
}
