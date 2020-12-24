package OxyEngine.Core.Renderer.Buffer.Platform;

import OxyEngine.Core.Renderer.Buffer.BufferLayoutAttributes;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutProducer;
import OxyEngine.Core.Renderer.Buffer.NormalsBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public class OpenGLNormalsBuffer extends NormalsBuffer {

    OpenGLNormalsBuffer(BufferLayoutProducer.BufferLayoutImpl template) {
        super(template);
    }

    @Override
    public void load() {
        if (bufferId == 0) bufferId = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);

        BufferLayoutAttributes[] attribPointers = implementation.getAttribPointers();
        for (BufferLayoutAttributes ptr : attribPointers) {
            glEnableVertexAttribArray(ptr.index());
            glVertexAttribPointer(ptr.index(), ptr.size(), ptr.type(), ptr.normalized(), ptr.stride(), ptr.pointer());
        }
    }

    public void setNormals(float[] normals) {
        this.normals = normals;
    }

    @Override
    public void dispose() {
        normals = null;
        glDeleteBuffers(bufferId);
    }
}
