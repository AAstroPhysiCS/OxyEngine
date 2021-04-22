package OxyEngine.Core.Renderer.Buffer;

import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLTangentBuffer;
import OxyEngine.Core.Renderer.Context.OpenGLRendererAPI;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;

import static OxyEngine.Core.Renderer.Context.OxyRenderCommand.rendererAPI;

public abstract class TangentBuffer extends Buffer {

    protected float[] biAndTangent = new float[0];

    protected final OxyPipeline.Layout layout;

    public TangentBuffer(OxyPipeline.Layout layout) {
        this.layout = layout;
    }

    public boolean emptyData(){
        return biAndTangent.length == 0;
    }

    public static <T extends TangentBuffer> T create(OxyPipeline pipeline){
        if(rendererAPI instanceof OpenGLRendererAPI) {
            var layout = pipeline.getLayout(TangentBuffer.class);
            try {
                var constructor = OpenGLTangentBuffer.class.getDeclaredConstructor(OxyPipeline.Layout.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(layout);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }
}
