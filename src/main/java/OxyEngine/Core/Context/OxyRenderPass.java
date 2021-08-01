package OxyEngine.Core.Context;

import OxyEngine.Core.Context.Renderer.Mesh.FrameBuffer;
import OxyEngine.Core.Context.Renderer.Mesh.MeshRenderMode;

public class OxyRenderPass {

    private final FrameBuffer frameBuffer;

    private final MeshRenderMode meshRenderingMode;

    private final CullMode cullMode;
    private final PolygonMode polygonMode;

    private final boolean blending;

    public static Builder createBuilder(FrameBuffer frameBuffer) {
        return new BuilderOpenGL(frameBuffer);
    }

    private OxyRenderPass(FrameBuffer frameBuffer, CullMode cullMode, PolygonMode polygonMode, MeshRenderMode meshRenderingMode, boolean blending) {
        this.frameBuffer = frameBuffer;
        this.cullMode = cullMode;
        this.polygonMode = polygonMode;
        this.meshRenderingMode = meshRenderingMode;
        this.blending = blending;
    }

    public sealed interface Builder permits BuilderOpenGL {

        Builder setCullFace(CullMode cullMode);

        Builder polygonMode(PolygonMode polygonMode);

        Builder renderingMode(MeshRenderMode renderMode);

        Builder enableBlending();

        OxyRenderPass create();
    }

    public static final class BuilderOpenGL implements Builder {

        private final FrameBuffer frameBuffer;

        PolygonMode polygonMode = PolygonMode.FILL;
        CullMode cullMode = CullMode.DISABLED;
        MeshRenderMode meshRenderMode = MeshRenderMode.TRIANGLES;
        boolean blending = false;

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
        public Builder enableBlending() {
            this.blending = true;
            return this;
        }

        @Override
        public OxyRenderPass create() {
            return new OxyRenderPass(frameBuffer, cullMode, polygonMode, meshRenderMode, blending);
        }
    }

    CullMode getCullMode() {
        return cullMode;
    }

    PolygonMode getPolygonMode() {
        return polygonMode;
    }

    FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    boolean isBlendingEnabled() {
        return blending;
    }

    public MeshRenderMode getMeshRenderingMode() {
        return meshRenderingMode;
    }
}
