package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.AnimationComponent;
import OxyEngine.Core.Renderer.Texture.Image2DTexture;
import OxyEngineEditor.UI.AssetManager;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public class AnimationPanel extends Panel {

    private static final Image2DTexture playTexture = AssetManager.getInstance().getAsset("UI PLAY");
    private static final Image2DTexture stopTexture = AssetManager.getInstance().getAsset("UI STOP");

    private static final float[] animationController = new float[1];

    private static AnimationPanel INSTANCE = null;

    public static AnimationPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new AnimationPanel();
        return INSTANCE;
    }

    @Override
    public void preload() {

    }

    @Override
    public void renderPanel() {

        if (entityContext == null) return;
        if (!entityContext.has(AnimationComponent.class)) return;

        AnimationComponent animComponent = entityContext.get(AnimationComponent.class);

        final int windowWidth = 500, windowHeight = 40, windowYOffset = 20;

        ImGui.setNextWindowSize(windowWidth, windowHeight);
        ImGui.setNextWindowPos(((ScenePanel.windowPos.x + ScenePanel.windowSize.x) / 2) - windowWidth / 2f, ((ScenePanel.windowPos.y + ScenePanel.windowSize.y)) - windowHeight - windowYOffset);
        ImGui.begin("##hidelabel AnimationPanel", ImGuiWindowFlags.NoScrollbar |
                ImGuiWindowFlags.NoDecoration |
                ImGuiWindowFlags.NoDocking |
                ImGuiWindowFlags.NoMove
        );

        ImGui.indent(10);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 2);
        if(ImGui.imageButton(playTexture.getTextureId(), 20, 20, 0, 1, 1, 0, 0)){
            animComponent.stopAnimation(false);
        }

        ImGui.sameLine();

        ImGui.setCursorPosX(ImGui.getCursorPosX() + 4);
        if(ImGui.imageButton(stopTexture.getTextureId(), 20, 20, 0, 1, 1, 0, 0)){
            animComponent.stopAnimation(true);
        }
        ImGui.unindent(10);

        ImGui.sameLine();
        ImGui.alignTextToFramePadding();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 15);
        ImGui.text("Begin Time");

        ImGui.sameLine();
        ImGui.pushItemWidth(ImGui.getContentRegionAvailX() / 1.3f);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 12);
        ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, 12);
        animationController[0] = animComponent.getCurrentTime();
        ImGui.sliderFloat("##hideLabel AnimationController", animationController, 1, 250f);
        animComponent.setTime(animationController[0]);
        ImGui.popStyleVar(2);
        ImGui.popItemWidth();

        ImGui.sameLine();
        ImGui.alignTextToFramePadding();
        ImGui.text("End Time");

        ImGui.end();
    }
}
