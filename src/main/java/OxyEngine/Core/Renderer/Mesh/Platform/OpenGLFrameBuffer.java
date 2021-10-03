package OxyEngine.Core.Renderer.Mesh.Platform;

import OxyEngine.Core.Renderer.Mesh.FrameBuffer;
import OxyEngine.Core.Renderer.Mesh.RenderBuffer;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Renderer.Texture.Color;
import OxyEngine.OxyEngine;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL45.*;

public final class OpenGLFrameBuffer extends FrameBuffer {

    private final FrameBufferSpecification[] specs;

    private final List<int[]> colorAttachments = new ArrayList<>();

    private int[] drawIndices = null;

    OpenGLFrameBuffer(int width, int height, Color clearColor, FrameBufferSpecification... specs) {
        super(width, height, clearColor);
        this.specs = specs;
        load();
    }

    private static final int WIDTH_HEIGHT_IGNORE = -1;

    OpenGLFrameBuffer(Color clearColor, FrameBufferSpecification... specs) {
        super(WIDTH_HEIGHT_IGNORE, WIDTH_HEIGHT_IGNORE, clearColor);
        this.specs = specs;
        load();
    }

    private int getTargetTexture(FrameBufferSpecification spec) {
        return spec.multiSampled ? GL_TEXTURE_2D_MULTISAMPLE : GL_TEXTURE_2D;
    }

    private void texImage2D(FrameBufferSpecification spec, int targetTexture, int width, int height) {
        if (spec.textureFormat == null) return;
        if (spec.multiSampled) {
            int samples = OxyEngine.getAntialiasing().getLevel();
            glTexImage2DMultisample(targetTexture, samples, spec.textureFormat.internalFormatInteger, width, height, true);
        } else {
            glTexImage2D(targetTexture, 0, spec.textureFormat.internalFormatInteger, width, height, 0, spec.textureFormat.storageFormat, spec.texelDataType, (FloatBuffer) null); //GL_RGBA8 for standard
        }
    }

