package OxyEngine;

import OxyEngine.Core.Renderer.Context.OxyRenderCommand;
import OxyEngine.Core.Renderer.Context.RendererAPI;
import OxyEngine.Core.Renderer.Context.RendererContext;
import OxyEngine.Core.Window.WindowBuilder;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.UI.UIThemeLoader;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.Objects;
import java.util.function.Supplier;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.glfw.GLFW.*;

public class OxyEngine implements OxyDisposable {

    private static WindowHandle windowHandle;
    private static Antialiasing antialiasing;

    private final boolean vSync;
    private final boolean debug;

    private final Thread thread;

    private static final float[][] LOADED_THEME = UIThemeLoader.getInstance().load();

    public OxyEngine(Supplier<Runnable> supplier, WindowHandle windowHandle, Antialiasing antialiasing, boolean vSync, boolean debug, TargetPlatform targetPlatform) {
        thread = new Thread(supplier.get(), "OxyEngine - 1");
        OxyEngine.windowHandle = windowHandle;
        this.vSync = vSync;
        this.debug = debug;
        OxyEngine.antialiasing = antialiasing;

        OxyRenderCommand.getInstance(RendererContext.getContext(targetPlatform), RendererAPI.getContext(targetPlatform));
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

        WindowHandle.WindowSpecs specs = windowHandle.getSpecs();
        WindowBuilder builder = new WindowBuilder.WindowFactory();
        builder.createHints()
                .resizable(specs.resizable())
                .doubleBuffered(specs.doubleBuffered())
                .colorBitsSetDefault()
                .create();
        windowHandle.setPointer(switch (windowHandle.getMode()) {
            case WINDOWED -> builder.createOpenGLWindow(windowHandle.getWidth(), windowHandle.getHeight(), windowHandle.getTitle());
            case FULLSCREEN -> builder.createFullscreenOpenGLWindow(windowHandle.getTitle());
            case WINDOWEDFULLSCREEN -> builder.createWindowedFullscreenOpenGLWindow(windowHandle.getTitle());
        });
        windowHandle.init();
        glfwSwapInterval(vSync ? 1 : 0);

        OxyRenderCommand.init(debug);
    }

    @Override
    public void dispose() {
        windowHandle.dispose();

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    public static Antialiasing getAntialiasing() {
        return antialiasing;
    }

    public Thread getMainThread() {
        return thread;
    }

    public static float[][] getLoadedTheme() {
        return LOADED_THEME;
    }

    public static WindowHandle getWindowHandle() {
        return windowHandle;
    }
}
