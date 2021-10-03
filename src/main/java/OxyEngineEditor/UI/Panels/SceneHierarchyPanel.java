package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.Camera;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Light.SkyLight;
import OxyEngine.Core.Renderer.Mesh.OpenGLMesh;
import OxyEngine.Core.Renderer.Mesh.RenderMode;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Renderer.Texture.Image2DTexture;
import OxyEngine.Core.Scene.Entity;
import OxyEngineEditor.UI.UIAssetManager;
import imgui.ImGui;
import imgui.flag.*;

import java.util.List;
import java.util.Set;

import static OxyEngine.Core.Scene.SceneRuntime.sceneContext;
import static OxyEngine.Core.Scene.SceneRuntime.entityContext;

public final class SceneHierarchyPanel extends Panel {

    private static SceneHierarchyPanel INSTANCE = null;

    private static final Image2DTexture eyeViewTexture = UIAssetManager.getInstance().getUIAsset("UI VIEW");
    private static final Image2DTexture materialGreyMesh = UIAssetManager.getInstance().getUIAsset("UI MATERIALGREYMESH");
    private static final Image2DTexture materialGroupGizmo = UIAssetManager.getInstance().getUIAsset("UI MATERIALGROUPGIZMO");
    private static final Image2DTexture materialLightBulb = UIAssetManager.getInstance().getUIAsset("UI MATERIALLIGHTBULB");
    private static final Image2DTexture materialCamera = UIAssetManager.getInstance().getUIAsset("UI MATERIALCAMERA");
    static final Image2DTexture materialPinkSphere = UIAssetManager.getInstance().getUIAsset("UI MATERIALPINKSPHERE");

    public static boolean focusedWindow, focusedWindowDragging;

    private static int TABLE_COLORS;

