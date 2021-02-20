package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.*;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Scene.Objects.Model.OxyModel;
import OxyEngine.Scene.OxyEntity;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTreeNodeFlags;

import java.util.List;
import java.util.Set;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public class SceneHierarchyPanel extends Panel {

    private static SceneHierarchyPanel INSTANCE = null;

    private final OxyShader shader;

    private static ImageTexture eyeViewTexture;

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
        eyeViewTexture = OxyTexture.loadImage(-1, "src/main/resources/assets/view.png");
    }

    private void updateEntityPanel(Set<OxyEntity> entities) {
        for (OxyEntity e : entities) {
            if (!(e instanceof OxyModel)) continue;
            if (!e.familyHasRoot()) { //means that it is the top root

                ImGui.tableNextRow();

                TagComponent tagComponent = e.get(TagComponent.class);
                ImGui.pushID(e.get(UUIDComponent.class).toString());

                renderRelativesView(e);
                renderRelativesType(e);

                ImGui.tableSetColumnIndex(0);
                if (ImGui.treeNodeEx(tagComponent.tag(), ImGuiTreeNodeFlags.OpenOnArrow)) {

                    if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                        if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                        entityContext = e;
                        entityContext.get(SelectedComponent.class).selected = true;
                    }

                    List<OxyEntity> relatedEntities = e.getEntitiesRelatedTo();
                    renderRelativesTreeNode(relatedEntities);
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

    private void renderRelativesView(OxyEntity relatedEntities) { // param: for future use
        ImGui.tableSetColumnIndex(1);
        ImGui.pushStyleColor(ImGuiCol.Button, 0f, 0f, 0f, 0f);
        ImGui.imageButton(eyeViewTexture.getTextureId(), 15, 15, 0, 1, 1, 0, 0);
        ImGui.popStyleColor();
    }

    private void renderRelativesType(OxyEntity relatedEntities) { // param: for future use
        ImGui.tableSetColumnIndex(2);
        ImGui.text("Entity");
    }

    private void renderRelativesTreeNode(List<OxyEntity> relatedEntities) {
        if (relatedEntities == null) return;
        if (relatedEntities.size() == 0) return;
        for (int i = 0; i < relatedEntities.size(); i++) {
            ImGui.tableNextRow();
            OxyEntity m = relatedEntities.get(i);

            ImGui.pushID(relatedEntities.get(i).hashCode());
            renderRelativesView(m);
            renderRelativesType(m);
            ImGui.tableSetColumnIndex(0);
            if (ImGui.treeNodeEx(m.get(TagComponent.class).tag(), ImGuiTreeNodeFlags.OpenOnArrow)) {
                if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                    if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                    entityContext = m;
                    entityContext.get(SelectedComponent.class).selected = true;
                }
                List<OxyEntity> relatedToRelated = m.getEntitiesRelatedTo();
                renderRelativesTreeNode(relatedToRelated);
                ImGui.treePop();
            } else {
                if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                    if (entityContext != null)
                        entityContext.get(SelectedComponent.class).selected = false;
                    entityContext = m;
                    entityContext.get(SelectedComponent.class).selected = true;
                }
            }
            ImGui.popID();
        }
    }

    @Override
    public void renderPanel() {

        ImGui.begin("Scene Hierarchy");
        focusedWindow = ImGui.isWindowFocused();
        focusedWindowDragging = focusedWindow && ImGui.isMouseDragging(2);

        if (ImGui.isAnyMouseDown() && !ImGui.isAnyItemHovered() && ImGui.isWindowHovered()) {
            entityContext = null;
        }

        if (ImGui.beginTable("HierarchyTable", 3)) {
            float maxWidth = ImGui.getContentRegionAvailX();
            ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthFixed, maxWidth / 1.8f);
            ImGui.tableSetupColumn("View", ImGuiTableColumnFlags.WidthFixed, 60.0f);
            ImGui.tableSetupColumn("Type", ImGuiTableColumnFlags.WidthFixed, maxWidth - (maxWidth / 2f) - 40f);
            ImGui.tableHeadersRow();

            updateEntityPanel(ACTIVE_SCENE.getEntities());

            ImGui.endTable();
        }

        if (ImGui.beginPopupContextWindow("Entity menu")) {
            if (ImGui.button("Create Entity"))
                addEntity(shader);
            ImGui.endPopup();
        }

        ImGui.end();
    }

    private void addEntity(OxyShader shader) {
        OxyEntity model = ACTIVE_SCENE.createEmptyModel(shader);
        if (entityContext != null) {
            model.addComponent(new TagComponent("Empty Entity"), new SelectedComponent(false));
            model.setFamily(new EntityFamily(entityContext.getFamily()));
            model.transformLocally();
        } else {
            model.addComponent(new TagComponent("Empty Entity"), new SelectedComponent(false));
            //model.setFamily(new EntityFamily()); this is already the default behaviour once the entity is created
            model.transformLocally();
        }
        SceneLayer.getInstance().rebuild();
    }
}
