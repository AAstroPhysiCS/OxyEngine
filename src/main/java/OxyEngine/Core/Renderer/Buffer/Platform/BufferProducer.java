package OxyEngine.Core.Renderer.Buffer.Platform;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Context.RendererContext;
import OxyEngine.Core.Renderer.OxyRendererPlatform;

import static OxyEngine.System.OxySystem.logger;

@SuppressWarnings("unchecked")
public class BufferProducer {

    private BufferProducer(){}

    private static void contextCheck(){
        if(RendererContext.selectedPlatform != OxyRendererPlatform.OpenGL){
            logger.severe("Unsupported Platform!");
            System.exit(-1);
        }
    }

    public static <T extends VertexBuffer> T createVertexBuffer(BufferLayoutProducer.BufferLayoutImpl impl){
        contextCheck();
        return (T) new OpenGLVertexBuffer(impl);
    }

    public static <T extends IndexBuffer> T createIndexBuffer(){
        contextCheck();
        return (T) new OpenGLIndexBuffer();
    }

    public static <T extends FrameBuffer> T createFrameBuffer(int width, int height){
        contextCheck();
        return (T) new OpenGLFrameBuffer(width, height);
    }

    public static <T extends NormalsBuffer> T createNormalsBuffer(BufferLayoutProducer.BufferLayoutImpl impl){
        contextCheck();
        return (T) new OpenGLNormalsBuffer(impl);
    }

    public static <T extends TangentBuffer> T createTangentBuffer(BufferLayoutProducer.BufferLayoutImpl impl){
        contextCheck();
        return (T) new OpenGLTangentBuffer(impl);
    }

    public static <T extends TextureBuffer> T createTextureBuffer(BufferLayoutProducer.BufferLayoutImpl impl){
        contextCheck();
        return (T) new OpenGLTextureBuffer(impl);
    }
}
