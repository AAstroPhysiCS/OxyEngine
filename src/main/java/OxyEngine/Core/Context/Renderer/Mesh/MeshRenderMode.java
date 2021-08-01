package OxyEngine.Core.Context.Renderer.Mesh;

import static org.lwjgl.opengl.GL11.*;

public enum MeshRenderMode {
    LINES(GL_LINES), TRIANGLES(GL_TRIANGLES), TRIANGLE_STRIP(GL_TRIANGLE_STRIP), TRIANGLE_FAN(GL_TRIANGLE_FAN);

    final int modeID;
    MeshRenderMode(int modeID) { this.modeID = modeID; }

    public int getModeID() {
        return modeID;
    }
}
