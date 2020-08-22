package OxyEngine.Events.GLFW;

import imgui.ImGui;
import imgui.ImGuiIO;
import org.lwjgl.glfw.*;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.glfw.GLFW.*;

public interface GLFWEventDispatcher {

    final class KeyEvent extends GLFWKeyCallback implements GLFWEventDispatcher {

        private final boolean[] keys = new boolean[1000];

        private final ImGuiIO io;

        public KeyEvent(ImGuiIO io) {
            this.io = io;
        }

        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            keys[key] = action != GLFW.GLFW_RELEASE;

            assert io != null : oxyAssert("ImGui hasn't been instantiated");

            if (action == GLFW_PRESS) {
                io.setKeysDown(key, true);
            } else if (action == GLFW_RELEASE) {
                io.setKeysDown(key, false);
            }

            io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
            io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
            io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
            io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));
        }

        public boolean[] getKeys() {
            return keys;
        }
    }

    final class KeyCharEvent extends GLFWCharCallback implements GLFWEventDispatcher {

        private final ImGuiIO io;

        public KeyCharEvent(ImGuiIO io) {
            this.io = io;
        }

        @Override
        public void invoke(long window, int c) {
            if (c != GLFW_KEY_DELETE) {
                io.addInputCharacter(c);
            }
        }
    }

    final class MouseEvent extends GLFWMouseButtonCallback implements GLFWEventDispatcher {

        private final boolean[] buttons = new boolean[10];

        private final ImGuiIO io;

        public MouseEvent(ImGuiIO io) {
            this.io = io;
        }

        @Override
        public void invoke(long window, int button, int action, int mods) {
            buttons[button] = action != GLFW_RELEASE;

            io.setMouseDown(0, button == GLFW_MOUSE_BUTTON_1 && action != GLFW_RELEASE);
            io.setMouseDown(1, button == GLFW_MOUSE_BUTTON_2 && action != GLFW_RELEASE);
            io.setMouseDown(2, button == GLFW_MOUSE_BUTTON_3 && action != GLFW_RELEASE);
            io.setMouseDown(3, button == GLFW_MOUSE_BUTTON_4 && action != GLFW_RELEASE);
            io.setMouseDown(4, button == GLFW_MOUSE_BUTTON_5 && action != GLFW_RELEASE);

            if (!io.getWantCaptureMouse() && io.getMouseDown(1)) {
                ImGui.setWindowFocus(null);
            }
        }

        public boolean[] getButtons() {
            return buttons;
        }
    }

    final class MouseScrollEvent extends GLFWScrollCallback implements GLFWEventDispatcher {

        private double xOffset, yOffset;
        private final ImGuiIO io;

        public MouseScrollEvent(ImGuiIO io){
            this.io = io;
        }

        @Override
        public void invoke(long window, double xoffset, double yoffset) {
            this.xOffset = xoffset;
            this.yOffset = yoffset;

            io.setMouseWheelH(io.getMouseWheelH() + (float) xoffset);
            io.setMouseWheel(io.getMouseWheel() + (float) yoffset);
        }

        public double getXOffset() {
            return xOffset;
        }

        public double getYOffset() {
            return yOffset;
        }
    }

    final class MouseCursorPosEvent extends GLFWCursorPosCallback implements GLFWEventDispatcher {

        private double xPos, yPos;

        @Override
        public void invoke(long window, double xpos, double ypos) {
            xPos = xpos;
            yPos = ypos;
        }

        public double getXPos() {
            return xPos;
        }

        public double getYPos() {
            return yPos;
        }
    }

    static GLFWEventDispatcher getInstance(GLFWEventType type, ImGuiIO io) {
        return switch (type) {
            case KeyEvent -> new KeyEvent(io);
            case MouseEvent -> new MouseEvent(io);
            case MouseCursorPosEvent -> new MouseCursorPosEvent();
            case KeyCharEvent -> new KeyCharEvent(io);
            case MouseScrollEvent -> new MouseScrollEvent(io);
        };
    }
}
