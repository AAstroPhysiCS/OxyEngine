package OxyEngine.Core.Renderer.Buffer;

import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLTextureBuffer;
import OxyEngine.Core.Renderer.Context.OpenGLRendererAPI;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;

import static OxyEngine.Core.Renderer.Context.OxyRenderCommand.rendererAPI;

public abstract class TextureBuffer extends Buffer {

    protected final OxyPipeline.Layout layout;

    protected float[] textureCoords;

    public TextureBuffer(OxyPipeline.Layout layout) {
        this.layout = layout;
        textureCoords = new float[0];
    }

    public static <T extends TextureBuffer> T create(OxyPipeline pipeline){
        if(rendererAPI instanceof OpenGLRendererAPI) {
            var layout = pipeline.getLayout(TextureBuffer.class);
            try {
                var constructor = OpenGLTextureBuffer.class.getDeclaredConstructor(OxyPipeline.Layout.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(layout);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public void setTextureCoords(float[] textureCoords) {
        this.textureCoords = textureCoords;
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }

    public boolean emptyData() {
        return textureCoords.length == 0;
    }
}
