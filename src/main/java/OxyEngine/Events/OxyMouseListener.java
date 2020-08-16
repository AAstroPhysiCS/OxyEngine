package OxyEngine.Events;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.UI.Layers.SceneLayer;
import OxyEngineEditor.UI.OxyUISystem;
import OxyEngineEditor.UI.Selector.Tools.MouseSelector;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector3f;

public interface OxyMouseListener extends OxyEventListener {

    void mouseClicked(OxyEntity selectedEntity, OxyMouseEvent mouseEvent);

    void mouseDragged(OxyEntity selectedEntity, OxyMouseEvent mouseEvent);

    void mouseHovered(OxyEntity hoveredEntity);

    void mouseDown(OxyEntity selectedEntity, OxyMouseEvent mouseEvent);

    void mouseNoAction();

    OxyMouseEvent mouseEvent = new OxyMouseEvent();

    default void dispatch(OxyEntity entity) {
        Vector3f direction = MouseSelector.getInstance().getObjectPosRelativeToCamera(SceneLayer.windowSize.x - SceneLayer.offset.x, SceneLayer.windowSize.y - SceneLayer.offset.y, new Vector2f(SceneLayer.mousePos.x - SceneLayer.windowPos.x - SceneLayer.offset.x, SceneLayer.mousePos.y - SceneLayer.windowPos.y - SceneLayer.offset.y), OxyRenderer.currentBoundedCamera);
        OxyEntity e = MouseSelector.getInstance().selectObject(entity, OxyRenderer.currentBoundedCamera.getCameraController().origin, direction);
        if (e == null || !SceneLayer.focusedWindow) {
            mouseNoAction();
        }
        mouseEvent.getLastRayPosition().set(MouseSelector.getInstance().nearFar);
        dispatchMethods(e);
    }

    private void dispatchMethods(OxyEntity e) {
        mouseHovered(e);
        for (int i = 0; i < 3; i++) { //goes through the imgui supported buttons
            if (ImGui.isMouseDown(i)) {
                mouseEvent.buttonId = i;
                mouseDown(e, mouseEvent);
            }
            if (OxyUISystem.OxyEventSystem.mouseButtonDispatcher.getButtons()[i]) {
                mouseEvent.buttonId = i;
                mouseClicked(e, mouseEvent);
            }
            if (ImGui.isMouseDragging(i)) {
                mouseEvent.buttonId = i;
                mouseDragged(e, mouseEvent);
            }
            mouseEvent.buttonId = -1;
        }
    }
}
