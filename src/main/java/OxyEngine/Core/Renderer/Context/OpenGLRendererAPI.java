package OxyEngine.Core.Renderer.Context;

import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.Globals.normalizeColor;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;

public class OpenGLRendererAPI extends RendererAPI {

    OpenGLRendererAPI() {
    }

    @Override
    public void init(boolean debug) {
        //DEBUG
        if(debug) {
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
    public void clearColor(float r, float g, float b, float a) {
        if (r > 1 || b > 1 || g > 1 || a > 1) {
            r = normalizeColor(r);
            g = normalizeColor(g);
            b = normalizeColor(b);
            a = normalizeColor(a);
        }
        glClearColor(r, g, b, a);
    }

    @Override
    public void clearBuffer() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
}
