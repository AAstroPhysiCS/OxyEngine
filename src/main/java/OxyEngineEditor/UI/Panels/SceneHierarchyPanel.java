package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Components.ModelMesh;
import OxyEngineEditor.Components.SelectedComponent;
import OxyEngineEditor.Components.TagComponent;
import OxyEngineEditor.Scene.Objects.Model.ModelType;
import OxyEngineEditor.Scene.OxyEntity;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;

import static OxyEngineEditor.UI.Selector.OxySelectHandler.entityContext;

public class SceneHierarchyPanel extends Panel {

    private static SceneHierarchyPanel INSTANCE = null;

    private final SceneLayer sceneLayer;
    private final OxyShader shader;

    public static boolean focusedWindow, focusedWindowDragging;

    public static SceneHierarchyPanel getInstance(SceneLayer scene, OxyShader shader) {
        if (INSTANCE == null) INSTANCE = new SceneHierarchyPanel(scene, shader);
        return INSTANCE;
    }

    private SceneHierarchyPanel(SceneLayer sceneLayer, OxyShader shader) {
        this.sceneLayer = sceneLayer;
        this.shader = shader;
    }

    @Override
    public void preload() {
    }

    public void updateEntityPanel() {
        sceneLayer.getScene().each(entity -> {
            TagComponent tagComponent = entity.get(TagComponent.class);
            if (tagComponent != null) {
                if (ImGui.treeNodeEx(String.valueOf(entity.hashCode()), ImGuiTreeNodeFlags.OpenOnArrow | (entityContext == entity ? ImGuiTreeNodeFlags.Selected : 0), tagComponent.tag())) {
                    ImGui.treePop();
                }
                if (ImGui.isItemClicked()) {
                    if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                    entityContext = entity;
                    entityContext.get(SelectedComponent.class).selected = true;
                }
            }
        }, ModelMesh.class); //all entities that have x
    }

    @Override
    public void renderPanel() {

        ImGui.begin("Scene Hierarchy");
        focusedWindow = ImGui.isWindowFocused();
        focusedWindowDragging = focusedWindow && ImGui.isMouseDragging(2);

        updateEntityPanel();

        if (ImGui.beginPopupContextWindow("item context menu")) {
            if (ImGui.button("Create Entity"))
                addEntity(ModelType.Sphere.getPath().getBytes(), shader);
            ImGui.endPopup();
        }

        ImGui.end();
    }

    private void addEntity(byte[] data, OxyShader shader) {
        OxyEntity model = sceneLayer.getScene().createModelEntity(new String(data), shader);
        model.addComponent(new SelectedComponent(false));
        model.updateData();
        sceneLayer.rebuild();
    }
}
