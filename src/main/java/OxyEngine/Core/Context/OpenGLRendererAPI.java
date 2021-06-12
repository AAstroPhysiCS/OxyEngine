package OxyEngine.Core.Context;

import OxyEngine.Core.Context.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Context.Renderer.Texture.OxyColor;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;

public class OpenGLRendererAPI extends RendererAPI {

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
    public void clearColor(OxyColor clearColor) {
        glClearColor(clearColor.getRedChannel(), clearColor.getGreenChannel(), clearColor.getBlueChannel(), clearColor.getAlphaChannel());
    }

    @Override
    public void clearBuffer() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void beginRenderPass(OxyRenderPass renderPass) {
        onStackRenderPass = renderPass;

        FrameBuffer frameBuffer = renderPass.getFrameBuffer();
        frameBuffer.bind();

        if (!frameBuffer.isFlushed()) frameBuffer.flush();

        CullMode cullMode = renderPass.getCullMode();
        if (cullMode != CullMode.DISABLED) {
            glEnable(GL_CULL_FACE);
            glCullFace(cullMode.value);
        } else {
            glDisable(GL_CULL_FACE);
            glCullFace(CullMode.BACK.value);
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
        glDisable(GL_CULL_FACE);
        glCullFace(CullMode.BACK.value);
        onStackRenderPass.getFrameBuffer().unbind();
        onStackRenderPass = null;
    }
}
