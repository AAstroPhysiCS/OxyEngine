package OxyEngine;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Window.WindowBuilder;
import OxyEngine.Core.Window.OxyWindow;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.UI.UIThemeLoader;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.Objects;
import java.util.function.Supplier;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.glfw.GLFW.*;

public class OxyEngine implements OxyDisposable {

    private static OxyWindow oxyWindow;
    private static Antialiasing antialiasing;

    private final boolean vSync;
    private final boolean debug;

    private final Thread thread;

    private final TargetPlatform targetPlatform;

    private static final float[][] LOADED_THEME = UIThemeLoader.getInstance().load();

    public OxyEngine(Supplier<Runnable> supplier, OxyWindow oxyWindow, Antialiasing antialiasing, boolean vSync, boolean debug, TargetPlatform targetPlatform) {
        thread = new Thread(supplier.get(), "OxyEngine - 1");
        OxyEngine.oxyWindow = oxyWindow;
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

        OxyWindow.WindowSpecs specs = oxyWindow.getSpecs();
        WindowBuilder builder = new WindowBuilder.WindowFactory();
        builder.createHints()
                .resizable(specs.resizable())
                .doubleBuffered(specs.doubleBuffered())
                .colorBitsSetDefault()
                .create();
        oxyWindow.setPointer(switch (oxyWindow.getMode()) {
            case WINDOWED -> builder.createOpenGLWindow(oxyWindow.getWidth(), oxyWindow.getHeight(), oxyWindow.getTitle());
            case FULLSCREEN -> builder.createFullscreenOpenGLWindow(oxyWindow.getTitle());
            case WINDOWEDFULLSCREEN -> builder.createWindowedFullscreenOpenGLWindow(oxyWindow.getTitle());
        });
        oxyWindow.init();
        glfwSwapInterval(vSync ? 1 : 0);
        OxyRenderer.init(targetPlatform, debug);
    }

    @Override
    public void dispose() {
        oxyWindow.dispose();

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    public static Antialiasing getAntialiasing() {
        return antialiasing;
    }

    public static float[][] getLoadedTheme() {
        return LOADED_THEME;
    }

    public static OxyWindow getWindowHandle() {
        return oxyWindow;
    }
}
