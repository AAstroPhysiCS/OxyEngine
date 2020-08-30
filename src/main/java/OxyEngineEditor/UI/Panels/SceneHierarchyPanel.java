package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngineEditor.Components.ModelMesh;
import OxyEngineEditor.Components.SelectedComponent;
import OxyEngineEditor.Components.TagComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Model.ModelType;
import OxyEngineEditor.Scene.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;

public class SceneHierarchyPanel extends Panel {

    private static SceneHierarchyPanel INSTANCE = null;

    private final SceneLayer sceneLayer;
    private final OxyShader shader;

    private OxyEntity selectedContextEntity;

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
                if (ImGui.treeNodeEx(String.valueOf(entityTagCounter), ImGuiTreeNodeFlags.OpenOnArrow | (selectedContextEntity == entity ? ImGuiTreeNodeFlags.Selected : 0), tagComponent.tag())) {
                    ImGui.treePop();
                }
                if (ImGui.isItemClicked()) {
                    selectedContextEntity = entity;
                }
                entityTagCounter++;
            }
        }, ModelMesh.class, TagComponent.class); //all entities that have a TagComponent and ModelMesh
        entityTagCounter = 0;
    }

    @Override
    public void renderPanel() {
        ImGui.pushStyleColor(ImGuiCol.WindowBg, bgC[0], bgC[1], bgC[2], bgC[3]);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding | ImGuiStyleVar.WindowBorderSize, 0);

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, -1, 8);
        ImGui.begin("Scene Hierarchy");
        ImGui.beginChild("Entities");

        updateEntityPanel();

        if (ImGui.beginPopupContextWindow("item context menu")) {
            if (ImGui.button("Create new entity"))
                addEntity(ModelType.Cube.getPath().getBytes(), shader);
            ImGui.endPopup();
        }
        ImGui.popStyleVar();
        ImGui.endChild();

        ImGui.end();
        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }

    static int counter = 0;

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
        model.get(OxyMaterial.class).texture = texture;
        model.addComponent(new SelectedComponent(false), new TransformComponent(new Vector3f(-30, -10 * counter++, 0)));
        model.updateData();
        sceneLayer.rebuild();
    }
}
