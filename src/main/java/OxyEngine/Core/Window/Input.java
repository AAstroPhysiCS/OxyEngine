package OxyEngine.Core.Window;

import OxyEngine.OxyEngine;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public final class Input {

    private Input() {

    }

    public static boolean isKeyPressed(KeyCode key) {
        Window window = OxyEngine.getWindowHandle();
        int state = glfwGetKey(window.getPointer(), key.value);
        return state == GLFW_PRESS;
    }

    public static boolean isKeyReleased(KeyCode key) {
        Window window = OxyEngine.getWindowHandle();
        int state = glfwGetKey(window.getPointer(), key.value);
        return state == GLFW_RELEASE;
    }

    public static boolean isMouseButtonPressed(MouseCode button) {
        Window window = OxyEngine.getWindowHandle();
        int state = glfwGetMouseButton(window.getPointer(), button.value);
        return state == GLFW_PRESS;
    }

    public static Vector2f getMousePosition() {
        Window window = OxyEngine.getWindowHandle();
        double[] xPos = new double[1], yPos = new double[1];
        glfwGetCursorPos(window.getPointer(), xPos, yPos);
        return new Vector2f((float) xPos[0], (float) yPos[0]);
    }

    public static float getMouseX() {
        return getMousePosition().x;
    }

    public static float getMouseY() {
        return getMousePosition().y;
    }
}
