package OxyEngine.Core.Context.Scene;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Camera.Camera;
import OxyEngine.Core.Context.Renderer.Texture.Image2DTexture;
import OxyEngine.PhysX.OxyPhysX;
import OxyEngine.Scripting.Script;
import OxyEngine.Scripting.ScriptEngine;
import OxyEngineEditor.UI.Panels.Panel;
import OxyEngineEditor.UI.UIAssetManager;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class SceneRuntime {

    private static final RuntimeControlPanel panel = new RuntimeControlPanel();

    public static Camera cameraContext;
    public static Entity skyLightEntityContext;

    public static Entity entityContext;
    public static Scene sceneContext;

    private static final List<Matrix4f> transforms = new ArrayList<>();

    private SceneRuntime() {
    }

    public static void runtimePlay() {
        sceneContext.setState(SceneState.PLAY);
        for (Entity e : sceneContext.getEntities()) {
            for (Script c : e.getScripts()) {
                c.invokeCreate();
                ScriptEngine.addProvider(c.getProvider());
            }
        }
        saveOriginalTransforms();
        ScriptEngine.restart();
        OxyPhysX.buildComponents();
        System.gc();
    }

    public static void runtimeStop() {
        if (sceneContext.getState() != SceneState.PLAY) return;
        sceneContext.setState(SceneState.STOP);
        ScriptEngine.stop();
        OxyPhysX.resetSimulation();
        System.gc();

        loadOriginalTransforms();
    }

    private static void saveOriginalTransforms() {
        for (Entity e : sceneContext.getEntities()) {
            if(e.familyHasRoot()){
                transforms.add(new Matrix4f(e.getTransform()).mulLocal(new Matrix4f(e.getRoot().getTransform()).invert()));
            } else {
                transforms.add(new Matrix4f(e.getTransform()));
            }
        }
    }

    private static void loadOriginalTransforms() {
        int i = 0;
        for (Entity e : sceneContext.getEntities()) {
            e.get(TransformComponent.class).set(transforms.get(i++));
            e.updateTransform();
        }
        transforms.clear();
    }

    public static final class RuntimeControlPanel extends Panel {

        private static Image2DTexture playTexture;
        private static Image2DTexture stopTexture;

        private RuntimeControlPanel() {
        }

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
                SceneRuntime.runtimePlay();
            }
            ImGui.sameLine(0, 15);
            if (ImGui.imageButton(stopTexture.getTextureId(), size, size, 0, 1, 1, 0, 1)) {
                SceneRuntime.runtimeStop();
            }
            ImGui.popStyleColor(4);
            ImGui.end();
        }
    }

    public static RuntimeControlPanel getPanel() {
        return panel;
    }
}