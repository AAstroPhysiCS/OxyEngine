package OxyEngineEditor.Components;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.RenderingMode;
import OxyEngine.Core.Renderer.Shader.OxyShader;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class ModelMesh extends Mesh {

    public static final BufferTemplate.Attributes attributesVert = new BufferTemplate.Attributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
    public static final BufferTemplate.Attributes attributesTXSlots = new BufferTemplate.Attributes(OxyShader.TEXTURE_SLOT, 1, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
    public static final BufferTemplate.Attributes attributesColor = new BufferTemplate.Attributes(OxyShader.COLOR, 4, GL_FLOAT, false, 8 * Float.BYTES, 4 * Float.BYTES);

    public static final BufferTemplate.Attributes attributesNormals = new BufferTemplate.Attributes(OxyShader.NORMALS, 3, GL_FLOAT, false, 0, 0);

    public static final BufferTemplate.Attributes attributesTXCoords = new BufferTemplate.Attributes(OxyShader.TEXTURE_COORDS, 2, GL_FLOAT, false, 0, 0);

    private final float[] vertices, textureCoords, normals;
    private final int[] indices;

    private ModelMesh(OxyShader shader, BufferTemplate.Usage usage, int mode, RenderableComponent renderableComponent, float[] vertices, int[] indices, float[] textureCoords, float[] normals) {
        this.shader = shader;
        this.vertices = vertices;
        this.indices = indices;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.mode = mode;
        this.renderableComponent = renderableComponent;

        vertexBuffer = new VertexBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                .setVerticesStrideSize(attributesVert.stride() / Float.BYTES)
                .setUsage(usage)
                .setAttribPointer(attributesVert, attributesTXSlots, attributesColor));

        indexBuffer = new IndexBuffer();

        textureBuffer = new TextureBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                .setAttribPointer(attributesTXCoords));

        normalsBuffer = new NormalsBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                .setAttribPointer(attributesNormals));

        vertexBuffer.setVertices(vertices);
        indexBuffer.setIndices(indices);
        textureBuffer.setTextureCoords(textureCoords);
        normalsBuffer.setNormals(normals);
    }

    interface ModelMeshBuilder {

        ModelMeshBuilder setShader(OxyShader shader);

        ModelMeshBuilder setVertices(float[] vertices);

        ModelMeshBuilder setIndices(int[] vertices);

        ModelMeshBuilder setTextureCoords(float[] vertices);

        ModelMeshBuilder setNormals(float[] normals);

        ModelMeshBuilder setMode(int mode);

        ModelMeshBuilder setUsage(BufferTemplate.Usage usage);

        ModelMeshBuilder setRenderableComponent(RenderableComponent renderable);

        ModelMesh create();
    }

    public static class ModelMeshBuilderImpl implements ModelMeshBuilder {

        private OxyShader shader;
        private float[] vertices, textureCoords, normals;
        private int[] indices;
        private int mode;
        private BufferTemplate.Usage usage;
        private RenderableComponent component = new RenderableComponent(RenderingMode.Normal);

        @Override
        public ModelMeshBuilderImpl setShader(OxyShader shader) {
            this.shader = shader;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setVertices(float[] vertices) {
            this.vertices = vertices;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setIndices(int[] indices) {
            this.indices = indices;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setTextureCoords(float[] textureCoords) {
            this.textureCoords = textureCoords;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setNormals(float[] normals) {
            this.normals = normals;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setMode(int mode) {
            this.mode = mode;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setUsage(BufferTemplate.Usage usage) {
            this.usage = usage;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setRenderableComponent(RenderableComponent component) {
            this.component = component;
            return this;
        }

        @Override
        public ModelMesh create() {
            assert textureCoords != null && indices != null && vertices != null : oxyAssert("Data that is given is null.");
            return new ModelMesh(shader, usage, mode, component, vertices, indices, textureCoords, normals);
        }
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }

    public float[] getVertices() {
        return vertices;
    }

    public float[] getNormals() {
        return normals;
    }

    public int[] getIndices() {
        return indices;
    }

    public RenderableComponent getRenderableComponent() {
        return renderableComponent;
    }
}
