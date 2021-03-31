package OxyEngine.Core.Renderer.Buffer.Platform;

import OxyEngine.Core.Renderer.Buffer.BufferLayoutAttributes;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutConstructor;
import OxyEngine.Core.Renderer.Buffer.TextureBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public class OpenGLTextureBuffer extends TextureBuffer {

    OpenGLTextureBuffer(BufferLayoutConstructor.BufferLayoutImpl template) {
        super(template);
    }

    @Override
    public void load() {
        if (bufferId == 0) bufferId = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, textureCoords, GL_STATIC_DRAW);

        BufferLayoutAttributes[] attribPointers = implementation.getAttribPointers();
        for (BufferLayoutAttributes ptr : attribPointers) {
            glEnableVertexAttribArray(ptr.index());
            glVertexAttribPointer(ptr.index(), ptr.size(), ptr.type(), ptr.normalized(), ptr.stride(), ptr.pointer());
        }
    }

    @Override
    public void dispose() {
        textureCoords = null;
        glDeleteBuffers(bufferId);
    }
}
