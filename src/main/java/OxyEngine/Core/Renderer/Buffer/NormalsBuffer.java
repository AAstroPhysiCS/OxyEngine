package OxyEngine.Core.Renderer.Buffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public class NormalsBuffer extends Buffer {

    private float[] normals = new float[0];
    private final BufferTemplate.BufferTemplateImpl implementation;

    public NormalsBuffer(BufferTemplate template) {
        this.implementation = template.setup();
    }

    @Override
    protected void load() {
        if (bufferId == 0) bufferId = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);

        BufferTemplate.Attributes[] attribPointers = implementation.getAttribPointers();
        for (BufferTemplate.Attributes ptr : attribPointers) {
            glEnableVertexAttribArray(ptr.index());
            glVertexAttribPointer(ptr.index(), ptr.size(), ptr.type(), ptr.normalized(), ptr.stride(), ptr.pointer());
        }
    }

    public void setNormals(float[] normals) {
        this.normals = normals;
    }

    @Override
    public void dispose() {
        glDeleteBuffers(bufferId);
    }

    public void clear() {
        normals = new float[0];
    }
}
