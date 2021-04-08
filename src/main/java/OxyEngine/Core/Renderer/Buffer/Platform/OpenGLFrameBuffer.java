package OxyEngine.Core.Renderer.Buffer.Platform;

import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.OxyEngine;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL45.*;

public class OpenGLFrameBuffer extends FrameBuffer {

    public static <T> T createNewSpec(Class<T> tClass) {
        try {
            return tClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Spec Builder should not be empty!");
    }

    private final FrameBufferSpecification[] specs;

    OpenGLFrameBuffer(int width, int height, FrameBufferSpecification... specs) {
        super(width, height);
        this.specs = specs;
        load();
    }

    private int getTargetTexture(FrameBufferSpecification spec) {
        return spec.multiSampled ? GL_TEXTURE_2D_MULTISAMPLE : GL_TEXTURE_2D;
    }

    private void texImage2D(FrameBufferSpecification spec, int targetTexture) {
        if (spec.textureFormat == null) return;
        if (spec.multiSampled) {
            int samples = OxyEngine.getAntialiasing().getLevel();
            glTexImage2DMultisample(targetTexture, samples, spec.textureFormat.internalFormatInteger, width, height, true);
        } else {
            glTexImage2D(targetTexture, 0, spec.textureFormat.internalFormatInteger, width, height, 0, spec.textureFormat.storageFormat, GL_UNSIGNED_BYTE, (FloatBuffer) null); //GL_RGBA8 for standard
        }
    }

    private void renderBufferStorage(FrameBufferSpecification spec) {
        if (spec.multiSampled) {
            int samples = OxyEngine.getAntialiasing().getLevel();
            glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, spec.renderBufferFormat.internalFormatInteger, width, height);
        } else {
            glRenderbufferStorage(GL_RENDERBUFFER, spec.renderBufferFormat.internalFormatInteger, width, height);
        }
    }

    private void storage(FrameBufferSpecification spec, int targetTexture, int colorAttachmentTexture) {
        if (spec.isStorage) {
            glTexStorage2D(targetTexture, spec.level, GL_DEPTH24_STENCIL8, width, height);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, targetTexture, colorAttachmentTexture, 0);
        }
    }

    private int[] drawIndices = null;

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

    private void disableDrawReadBuffer(FrameBufferSpecification spec){
        if(spec.disableReadWriteBuffer){
            glDrawBuffer(GL_NONE);
            glReadBuffer(GL_NONE);
        }
    }

    private void textureParameters(FrameBufferSpecification spec, int targetTexture) {
        if (spec.paramMinFilter != -1) glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, spec.paramMinFilter);
        if (spec.paramMagFilter != -1) glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, spec.paramMagFilter);
        if (spec.textureFormat == FrameBufferTextureFormat.DEPTHCOMPONENT32) glTexParameteri(targetTexture, GL_TEXTURE_COMPARE_MODE, GL_NONE);
        if (spec.wrapS != -1) glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, spec.wrapS);
        if (spec.wrapT != -1) glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, spec.wrapT);
        if (spec.wrapR != -1) glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, spec.wrapR);
    }

    private final List<int[]> colorAttachments = new ArrayList<>();

    @Override
    public void load() {
        if (width <= 10 || height <= 10) {
            windowMinized = true;
            return;
        } else windowMinized = false;

        if (bufferId == 0) bufferId = glCreateFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, bufferId);
        for (int i = 0; i < specs.length; i++)  {
            FrameBufferSpecification fbS = specs[i];
            int targetTexture = getTargetTexture(fbS);
            glCreateTextures(targetTexture, fbS.colorAttachmentTextures);
            for(int colorAttachmentTexture : fbS.colorAttachmentTextures) {
                glBindTexture(targetTexture, colorAttachmentTexture);
                texImage2D(fbS, targetTexture);
                textureParameters(fbS, targetTexture);
                if (!fbS.isStorage)
                    glFramebufferTexture2D(GL_FRAMEBUFFER, fbS.textureFormat == FrameBufferTextureFormat.DEPTHCOMPONENT32 ? GL_DEPTH_ATTACHMENT : GL_COLOR_ATTACHMENT0 + fbS.attachmentIndex, targetTexture, colorAttachmentTexture, 0);
                if (fbS.renderBuffered) {
                    int rbo = glCreateRenderbuffers();
                    glBindRenderbuffer(GL_RENDERBUFFER, rbo);
                    renderBufferStorage(fbS);
                    glBindRenderbuffer(GL_RENDERBUFFER, 0);
                    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo);
                }
                storage(fbS, targetTexture, colorAttachmentTexture);
                glBindTexture(targetTexture, 0);
            }
            if (fbS.attachmentIndex != -1) colorAttachments.add(fbS.attachmentIndex, fbS.colorAttachmentTextures);
            disableDrawReadBuffer(specs[i]);
        }
        if (drawIndices != null) drawBuffers();

        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE : oxyAssert("Framebuffer is incomplete!");
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void bind() {
        if (!windowMinized) {
            glBindFramebuffer(GL_FRAMEBUFFER, bufferId);
            glViewport(0, 0, width, height);
        }
    }

    @Override
    public void bindDepthAttachment(int specIndex, int index) {
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, colorAttachments.get(specIndex)[index], 0);
    }

    @Override
    public void bindColorAttachment(int specIndex, int index) {
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + index, GL_TEXTURE_2D, colorAttachments.get(specIndex)[index], 0);
    }

    public static void blit(OpenGLFrameBuffer srcBuffer, OpenGLFrameBuffer destBuffer) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, srcBuffer.getBufferId());
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, destBuffer.getBufferId());
        glBlitFramebuffer(0, 0, srcBuffer.getWidth(), srcBuffer.getHeight(), 0, 0, destBuffer.getWidth(), destBuffer.getHeight(), GL_COLOR_BUFFER_BIT, GL_NEAREST);
    }

    @Override
    public void resize(float width, float height) {
        this.width = (int) width;
        this.height = (int) height;

        load(); //load it again, so that it has the new width/height values.
    }

    @Override
    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void dispose() {
        glDeleteFramebuffers(bufferId);
    }

    public int[] getColorAttachmentTexture(int index) {
        if (colorAttachments.size() == 0) return null;
        return colorAttachments.get(index);
    }

    public FrameBufferTextureFormat getTextureFormat(int index) {
        if (specs[index].textureFormat == null) {
            logger.warning("Accessing a buffer which is null");
            return null;
        }
        return specs[index].textureFormat;
    }

    public FrameBufferTextureFormat getRenderBufferFormat(int index) {
        if (specs[index].renderBufferFormat == null) {
            logger.warning("Accessing a buffer which is null");
            return null;
        }
        return specs[index].renderBufferFormat;
    }
}