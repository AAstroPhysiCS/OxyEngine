package OxyEngine.Core.Context.Renderer.Buffer.Platform;

import OxyEngine.Core.Context.Renderer.Buffer.RenderBuffer;

import java.util.HashMap;
import java.util.Map;

public class FrameBufferSpecification {

    int attachmentIndex = -1;
    boolean multiSampled;
    TextureFormat textureFormat;
    int paramMinFilter = -1, paramMagFilter = -1;

    int wrapS = -1, wrapT = -1, wrapR = -1;

    int[] colorAttachmentTextures = null;
    boolean disableReadWriteBuffer;

    boolean isStorage;
    int level = -1;

    RenderBuffer renderBuffer = null;

    int textureCount = -1;

    final Map<Integer, int[]> sizeForTextures = new HashMap<>();

    public FrameBufferSpecification setTextureCount(int textureCount){
        this.textureCount = textureCount;
        colorAttachmentTextures = new int[textureCount];
        return this;
    }

    public FrameBufferSpecification setAttachmentIndex(int attachmentIndex) {
        this.attachmentIndex = attachmentIndex;
        return this;
    }

    public FrameBufferSpecification setFormat(TextureFormat textureFormat) {
        this.textureFormat = textureFormat;
        return this;
    }

    public FrameBufferSpecification setMultiSampled(boolean multiSampled) {
        this.multiSampled = multiSampled;
        return this;
    }

    public FrameBufferSpecification disableReadWriteBuffer(boolean disableReadWriteBuffer){
        this.disableReadWriteBuffer = disableReadWriteBuffer;
        return this;
    }

    public FrameBufferSpecification setFilter(int paramMinFilter, int paramMagFilter) {
        this.paramMagFilter = paramMagFilter;
        this.paramMinFilter = paramMinFilter;
        return this;
    }

    public FrameBufferSpecification setSizeForTextures(int textureIndex, int width, int height){
        sizeForTextures.put(textureIndex, new int[]{width, height});
        return this;
    }

    public FrameBufferSpecification wrapSTR(int s, int t, int r){
        this.wrapS = s;
        this.wrapT = t;
        this.wrapR = r;
        return this;
    }

    public FrameBufferSpecification setStorage(boolean storage, int level) {
        this.isStorage = storage;
        this.level = level;
        return this;
    }

    public FrameBufferSpecification useRenderBuffer(RenderBuffer renderBuffer) {
        this.renderBuffer = renderBuffer;
        return this;
    }
}