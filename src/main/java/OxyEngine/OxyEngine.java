package OxyEngine;

import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.Core.Window.Window;
import OxyEngine.Core.Window.WindowBuilder;
import OxyEngine.System.Disposable;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.Objects;
import java.util.function.Supplier;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.glfw.GLFW.*;

public final class OxyEngine implements Disposable {

    private static Window window;
    private static Antialiasing antialiasing;

    private final boolean vSync;
    private final boolean debug;

    private final Thread thread;

    private final TargetPlatform targetPlatform;

    public OxyEngine(Supplier<Runnable> supplier, Window window, Antialiasing antialiasing, boolean vSync, boolean debug, TargetPlatform targetPlatform) {
        thread = new Thread(supplier.get(), "OxyEngine - 1");
        OxyEngine.window = window;
        this.vSync = vSync;
        this.debug = debug;
        this.targetPlatform = targetPlatform;
        OxyEngine.antialiasing = antialiasing;
    }

    public enum Antialiasing {
        ON(4), OFF(0);

        private final int level;

        Antialiasing(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    public void start() {
        thread.start();
    }

    public void init() {
        if (!glfwInit()) oxyAssert("Can't init GLFW");
        logger.info("GLFW init successful");
        GLFWErrorCallback.createPrint(System.err).set();

        Window.WindowSpecs specs = window.getSpecs();
        WindowBuilder builder = new WindowBuilder.WindowFactory();
        builder.createHints()
                .resizable(specs.resizable())
                .doubleBuffered(specs.doubleBuffered())
                .colorBitsSetDefault()
                .create();
        window.setPointer(switch (window.getMode()) {
            case WINDOWED -> builder.createOpenGLWindow(window.getWidth(), window.getHeight(), window.getTitle());
            case FULLSCREEN -> builder.createFullscreenOpenGLWindow(window.getTitle());
            case WINDOWEDFULLSCREEN -> builder.createWindowedFullscreenOpenGLWindow(window.getTitle());
        });
        window.init();
        glfwSwapInterval(vSync ? 1 : 0);
        Renderer.init(targetPlatform, debug);
        Renderer.enableGrid(true);
    }

    @Override
    public void dispose() {
        window.dispose();
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    public static Antialiasing getAntialiasing() {
        return antialiasing;
    }

    public static Window getWindowHandle() {
        return window;
    }
}
