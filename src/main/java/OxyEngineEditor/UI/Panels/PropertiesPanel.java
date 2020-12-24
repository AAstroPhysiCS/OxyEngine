package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.*;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiPopupFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
    final ImString searchAddComponent = new ImString(100);

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

        for (GUINode guiNode : entityContext.getGUINodes()) guiNode.runEntry();

        final float windowWidth = ImGui.getWindowWidth();
        ImGui.dummy(0, 25);
        ImGui.setCursorPosX(windowWidth / 2 - 125);
        ImGui.pushItemWidth(-1);
        if (ImGui.button("Add Component", 250, 25)) ImGui.openPopup("comp_popup", ImGuiPopupFlags.AnyPopup);
        float buttonY = ImGui.getCursorScreenPosY();
        ImGui.popItemWidth();

        ImGui.setNextWindowPos(ImGui.getWindowPosX() + (windowWidth / 2 - 150), buttonY - 165);
        ImGui.pushStyleColor(ImGuiCol.PopupBg, 36, 36, 36, 255);
        if (ImGui.beginPopup("comp_popup")) {
            ImGui.alignTextToFramePadding();
            ImGui.text("Search:");
            ImGui.sameLine();
            ImGui.inputText("##hidelabel comp_popup_search", searchAddComponent);
            if (ImGui.beginMenu("Mesh")) {
                if (ImGui.menuItem("Mesh Renderer"))
                    if (!entityContext.getGUINodes().contains(ModelMeshOpenGL.guiNode))
                        entityContext.getGUINodes().add(ModelMeshOpenGL.guiNode);
                if (ImGui.menuItem("Material"))
                    if (!entityContext.getGUINodes().contains(OxyMaterial.guiNode)) {
                        entityContext.addComponent(new OxyMaterial(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), 1.0f, 1.0f, 1.0f));
                        entityContext.getGUINodes().add(OxyMaterial.guiNode);
                    }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Scripts")) {
                if (ImGui.menuItem("Basic Script")) entityContext.addScript(new OxyScript(null));
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Physics")) {
                ImGui.menuItem("Mesh Collider");
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("UI")) {
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Audio")) {
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Light")) {
                if (ImGui.menuItem("Point Light")) {
                    if (!entityContext.has(Light.class)) {
                        PointLight pointLight = new PointLight(new Vector3f(2f, 2f, 2f), new Vector3f(1f, 1f, 1f), 1.0f, 0.027f, 0.0028f);
                        entityContext.addComponent(pointLight, new OxyMaterial(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
                        entityContext.getGUINodes().add(PointLight.guiNode);
                        sceneLayer.updateAllEntities();
                    }
                    //error or hint that lights are single instanced. TODO
                }
                if (ImGui.menuItem("Directional Light")) {
                    if (!entityContext.has(Light.class)) {
                        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(2f, 2f, 2f), new Vector3f(1f, 1f, 1f));
                        entityContext.addComponent(directionalLight, new OxyMaterial(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
                        entityContext.getGUINodes().add(DirectionalLight.guiNode);
                        sceneLayer.updateAllEntities();
                    }
                    //error or hint that lights are single instanced. TODO
                }
                ImGui.endMenu();
            }
            ImGui.endPopup();
        }
        ImGui.popStyleColor();

        ImGui.checkbox("Demo", helpWindowBool);
        if (helpWindowBool.get()) ImGui.showDemoWindow();
        initPanel = true;

        ImGui.end();
    }
}
