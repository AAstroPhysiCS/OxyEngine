package OxyEngine.Core.Renderer.Mesh;

import static org.lwjgl.opengl.GL11.*;

public enum RenderMode {
    LINES(GL_LINES), TRIANGLES(GL_TRIANGLES), TRIANGLE_STRIP(GL_TRIANGLE_STRIP), TRIANGLE_FAN(GL_TRIANGLE_FAN), NONE(-1);

    final int modeID;
    RenderMode(int modeID) { this.modeID = modeID; }

    public int getModeID() {
        return modeID;
    }
}
