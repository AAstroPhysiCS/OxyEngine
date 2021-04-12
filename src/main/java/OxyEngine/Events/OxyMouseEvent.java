package OxyEngine.Events;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Scene.SceneRuntime;
import OxyEngineEditor.UI.Gizmo.OxySelectHandler;
import OxyEngineEditor.UI.Panels.ScenePanel;

import static OxyEngine.System.OxyEventSystem.mouseButtonDispatcher;
import static OxyEngineEditor.UI.Panels.ScenePanel.editorCameraEntity;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class OxyMouseEvent extends OxyEvent {

    public OxyMouseEvent() {
        super("Mouse Event");
    }

    @Override
    public EventType getEventType() {
        return EventType.MouseEvent;
    }

    public void onMousePressed() {
        if (mouseButtonDispatcher.getButtonState(GLFW_MOUSE_BUTTON_1) == GLFW_PRESS && ScenePanel.hoveredWindow
                && SceneRuntime.currentBoundedCamera.equals(editorCameraEntity.get(OxyCamera.class)))
            OxySelectHandler.startPicking();
    }
}
