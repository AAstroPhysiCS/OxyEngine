package OxyEngine.Core.Context.Scene;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Context.Renderer.Texture.Image2DTexture;
import OxyEngine.PhysX.OxyPhysX;
import OxyEngine.Scripting.OxyScript;
import OxyEngine.Scripting.ScriptEngine;
import OxyEngineEditor.UI.Panels.Panel;
import OxyEngineEditor.UI.UIAssetManager;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static OxyEngine.System.OxyFileSystem.deleteDir;

public final class SceneRuntime {

    private static final RuntimeControlPanel panel = new RuntimeControlPanel();

    public static OxyCamera currentBoundedCamera;
    public static OxyNativeObject currentBoundedSkyLightEntity;

    public static OxyEntity entityContext;
    public static OxyMaterial materialContext;
    public static Scene ACTIVE_SCENE;

    public static float FPS = 0;
    public static float FRAME_TIME = 0;
    public static float TS = 0;

    private SceneRuntime() {
    }

    public static Object loadClass(String path, String packageName, Scene scene, OxyEntity entity) {
        File f = new File(path);
        try {
            URL url = f.toURI().toURL();
            try (URLClassLoader loader = new URLClassLoader(new URL[]{url})) {
                return loader.loadClass(packageName).getDeclaredConstructor(Scene.class, OxyEntity.class).newInstance(scene, entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void onPlay() {
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                c.invokeCreate();
                ScriptEngine.addProvider(c.getProvider());
            }
        }
        ACTIVE_SCENE.STATE = SceneState.RUNNING;
        ScriptEngine.restart();
        OxyPhysX.getInstance().onScenePlay();
        System.gc();
    }

    public static void onUpdate(float ts) {
        TS = ts;
        ScriptEngine.onUpdate();
    }

    public static void onStop() {
        ACTIVE_SCENE.STATE = SceneState.IDLE;
        ScriptEngine.stop();
        System.gc();
        //TODO: MAKE SOME SCENE STATE RESETTING OR STORAGE
        OxyPhysX.getInstance().resetSimulation();
    }

    public static void dispose() {
        ScriptEngine.dispose();
        OxyPhysX.getInstance().dispose();
        ACTIVE_SCENE.STATE = SceneState.TERMINATED;
        ACTIVE_SCENE.dispose();

        //Deleting the script class directory
        deleteDir(new File(System.getProperty("user.dir") + "\\target\\classes\\Scripts"));
    }

    public static final class RuntimeControlPanel extends Panel {

        private static Image2DTexture playTexture;
        private static Image2DTexture stopTexture;

        private RuntimeControlPanel(){}

        @Override
        public void preload() {
            playTexture = UIAssetManager.getInstance().getUIAsset("UI PLAY");
            stopTexture = UIAssetManager.getInstance().getUIAsset("UI STOP");
        }

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

            float size = 25;
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 253, 76, 61, 255);
            ImGui.dummy(5, 0);
            ImGui.sameLine();
            if (ImGui.imageButton(playTexture.getTextureId(), size, size, 0, 1, 1, 0, 1)) {
                SceneRuntime.onPlay();
            }
            ImGui.sameLine(0, 15);
            if (ImGui.imageButton(stopTexture.getTextureId(), size, size, 0, 1, 1, 0, 1)) {
                SceneRuntime.onStop();
            }
            ImGui.popStyleColor(4);
            ImGui.end();
        }
    }

    public static RuntimeControlPanel getPanel() {
        return panel;
    }
}