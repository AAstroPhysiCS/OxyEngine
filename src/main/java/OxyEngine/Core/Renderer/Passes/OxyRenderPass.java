package OxyEngine.Core.Renderer.Passes;

import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Mesh.MeshRenderMode;

import static org.lwjgl.opengl.GL11.*;

public abstract class OxyRenderPass {

    private final FrameBuffer frameBuffer;
    
    private MeshRenderMode meshRenderingMode;

    public OxyRenderPass(FrameBuffer frameBuffer){
        this.frameBuffer = frameBuffer;
    }

    public void renderingMode(MeshRenderMode renderingMode){
        this.meshRenderingMode = renderingMode;
    }

    public void enableCullFace(int cullMode){
        glEnable(GL_CULL_FACE);
        glCullFace(cullMode);
    }

    public void disableCullFace(){
        glDisable(GL_CULL_FACE);
        glCullFace(GL_BACK); //Default setting
    }

    public void fillMode(int face, int mode){
        glPolygonMode(face, mode);
    }

}
