package OxyEngine.Core.Context.Renderer.Buffer;

import OxyEngine.Core.Context.Renderer.Buffer.Platform.OpenGLTextureBuffer;
import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.TargetPlatform;

public abstract class TextureBuffer extends Buffer {

    protected final OxyPipeline.Layout layout;

    protected float[] textureCoords;

    public TextureBuffer(OxyPipeline.Layout layout) {
        this.layout = layout;
        textureCoords = new float[0];
    }

    public static <T extends TextureBuffer> T create(OxyPipeline pipeline){
        if(OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
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
