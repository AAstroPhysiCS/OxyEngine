package OxyEngineEditor.Scene;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Core.Threading.OxySubThread;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.UI.Panels.Panel;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

public final class SceneRuntime {

    private static final SceneRuntimeControlPanel panel = new SceneRuntimeControlPanel();

    public static OxyCamera currentBoundedCamera;
    public static Scene ACTIVE_SCENE;

    public SceneRuntime(Scene scene) {
        ACTIVE_SCENE = scene;
    }

    private static final class SceneRuntimeControlPanel extends Panel {

        private static final ImageTexture playTexture = OxyTexture.loadImage(-1, "src/main/resources/assets/play.png");
        private static final ImageTexture stopTexture = OxyTexture.loadImage(-1, "src/main/resources/assets/stop.png");

        @Override
        public void preload() {

        }

        @SuppressWarnings("SuspiciousNameCombination")
        @Override
        public void renderPanel() {
            ImGui.pushStyleColor(ImGuiCol.Button, 36, 36, 36, 0);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, 36, 36, 36, 0);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 36, 36, 36, 0);
            ImGui.begin("###hidelabel", ImGuiWindowFlags.NoTitleBar |
                    ImGuiWindowFlags.NoScrollbar |
                    ImGuiWindowFlags.NoResize |
                    ImGuiWindowFlags.NoMove
            );

            float height = ImGui.getWindowHeight() - 10;
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 253, 76, 61, 255);
            ImGui.sameLine(20);
            ImGui.setCursorPosY(5);
            if (ImGui.imageButton(playTexture.getTextureId(), height, height, 0, 1, 1, 0, 1)) {
                SceneRuntime.onCreate();
                running = true;
                resume();
            }
            ImGui.sameLine(60);
            if (ImGui.imageButton(stopTexture.getTextureId(), height, height, 0, 1, 1, 0, 1))
                SceneRuntime.stop();
            ImGui.popStyleColor(4);
            ImGui.end();
        }
    }

    static boolean running = false; //for MainThread

    static void onCreate() {
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                OxyScript.EntityInfoProvider provider = c.getProvider();
                if (provider == null) continue;
                provider.invokeCreate();
            }
        }
    }

    public static void onUpdate(float ts) {
        if (!running) return;
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                OxyScript.EntityInfoProvider provider = c.getProvider();
                if (provider == null) continue;
                if (c.getOxySubThread() == null) {
                    OxySubThread subThread = new OxySubThread();
                    subThread.setTarget(() -> {
                        while(subThread.getRunningState().get()) provider.invokeUpdate(ts);
                    });
                    c.setOxySubThread(subThread);
                    subThread.start();
                }
            }
        }
    }

    static void stop() {
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                if(c.getOxySubThread() != null)  c.getOxySubThread().stop();
            }
        }
    }

    public static void resume() {
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                if(c.getOxySubThread() != null) c.getOxySubThread().restart();
            }
        }
    }

    public static void dispose() {
        running = false;
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                c.dispose();
            }
        }
    }

    public static SceneRuntimeControlPanel getPanel() {
        return panel;
    }
}