    private void storage(FrameBufferSpecification spec, int targetTexture, int colorAttachmentTexture, int width, int height) {
        if (spec.isStorage) {
            glTexStorage2D(targetTexture, spec.level, GL_DEPTH24_STENCIL8, width, height);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, targetTexture, colorAttachmentTexture, 0);
        }
    }

    public void drawBuffers(int... buffers) {
        this.drawIndices = buffers;
    }

    private void drawBuffers() {
        //copying bcs i dont want to increment the srcBuffer every time i am loading the framebuffer again.
        int[] copiedDrawIndices = new int[drawIndices.length];
        System.arraycopy(drawIndices, 0, copiedDrawIndices, 0, copiedDrawIndices.length);
        for (int i = 0; i < drawIndices.length; i++) copiedDrawIndices[i] += GL_COLOR_ATTACHMENT0;
        glDrawBuffers(copiedDrawIndices);

    }

    private void disableDrawReadBuffer(FrameBufferSpecification spec) {
        if (spec.disableReadWriteBuffer) {
            glDrawBuffer(GL_NONE);
            glReadBuffer(GL_NONE);
        }
    }

    private void textureParameters(FrameBufferSpecification spec) {
        if (spec.paramMinFilter != -1) glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, spec.paramMinFilter);
        if (spec.paramMagFilter != -1) glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, spec.paramMagFilter);
        if (spec.wrapS != -1) glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, spec.wrapS);
        if (spec.wrapT != -1) glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, spec.wrapT);
        if (spec.wrapR != -1) glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, spec.wrapR);
    }

    public void attachRenderBuffer(RenderBuffer renderBuffer) {
        renderBuffer.bind();
        renderBuffer.loadStorage(renderBuffer.width, renderBuffer.height);
        renderBuffer.unbind();
        attachRenderBufferToFrameBuffer(renderBuffer.getFormat().getStorageFormat(), renderBuffer);
    }

    public void attachRenderBuffer(RenderBuffer renderBuffer, final int samples) {
        renderBuffer.bind();
        renderBuffer.loadStorageWithSamples(samples, renderBuffer.width, renderBuffer.height);
        renderBuffer.unbind();
        attachRenderBufferToFrameBuffer(renderBuffer.getFormat().getStorageFormat(), renderBuffer);
    }

    public void load() {
        if (width != WIDTH_HEIGHT_IGNORE && height != WIDTH_HEIGHT_IGNORE) {
            if (width <= 10 || height <= 10) {
                windowMinized = true;
                return;
            } else windowMinized = false;
        }

        if (bufferId == 0) bufferId = glCreateFramebuffers();
        create();
    }

    private void create() {
        glBindFramebuffer(GL_FRAMEBUFFER, bufferId);

        if (specs != null) {
            for (int i = 0; i < specs.length; i++) {
                FrameBufferSpecification fbS = specs[i];
                RenderBuffer renderBuffer = fbS.renderBuffer;

                if ((renderBuffer != null) && !renderBuffer.isNull()) {
                    if (fbS.multiSampled) attachRenderBuffer(renderBuffer, OxyEngine.getAntialiasing().getLevel());
                    else attachRenderBuffer(renderBuffer);
                }

                if (fbS.colorAttachmentTextures == null)
                    continue;

                if (fbS.colorAttachmentTextures.length > 0) {
                    int targetTexture = getTargetTexture(fbS);

                    //means that we haven't created the texture
                    if (!colorAttachments.contains(fbS.colorAttachmentTextures))
                        glCreateTextures(targetTexture, fbS.colorAttachmentTextures);

                    for (int j = 0; j < fbS.colorAttachmentTextures.length; j++) {
                        int colorAttachmentTexture = fbS.colorAttachmentTextures[j];

                        int width = this.width; //assume that the indexCount is not set on the texture attachments
                        int height = this.height;

                        if (fbS.sizeForTextures.size() != 0) {
                            if (fbS.sizeForTextures.get(j)[0] != -1 && fbS.sizeForTextures.get(j)[1] != -1) {
                                width = fbS.sizeForTextures.get(j)[0];
                                height = fbS.sizeForTextures.get(j)[1];
                            }
                        }

                        glBindTexture(targetTexture, colorAttachmentTexture);

                        texImage2D(fbS, targetTexture, width, height);
                        textureParameters(fbS);

                        if (!fbS.isStorage) {
                            switch (fbS.textureFormat) {
                                case DEPTH24STENCIL8, DEPTHCOMPONENT24, DEPTHCOMPONENT32, DEPTHCOMPONENT32COMPONENT, DEPTHCOMPONENT -> glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, targetTexture, colorAttachmentTexture, 0);
                                case R32I, RGB16F, RGBA16, RGBA8, RGB32F -> glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + fbS.attachmentIndex, targetTexture, colorAttachmentTexture, 0);
                                default -> throw new IllegalStateException("Format not defined!");
                            }
                        }

                        storage(fbS, targetTexture, colorAttachmentTexture, width, height);
                        glBindTexture(targetTexture, 0);
                    }

                    if (fbS.attachmentIndex != -1)
                        colorAttachments.add(fbS.attachmentIndex, fbS.colorAttachmentTextures);
                    else logger.severe("Attachment index is null");
                }

                disableDrawReadBuffer(specs[i]);
            }
        }

        if (drawIndices != null) drawBuffers();

        checkStatus();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    /*
     * This method should be called when you reuse a framebuffer
     */

    @Override
    public void bind() {
        if (!windowMinized) {
            glBindFramebuffer(GL_FRAMEBUFFER, bufferId);
            //we dont want to change the viewport when the width and height arent specified
            //those framebuffers do need to have their own ways to deal with the viewport
            if (width != WIDTH_HEIGHT_IGNORE && height != WIDTH_HEIGHT_IGNORE) glViewport(0, 0, width, height);
        }
    }

    @Override
    public void flushDepthAttachment(int specIndex, int index) {
        bindDepthAttachment(specIndex, index);
        Renderer.clearBuffer();
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, 0, 0);
    }

    @Override
    public void bindDepthAttachment(int specIndex, int index) {
        int[] size = new int[]{this.width, this.height};
        if(specs[specIndex].sizeForTextures.size() != 0)
            size = specs[specIndex].sizeForTextures.get(index);
        glViewport(0, 0, size[0], size[1]);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, colorAttachments.get(specIndex)[index], 0);
    }

    @Override
    public void bindColorAttachment(int specIndex, int indexOfColorAttachment) {
        int[] size = new int[]{this.width, this.height};
        if(specs[specIndex].sizeForTextures.size() != 0)
            size = specs[specIndex].sizeForTextures.get(indexOfColorAttachment);
        glViewport(0, 0, size[0], size[1]);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + indexOfColorAttachment, GL_TEXTURE_2D, colorAttachments.get(specIndex)[indexOfColorAttachment], 0);
    }

    public void attachColorAttachment(int textarget, int indexOfColorAttachment, int textureId) {
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + indexOfColorAttachment,
                textarget, textureId, 0);
    }

    public void attachColorAttachment(int textarget, int indexOfColorAttachment, int textureId, int mip) {
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + indexOfColorAttachment,
                textarget, textureId, mip);
    }

    @Override
    protected void attachRenderBufferToFrameBuffer(int attachmentId, RenderBuffer renderBuffer) {
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, attachmentId, GL_RENDERBUFFER, renderBuffer.getBufferId());
    }

    @Override
    public void checkStatus() {
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        assert status == GL_FRAMEBUFFER_COMPLETE : oxyAssert("Framebuffer is incomplete! Error Code: 0x" + Integer.toHexString(status));
    }

    @Override
    public void blit() {
        if (blitted == null)
            throw new IllegalStateException("Framebuffer blitting failed because there's no blitting framebuffer");
        glBindFramebuffer(GL_READ_FRAMEBUFFER, bufferId);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, blitted.getBufferId());
        glBlitFramebuffer(0, 0, getWidth(), getHeight(), 0, 0, getWidth(), getHeight(), GL_COLOR_BUFFER_BIT, GL_NEAREST);
    }

    @Override
    public void resize(int width, int height) {
        //for shadow map
        if (WIDTH_HEIGHT_IGNORE == this.width && WIDTH_HEIGHT_IGNORE == this.height) return;
        this.width = width;
        this.height = height;

        for (FrameBufferSpecification fbS : specs) {
            if (fbS.renderBuffer != null)
                fbS.renderBuffer.resize(width, height);
        }

        load(); //load it again, so that it has the new width/height values.
        needResize = false;

        if (blitted != null) blitted.resize(width, height);
    }

    @Override
    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void dispose() {
        glDeleteFramebuffers(bufferId);
        for (FrameBufferSpecification spec : specs) {
            RenderBuffer renderBuffer = spec.renderBuffer;
            if (renderBuffer != null) renderBuffer.dispose();
        }
    }

    public int[] getColorAttachmentTexture(int index) {
        if (colorAttachments.size() == 0) return null;
        return colorAttachments.get(index);
    }

    public TextureFormat getTextureFormat(int index) {
        if (specs[index].textureFormat == null) {
            logger.warning("Accessing a buffer which is null");
            return null;
        }
        return specs[index].textureFormat;
    }

    @Override
    public void flush() {
        bind();
        Renderer.clearColor(clearColor);
        Renderer.clearBuffer();
        flushed = true;
    }

    public OpenGLRenderBuffer getRenderBuffer(int i) {
        return (OpenGLRenderBuffer) specs[i].renderBuffer;
    }
}