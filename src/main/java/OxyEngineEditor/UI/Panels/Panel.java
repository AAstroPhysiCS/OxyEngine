package OxyEngineEditor.UI.Panels;

import imgui.ImGui;

public abstract class Panel {

    public static final float[] bgC = new float[]{33 / 255f, 33 / 255f, 36 / 255f, 1.0f};
    public static final float[] frameBgC = new float[]{60 / 255f, 58 / 255f, 60 / 255f, 1.0f};
    public static final float[] masterCardColor = new float[]{37 / 255f, 38 / 255f, 39 / 255f, 1.0f};
    public static final float[] childCardBgC = new float[]{45 / 255f, 44 / 255f, 45 / 255f, 1.0f};

    public abstract void renderPanel();

    protected static String renderImageBesideTreeNode(String name, int textureId, final int offsetX, final int offsetY, final float sizeX, final float sizeY) {
        name = "  \t " + name;
        float cursorPosX = ImGui.getCursorPosX();
        float cursorPosY = ImGui.getCursorPosY();
        ImGui.setCursorPosX(cursorPosX + offsetX);
        ImGui.image(textureId, sizeX, sizeY, 0, 1, 1, 0);
        ImGui.sameLine(cursorPosX);
        ImGui.setCursorPosY(cursorPosY + offsetY);
        return name;
    }
}
