package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Context.Renderer.Mesh.OpenGLMesh;
import OxyEngine.Core.Context.Renderer.Light.Light;
import OxyEngine.Core.Context.Renderer.Light.SkyLight;
import OxyEngine.Core.Context.Renderer.Texture.Image2DTexture;
import OxyEngine.Core.Context.Scene.OxyMaterialPool;
import OxyEngine.Core.Context.Scene.OxyModel;
import OxyEngine.Core.Context.Scene.OxyEntity;
import OxyEngine.Core.Context.Scene.OxyMaterial;
import OxyEngineEditor.UI.UIAssetManager;
import imgui.ImGui;
import imgui.flag.*;

import java.util.List;
import java.util.Set;

import static OxyEngine.Core.Context.Scene.SceneRuntime.*;
import static OxyEngineEditor.UI.Panels.ProjectPanel.dirAssetGrey;

public class SceneHierarchyPanel extends Panel {

    private static SceneHierarchyPanel INSTANCE = null;

    private static final Image2DTexture eyeViewTexture = UIAssetManager.getInstance().getUIAsset("UI VIEW");
    private static final Image2DTexture materialGreyMesh = UIAssetManager.getInstance().getUIAsset("UI MATERIALGREYMESH");
    private static final Image2DTexture materialGroupGizmo = UIAssetManager.getInstance().getUIAsset("UI MATERIALGROUPGIZMO");
    private static final Image2DTexture materialLightBulb = UIAssetManager.getInstance().getUIAsset("UI MATERIALLIGHTBULB");
    private static final Image2DTexture materialCamera = UIAssetManager.getInstance().getUIAsset("UI MATERIALCAMERA");
    static final Image2DTexture materialPinkSphere = UIAssetManager.getInstance().getUIAsset("UI MATERIALPINKSPHERE");

    public static boolean focusedWindow, focusedWindowDragging;

    private static final int TABLE_COLORS = ImGui.getColorU32(bgC[0], bgC[1], bgC[2], bgC[3]);

