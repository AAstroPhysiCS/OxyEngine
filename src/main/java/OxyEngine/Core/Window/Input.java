package OxyEngine.Core.Window;

import OxyEngine.OxyEngine;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public final class Input {

    private Input() {

    }

    public static boolean isKeyPressed(KeyCode key) {
        OxyWindow oxyWindow = OxyEngine.getWindowHandle();
        int state = glfwGetKey(oxyWindow.getPointer(), key.value);
        return state == GLFW_PRESS;
    }

    public static boolean isKeyReleased(KeyCode key) {
        OxyWindow oxyWindow = OxyEngine.getWindowHandle();
        int state = glfwGetKey(oxyWindow.getPointer(), key.value);
        return state == GLFW_RELEASE;
    }

    public static boolean isMouseButtonPressed(MouseCode button) {
        OxyWindow oxyWindow = OxyEngine.getWindowHandle();
        int state = glfwGetMouseButton(oxyWindow.getPointer(), button.value);
        return state == GLFW_PRESS;
    }

    public static Vector2f getMousePosition() {
        OxyWindow oxyWindow = OxyEngine.getWindowHandle();
        double[] xPos = new double[1], yPos = new double[1];
        glfwGetCursorPos(oxyWindow.getPointer(), xPos, yPos);
        return new Vector2f((float) xPos[0], (float) yPos[0]);
    }

    public static float getMouseX(){
        return getMousePosition().x;
    }

    public static float getMouseY(){
        return getMousePosition().y;
    }
}
