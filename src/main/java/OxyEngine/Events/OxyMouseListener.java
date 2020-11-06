package OxyEngine.Events;

import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.SceneRuntime;
import OxyEngineEditor.UI.Panels.ScenePanel;
import OxyEngineEditor.UI.Selector.Tools.MouseSelector;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Set;

public interface OxyMouseListener extends OxyEventListener {

    void mouseClicked(OxyEntity selectedEntity, int mouseButton);

    void mouseDragged(OxyEntity selectedEntity, int mouseButton);

    void mouseHovered(OxyEntity hoveredEntity);

    void mouseDown(OxyEntity selectedEntity, int mouseButton);

    void mouseReleased(OxyEntity selectedEntity, int mouseButton);

    default void dispatch(Set<OxyEntity> entities) {
        Vector3f direction = MouseSelector.getInstance().getObjectPosRelativeToCamera(ScenePanel.windowSize.x - ScenePanel.offset.x, ScenePanel.windowSize.y - ScenePanel.offset.y, new Vector2f(ScenePanel.mousePos.x - ScenePanel.windowPos.x - ScenePanel.offset.x, ScenePanel.mousePos.y - ScenePanel.windowPos.y - ScenePanel.offset.y), SceneRuntime.currentBoundedCamera);
        OxyEntity e = MouseSelector.getInstance().selectObjectGizmo(entities, SceneRuntime.currentBoundedCamera.getCameraController().origin, direction);
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
            if (ImGui.isMouseReleased(i))
                mouseReleased(e, i);
        }
    }
}
