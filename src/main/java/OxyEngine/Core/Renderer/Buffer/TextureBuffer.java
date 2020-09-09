package OxyEngine.Core.Renderer.Buffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public class TextureBuffer extends Buffer {

    private final BufferTemplate.BufferTemplateImpl implementation;

    private float[] textureCoords;

    public TextureBuffer(BufferTemplate template) {
        this.implementation = template.setup();
        textureCoords = new float[0];
    }

    @Override
    protected void load() {
        if (bufferId == 0) bufferId = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, textureCoords, GL_STATIC_DRAW);

        BufferTemplate.Attributes[] attribPointers = implementation.getAttribPointers();
        for (BufferTemplate.Attributes ptr : attribPointers) {
            glEnableVertexAttribArray(ptr.index());
            glVertexAttribPointer(ptr.index(), ptr.size(), ptr.type(), ptr.normalized(), ptr.stride(), ptr.pointer());
        }
    }

    public void setTextureCoords(float[] textureCoords) {
        this.textureCoords = textureCoords;
    }

    @Override
    public void dispose() {
        textureCoords = null;
        glDeleteBuffers(bufferId);
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }
}
