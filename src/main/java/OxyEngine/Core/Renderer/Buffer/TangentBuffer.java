package OxyEngine.Core.Renderer.Buffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public class TangentBuffer extends Buffer {

    private float[] biAndTangent = new float[0];

    private final BufferTemplate.BufferTemplateImpl implementation;

    public TangentBuffer(BufferTemplate template) {
        this.implementation = template.setup();
    }

    @Override
    protected void load() {
        if (bufferId == 0) bufferId = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, biAndTangent, GL_STATIC_DRAW);

        BufferTemplate.Attributes[] attribPointers = implementation.getAttribPointers();
        for (BufferTemplate.Attributes ptr : attribPointers) {
            glEnableVertexAttribArray(ptr.index());
            glVertexAttribPointer(ptr.index(), ptr.size(), ptr.type(), ptr.normalized(), ptr.stride(), ptr.pointer());
        }
    }

    public void setBiAndTangent(float[] biAndTangent) {
        this.biAndTangent = biAndTangent;
    }

    @Override
    public void dispose() {
        glDeleteBuffers(bufferId);
    }
}
