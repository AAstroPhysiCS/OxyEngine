package OxyEngine.System;

import OxyEngine.Events.GLFW.GLFWEventDispatcher;
import OxyEngine.Events.OxyEventDispatcher;

public record OxyEventSystem() {
    public static GLFWEventDispatcher.MouseEvent mouseButtonDispatcher;
    public static GLFWEventDispatcher.MouseCursorPosEvent mouseCursorPosDispatcher;
    public static GLFWEventDispatcher.KeyEvent keyEventDispatcher;
    public static GLFWEventDispatcher.MouseScrollEvent mouseScrollDispatcher;

    //Native
    public static OxyEventDispatcher dispatcher;
}
