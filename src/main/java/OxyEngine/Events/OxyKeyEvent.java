package OxyEngine.Events;

import OxyEngine.Core.Layers.UILayer;

import static OxyEngineEditor.Scene.Scene.*;
import static org.lwjgl.glfw.GLFW.*;

public class OxyKeyEvent extends OxyEvent {

    public OxyKeyEvent() {
        super("Key Event");
    }

    @Override
    public EventType getEventType() {
        return EventType.KeyEvent;
    }

    public void onKeyPressed() {
        if (glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS &&
                glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_O) == GLFW_PRESS) {
            openScene();
        }

        if (glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS &&
                glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_S) == GLFW_PRESS) {
            saveScene();
        }

        if (glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS &&
                glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_N) == GLFW_PRESS) {
            newScene();
        }
    }
}
