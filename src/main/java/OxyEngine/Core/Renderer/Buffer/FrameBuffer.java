package OxyEngine.Core.Renderer.Buffer;

import java.nio.ByteBuffer;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.glTexStorage2D;
import static org.lwjgl.opengl.GL45.glCreateFramebuffers;
import static org.lwjgl.opengl.GL45.glCreateTextures;

public class FrameBuffer extends Buffer {

    private int colorAttachmentId, depthAttachment;

    private int width;
    private int height;

    private static boolean windowMinized;

    public FrameBuffer(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void load() {

        if (width <= 10 || height <= 10) {
            windowMinized = true;
            return;
        } else windowMinized = false;

        if(bufferId == 0) bufferId = glCreateFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, bufferId);

        colorAttachmentId = glCreateTextures(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, colorAttachmentId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorAttachmentId, 0);

        depthAttachment = glCreateTextures(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, depthAttachment);
        glTexStorage2D(GL_TEXTURE_2D, 1, GL_DEPTH24_STENCIL8, width, height);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, depthAttachment, 0);

        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE : oxyAssert("Framebuffer is incomplete!");
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void bind() {
        if (!windowMinized) {
            glBindFramebuffer(GL_FRAMEBUFFER, bufferId);
            glViewport(0, 0, width, height);
        }
    }

    public void resize(float width, float height) {
        this.width = (int) width;
        this.height = (int) height;

        load(); //load it again, so that it has the new width/height values.
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getColorAttachment() {
        return colorAttachmentId;
    }

    public int getDepthAttachment() {
        return depthAttachment;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void dispose() {
        glDeleteFramebuffers(bufferId);
        glDeleteTextures(colorAttachmentId);
        glDeleteTextures(depthAttachment);
    }
}
