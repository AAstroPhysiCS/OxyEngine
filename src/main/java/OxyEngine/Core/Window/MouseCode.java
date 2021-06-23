package OxyEngine.Core.Window;

//For type safety
@SuppressWarnings("unused")
public enum MouseCode {
    GLFW_MOUSE_BUTTON_1(0),
    GLFW_MOUSE_BUTTON_2(1),
    GLFW_MOUSE_BUTTON_3(2),
    GLFW_MOUSE_BUTTON_4(3),
    GLFW_MOUSE_BUTTON_5(4),
    GLFW_MOUSE_BUTTON_6(5),
    GLFW_MOUSE_BUTTON_7(6),
    GLFW_MOUSE_BUTTON_8(7),
    GLFW_MOUSE_BUTTON_LAST(GLFW_MOUSE_BUTTON_8.value),
    GLFW_MOUSE_BUTTON_LEFT(GLFW_MOUSE_BUTTON_1.value),
    GLFW_MOUSE_BUTTON_RIGHT(GLFW_MOUSE_BUTTON_2.value),
    GLFW_MOUSE_BUTTON_MIDDLE(GLFW_MOUSE_BUTTON_3.value);

    final int value;

    MouseCode(int value) {
        this.value = value;
    }

    public static MouseCode getMouseCodeByValue(int button) {
        for(MouseCode code : MouseCode.values()){
            if(code.value == button) return code;
        }
        return null;
    }
}
