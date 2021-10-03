package OxyEngine.Core.Renderer.Texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public enum TextureParameter {
    LINEAR(GL_LINEAR), REPEAT(GL_REPEAT), LINEAR_MIPMAP_LINEAR(GL_LINEAR_MIPMAP_LINEAR), CLAMP_TO_EDGE(GL_CLAMP_TO_EDGE);

    final int apiValue;
    TextureParameter(int apiValue){
        this.apiValue = apiValue;
    }
}
