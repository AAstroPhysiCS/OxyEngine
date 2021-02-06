package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.FamilyComponent;
import OxyEngine.Components.UUIDComponent;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Components.SelectedComponent;
import OxyEngine.Components.TagComponent;
import OxyEngine.Scene.OxyEntity;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTreeNodeFlags;

import java.util.List;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
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
        for (var e : ACTIVE_SCENE.getEntities()) {
            if (e.isRoot()) {
                TagComponent tagComponent = e.get(TagComponent.class);
                ImGui.pushID(e.get(UUIDComponent.class).toString());
                if (ImGui.treeNodeEx(tagComponent.tag(), ImGuiTreeNodeFlags.OpenOnArrow)) {
                    if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                        if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                        entityContext = e;
                        entityContext.get(SelectedComponent.class).selected = true;
                    }
                    List<OxyEntity> relatedEntities = e.getEntitiesRelatedTo(FamilyComponent.class);
                    if(relatedEntities != null) {
                        for (int i = 0; i < relatedEntities.size(); i++) {
                            OxyEntity m = relatedEntities.get(i);
                            if(ImGui.treeNodeEx(m.get(TagComponent.class).tag(), ImGuiTreeNodeFlags.OpenOnArrow)){
                                ImGui.treePop();
                            }
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
                        entityContext = e;
                        entityContext.get(SelectedComponent.class).selected = true;
                    }
                }
                ImGui.popID();
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
        OxyEntity model = ACTIVE_SCENE.createEmptyModel(shader);
        model.setRoot(true);
        model.addComponent(new TagComponent("Empty Entity"), new SelectedComponent(false), new FamilyComponent());
        model.transformLocally();
        SceneLayer.getInstance().rebuild();
    }
}
