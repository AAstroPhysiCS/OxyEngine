package OxyEngine.Core.Context.Renderer.Mesh.Platform;

import OxyEngine.Core.Context.Renderer.Mesh.VertexBuffer;
import OxyEngine.Core.Context.Renderer.Mesh.MeshUsage;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Context.Scene.OxyNativeObject;

import static OxyEngine.OxyUtils.copy;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public final class OpenGLVertexBuffer extends VertexBuffer {

    protected OpenGLVertexBuffer(OxyPipeline.Layout layout, MeshUsage usage) {
        super(layout, usage);
    }

    @Override
    public void load() {
        if (bufferId == 0) bufferId = glCreateBuffers();
        if (usage == MeshUsage.STATIC) loadStatically();
        else loadDynamically();
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
        vertices = copy(vertices, m_Vertices);
    }


    @Override
    public void updateSingleEntityData(int pos, float[] newVertices) {
        this.offsetToUpdate = pos * Float.BYTES;
        this.dataToUpdate = newVertices;
        for (float newVertex : newVertices) {
            if (this.vertices == null) return; //for not breaking when object is deleted
            vertices[pos++] = newVertex;
        }
    }

    @Override
    public void dispose() {
        vertices = null;
        dataToUpdate = null;
        glDeleteBuffers(bufferId);
        bufferId = 0;
    }
}