    public static SceneHierarchyPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new SceneHierarchyPanel();
        return INSTANCE;
    }

    private SceneHierarchyPanel() {
    }

    @Override
    public void preload() {

    }

    private void updateEntityPanel(Set<OxyEntity> entities) {
        for (OxyEntity e : entities) {
            if (!e.has(SkyLight.class) & !(e instanceof OxyModel)) continue;
            if (!e.familyHasRoot()) { //means that it is the top root
                boolean hasMesh = e.has(OpenGLMesh.class);
                boolean isLight = e.has(Light.class);
                boolean isCamera = e.has(OxyCamera.class);

                ImGui.tableNextRow();

                TagComponent tagComponent = e.get(TagComponent.class);
                ImGui.pushID(e.get(UUIDComponent.class).toString());

                renderView(e);
                if (isLight) renderType(e, e.get(Light.class).getClass().getSimpleName());
                else if (hasMesh) renderType(e, "Mesh");
                else if (isCamera) renderType(e, e.get(OxyCamera.class).getClass().getSimpleName());
                else renderType(e, "Group");

                ImGui.tableSetColumnIndex(0);
                ImGui.tableSetBgColor(ImGuiTableBgTarget.CellBg, TABLE_COLORS);

                String name = tagComponent.tag();

                if (isLight) {
                    name = renderImageBesideTreeNode(name, materialLightBulb.getTextureId(), 19, 2, 22f, 20f);
                } else if (hasMesh) {
                    name = renderImageBesideTreeNode(name, materialGreyMesh.getTextureId(), 19, 2, 22.4f, 20f);
                } else if (isCamera) {
                    name = renderImageBesideTreeNode(name, materialCamera.getTextureId(), 19, 2, 22f, 20f);
                } else { //its a group
                    name = renderImageBesideTreeNode(name, materialGroupGizmo.getTextureId(), 19, 2, 22, 20);
                }

                boolean open = ImGui.treeNodeEx(name, ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.SpanFullWidth);

                if (ImGui.beginDragDropTarget()) {
                    OxyModel srcEntity = ImGui.acceptDragDropPayload(OxyModel.class);
                    if (srcEntity != null)
                        srcEntity.getFamily().setRoot(e.getFamily());
                    ImGui.endDragDropTarget();
                }

                if (ImGui.beginDragDropSource()) {
                    ImGui.setDragDropPayload(e);
                    ImGui.endDragDropSource();
                }

                if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                    materialContext = null;
                    if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                    entityContext = e;
                    entityContext.get(SelectedComponent.class).selected = true;
                }

                if (open) {
                    List<OxyEntity> relatedToRelated = e.getEntitiesRelatedTo();
                    renderTreeNode(relatedToRelated);
                    ImGui.treePop();
                } else {
                    if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                        materialContext = null;
                        if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                        entityContext = e;
                        entityContext.get(SelectedComponent.class).selected = true;
                    }
                }
                ImGui.popID();
            }
        }
    }

    private void renderView(OxyEntity entity) {
        ImGui.tableSetColumnIndex(1);
        ImGui.tableSetBgColor(ImGuiTableBgTarget.CellBg, TABLE_COLORS);

        final int buttonSize = 18;

        ImGui.dummy(ImGui.getContentRegionAvailX() / 2 - buttonSize, 0);
        ImGui.sameLine();

        ImGui.pushStyleColor(ImGuiCol.Button, 0f, 0f, 0f, 0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, Panel.bgC[0] + 0.1f, Panel.bgC[1] + 0.1f, Panel.bgC[2] + 0.1f, Panel.bgC[3]);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, Panel.bgC[0] + 0.2f, Panel.bgC[1] + 0.2f, Panel.bgC[2] + 0.2f, Panel.bgC[3]);
        if (ImGui.imageButton(eyeViewTexture.getTextureId(), buttonSize, buttonSize, 0, 1, 1, 0, 0) && entity != null)
            viewAction(entity, entity.getEntitiesRelatedTo());
        ImGui.popStyleColor(3);
    }

    private void viewAction(OxyEntity entity, List<OxyEntity> relatedEntities) {
        renderingModeSwitch(entity);
        for (OxyEntity e : relatedEntities) {
            //calling the relatives too
            viewAction(e, e.getEntitiesRelatedTo());
        }
    }

    private void renderingModeSwitch(OxyEntity entity) {
        var comp = entity.get(RenderableComponent.class);
        if (comp.mode == RenderingMode.Normal) comp.mode = RenderingMode.None;
        else comp.mode = RenderingMode.Normal;
    }

    private void renderType(OxyEntity relatedEntities, String type) { // param: for future use
        ImGui.tableSetColumnIndex(2);
        ImGui.tableSetBgColor(ImGuiTableBgTarget.CellBg, TABLE_COLORS);

        /*
         * TO CENTER THE TEXT
         * ImVec2 textSize = new ImVec2();  to center the text
         * ImGui.calcTextSize(textSize, type);
         * ImGui.dummy(ImGui.getContentRegionAvailX() / 2f - textSize.x / 2, 0);
         */

        ImGui.dummy(0, 0);
        ImGui.sameLine();
        ImGui.text(type);
    }


    private void renderTreeNode(List<OxyEntity> relatedEntities) {
        if (relatedEntities == null) return;
        if (relatedEntities.size() == 0) return;
        for (int i = 0; i < relatedEntities.size(); i++) {
            ImGui.tableNextRow();
            OxyEntity e = relatedEntities.get(i);
            boolean hasMesh = e.has(OpenGLMesh.class);
            boolean isLight = e.has(Light.class);
            boolean isCamera = e.has(OxyCamera.class);

            ImGui.pushID(relatedEntities.get(i).hashCode());
            renderView(e);
            if (isLight) renderType(e, e.get(Light.class).getClass().getSimpleName());
            else if (hasMesh) renderType(e, "Mesh");
            else if (isCamera) renderType(e, e.get(OxyCamera.class).getClass().getSimpleName());
            else renderType(e, "Group");
            ImGui.tableSetColumnIndex(0);
            ImGui.tableSetBgColor(ImGuiTableBgTarget.CellBg, TABLE_COLORS);

            String name = e.get(TagComponent.class).tag();

            if (isLight) {
                name = renderImageBesideTreeNode(name, materialLightBulb.getTextureId(), 19, 2, 22f, 20f);
            } else if (hasMesh) {
                name = renderImageBesideTreeNode(name, materialGreyMesh.getTextureId(), 19, 2, 22f, 20f);
            } else if (isCamera) {
                name = renderImageBesideTreeNode(name, materialCamera.getTextureId(), 19, 2, 22f, 20f);
            } else { //its a group
                name = renderImageBesideTreeNode(name, materialGroupGizmo.getTextureId(), 19, 2, 22, 20f);
            }

            boolean open = ImGui.treeNodeEx(name, ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.SpanFullWidth);

            if (ImGui.beginDragDropTarget()) {
                OxyModel srcEntity = ImGui.acceptDragDropPayload(OxyModel.class);
                if (srcEntity != null)
                    srcEntity.getFamily().setRoot(e.getFamily());
                ImGui.endDragDropTarget();
            }

            if (ImGui.beginDragDropSource()) {
                ImGui.setDragDropPayload(e);
                ImGui.endDragDropSource();
            }

            if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                materialContext = null;
                if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                entityContext = e;
                entityContext.get(SelectedComponent.class).selected = true;
            }

            if (open) {
                List<OxyEntity> relatedToRelated = e.getEntitiesRelatedTo();
                renderTreeNode(relatedToRelated);
                ImGui.treePop();
            } else {
                if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                    materialContext = null;
                    if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                    entityContext = e;
                    entityContext.get(SelectedComponent.class).selected = true;
                }
            }

            ImGui.popID();
        }
    }

    private void updateLooks() {
        for (OxyMaterial m : OxyMaterialPool.getMaterialPool()) {
            ImGui.tableNextRow();
            renderType(null, "Material");
            ImGui.tableSetColumnIndex(0);
            ImGui.image(materialPinkSphere.getTextureId(), 20, 20, 0, 1, 1, 0);
            ImGui.sameLine();
            ImGui.setCursorPosY(ImGui.getCursorPosY() + 2);
            if (ImGui.selectable(m.name, false)) {
                materialContext = m;
                entityContext = null;
            }
        }
    }

    @Override
    public void renderPanel() {

        ImGui.begin("Scene Hierarchy");

        focusedWindow = ImGui.isWindowFocused();
        focusedWindowDragging = focusedWindow && ImGui.isMouseDragging(2);

        if (ImGui.isAnyMouseDown() && !ImGui.isAnyItemHovered() && ImGui.isWindowHovered()) {
            entityContext = null;
            materialContext = null;
        }

        ImGui.pushStyleVar(ImGuiStyleVar.CellPadding, 0, 2);
        ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
        if (ImGui.beginTable("HierarchyTable", 3, ImGuiTableFlags.BordersInnerV)) {
            ImGui.tableSetupColumn("\tName", ImGuiTableColumnFlags.WidthFixed, ImGui.getWindowWidth() / 1.55f);
            ImGui.tableSetupColumn(" View", ImGuiTableColumnFlags.WidthFixed, 35f);
            ImGui.tableSetupColumn("\tType", ImGuiTableColumnFlags.WidthFixed, ImGui.getContentRegionAvailX() / 2);
            ImGui.tableHeadersRow();

            updateEntityPanel(ACTIVE_SCENE.getEntities());

            ImGui.tableNextRow();
            renderType(null, "Scope");
            ImGui.tableSetColumnIndex(0);
            ImGui.tableSetBgColor(ImGuiTableBgTarget.CellBg, TABLE_COLORS);
            String name = "Looks";
            name = renderImageBesideTreeNode(name, dirAssetGrey.getTextureId(), 19, 2, 20, 20);
            if (ImGui.treeNodeEx(name)) {
                updateLooks();
                ImGui.treePop();
            }

            ImGui.endTable();
        }
        ImGui.popItemWidth();
        ImGui.popStyleVar();

        if (ImGui.beginPopupContextWindow("Entity menu")) {
            if (ImGui.button("Create Entity"))
                ACTIVE_SCENE.createEmptyEntity();
            ImGui.separator();
            if (ImGui.button("Mesh"))
                ACTIVE_SCENE.createMeshEntity();
            if (ImGui.button("Sky Light"))
                ACTIVE_SCENE.createSkyLight();
            if (ImGui.button("Point Light"))
                ACTIVE_SCENE.createPointLight();
            if (ImGui.button("Directional Light"))
                ACTIVE_SCENE.createDirectionalLight();
            if (ImGui.button("Perspective Camera"))
                ACTIVE_SCENE.createPerspectiveCamera();
            ImGui.endPopup();
        }

        ImGui.end();
    }
}
