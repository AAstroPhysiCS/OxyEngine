package OxyEngine.Core.Context;

import static org.lwjgl.opengl.GL11.*;

public enum PolygonMode {
    POINT(GL_POINT), LINE(GL_LINE), FILL(GL_FILL);

    final int value;

    PolygonMode(int value) {
        this.value = value;
    }
}