package OxyEngineEditor.Scene;

import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngineEditor.UI.Panels.Panel;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

public final class SceneRuntime {

    private static final SceneRuntimePanel panel = new SceneRuntimePanel();

    public static Scene ACTIVE_SCENE;

    public SceneRuntime(Scene scene) {
        ACTIVE_SCENE = scene;
    }

    private static final class SceneRuntimePanel extends Panel {

        private static final ImageTexture playTexture = OxyTexture.loadImage(-1, "src/main/resources/assets/play.png");
        private static final ImageTexture stopTexture = OxyTexture.loadImage(-1, "src/main/resources/assets/stop.png");
        private static final ImageTexture stopHoveredTexture = OxyTexture.loadImage(-1, "src/main/resources/assets/stopHovered.png");

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
            ImGui.setCursorPosY(6);
            if(ImGui.imageButton(playTexture.getTextureId(), height, height, 0, 1, 1, 0, 1)){

            }
            ImGui.sameLine(60);
            if(ImGui.imageButton(stopTexture.getTextureId(), height, height, 0, 1, 1, 0, 1)){

            }
            ImGui.popStyleColor();
            ImGui.popStyleColor();
            ImGui.popStyleColor();
            ImGui.popStyleColor();
            ImGui.end();
        }
    }

    public static SceneRuntimePanel getPanel() {
        return panel;
    }
}
