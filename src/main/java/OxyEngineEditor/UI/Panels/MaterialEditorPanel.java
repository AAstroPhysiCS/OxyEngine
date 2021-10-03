package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.Texture.Image2DTexture;
import OxyEngine.Core.Renderer.Texture.Texture;
import OxyEngine.Core.Renderer.Texture.TexturePixelType;
import OxyEngine.Core.Renderer.Texture.TextureSlot;
import OxyEngine.Core.Scene.Material;
import imgui.flag.*;
import imgui.internal.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.FileSystem.openDialog;
import static OxyEngine.System.OxySystem.getExtension;
import static OxyEngine.System.OxySystem.isSupportedTextureFile;
import static OxyEngineEditor.UI.Panels.ProjectPanel.dirAssetGrey;
import static OxyEngineEditor.UI.UIAssetManager.DEFAULT_TEXTURE_PARAMETER;

public final class MaterialEditorPanel extends Panel {

    private static MaterialEditorPanel INSTANCE = null;

    public static MaterialEditorPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new MaterialEditorPanel();
        return INSTANCE;
    }

    private boolean show;
    private final ImBoolean closeButton = new ImBoolean(true);

    private final List<Material> pushedMaterialList = new ArrayList<>();
    private final List<Boolean> openStateList = new ArrayList<>();

    private static final ImString albedoInputBuffer = new ImString(200);
    private static final ImString metalnessInputBuffer = new ImString(200);
    private static final ImString roughnessInputBuffer = new ImString(200);
    private static final ImString normalInputBuffer = new ImString(200);
    private static final ImString aoInputBuffer = new ImString(200);
    private static final ImString emissiveInputBuffer = new ImString(200);

    private MaterialEditorPanel() {
    }

    @Override
    public void renderPanel() {
        if (!show) return;

        ImGui.begin("Material Editor");

        //close button of the window is clicked
        if (!closeButton.get()) {
            closeButton.set(true);
            show = false;
            ImGui.end();
            pushedMaterialList.clear();
            openStateList.clear();
            return;
        }

        if (ImGui.beginTabBar("##hideLabel MaterialTabBar")) {
            for (int i = 0; i < pushedMaterialList.size(); i++) {
                Material m = pushedMaterialList.get(i);
                ImBoolean openState = new ImBoolean(openStateList.get(i));
                ImGui.pushID(i);
                if (ImGui.beginTabItem(m.name, openState) && openState.get()) {
                    if (ImGui.collapsingHeader("Albedo", ImGuiTreeNodeFlags.DefaultOpen)) {
                        m.albedoTexture = renderNode(m.albedoTexture, albedoInputBuffer, TextureSlot.ALBEDO);
                        ImGui.alignTextToFramePadding();
                        ImGui.text("Base Color");
                        ImGui.sameLine();
                        ImGui.colorEdit3("##hideLabelalbedoColorPicker", m.albedoColor.getNumbers(), ImGuiColorEditFlags.DisplayRGB);
                    }
                    if (ImGui.collapsingHeader("Normals", ImGuiTreeNodeFlags.DefaultOpen)) {
                        m.normalTexture = renderNode(m.normalTexture, normalInputBuffer, TextureSlot.NORMAL);
                        ImGui.dragFloat("Normals Strength", m.normalStrength, 0.01f, 0f, 5f);
                    }
                    if (ImGui.collapsingHeader("Roughness", ImGuiTreeNodeFlags.DefaultOpen)) {
                        m.roughnessTexture = renderNode(m.roughnessTexture, roughnessInputBuffer, TextureSlot.ROUGHNESS);
                        ImGui.dragFloat("Roughness Strength", m.roughness, 0.01f, 0f, 1f);
                    }
                    if (ImGui.collapsingHeader("Metallic", ImGuiTreeNodeFlags.DefaultOpen)) {
                        m.metallicTexture = renderNode(m.metallicTexture, metalnessInputBuffer, TextureSlot.METALLIC);
                        ImGui.dragFloat("Metallic Strength", m.metalness, 0.01f, 0f, 1f);
                    }
                    if (ImGui.collapsingHeader("AO", ImGuiTreeNodeFlags.DefaultOpen)) {
                        m.aoTexture = renderNode(m.aoTexture, aoInputBuffer, TextureSlot.AO);
                        ImGui.dragFloat("AO Strength", m.aoStrength, 0.01f, 0f, 1f);
                    }
                    if (ImGui.collapsingHeader("Emissive", ImGuiTreeNodeFlags.DefaultOpen)) {
                        m.emissiveTexture = renderNode(m.emissiveTexture, emissiveInputBuffer, TextureSlot.EMISSIVE);
                        ImGui.dragFloat("Emissive Strength", m.emissiveStrength, 0.01f, 0f, 1f);
                    }
                    ImGui.endTabItem();
                }
                ImGui.popID();

                //if the close button of individual material tab is clicked... close that tab
                if (!openState.get()) openStateList.set(i, false);
            }

            ImGui.endTabBar();
        }
        ImGui.end();
    }

    private Image2DTexture renderNode(Image2DTexture previewTexture, ImString inputBuffer, TextureSlot slot) {
        renderPreviewImage(previewTexture, 3);
        ImGui.sameLine();

        ImGui.pushItemWidth(ImGui.getContentRegionAvailX() - 50);
        inputBuffer.set("");
        if (previewTexture != null)
            inputBuffer.set(previewTexture.getPath());
        if (ImGui.inputText("##hideLabelEmissiveInput", inputBuffer, ImGuiInputTextFlags.EnterReturnsTrue)) {
            String path = inputBuffer.get();
            inputBuffer.set(path);
            if (path != null) {
                if (previewTexture != null) previewTexture.dispose();
                previewTexture = Texture.loadImage(slot, path, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
            }
        }
        ImGui.popItemWidth();

        Image2DTexture imgText = acceptTexturePayload(slot);
        if (imgText != null) previewTexture = imgText;

        ImGui.sameLine();
        ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1.0f, 1.0f, 1.0f, 0.2f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 1.0f, 1.0f, 0.2f);
        ImGui.pushItemWidth(ImGui.getContentRegionAvailX());

        if (ImGui.imageButton(dirAssetGrey.getTextureId(), 20, 20, 0, 1, 1, 0, 0)) {
            String path = openDialog("", null);
            if (path != null) {
                if (previewTexture != null) previewTexture.dispose();
                previewTexture = Texture.loadImage(slot, path, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
            }
        }

        ImGui.popStyleColor(3);
        ImGui.popItemWidth();

        Image2DTexture imgButton = acceptTexturePayload(slot);
        if (imgButton != null) previewTexture = imgButton;

        ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
        ImGui.sameLine();
        if (ImGui.button("R")) {
            if (previewTexture != null) {
                previewTexture.dispose();
                previewTexture = null;
            }
        }
        ImGui.popItemWidth();

        return previewTexture;
    }

    private void renderPreviewImage(Image2DTexture t, float offsetY) {
        int id = -1;
        if (t != null) id = t.getTextureId();
        float oldCursorPosY = ImGui.getCursorPosY();
        ImGui.setCursorPosY(oldCursorPosY + offsetY);
        ImGui.imageButton(id, 65, 60, 0, 1, 1, 0, 1);
        if (ImGui.isItemHovered(ImGuiHoveredFlags.AnyWindow |
                ImGuiHoveredFlags.AllowWhenBlockedByPopup |
                ImGuiHoveredFlags.AllowWhenBlockedByActiveItem |
                ImGuiHoveredFlags.AllowWhenOverlapped)) {
            ImGui.openPopup("previewPopup");
            if (ImGui.beginPopup("previewPopup")) {
                ImGui.image(id, 150, 150, 0, 1, 1, 0);
                ImGui.endPopup();
            }
        }
        ImGui.sameLine();
        ImGui.setCursorPosY(oldCursorPosY);
    }

    private Image2DTexture acceptTexturePayload(TextureSlot slot) {
        if (ImGui.beginDragDropTarget()) {
            File f = ImGui.acceptDragDropPayload("projectPanelFile");
            if (f != null) {
                String fPath = f.getPath();
                if (isSupportedTextureFile(getExtension(fPath))) {
                    return Texture.loadImage(slot, fPath, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
                }
            }
            ImGui.endDragDropTarget();
        }
        return null;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public void pushMaterialToShow(Material m) {
        if (!pushedMaterialList.contains(m)) {
            pushedMaterialList.add(m);
            openStateList.add(true);
        }
    }
}
