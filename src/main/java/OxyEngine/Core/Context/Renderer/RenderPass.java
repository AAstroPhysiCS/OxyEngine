package OxyEngine.Core.Context.Renderer;

import OxyEngine.Core.Context.Renderer.Mesh.FrameBuffer;
import OxyEngine.Core.Context.Renderer.Mesh.RenderMode;

public final class RenderPass {

    private final FrameBuffer frameBuffer;

    private final RenderMode meshRenderingMode;

    private final CullMode cullMode;
    private final PolygonMode polygonMode;

    private final boolean blending;

    public static Builder createBuilder(FrameBuffer frameBuffer) {
        return new BuilderOpenGL(frameBuffer);
    }

    private RenderPass(FrameBuffer frameBuffer, CullMode cullMode, PolygonMode polygonMode, RenderMode meshRenderingMode, boolean blending) {
        this.frameBuffer = frameBuffer;
        this.cullMode = cullMode;
        this.polygonMode = polygonMode;
        this.meshRenderingMode = meshRenderingMode;
        this.blending = blending;
    }

    public sealed interface Builder permits BuilderOpenGL {

        Builder setCullFace(CullMode cullMode);

        Builder polygonMode(PolygonMode polygonMode);

        Builder renderingMode(RenderMode renderMode);

        Builder enableBlending();

        RenderPass create();
    }

    public static final class BuilderOpenGL implements Builder {

        private final FrameBuffer frameBuffer;

        PolygonMode polygonMode = PolygonMode.FILL;
        CullMode cullMode = CullMode.DISABLED;
        RenderMode renderMode = RenderMode.TRIANGLES;
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
        public Builder renderingMode(RenderMode renderMode) {
            this.renderMode = renderMode;
            return this;
        }

        @Override
        public Builder enableBlending() {
            this.blending = true;
            return this;
        }

        @Override
        public RenderPass create() {
            return new RenderPass(frameBuffer, cullMode, polygonMode, renderMode, blending);
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

    public RenderMode getRenderMode() {
        return meshRenderingMode;
    }
}
