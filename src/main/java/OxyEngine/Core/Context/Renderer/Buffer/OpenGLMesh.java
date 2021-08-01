package OxyEngine.Core.Context.Renderer.Buffer;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.*;
import OxyEngine.Core.Context.Renderer.Mesh.MeshRenderMode;
import OxyEngine.Core.Context.Renderer.Mesh.MeshUsage;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderType;
import OxyEngine.Core.Context.Scene.OxyEntity;
import OxyEngine.System.OxyDisposable;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL45.*;

//TODO: Make a Mesh class that this class will inherit from, as well as the VulkanMesh class.
public abstract class OpenGLMesh implements OxyDisposable, EntityComponent {

    protected OpenGLIndexBuffer indexBuffer;
    protected OpenGLVertexBuffer vertexBuffer;
    protected OpenGLTextureBuffer textureBuffer;
    protected OpenGLNormalsBuffer normalsBuffer;
    protected OpenGLTangentBuffer tangentBuffer;

    protected OxyPipeline pipeline;

    public OpenGLMesh(OxyPipeline pipeline){
        assert pipeline != null : oxyAssert("Pipeline null!");
        this.pipeline = pipeline;
    }

    protected String path;

    protected int vao;
    protected MeshRenderMode mode;

    public boolean empty() {
        return indexBuffer.glBufferNull() && vertexBuffer.glBufferNull();
    }

    public String getPath() {
        return path;
    }

    public void load() {

        if (vao == 0) vao = glCreateVertexArrays();
        glBindVertexArray(vao);

        vertexBuffer.load();
        if (indexBuffer != null) if (indexBuffer.glBufferNull() && !indexBuffer.emptyData()) indexBuffer.load();
        if (normalsBuffer != null) if (normalsBuffer.glBufferNull() && !normalsBuffer.emptyData()) normalsBuffer.load();
        if (tangentBuffer != null) if (tangentBuffer.glBufferNull() && !tangentBuffer.emptyData()) tangentBuffer.load();
        if (textureBuffer != null) if (textureBuffer.glBufferNull() && !textureBuffer.emptyData()) textureBuffer.load();

        if (vertexBuffer.getUsage() == MeshUsage.DYNAMIC) {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getBufferId());
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer.getVertices());
        }

        processPipeline(pipeline);
    }

    private void draw() {
        glDrawElements(mode.getModeID(), indexBuffer.length(), GL_UNSIGNED_INT, 0);
    }

    private void bind() {
        glBindVertexArray(vao);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.bufferId);
        if (vertexBuffer.getUsage() == MeshUsage.DYNAMIC && vertexBuffer.offsetToUpdate != -1) {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.bufferId);
            glBufferSubData(GL_ARRAY_BUFFER, vertexBuffer.offsetToUpdate, vertexBuffer.dataToUpdate);

            vertexBuffer.offsetToUpdate = -1;
        }
    }

    private void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        OxyRenderer.Stats.drawCalls++;
        OxyRenderer.Stats.totalVertexCount += vertexBuffer.getVertices().length;
        OxyRenderer.Stats.totalIndicesCount += indexBuffer.getIndices().length;
    }

    public void addToList(OxyEntity e) {
        vertexBuffer.addToBuffer(e.getVertices());
        indexBuffer.addToBuffer(e.getIndices());
    }

    //TODO: Dont do this public
    public void render() {
        bind();
        draw();
        unbind();
    }

    public void updateSingleEntityData(int offsetToUpdate, float[] dataToUpdate) {
        vertexBuffer.updateSingleEntityData(offsetToUpdate, dataToUpdate);
    }

    @Override
    public void dispose() {
        vertexBuffer.dispose();
        if (indexBuffer != null) indexBuffer.dispose();
        if (textureBuffer != null) textureBuffer.dispose();
        if (normalsBuffer != null) normalsBuffer.dispose();
        if (tangentBuffer != null) tangentBuffer.dispose();
        glDeleteVertexArrays(vao);
        vao = 0;
    }

    public void setRenderingMode(MeshRenderMode mode) {
        this.mode = mode;
    }

    private void processPipeline(OxyPipeline pipeline) {
        if (pipeline.getLayouts() == null) return;
        for (OxyPipeline.Layout layout : pipeline.getLayouts()) {

            int offset = 0;
            int stride = 0;

            //stride = 0 when just vertexbuffer and indexbuffer is defined
            if (pipeline.getLayouts().size() != 2) { //vertexbuffer and indexbuffer
                for (var type : layout.shaderLayout().values()) {
                    stride += type.getSize();
                }
            }

            Class<? extends Buffer> targetBuffer = layout.getTargetBuffer();

            if (targetBuffer.equals(VertexBuffer.class)) {
                glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.bufferId);
            } else if (targetBuffer.equals(IndexBuffer.class)) {
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.bufferId);
            } else if (targetBuffer.equals(TextureBuffer.class)) {
                glBindBuffer(GL_ARRAY_BUFFER, textureBuffer.bufferId);
            } else if (targetBuffer.equals(NormalsBuffer.class)) {
                glBindBuffer(GL_ARRAY_BUFFER, normalsBuffer.bufferId);
            } else if (targetBuffer.equals(TangentBuffer.class)) {
                glBindBuffer(GL_ARRAY_BUFFER, tangentBuffer.bufferId);
            }

            for (var entrySet : layout.shaderLayout().entrySet()) {
                int bufferIndex = entrySet.getKey();
                ShaderType type = entrySet.getValue();

                glEnableVertexAttribArray(bufferIndex);
                switch (type) {
                    case Float1, Float2, Float3, Float4, Matrix3f, Matrix4f -> glVertexAttribPointer(bufferIndex, type.getSize(), type.getOpenGLType(), layout.normalized(), stride * Float.BYTES, (long) offset * Float.BYTES);
                    case Int1, Int2, Int3, Int4 -> glVertexAttribIPointer(bufferIndex, type.getSize(), type.getOpenGLType(), stride * Float.BYTES, (long) offset * Integer.BYTES);
                    default -> throw new IllegalStateException("No implementation to a type");
                }
                offset += type.getSize();
            }

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }
}