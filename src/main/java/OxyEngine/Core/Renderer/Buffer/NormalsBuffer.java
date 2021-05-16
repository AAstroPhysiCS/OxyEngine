package OxyEngine.Core.Renderer.Buffer;

import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLNormalsBuffer;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.TargetPlatform;

public abstract class NormalsBuffer extends Buffer {

    protected float[] normals = new float[0];

    protected final OxyPipeline.Layout layout;

    public NormalsBuffer(OxyPipeline.Layout layout) {
        this.layout = layout;
    }

    public void setNormals(float[] normals) {
        this.normals = normals;
    }

    public boolean emptyData() {
        return normals.length == 0;
    }

    public static <T extends NormalsBuffer> T create(OxyPipeline pipeline){
        if(OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            var layout = pipeline.getLayout(NormalsBuffer.class);
            try {
                var constructor = OpenGLNormalsBuffer.class.getDeclaredConstructor(OxyPipeline.Layout.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(layout);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }
}
