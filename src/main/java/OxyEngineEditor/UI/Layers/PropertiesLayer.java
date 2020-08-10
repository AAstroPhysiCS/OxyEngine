package OxyEngineEditor.UI.Layers;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Sandbox.Scene.Scene;
import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImString;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NativeFileDialog;

public class PropertiesLayer extends UILayer {

    public PropertiesLayer(WindowHandle windowHandle, Scene scene) {
        super(windowHandle, scene);
    }

    private static PropertiesLayer INSTANCE = null;

    public static PropertiesLayer getInstance(WindowHandle windowHandle, Scene scene) {
        if (INSTANCE == null) INSTANCE = new PropertiesLayer(windowHandle, scene);
        return INSTANCE;
    }

    static String lastTexturePath = null;
    static int lastTextureID = -1;
    static final ImString inputTextPath = new ImString();
    static final float[] diffuseColor = new float[]{0f, 0.0f, 0.0f, 0.0f};
    static final float[] specularColor = new float[]{0f, 0.0f, 0.0f, 0.0f};
    static final float[] ambientColor = new float[]{0f, 0.0f, 0.0f, 0.0f};

    @Override
    public void preload() {

    }

    @Override
    public void renderLayer() {
        ImGui.setNextWindowSize(windowHandle.getWidth() / 5f, windowHandle.getHeight() - 300, ImGuiCond.Once);
        ImGui.setNextWindowPos(0, 40, ImGuiCond.Once);

        ImGui.pushStyleColor(ImGuiCol.WindowBg, bgC[0], bgC[1], bgC[2], bgC[3]);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding | ImGuiStyleVar.WindowBorderSize, 0);

        ImGui.begin("Properties");

        ImGui.spacing();
        if (ImGui.collapsingHeader("Diffuse color", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.colorButton("diff", diffuseColor,
                    ImGuiColorEditFlags.AlphaBar |
                            ImGuiColorEditFlags.AlphaPreview |
                            ImGuiColorEditFlags.NoBorder,
                    70, 70
            );
            ImGui.sameLine();
            ImGui.colorEdit4("diff", diffuseColor,
                    ImGuiColorEditFlags.NoSidePreview |
                            ImGuiColorEditFlags.NoSmallPreview |
                            ImGuiColorEditFlags.DisplayRGB |
                            ImGuiColorEditFlags.NoLabel
            );
        }
        if (ImGui.collapsingHeader("Ambient color", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.colorButton("amb", ambientColor,
                    ImGuiColorEditFlags.AlphaBar |
                            ImGuiColorEditFlags.AlphaPreview |
                            ImGuiColorEditFlags.NoBorder,
                    70, 70
            );
            ImGui.sameLine();
            ImGui.colorEdit4("amb", ambientColor,
                    ImGuiColorEditFlags.NoSidePreview |
                            ImGuiColorEditFlags.NoSmallPreview |
                            ImGuiColorEditFlags.DisplayRGB |
                            ImGuiColorEditFlags.NoLabel
            );
        }
        if (ImGui.collapsingHeader("Specular color", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.colorButton("spec", specularColor,
                    ImGuiColorEditFlags.AlphaBar |
                            ImGuiColorEditFlags.AlphaPreview |
                            ImGuiColorEditFlags.NoBorder,
                    70, 70
            );
            ImGui.sameLine();
            ImGui.colorEdit4("spec", specularColor,
                    ImGuiColorEditFlags.NoSidePreview |
                            ImGuiColorEditFlags.NoSmallPreview |
                            ImGuiColorEditFlags.DisplayRGB |
                            ImGuiColorEditFlags.NoLabel
            );
        }

        if (ImGui.collapsingHeader("Texture", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.inputText("###label", inputTextPath, ImGuiInputTextFlags.ReadOnly);
            ImGui.sameLine();
            if (ImGui.button("...")) {
                PointerBuffer buffer = PointerBuffer.allocateDirect(16);
                int result = NativeFileDialog.NFD_OpenDialog("", null, buffer);
                if (result == NativeFileDialog.NFD_OKAY) {
                    PropertiesLayer.lastTexturePath = buffer.getStringASCII();
                    PropertiesLayer.inputTextPath.set(PropertiesLayer.lastTexturePath);
                }
                NativeFileDialog.nNFD_Free(buffer.get());
            }
        }

        ImGui.end();
        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }
}
