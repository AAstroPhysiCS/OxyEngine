package OxyEngine.Core.Renderer.Buffer.Platform;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Context.RendererContext;
import OxyEngine.TargetPlatform;

import static OxyEngine.System.OxySystem.logger;

@SuppressWarnings("unchecked")
public class BufferConstructor {

    private BufferConstructor(){}

    private static void contextCheck(){
        if(RendererContext.selectedPlatform != TargetPlatform.OpenGL){
            logger.severe("Unsupported Platform!");
            System.exit(-1);
        }
    }

    public static <T extends VertexBuffer> T createVertexBuffer(BufferLayoutConstructor.BufferLayoutImpl impl){
        contextCheck();
        return (T) new OpenGLVertexBuffer(impl);
    }

    public static <T extends IndexBuffer> T createIndexBuffer(BufferLayoutConstructor.BufferLayoutImpl impl){
        contextCheck();
        return (T) new OpenGLIndexBuffer(impl);
    }

    public static <T extends FrameBuffer> T createFrameBuffer(int width, int height, FrameBufferSpecification... specBuilders){
        contextCheck();
        for(FrameBufferSpecification spec : specBuilders)
            if(spec.attachmentIndex == -1) logger.severe("Framebuffer incomplete: Attachment Index not defined!");
        return (T) new OpenGLFrameBuffer(width, height, specBuilders);
    }

    public static <T extends NormalsBuffer> T createNormalsBuffer(BufferLayoutConstructor.BufferLayoutImpl impl){
        contextCheck();
        return (T) new OpenGLNormalsBuffer(impl);
    }

    public static <T extends TangentBuffer> T createTangentBuffer(BufferLayoutConstructor.BufferLayoutImpl impl){
        contextCheck();
        return (T) new OpenGLTangentBuffer(impl);
    }

    public static <T extends TextureBuffer> T createTextureBuffer(BufferLayoutConstructor.BufferLayoutImpl impl){
        contextCheck();
        return (T) new OpenGLTextureBuffer(impl);
    }
}
