package OxyEngine.Core.Renderer;

import static org.lwjgl.opengl.GL11.*;

public enum CullMode {
    FRONT(GL_FRONT), BACK(GL_BACK), FRONT_AND_BACK(GL_FRONT_AND_BACK), DISABLED(GL_BACK);

    final int value;

    CullMode(int value) {
        this.value = value;
    }
}
