package OxyEngine.Core.Renderer.Buffer;

import OxyEngineEditor.Sandbox.Scene.InternObjects.OxyInternObject;

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

    public void addToBuffer(OxyInternObject oxyEntity) {
        addToBuffer(oxyEntity.getVertices());
    }

    private void addToBuffer(float[] m_Vertices) {
        float[] newObjVert = new float[textureCoords.length + m_Vertices.length];
        System.arraycopy(textureCoords, 0, newObjVert, 0, textureCoords.length);
        System.arraycopy(m_Vertices, 0, newObjVert, textureCoords.length, m_Vertices.length);
        this.textureCoords = newObjVert;
    }

    public void setTextureCoords(float[] textureCoords) {
        this.textureCoords = textureCoords;
    }

    @Override
    public void dispose() {
        glDeleteBuffers(bufferId);
    }
}
