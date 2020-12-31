package OxyEngine.Events;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Layers.UILayer;

import static org.lwjgl.glfw.GLFW.*;

public class OxyMouseEvent extends OxyEvent {

    public OxyMouseEvent() {
        super("Mouse Event");
    }

    @Override
    public EventType getEventType() {
        return EventType.MouseEvent;
    }

    public void onMousePressed(){
        if(glfwGetMouseButton(UILayer.windowHandle.getPointer(), GLFW_MOUSE_BUTTON_1) == GLFW_PRESS)
            SceneLayer.getInstance().startPicking();
    }
}
