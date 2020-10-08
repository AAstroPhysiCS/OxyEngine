package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Scripting.OxyScriptItem;
import OxyEngineEditor.Components.*;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.util.List;
import java.util.Set;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngineEditor.UI.Selector.OxySelectHandler.entityContext;
import static OxyEngineEditor.UI.Selector.OxySelectHandler.gizmoEntityContextControl;

public class PropertiesPanel extends Panel {

    private static PropertiesPanel INSTANCE = null;

    public static PropertiesPanel getInstance(SceneLayer sceneLayer) {
        if (INSTANCE == null) INSTANCE = new PropertiesPanel(sceneLayer);
        return INSTANCE;
    }

    private static SceneLayer sceneLayer;

    public PropertiesPanel(SceneLayer sceneLayer) {
        PropertiesPanel.sceneLayer = sceneLayer;
    }

    public static boolean initPanel = false;
    public static boolean focusedWindow = false;
    private static final ImBoolean helpWindowBool = new ImBoolean();

    ImString name = new ImString(0);
    ImString meshPath = new ImString(0);

    private final String[] componentNames = UIEditable.allUIEditableNames();
    private final String[] componentFullName = UIEditable.allUIEditableFullNames();

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
        if (ImGui.inputText("##hidelabel", name, ImGuiInputTextFlags.EnterReturnsTrue)) {
            if (name.get().length() == 0) name.set("Unnamed");
            entityContext.get(TagComponent.class).setTag(name.get());
        }

        focusedWindow = ImGui.isWindowFocused();

        if (!initPanel) ImGui.setNextItemOpen(true);
        if (ImGui.treeNode("Transform")) {
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
            ImGui.treePop();
        }

        if (entityContext == null) {
            ImGui.end();
            return;
        }

        { // Scripting
            Set<ScriptingComponent> set = sceneLayer.cachedScriptComponents;
            for (EntityComponent e : set) {
                OxyScriptItem item = ((ScriptingComponent) e).getScriptItem();
                for (var f : item.getFieldsAsObject()) {
                    if (f.getObject() instanceof Float m) {
                        ImGui.alignTextToFramePadding();
                        ImGui.text(f.getName());
                        ImGui.sameLine();
                        ImGui.sliderFloat("##hideLabel item1" + f.hashCode(), new float[]{m}, 0, 1000);
                    }
                }
            }
        }

        {
            if (ImGui.treeNodeEx("Mesh Renderer", ImGuiTreeNodeFlags.DefaultOpen)) {

                meshPath = new ImString(entityContext.get(Mesh.class).getPath());

                ImGui.checkbox("Cast Shadows", false);

                if (!initPanel) ImGui.setNextItemOpen(true);
                ImGui.columns(2, "myColumns");
                if (!initPanel) ImGui.setColumnOffset(0, -120f);
                ImGui.alignTextToFramePadding();
                ImGui.text("Mesh:");
                ImGui.nextColumn();
                ImGui.pushItemWidth(ImGui.getContentRegionAvailWidth() - 30f);
                ImGui.inputText("##hidelabel", meshPath);
                ImGui.popItemWidth();
                ImGui.sameLine();
                if (ImGui.button("...")) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (entityContext != null) {
                            List<OxyModel> eList = sceneLayer.getScene().createModelEntities(path, entityContext.get(OxyShader.class));
                            for (OxyModel e : eList) {
                                TransformComponent t = new TransformComponent(entityContext.get(TransformComponent.class));
                                e.addComponent(t, new SelectedComponent(true, false));
                                e.constructData();
                            }
                            sceneLayer.getScene().removeEntity(entityContext);
                            sceneLayer.updateAllModelEntities();
                            meshPath = new ImString(path);
                            entityContext = null;
                        }
                    }
                }
                ImGui.columns(1);
                if (ImGui.treeNodeEx("Materials", ImGuiTreeNodeFlags.DefaultOpen)) {
                    ImGui.columns(2, "myColumnsMesh");
                    if (!initPanel) ImGui.setNextItemOpen(true);
                    if (!initPanel) {
                        ImGui.setColumnOffset(0, -80f);
                        initPanel = true;
                    }
                    ImGui.alignTextToFramePadding();
                    ImGui.text("Material");
                    ImGui.sameLine();
                    ImGui.nextColumn();
                    ImGui.text("DROP");
                    /*Material code here*/
                    ImGui.nextColumn();
                    ImGui.treePop();
                    ImGui.columns(1);
                }
                ImGui.treePop();
            }
        }

        final float windowWidth = ImGui.getWindowWidth();
        ImGui.spacing();
        ImGui.spacing();
        ImGui.setCursorPosX(windowWidth / 2 - 150);
        ImGui.pushItemWidth(-1);

        if (ImGui.button("Add Component", 300, 30)) {
            try {
                @SuppressWarnings("unchecked")
                EntityComponent component = entityContext.get((Class<? extends EntityComponent>) Class.forName(componentFullName[0]));
                PropertyEntry entry = (PropertyEntry) component.getClass().getField("node").get(component);
                if (!entityContext.getPropertyEntries().contains(entry))
                    entityContext.getPropertyEntries().add(entry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (entityContext != null) for (PropertyEntry n : entityContext.getPropertyEntries()) n.runEntry();

        ImGui.popItemWidth();

        ImGui.checkbox("Demo", helpWindowBool);
        if (helpWindowBool.get()) ImGui.showDemoWindow();

        ImGui.end();
    }
}
