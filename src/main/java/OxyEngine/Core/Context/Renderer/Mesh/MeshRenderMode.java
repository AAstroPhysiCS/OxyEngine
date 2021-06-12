package OxyEngine.Core.Context.Renderer.Mesh;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public enum MeshRenderMode {
    LINES(GL_LINES), TRIANGLES(GL_TRIANGLES);

    final int modeID;
    MeshRenderMode(int modeID) { this.modeID = modeID; }

    public int getModeID() {
        return modeID;
    }
}
