package OxyEngine.Core.Renderer.Buffer.Platform;

import OxyEngine.Core.Renderer.Buffer.TextureBuffer;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public class OpenGLTextureBuffer extends TextureBuffer {

    OpenGLTextureBuffer(OxyPipeline.Layout layout) {
        super(layout);
    }

    @Override
    public void load() {
        if (bufferId == 0) bufferId = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, textureCoords, GL_STATIC_DRAW);
    }

    @Override
    public void dispose() {
        textureCoords = null;
        glDeleteBuffers(bufferId);
        bufferId = 0;
    }
}
