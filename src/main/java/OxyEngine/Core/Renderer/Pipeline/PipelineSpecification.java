package OxyEngine.Core.Renderer.Pipeline;

import OxyEngine.Core.Renderer.Passes.OxyRenderPass;
import OxyEngine.Core.Renderer.Shader.OxyShader;

public class PipelineSpecification implements OxyPipeline.Builder {

    OxyShader shader;
    String debugName;

    PipelineSpecification(){}

    @Override
    public PipelineSpecification setShader(OxyShader shader) {
        this.shader = shader;
        return this;
    }

    @Override
    public PipelineSpecification setRenderPass(OxyRenderPass renderPass) {
        //TODO: Later
        return this;
    }

    @Override
    public PipelineSpecification setDebugName(String name) {
        this.debugName = name;
        return this;
    }
}
