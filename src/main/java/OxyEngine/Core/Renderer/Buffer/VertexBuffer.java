package OxyEngine.Core.Renderer.Buffer;

import OxyEngineEditor.Scene.NativeObjects.OxyNativeObject;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public final class VertexBuffer extends Buffer {

    private float[] vertices = new float[0];
    private final BufferTemplate.BufferTemplateImpl implementation;

    int offsetToUpdate = -1;
    float[] dataToUpdate;

    public VertexBuffer(BufferTemplate template) {
        this.implementation = template.setup();

        assert implementation.getUsage() != null && implementation.getStrideSize() != -1 && implementation.getAttribPointers() != null : oxyAssert("Some Implementation arguments are null");
    }

    @Override
    public void load() {
        if (bufferId == 0) bufferId = glCreateBuffers();

        if (implementation.getUsage() == BufferTemplate.Usage.STATIC) loadStatically();
        else loadDynamically();

        BufferTemplate.Attributes[] attribPointers = implementation.getAttribPointers();
        for (BufferTemplate.Attributes ptr : attribPointers) {
            glEnableVertexAttribArray(ptr.index());
            glVertexAttribPointer(ptr.index(), ptr.size(), ptr.type(), ptr.normalized(), ptr.stride(), ptr.pointer());
        }
    }

    private void loadStatically() {
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
    }

    private void loadDynamically() {
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices.length * implementation.getStrideSize(), GL_DYNAMIC_DRAW);
    }

    public void addToBuffer(OxyNativeObject oxyEntity) {
        addToBuffer(oxyEntity.getVertices());
    }

    public void addToBuffer(float[] m_Vertices) {
        if (vertices.length == 0) {
            vertices = m_Vertices;
            return;
        }
        copy(m_Vertices);
    }

    private void copy(float[] m_Vertices) {
        float[] newObjVert = new float[vertices.length + m_Vertices.length];
        System.arraycopy(vertices, 0, newObjVert, 0, vertices.length);
        System.arraycopy(m_Vertices, 0, newObjVert, vertices.length, m_Vertices.length);
        this.vertices = newObjVert;
    }

    public void updateSingleEntityData(int pos, float[] newVertices) {
        this.offsetToUpdate = pos * Float.BYTES;
        this.dataToUpdate = newVertices;
        for (float newVertex : newVertices) {
            vertices[pos++] = newVertex;
        }
    }

    public BufferTemplate.BufferTemplateImpl getImplementation() {
        return implementation;
    }

    public float[] getVertices() {
        return vertices;
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
    }

    @Override
    public void dispose() {
        vertices = null;
        dataToUpdate = null;
        glDeleteBuffers(bufferId);
    }
}