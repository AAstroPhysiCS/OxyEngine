package OxyEngine.Core.Renderer;

import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Mesh.MeshRenderMode;

import static org.lwjgl.opengl.GL11.*;

public class OxyRenderPass {

    private final FrameBuffer frameBuffer;

    private final MeshRenderMode meshRenderingMode;

    private final CullMode cullMode;
    private final PolygonMode polygonMode;

    public static Builder createBuilder(FrameBuffer frameBuffer) {
        return new BuilderOpenGL(frameBuffer);
    }

    private OxyRenderPass(FrameBuffer frameBuffer, CullMode cullMode, PolygonMode polygonMode, MeshRenderMode meshRenderingMode) {
        this.frameBuffer = frameBuffer;
        this.cullMode = cullMode;
        this.polygonMode = polygonMode;
        this.meshRenderingMode = meshRenderingMode;
    }

    public sealed interface Builder permits BuilderOpenGL {

        Builder setCullFace(CullMode cullMode);

        Builder polygonMode(PolygonMode polygonMode);

        Builder renderingMode(MeshRenderMode renderMode);

        OxyRenderPass create();
    }

    public static final class BuilderOpenGL implements Builder {

        private final FrameBuffer frameBuffer;

        PolygonMode polygonMode = PolygonMode.FILL;
        CullMode cullMode = CullMode.DISABLED;
        MeshRenderMode meshRenderMode = MeshRenderMode.TRIANGLES;

        public BuilderOpenGL(FrameBuffer frameBuffer) {
            this.frameBuffer = frameBuffer;
        }

        @Override
        public BuilderOpenGL setCullFace(CullMode cullMode) {
            this.cullMode = cullMode;
            return this;
        }

        @Override
        public BuilderOpenGL polygonMode(PolygonMode polygonMode) {
            this.polygonMode = polygonMode;
            return this;
        }

        @Override
        public Builder renderingMode(MeshRenderMode renderMode) {
            this.meshRenderMode = renderMode;
            return this;
        }

        @Override
        public OxyRenderPass create() {
            return new OxyRenderPass(frameBuffer, cullMode, polygonMode, meshRenderMode);
        }
    }

    public void beginRenderPass() {
        frameBuffer.bind();

        if (cullMode != CullMode.DISABLED) {
            glEnable(GL_CULL_FACE);
            glCullFace(cullMode.value);
        } else {
            glDisable(GL_CULL_FACE);
            glCullFace(CullMode.BACK.value);
        }
        glPolygonMode(GL_FRONT_AND_BACK, polygonMode.value);
    }

    public void endRenderPass() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glDisable(GL_CULL_FACE);
        frameBuffer.unbind();
    }

    public MeshRenderMode getMeshRenderingMode() {
        return meshRenderingMode;
    }
}
