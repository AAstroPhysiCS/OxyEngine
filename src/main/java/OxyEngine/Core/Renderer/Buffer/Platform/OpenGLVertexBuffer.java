package OxyEngine.Core.Renderer.Buffer.Platform;

import OxyEngine.Core.Renderer.Buffer.BufferLayoutAttributes;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutProducer;
import OxyEngine.Core.Renderer.Buffer.VertexBuffer;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;

import static org.lwjgl.opengl.EXTGPUShader4.glVertexAttribIPointerEXT;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public final class OpenGLVertexBuffer extends VertexBuffer {

    OpenGLVertexBuffer(BufferLayoutProducer.BufferLayoutImpl impl) {
        super(impl);
    }

    @Override
    public void load() {
        if (bufferId == 0) bufferId = glCreateBuffers();

        if (impl.getUsage() == BufferLayoutProducer.Usage.STATIC) loadStatically();
        else loadDynamically();

        BufferLayoutAttributes[] attribPointers = impl.getAttribPointers();
        for (BufferLayoutAttributes ptr : attribPointers) {
            glEnableVertexAttribArray(ptr.index());
            if(ptr.type() == GL_INT || ptr.type() == GL_UNSIGNED_INT) glVertexAttribIPointerEXT(ptr.index(), ptr.size(), ptr.type(), ptr.stride(), ptr.pointer());
            else glVertexAttribPointer(ptr.index(), ptr.size(), ptr.type(), ptr.normalized(), ptr.stride(), ptr.pointer());
        }
    }

    private void loadStatically() {
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
    }

    private void loadDynamically() {
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);
    }

    @Override
    public void addToBuffer(OxyNativeObject oxyEntity) {
        addToBuffer(oxyEntity.getVertices());
    }

    @Override
    public void addToBuffer(float[] m_Vertices) {
        if (vertices.length == 0) {
            vertices = m_Vertices;
            return;
        }
        copy(m_Vertices);
    }

    @Override
    protected void copy(float[] m_Vertices) {
        float[] newObjVert = new float[vertices.length + m_Vertices.length];
        System.arraycopy(vertices, 0, newObjVert, 0, vertices.length);
        System.arraycopy(m_Vertices, 0, newObjVert, vertices.length, m_Vertices.length);
        this.vertices = newObjVert;
    }

    @Override
    public void updateSingleEntityData(int pos, float[] newVertices) {
        this.offsetToUpdate = pos * Float.BYTES;
        this.dataToUpdate = newVertices;
        for (float newVertex : newVertices) {
            if(this.vertices == null) return; //for not breaking when object is deleted
            vertices[pos++] = newVertex;
        }
    }

    @Override
    public void dispose() {
        vertices = null;
        dataToUpdate = null;
        glDeleteBuffers(bufferId);
    }
}