package OxyEngine.Core.Context.Renderer.Pipeline;

import OxyEngine.Core.Context.OxyRenderPass;
import OxyEngine.Core.Context.Renderer.Buffer.Buffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static OxyEngine.System.OxySystem.logger;

public final class OxyPipeline {

    private OxyShader shader;
    private final String debugName; //TODO: Do something with the debug name
    private final List<Layout> layouts;
    private final OxyRenderPass renderPass;

    private OxyPipeline(PipelineSpecification builder) {
        this.shader = builder.shader;
        this.debugName = builder.debugName;
        this.layouts = builder.layouts;
        this.renderPass = builder.renderPass;
    }

    public static PipelineSpecification createNewSpecification() {
        return new PipelineSpecification();
    }

    //if the shader has been recompiled...
    //we are destroying the old shader and we need to update shader reference for this class too
    public void updatePipelineShader() {
        String name = shader.getName();
        shader = ShaderLibrary.get(name);
    }

    interface Builder {

        Builder setShader(OxyShader shader);

        Builder setRenderPass(OxyRenderPass renderPass);

        Builder setDebugName(String name);

        Builder createLayout(Layout layout);
    }

    public static final class Layout {

        private final Map<Integer, ShaderType> shaderLayout;
        private boolean normalized;
        private Class<? extends Buffer> bufferClass;

        public Layout(Map<Integer, ShaderType> shaderLayout) {
            this.shaderLayout = shaderLayout;
        }

        public Layout set(int shaderPos, ShaderType type) {
            if (bufferClass == null) throw new IllegalStateException("Buffer class not defined!");
            if (shaderLayout.containsKey(shaderPos)) {
                logger.severe("Shader layout duplicate name error!");
                return this;
            }
            shaderLayout.put(shaderPos, type);
            return this;
        }

        public Layout targetBuffer(Class<? extends Buffer> bufferClass) {
            this.bufferClass = bufferClass;
            return this;
        }

        public Layout normalized(boolean normalized) {
            this.normalized = normalized;
            return this;
        }

        public Map<Integer, ShaderType> shaderLayout() {
            return shaderLayout;
        }

        public boolean normalized() {
            return normalized;
        }

        public Class<? extends Buffer> getTargetBuffer() {
            return bufferClass;
        }
    }

    public static class PipelineSpecification implements OxyPipeline.Builder {

        OxyShader shader;
        String debugName;
        List<Layout> layouts;
        OxyRenderPass renderPass;

        PipelineSpecification() {
        }

        @Override
        public PipelineSpecification setShader(OxyShader shader) {
            this.shader = shader;
            return this;
        }

        @Override
        public PipelineSpecification setRenderPass(OxyRenderPass renderPass) {
            this.renderPass = renderPass;
            return this;
        }

        @Override
        public PipelineSpecification setDebugName(String name) {
            this.debugName = name;
            return this;
        }

        @Override
        public PipelineSpecification createLayout(Layout layout) {
            if (layouts == null) layouts = new ArrayList<>();
            layouts.add(layout);
            return this;
        }
    }

    public static Layout createNewPipelineLayout() {
        return new Layout(new HashMap<>());
    }

    public static OxyPipeline createNewPipeline(PipelineSpecification builder) {
        if (builder.renderPass == null) throw new IllegalStateException("RenderPass is null!");
        if (builder.shader == null) throw new IllegalStateException("Shader is null!");
        return new OxyPipeline(builder);
    }

    public List<Layout> getLayouts() {
        return layouts;
    }

    public Layout getLayout(Class<? extends Buffer> bufferClass) {
        for (Layout l : layouts) {
            if (l.bufferClass.equals(bufferClass)) return l;
        }
        return null;
    }

    public OxyRenderPass getRenderPass() {
        return renderPass;
    }

    public OxyShader getShader() {
        return shader;
    }
}
