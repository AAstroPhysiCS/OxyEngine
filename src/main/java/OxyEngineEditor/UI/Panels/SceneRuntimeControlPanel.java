package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Scene.SceneRuntime;
import OxyEngine.Scene.SceneState;
import OxyEngine.TextureSlot;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;

public final class SceneRuntimeControlPanel extends Panel {

    private static final ImageTexture playTexture = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/play.png");
    private static final ImageTexture stopTexture = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/stop.png");

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

        float size = 25;
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 253, 76, 61, 255);
        ImGui.dummy(5, 0);
        ImGui.sameLine();
        if (ImGui.imageButton(playTexture.getTextureId(), size, size, 0, 1, 1, 0, 1)) {
            ACTIVE_SCENE.STATE = SceneState.RUNNING;
            SceneRuntime.onCreate();
        }
        ImGui.sameLine(0, 15);
        if (ImGui.imageButton(stopTexture.getTextureId(), size, size, 0, 1, 1, 0, 1)) SceneRuntime.stop();
        ImGui.popStyleColor(4);
        ImGui.end();
    }
}