    public static SceneHierarchyPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new SceneHierarchyPanel();
        return INSTANCE;
    }

    private SceneHierarchyPanel() {
        TABLE_COLORS = ImGui.getColorU32(bgC[0], bgC[1], bgC[2], bgC[3]);
    }

    private void updateEntityPanel(Set<Entity> entities) {
        for (Entity e : entities) {

            //means that it is the top root
            if (e.familyHasRoot()) continue;

            //skip entities that are hidden
            if (e.has(HiddenComponent.class)) continue;

            boolean hasMesh = e.has(OpenGLMesh.class);
            boolean isLight = e.has(Light.class);
            boolean isSkyLight = e.has(SkyLight.class);
            boolean isCamera = e.has(Camera.class);

            ImGui.tableNextRow();

            TagComponent tagComponent = e.get(TagComponent.class);
            ImGui.pushID(e.get(UUIDComponent.class).toString());

            renderView(e);
            if (isLight) renderType(e, e.get(Light.class).getClass().getSimpleName());
            else if (isSkyLight) renderType(e, e.get(SkyLight.class).getClass().getSimpleName());
            else if (hasMesh) renderType(e, "Mesh");
            else if (isCamera) renderType(e, e.get(Camera.class).getClass().getSimpleName());
            else renderType(e, "Group");

            ImGui.tableSetColumnIndex(0);
            ImGui.tableSetBgColor(ImGuiTableBgTarget.CellBg, TABLE_COLORS);

            String name = tagComponent.tag();

            if (isLight || isSkyLight) {
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
                Entity srcEntity = ImGui.acceptDragDropPayload(Entity.class);
                if (srcEntity != null) {
                    srcEntity.getFamily().setRoot(e.getFamily());
                    srcEntity.updateTransform();
                }
                ImGui.endDragDropTarget();
            }

            if (ImGui.beginDragDropSource()) {
                ImGui.setDragDropPayload(e);
                ImGui.endDragDropSource();
            }

            if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                entityContext = e;
                entityContext.get(SelectedComponent.class).selected = true;
            }

            if (open) {
                List<Entity> relatedToRelated = e.getEntitiesRelatedTo();
                renderTreeNode(relatedToRelated);
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

    private void renderView(Entity entity) {
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

    private void viewAction(Entity entity, List<Entity> relatedEntities) {
        renderModeSwitch(entity);
        for (Entity e : relatedEntities) {
            //calling the relatives too
            viewAction(e, e.getEntitiesRelatedTo());
        }
    }

    private void renderModeSwitch(Entity entity) {
        OpenGLMesh mesh = entity.get(OpenGLMesh.class);
        var comp = mesh.getRenderMode();
        if (comp != RenderMode.NONE) mesh.setRenderMode(RenderMode.NONE);
        else mesh.setRenderMode(RenderMode.TRIANGLES);
    }

    private void renderType(Entity relatedEntities, String type) { // param: for future use
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


    private void renderTreeNode(List<Entity> relatedEntities) {
        if (relatedEntities == null) return;
        if (relatedEntities.size() == 0) return;
        for (int i = 0; i < relatedEntities.size(); i++) {
            ImGui.tableNextRow();
            Entity e = relatedEntities.get(i);
            boolean hasMesh = e.has(OpenGLMesh.class);
            boolean isLight = e.has(Light.class) || e.has(SkyLight.class);
            boolean isSkyLight = e.has(SkyLight.class);
            boolean isCamera = e.has(Camera.class);

            ImGui.pushID(relatedEntities.get(i).hashCode());
            renderView(e);
            if (isLight) renderType(e, e.get(Light.class).getClass().getSimpleName());
            else if (isSkyLight) renderType(e, e.get(SkyLight.class).getClass().getSimpleName());
            else if (hasMesh) renderType(e, "Mesh");
            else if (isCamera) renderType(e, e.get(Camera.class).getClass().getSimpleName());
            else renderType(e, "Group");
            ImGui.tableSetColumnIndex(0);
            ImGui.tableSetBgColor(ImGuiTableBgTarget.CellBg, TABLE_COLORS);

            String name = e.get(TagComponent.class).tag();

            if (isLight || isSkyLight) {
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
                Entity srcEntity = ImGui.acceptDragDropPayload(Entity.class);
                if (srcEntity != null) {
                    srcEntity.getFamily().setRoot(e.getFamily());
                    srcEntity.updateTransform();
                }
                ImGui.endDragDropTarget();
            }

            if (ImGui.beginDragDropSource()) {
                ImGui.setDragDropPayload(e);
                ImGui.endDragDropSource();
            }

            if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
                if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                entityContext = e;
                entityContext.get(SelectedComponent.class).selected = true;
            }

            if (open) {
                List<Entity> relatedToRelated = e.getEntitiesRelatedTo();
                renderTreeNode(relatedToRelated);
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

    @Override
    public void renderPanel() {

        ImGui.begin("Scene Hierarchy");

        focusedWindow = ImGui.isWindowFocused();
        focusedWindowDragging = focusedWindow && ImGui.isMouseDragging(2);

        if (ImGui.isAnyMouseDown() && !ImGui.isAnyItemHovered() && ImGui.isWindowHovered()) {
            entityContext = null;
        }

        ImGui.pushStyleVar(ImGuiStyleVar.CellPadding, 0, 2);
        ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
        if (ImGui.beginTable("HierarchyTable", 3, ImGuiTableFlags.BordersInnerV)) {
            ImGui.tableSetupColumn("\tName", ImGuiTableColumnFlags.WidthFixed, ImGui.getWindowWidth() / 1.55f);
            ImGui.tableSetupColumn(" View", ImGuiTableColumnFlags.WidthFixed, 35f);
            ImGui.tableSetupColumn("\tType", ImGuiTableColumnFlags.WidthFixed, ImGui.getContentRegionAvailX() / 2);
            ImGui.tableHeadersRow();

            updateEntityPanel(sceneContext.getEntities());
            ImGui.tableNextRow();

            ImGui.endTable();
        }
        ImGui.popItemWidth();
        ImGui.popStyleVar();

        if (ImGui.beginPopupContextWindow("Entity menu")) {
            Entity e = null;
            if (ImGui.button("Create Entity"))
                e = sceneContext.createEmptyEntity();
            ImGui.separator();
            if (ImGui.button("Mesh"))
                e = sceneContext.createMeshEntity();
            if (ImGui.button("Sky Light"))
                e = sceneContext.createSkyLight();
            if (ImGui.button("Point Light"))
                e = sceneContext.createPointLight();
            if (ImGui.button("Directional Light"))
                e = sceneContext.createDirectionalLight();
            if (ImGui.button("Perspective Camera"))
                e = sceneContext.createPerspectiveCamera();

            if (e != null && entityContext != null) {
                e.setFamily(new EntityFamily(entityContext.getFamily()));
            }
            ImGui.endPopup();
        }

        ImGui.end();
    }
}
