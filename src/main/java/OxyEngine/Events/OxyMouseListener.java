package OxyEngine.Events;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.UI.Layers.SceneLayer;
import OxyEngineEditor.UI.Selector.Tools.MouseSelector;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector3f;

public interface OxyMouseListener extends OxyEventListener {

    void mouseClicked(OxyEntity selectedEntity, int mouseButton);

    void mouseDragged(OxyEntity selectedEntity, int mouseButton);

    void mouseHovered(OxyEntity hoveredEntity);

    void mouseDown(OxyEntity selectedEntity, int mouseButton);

    void mouseReleased(OxyEntity selectedEntity, int mouseButton);

    void mouseNoAction();

    default void dispatch(OxyEntity entity) {
        Vector3f direction = MouseSelector.getInstance().getObjectPosRelativeToCamera(SceneLayer.windowSize.x - SceneLayer.offset.x, SceneLayer.windowSize.y - SceneLayer.offset.y, new Vector2f(SceneLayer.mousePos.x - SceneLayer.windowPos.x - SceneLayer.offset.x, SceneLayer.mousePos.y - SceneLayer.windowPos.y - SceneLayer.offset.y), OxyRenderer.currentBoundedCamera);
        OxyEntity e = MouseSelector.getInstance().selectObject(entity, OxyRenderer.currentBoundedCamera.getCameraController().origin, direction);
        if (e == null) {
            mouseNoAction();
        }
        dispatchMethods(e);
    }

    private void dispatchMethods(OxyEntity e) {
        mouseHovered(e);
        for (int i = 0; i < 3; i++) { //goes through the imgui supported buttons
            if (ImGui.isMouseDown(i))
                mouseDown(e, i);
            if (ImGui.isMouseDragging(i))
                mouseDragged(e, i);
            if (ImGui.isMouseClicked(i))
                mouseClicked(e, i);
            if(ImGui.isMouseReleased(i))
                mouseReleased(e, i);
        }
    }
}
