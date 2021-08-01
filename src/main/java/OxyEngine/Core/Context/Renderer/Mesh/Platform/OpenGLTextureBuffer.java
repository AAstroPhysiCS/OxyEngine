package OxyEngine.Core.Context.Renderer.Mesh.Platform;

import OxyEngine.Core.Context.Renderer.Mesh.TextureBuffer;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;

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
