package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngineEditor.Components.TagComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Components.UIEditable;
import OxyEngineEditor.Components.UUIDComponent;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiPopupFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import static OxyEngineEditor.UI.Selector.OxySelectHandler.entityContext;
import static OxyEngineEditor.UI.Selector.OxySelectHandler.gizmoEntityContextControl;

public class PropertiesPanel extends Panel {

    private static PropertiesPanel INSTANCE = null;

    public static PropertiesPanel getInstance(SceneLayer sceneLayer) {
        if (INSTANCE == null) INSTANCE = new PropertiesPanel(sceneLayer);
        return INSTANCE;
    }

    public static SceneLayer sceneLayer;

    public PropertiesPanel(SceneLayer sceneLayer) {
        PropertiesPanel.sceneLayer = sceneLayer;
    }

    private static boolean initPanel = false;
    public static boolean focusedWindow = false;
    private static final ImBoolean helpWindowBool = new ImBoolean();

    ImString name = new ImString(0);

    public static final String[] componentNames = UIEditable.allUIEditableNames();
    public static final String[] componentFullName = UIEditable.allUIEditableFullNames();

    @Override
    public void preload() {
    }

    @Override
    public void renderPanel() {
        gizmoEntityContextControl(entityContext);

        ImGui.begin("Properties");

        if (entityContext == null) {
            ImGui.end();
            return;
        }

        name = new ImString(entityContext.get(TagComponent.class).tag(), 100);

        ImGui.alignTextToFramePadding();
        ImGui.text("Name: ");
        ImGui.sameLine();
        if (ImGui.inputText("##hidelabel InputTextTag", name, ImGuiInputTextFlags.EnterReturnsTrue)) {
            if (name.get().length() == 0) name.set("Unnamed");
            entityContext.get(TagComponent.class).setTag(name.get());
        }
        ImGui.textDisabled("ID: " + entityContext.get(UUIDComponent.class).getUUIDString());

        focusedWindow = ImGui.isWindowFocused();

        if (!initPanel) ImGui.setNextItemOpen(true);
        if (ImGui.collapsingHeader("Transform")) {
            ImGui.columns(2, "myColumns");
            if (!initPanel) ImGui.setColumnOffset(0, -90f);
            ImGui.alignTextToFramePadding();
            ImGui.text("Translation:");
            ImGui.alignTextToFramePadding();
            ImGui.text("Rotation:");
            ImGui.alignTextToFramePadding();
            ImGui.text("Scale:");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailWidth());

            TransformComponent t = entityContext.get(TransformComponent.class);
            float[] translationArr = new float[]{t.position.x, t.position.y, t.position.z};
            float[] rotationArr = new float[]{t.rotation.x, t.rotation.y, t.rotation.z};
            float[] scaleArr = new float[]{t.scale.x, t.scale.y, t.scale.z};
            ImGui.dragFloat3("##hidelabel T", translationArr, 0.1f);
            ImGui.dragFloat3("##hidelabel R", rotationArr, 0.1f);
            ImGui.dragFloat3("##hidelabel S", scaleArr, 0.1f, 0, Float.MAX_VALUE);
            t.position.set(translationArr);
            t.rotation.set(rotationArr);
            t.scale.set(scaleArr);
            entityContext.updateData();

            ImGui.popItemWidth();
            ImGui.columns(1);
            ImGui.separator();
        }

        if (entityContext == null) {
            ImGui.end();
            return;
        }

        for (PropertyEntry n : entityContext.getPropertyEntries()) n.runEntry();

        final float windowWidth = ImGui.getWindowWidth();
        ImGui.spacing();
        ImGui.spacing();
        ImGui.setCursorPosX(windowWidth / 2 - 150);
        ImGui.pushItemWidth(-1);
        if (ImGui.button("Add Component", 300, 25)) ImGui.openPopup("comp_popup", ImGuiPopupFlags.AnyPopup);
        ImGui.popItemWidth();

        if (ImGui.beginPopup("comp_popup")) {
            ImGui.alignTextToFramePadding();
            ImGui.text("Search:");
            ImGui.sameLine();
            ImGui.inputText("##hidelabel comp_popup_search", new ImString(""), ImGuiInputTextFlags.EnterReturnsTrue);
            ImGui.endPopup();
        }

        ImGui.checkbox("Demo", helpWindowBool);
        if (helpWindowBool.get()) ImGui.showDemoWindow();
        initPanel = true;

        ImGui.end();
    }
}
