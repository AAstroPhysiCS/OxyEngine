package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngineEditor.Scene.SceneRuntime;
import OxyEngineEditor.Scene.SceneState;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

import static OxyEngineEditor.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngineEditor.Scene.SceneRuntime.resume;

public final class SceneRuntimeControlPanel extends Panel {

    private static final ImageTexture playTexture = OxyTexture.loadImage(-1, "src/main/resources/assets/play.png");
    private static final ImageTexture stopTexture = OxyTexture.loadImage(-1, "src/main/resources/assets/stop.png");

    @Override
    public void preload() {

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

        float height = ImGui.getWindowHeight() - 10;
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 253, 76, 61, 255);
        ImGui.sameLine(20);
        ImGui.setCursorPosY(5);
        if (ImGui.imageButton(playTexture.getTextureId(), height, height, 0, 1, 1, 0, 1)) {
            SceneRuntime.onCreate();
            ACTIVE_SCENE.STATE = SceneState.RUNNING;
            resume();
        }
        ImGui.sameLine(60);
        if (ImGui.imageButton(stopTexture.getTextureId(), height, height, 0, 1, 1, 0, 1)) SceneRuntime.stop();
        ImGui.popStyleColor(4);
        ImGui.end();
    }
}

