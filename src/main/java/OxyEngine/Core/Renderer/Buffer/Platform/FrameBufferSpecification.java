package OxyEngine.Core.Renderer.Buffer.Platform;

public class FrameBufferSpecification {

    int attachmentIndex = -1;
    boolean multiSampled, renderBuffered;
    FrameBufferTextureFormat textureFormat, renderBufferFormat;
    int paramMinFilter = -1, paramMagFilter = -1;

    int colorAttachmentTexture = -1;

    boolean isStorage;
    int level = -1;

    public FrameBufferSpecification setAttachmentIndex(int attachmentIndex) {
        this.attachmentIndex = attachmentIndex;
        return this;
    }

    public FrameBufferSpecification setFormats(FrameBufferTextureFormat textureFormat, FrameBufferTextureFormat renderBufferFormat) {
        this.textureFormat = textureFormat;
        this.renderBufferFormat = renderBufferFormat;
        return this;
    }

    public FrameBufferSpecification setFormats(FrameBufferTextureFormat textureFormat) {
        this.textureFormat = textureFormat;
        return this;
    }

    public FrameBufferSpecification setMultiSampled(boolean multiSampled) {
        this.multiSampled = multiSampled;
        return this;
    }

    public FrameBufferSpecification useRenderBuffer(boolean renderBuffered) {
        this.renderBuffered = renderBuffered;
        return this;
    }

    public FrameBufferSpecification setFilter(int paramMinFilter, int paramMagFilter) {
        this.paramMagFilter = paramMagFilter;
        this.paramMinFilter = paramMinFilter;
        return this;
    }

    public FrameBufferSpecification setStorage(boolean storage, int level) {
        this.isStorage = storage;
        this.level = level;
        return this;
    }
}
