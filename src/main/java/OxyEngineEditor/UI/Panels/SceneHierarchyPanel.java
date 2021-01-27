package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.FamilyComponent;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Components.SelectedComponent;
import OxyEngine.Components.TagComponent;
import OxyEngineEditor.Scene.OxyEntity;
import imgui.ImGui;
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
                if (ImGui.treeNodeEx(String.valueOf(root.hashCode()), ImGuiTreeNodeFlags.OpenOnArrow | (entityContext == root ? ImGuiTreeNodeFlags.Selected : 0), tagComponentRoot.tag())) {
                    List<OxyEntity> relatedEntities = root.getEntitiesRelatedTo(FamilyComponent.class);
                    if(relatedEntities != null) {
                        for (int i = 0; i < relatedEntities.size(); i++) {
                            OxyEntity m = relatedEntities.get(i);
                            if(!m.equals(entityContext)){
                                m.get(SelectedComponent.class).selected = false;
                            }
                            ImGui.selectable(i + ": " + m.get(TagComponent.class).tag(), m.get(SelectedComponent.class).selected);
                            if (ImGui.isItemClicked()) {
                                if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                                entityContext = m;
                                entityContext.get(SelectedComponent.class).selected = true;
                            }
                        }
                    }
                    ImGui.treePop();
                }
                if (ImGui.isItemClicked()) {
                    if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                    entityContext = root;
                    entityContext.get(SelectedComponent.class).selected = true;
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
        OxyEntity model = ACTIVE_SCENE.createEmptyModel(shader);
        model.setRoot(true);
        model.addComponent(new SelectedComponent(false));
        model.constructData();
        SceneLayer.getInstance().rebuild();
    }
}
