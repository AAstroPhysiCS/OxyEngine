package OxyEngine.Core.Renderer.Buffer.Platform;

import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.OxyEngine;
import OxyEngineEditor.UI.Panels.ScenePanel;
import org.joml.Vector2f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static OxyEngine.System.OxySystem.oxyAssert;
import static OxyEngineEditor.Scene.SceneRuntime.ACTIVE_SCENE;
import static org.lwjgl.opengl.GL45.*;

public class OpenGLFrameBuffer extends FrameBuffer {

    private int idAttachment, idAttachmentFBO;

    OpenGLFrameBuffer(int width, int height) {
        super(width, height);
    }

    @Override
    public void load() {

        if (width <= 10 || height <= 10) {
            windowMinized = true;
            return;
        } else windowMinized = false;

        if (bufferId == 0) bufferId = glCreateFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, bufferId);

        int samples = OxyEngine.getAntialiasing().getLevel();

        colorAttachmentId = glCreateTextures(GL_TEXTURE_2D_MULTISAMPLE);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, colorAttachmentId);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, GL_RGBA8, width, height, true);
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, colorAttachmentId, 0);

        int rbo = glCreateRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rbo);
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, GL_DEPTH24_STENCIL8, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo);

        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE : oxyAssert("Framebuffer is incomplete!");

        intermediateFBO = glCreateFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, intermediateFBO);

        colorAttachmentTexture = glCreateTextures(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, colorAttachmentTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (FloatBuffer) null); //GL_RGBA8 for standard
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorAttachmentTexture, 0);

        glBindTexture(GL_TEXTURE_2D, 0);
        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE : oxyAssert("Framebuffer is incomplete!");
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        picking();
    }

    private void picking() {
        if (idAttachmentFBO == 0) idAttachmentFBO = glCreateFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, idAttachmentFBO);

        int colorAttachmentTexture = glCreateTextures(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, colorAttachmentTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (FloatBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorAttachmentTexture, 0);

        //ID Buffer
        idAttachment = glCreateTextures(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, idAttachment);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32I, width, height, 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, (IntBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, idAttachment, 0);

        var drawBuffers = new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1};
        glDrawBuffers(drawBuffers);

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

    public void bindPicking() {
        if (!windowMinized) {
            glBindFramebuffer(GL_FRAMEBUFFER, idAttachmentFBO);
            glViewport(0, 0, width, height);
        }
    }

    public int getEntityID() {
        Vector2f mousePos = new Vector2f(
                ScenePanel.mousePos.x - ScenePanel.windowPos.x - ScenePanel.offset.x,
                ScenePanel.mousePos.y - ScenePanel.windowPos.y - ScenePanel.offset.y);
        mousePos.y = ACTIVE_SCENE.getFrameBuffer().getHeight() - mousePos.y;
        glReadBuffer(GL_COLOR_ATTACHMENT1);
        int[] entityID = new int[1];
        glReadPixels((int) mousePos.x, (int) mousePos.y, 1, 1, GL_RED_INTEGER, GL_INT, entityID);
        return entityID[0];
    }

    @Override
    public void blit() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, bufferId);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, intermediateFBO);
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
    }

    public int getColorAttachmentTexture() {
        return colorAttachmentTexture;
    }

    public int getIdAttachmentFBO() {
        return idAttachmentFBO;
    }

    public int getIdAttachment() {
        return idAttachment;
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
        glDeleteTextures(colorAttachmentId);
        glDeleteTextures(colorAttachmentTexture);
    }
}
