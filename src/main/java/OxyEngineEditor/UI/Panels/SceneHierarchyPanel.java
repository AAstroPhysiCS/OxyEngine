package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.FamilyComponent;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Components.SelectedComponent;
import OxyEngine.Components.TagComponent;
import OxyEngineEditor.Scene.OxyEntity;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTreeNodeFlags;

import java.util.List;

import static OxyEngineEditor.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public class SceneHierarchyPanel extends Panel {

    private static SceneHierarchyPanel INSTANCE = null;

    private final OxyShader shader;

    public static boolean focusedWindow, focusedWindowDragging;

    public static SceneHierarchyPanel getInstance(OxyShader shader) {
        if (INSTANCE == null) INSTANCE = new SceneHierarchyPanel(shader);
        return INSTANCE;
    }

    private SceneHierarchyPanel(OxyShader shader) {
        this.shader = shader;
    }

    @Override
    public void preload() {
    }

    public void updateEntityPanel() {
        for (var root : ACTIVE_SCENE.getEntities()) {
            if (root.isRoot()) {
                TagComponent tagComponentRoot = root.get(TagComponent.class);
                if (ImGui.treeNodeEx(tagComponentRoot.tag(), ImGuiTreeNodeFlags.OpenOnArrow)) {
                    if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                        if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                        entityContext = root;
                        entityContext.get(SelectedComponent.class).selected = true;
                    }
                    List<OxyEntity> relatedEntities = root.getEntitiesRelatedTo(FamilyComponent.class);
                    if(relatedEntities != null) {
                        for (int i = 0; i < relatedEntities.size(); i++) {
                            OxyEntity m = relatedEntities.get(i);
                            ImGui.selectable(i + ": " + m.get(TagComponent.class).tag(), m.get(SelectedComponent.class).selected);
                            if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                                if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                                entityContext = m;
                                entityContext.get(SelectedComponent.class).selected = true;
                            }
                        }
                    }
                    ImGui.treePop();
                } else {
                    if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                        if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                        entityContext = root;
                        entityContext.get(SelectedComponent.class).selected = true;
                    }
                }
            }
        }
    }

    @Override
    public void renderPanel() {

        ImGui.begin("Scene Hierarchy");
        focusedWindow = ImGui.isWindowFocused();
        focusedWindowDragging = focusedWindow && ImGui.isMouseDragging(2);

        if (ImGui.beginPopupContextWindow("Entity menu")) {
            if (ImGui.button("Create Entity"))
                addEntity(shader);
            ImGui.endPopup();
        }

        updateEntityPanel();

        ImGui.end();
    }

    private void addEntity(OxyShader shader) {
        var familyComponent = new FamilyComponent();
        OxyEntity model = ACTIVE_SCENE.createEmptyModel(shader);
        model.setRoot(true);
        model.addComponent(new TagComponent("Empty Root"), new SelectedComponent(false), familyComponent);
        model.constructData();
        OxyEntity child = ACTIVE_SCENE.createEmptyModel(shader);
        child.addComponent(new TagComponent("Empty Child"), new SelectedComponent(false), familyComponent);
        child.constructData();
        SceneLayer.getInstance().rebuild();
    }
}
