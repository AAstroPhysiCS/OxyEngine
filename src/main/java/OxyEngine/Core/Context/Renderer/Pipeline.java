package OxyEngine.Core.Context.Renderer;

import java.util.LinkedHashMap;
import java.util.Map;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glVertexAttribIPointer;

public final class Pipeline {

    private final String debugName; //only perceivable when debug is enabled
    private final VertexBufferLayout vertexBufferLayout;
    private final RenderPass renderPass;

    private Pipeline(PipelineSpecification builder) {
        this.debugName = builder.debugName;
        this.vertexBufferLayout = builder.vertexBufferLayout;
        this.renderPass = builder.renderPass;
    }

    public static PipelineSpecification createNewSpecification() {
        return new PipelineSpecification();
    }

    private interface Builder {

        Builder setRenderPass(RenderPass renderPass);

        Builder setDebugName(String name);

        Builder setVertexBufferLayout(VertexBufferLayout vertexBufferLayout);

    }

    static class PipelineSpecification implements Pipeline.Builder {

        private String debugName;
        private VertexBufferLayout vertexBufferLayout;
        private RenderPass renderPass;

        private PipelineSpecification() {
        }

        @Override
        public PipelineSpecification setRenderPass(RenderPass renderPass) {
            this.renderPass = renderPass;
            return this;
        }

        @Override
        public PipelineSpecification setDebugName(String name) {
            this.debugName = name;
            return this;
        }

        @Override
        public PipelineSpecification setVertexBufferLayout(VertexBufferLayout vertexBufferLayout) {
            this.vertexBufferLayout = vertexBufferLayout;
            return this;
        }
    }

    static final class VertexBufferLayout {

        private final Map<Integer, ShaderType> shaderLayout;
        private boolean normalized;

        private VertexBufferLayout(Map<Integer, ShaderType> shaderLayout) {
            this.shaderLayout = shaderLayout;
        }

        public VertexBufferLayout addShaderType(int shaderPos, ShaderType type) {
            if (shaderLayout.containsKey(shaderPos)) {
                logger.severe("Shader layout duplicate position error!");
                return this;
            }
            shaderLayout.put(shaderPos, type);
            return this;
        }

        public VertexBufferLayout isNormalized(boolean normalized) {
            this.normalized = normalized;
            return this;
        }

        public Map<Integer, ShaderType> getShaderLayout() {
            return shaderLayout;
        }

        public boolean isNormalized() {
            return normalized;
        }
    }

    public static VertexBufferLayout createNewVertexBufferLayout() {
        return new VertexBufferLayout(new LinkedHashMap<>());
    }

    public static Pipeline createNewPipeline(PipelineSpecification builder) {
        if (builder.renderPass == null) throw new IllegalStateException("RenderPass is null!");
        return new Pipeline(builder);
    }

    public void processVertexBufferLayout() {
        int offset = 0;
        int stride = 0;
        boolean normalized = vertexBufferLayout.isNormalized();

        for (var entrySet : vertexBufferLayout.getShaderLayout().values()) stride += entrySet.getSize();

        for (var entrySet : vertexBufferLayout.getShaderLayout().entrySet()) {
            int bufferIndex = entrySet.getKey();
            ShaderType type = entrySet.getValue();

            glEnableVertexAttribArray(bufferIndex);
            switch (type) {
                case Float1, Float2, Float3, Float4, Matrix3f, Matrix4f -> glVertexAttribPointer(bufferIndex, type.getSize(), type.getContextType(), normalized, stride * Float.BYTES, (long) offset * Float.BYTES);
                case Int1, Int2, Int3, Int4 -> glVertexAttribIPointer(bufferIndex, type.getSize(), type.getContextType(), stride * Float.BYTES, (long) offset * Integer.BYTES);
                default -> throw new IllegalStateException("No implementation to a type");
            }
            offset += type.getSize();
        }
    }

    public RenderPass getRenderPass() {
        return renderPass;
    }
}
