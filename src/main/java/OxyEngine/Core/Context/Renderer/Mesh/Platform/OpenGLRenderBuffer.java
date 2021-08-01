package OxyEngine.Core.Context.Renderer.Mesh.Platform;

import OxyEngine.Core.Context.Renderer.Mesh.RenderBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.glCreateRenderbuffers;

public class OpenGLRenderBuffer extends RenderBuffer {

    public OpenGLRenderBuffer(TextureFormat textureFormat, int width, int height) {
        super(textureFormat, -1, width, height);
    }

    public OpenGLRenderBuffer(TextureFormat textureFormat, int samples, int width, int height) {
        super(textureFormat, samples, width, height);
    }

    @Override
    protected void load() {
        if (bufferId == 0) bufferId = glCreateRenderbuffers();
    }

    @Override
    public void bind() {
        glBindRenderbuffer(GL_RENDERBUFFER, bufferId);
    }

    @Override
    public void unbind() {
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
    }

    @Override
    public void loadStorageWithSamples(int samples, int width, int height) {
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, textureFormat.getInternalFormat(), width, height);
    }

    @Override
    public void loadStorage(int width, int height) {
        glRenderbufferStorage(GL_RENDERBUFFER, textureFormat.getInternalFormat(), width, height);
    }

    @Override
    public void dispose() {
        glDeleteRenderbuffers(bufferId);
        bufferId = 0;
    }

    public void resize(int newWidth, int newHeight) {
        this.width = newWidth;
        this.height = newHeight;

        bind();
        if (samples != -1) loadStorageWithSamples(samples, width, height);
        else loadStorage(width, height);
        unbind();
    }
}
