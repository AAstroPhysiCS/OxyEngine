package OxyEngine.Core.Context.Renderer;

import OxyEngine.Core.Context.Renderer.Mesh.FrameBuffer;
import OxyEngine.Core.Context.Renderer.Texture.Color;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengl.GL44.glClearTexImage;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;

public final class OpenGLRendererAPI extends RendererAPI {

    OpenGLRendererAPI() {
    }

    @Override
    public void init(boolean debug) {
        //DEBUG
        if (debug) {
            glEnable(GL_DEBUG_OUTPUT);
            glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
            glDebugMessageCallback((source, type1, id, severity, length, message, userParam) -> {
                switch (severity) {
                    case GL_DEBUG_SEVERITY_HIGH -> logger.severe(MemoryUtil.memUTF8(memByteBuffer(message, length)));
                    case GL_DEBUG_SEVERITY_MEDIUM -> logger.warning(MemoryUtil.memUTF8(memByteBuffer(message, length)));
                    case GL_DEBUG_SEVERITY_LOW, GL_DEBUG_SEVERITY_NOTIFICATION -> logger.info(MemoryUtil.memUTF8(memByteBuffer(message, length)));
                }
            }, 0);
            glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer) null, false);
        }
    }

    @Override
    public void clearColor(Color clearColor) {
        glClearColor(clearColor.getRedChannel(), clearColor.getGreenChannel(), clearColor.getBlueChannel(), clearColor.getAlphaChannel());
    }

    @Override
    public void clearBuffer() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void beginRenderPass(RenderPass renderPass) {
        onStackRenderPass = renderPass;

        FrameBuffer frameBuffer = renderPass.getFrameBuffer();
        frameBuffer.bind();

        if (!frameBuffer.isFlushed()) frameBuffer.flush();

        CullMode cullMode = renderPass.getCullMode();
        if (cullMode != CullMode.DISABLED) {
            glEnable(GL_CULL_FACE);
            glCullFace(cullMode.value);
        }
        glPolygonMode(GL_FRONT_AND_BACK, renderPass.getPolygonMode().value);

        if (renderPass.isBlendingEnabled()) {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    @Override
    public void endRenderPass() {
        if (onStackRenderPass == null) throw new IllegalStateException("On Stack RenderPass is null!");
        if (onStackRenderPass.isBlendingEnabled()) glDisable(GL_BLEND);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        CullMode cullMode = onStackRenderPass.getCullMode();
        if (cullMode != CullMode.DISABLED) {
            glDisable(GL_CULL_FACE);
            glCullFace(CullMode.BACK.value);
        }

        onStackRenderPass.getFrameBuffer().unbind();
        onStackRenderPass = null;
    }

    public void drawArrays(int modeID, int first, int count) {
        glDrawArrays(modeID, first, count);
    }

    @Override
    public void drawElements(int modeID, int size, int type, int indices) {
        glDrawElements(modeID, size, type, indices);
    }

    @Override
    public void drawElementsIndexed(int modeID, int size, int type, int baseIndex, int baseVertex) {
        glDrawElementsBaseVertex(modeID, size, type, (long) baseIndex * Integer.BYTES, baseVertex);
    }

    @Override
    public void readBuffer(int attachment) {
        glReadBuffer(attachment);
    }

    @Override
    public void readPixels(int x,
                           int y,
                           int width,
                           int height,
                           int format,
                           int type,
                           int[] pixels) {
        glReadPixels(x, y, width, height, format, type, pixels);
    }

    public void clearTexImage(int texture,
                              int level,
                              int format,
                              int type,
                              int[] data) {
        glClearTexImage(texture, level, format, type, data);
    }

    @Override
    public void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format) {
        glBindImageTexture(unit, texture, level, layered, layer, access, format);
    }

    @Override
    public void dispatchCompute(int numGroupsX, int numGroupsY, int numGroupsZ) {
        glDispatchCompute(numGroupsX, numGroupsY, numGroupsZ);
    }

    @Override
    public void memoryBarrier(int barriers) {
        glMemoryBarrier(barriers);
    }
}
