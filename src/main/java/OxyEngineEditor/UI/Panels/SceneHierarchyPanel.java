package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngineEditor.Components.ModelMesh;
import OxyEngineEditor.Components.SelectedComponent;
import OxyEngineEditor.Components.TagComponent;
import OxyEngineEditor.Scene.Model.ModelType;
import OxyEngineEditor.Scene.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;

import static OxyEngineEditor.UI.Selector.OxySelectSystem.entityContext;

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

    int entityTagCounter = 0;

    public void updateEntityPanel() {
        sceneLayer.getScene().each(entity -> {
            TagComponent tagComponent = entity.get(TagComponent.class);
            if (tagComponent != null) {
                if (ImGui.treeNodeEx(String.valueOf(entityTagCounter), ImGuiTreeNodeFlags.OpenOnArrow | (entityContext == entity ? ImGuiTreeNodeFlags.Selected : 0), tagComponent.tag())) {
                    ImGui.treePop();
                }
                if (ImGui.isItemClicked()) {
                    if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                    entityContext = entity;
                    entityContext.get(SelectedComponent.class).selected = true;
                }
                entityTagCounter++;
            }
        }, ModelMesh.class); //all entities that have x
        entityTagCounter = 0;
    }

    @Override
    public void renderPanel() {

        ImGui.begin("Scene Hierarchy");
        focusedWindow = ImGui.isWindowFocused();
        focusedWindowDragging = focusedWindow && ImGui.isMouseDragging(2);

        updateEntityPanel();

        if (ImGui.beginPopupContextWindow("item context menu")) {
            if (ImGui.button("Create Entity"))
                addEntity(ModelType.Cube.getPath().getBytes(), shader);
            ImGui.endPopup();
        }

        ImGui.end();
    }

    private void addEntity(byte[] data, OxyShader shader) {
        OxyEntity model = sceneLayer.getScene().createModelEntity(new String(data), shader);
        //TEMP
        ImageTexture texture = null;
        if (PropertiesPanel.lastTexturePath != null) {
            texture = OxyTexture.loadImage(PropertiesPanel.lastTexturePath);
            PropertiesPanel.lastTextureID = texture.getTextureId();
        }

        model.get(OxyMaterial.class).diffuseColor = new OxyColor(PropertiesPanel.diffuseColor);
        model.get(OxyMaterial.class).ambientColor = new OxyColor(PropertiesPanel.ambientColor);
        model.get(OxyMaterial.class).specularColor = new OxyColor(PropertiesPanel.specularColor);
        model.get(OxyMaterial.class).albedoTexture = texture;
        model.addComponent(new SelectedComponent(false));
        model.updateData();
        sceneLayer.rebuild();
    }
}
