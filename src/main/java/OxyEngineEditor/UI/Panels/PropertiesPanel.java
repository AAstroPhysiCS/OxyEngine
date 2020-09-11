package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngineEditor.Components.SelectedComponent;
import OxyEngineEditor.Components.TagComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.io.File;
import java.util.List;

import static OxyEngine.System.OxySystem.BASE_PATH;
import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngineEditor.UI.Selector.OxySelectHandler.entityContext;
import static OxyEngineEditor.UI.Selector.OxySelectHandler.gizmoEntityContextControl;

public class PropertiesPanel extends Panel {

    private static PropertiesPanel INSTANCE = null;

    public static PropertiesPanel getInstance(SceneLayer sceneLayer) {
        if (INSTANCE == null) INSTANCE = new PropertiesPanel(sceneLayer);
        return INSTANCE;
    }

    private final SceneLayer sceneLayer;

    public PropertiesPanel(SceneLayer sceneLayer) {
        this.sceneLayer = sceneLayer;
    }

    static boolean init = false;
    public static boolean focusedWindow = false;
    private static final ImBoolean helpWindowBool = new ImBoolean();
    public static final float[] normalMapStrength = new float[]{0f};

    ImString name = new ImString(0);
    ImString meshPath = new ImString(0);
    float[] albedo = {0, 0, 0, 0};
    float[] normals = {0, 0, 0, 0};
    float[] metalness = {0, 0, 0, 0};
    float[] roughness = {0, 0, 0, 0};

    @Override
    public void preload() {

    }

    @Override
    public void renderPanel() {
        gizmoEntityContextControl(entityContext);

        if (entityContext != null) {
            name = new ImString(entityContext.get(TagComponent.class).tag(), 100);
            meshPath = new ImString(new File(BASE_PATH).toURI().relativize(new File(entityContext.get(Mesh.class).getPath()).toURI()).getPath());
            OxyMaterial material = entityContext.get(OxyMaterial.class);
            albedo = material.diffuseColor.getNumbers();
            normals = material.specularColor.getNumbers();
            metalness = material.ambientColor.getNumbers();
            roughness = material.diffuseColor.getNumbers();
        }

        ImGui.begin("Properties");

        if (entityContext == null) {
            ImGui.end();
            return;
        }

        ImGui.alignTextToFramePadding();
        ImGui.text("Name: ");
        ImGui.sameLine();
        if (ImGui.inputText("##hidelabel", name, ImGuiInputTextFlags.EnterReturnsTrue)) {
            if (name.get().length() == 0) name.set("Unnamed");
            entityContext.get(TagComponent.class).setTag(name.get());
        }

        focusedWindow = ImGui.isWindowFocused();

        if (!init) ImGui.setNextItemOpen(true);
        if (ImGui.treeNode("Transform")) {
            ImGui.columns(2, "myColumns");
            if (!init) ImGui.setColumnOffset(0, -90f);
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

        if (!init) ImGui.setNextItemOpen(true);
        if (ImGui.treeNode("Mesh")) {
            ImGui.columns(2, "myColumns");
            if (!init) {
                ImGui.setColumnOffset(0, -100f);
                init = true;
            }
            ImGui.alignTextToFramePadding();
            ImGui.text("Mesh:");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailWidth() - 30f);
            ImGui.inputText("##hidelabel", meshPath);
            ImGui.popItemWidth();
            ImGui.sameLine();
            if (ImGui.button("...")) {
                String path = openDialog("obj", null);
                if (path != null) {
                    if (entityContext != null) {
                        List<OxyEntity> eList = sceneLayer.getScene().createModelEntities(path, entityContext.get(OxyShader.class));
                        for (OxyEntity e : eList) {
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
            ImGui.separator();
            ImGui.treePop();
        }

        if (entityContext == null) {
            ImGui.end();
            return;
        }

        if (ImGui.treeNodeEx("Base", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.alignTextToFramePadding();
            { // BASE COLOR
                ImGui.text("Base color: ");
                ImGui.sameLine();
                if (ImGui.colorEdit4("Base color", albedo,
                        ImGuiColorEditFlags.AlphaBar |
                                ImGuiColorEditFlags.AlphaPreview |
                                ImGuiColorEditFlags.NoBorder |
                                ImGuiColorEditFlags.NoDragDrop |
                                ImGuiColorEditFlags.DisplayRGB |
                                ImGuiColorEditFlags.NoLabel
                ) && entityContext != null) {
                    entityContext.get(OxyMaterial.class).diffuseColor.setColorRGBA(albedo);
                }
            }

            { // ALBEDO
                ImGui.spacing();
                ImGui.alignTextToFramePadding();
                ImGui.text("Albedo: (Base Texture): ");
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 130);
                boolean nullT = entityContext.get(OxyMaterial.class).albedoTexture == null;
                if (ImGui.imageButton(nullT ? 0 : entityContext.get(OxyMaterial.class).albedoTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    entityContext.get(OxyMaterial.class).albedoTexture = OxyTexture.loadImage(path);
                }
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 30);
                if (ImGui.button("Remove A")) {
                    entityContext.get(OxyMaterial.class).albedoTexture = null;
                }

                ImGui.alignTextToFramePadding();
                ImGui.text("Normal Map: ");
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 130);
                boolean nullN = entityContext.get(OxyMaterial.class).normalTexture == null;
                if (ImGui.imageButton(nullN ? 0 : entityContext.get(OxyMaterial.class).normalTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path == null) return;
                    entityContext.get(OxyMaterial.class).normalTexture = OxyTexture.loadImage(path);
                }
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 30);
                if (ImGui.button("Remove N")) {
                    entityContext.get(OxyMaterial.class).normalTexture = null;
                }
            }

            ImGui.treePop();
        }
        ImGui.alignTextToFramePadding();
        ImGui.text("Normal map strength:");
        ImGui.sameLine();
        ImGui.sliderFloat("###hidelabel", normalMapStrength, 0, 100);

        final float windowWidth = ImGui.getWindowWidth();
        ImGui.spacing();
        ImGui.spacing();
        ImGui.setCursorPosX(windowWidth / 2 - 150);
        ImGui.pushItemWidth(-1);
        ImGui.button("Add Component", 300, 30);
        ImGui.popItemWidth();
        /*ImGui.spacing();
        if (ImGui.checkbox("Albedo", albedoRadio)) {
            if (ImGui.colorButton("alb", albedo,
                    ImGuiColorEditFlags.AlphaBar |
                            ImGuiColorEditFlags.AlphaPreview |
                            ImGuiColorEditFlags.NoBorder,
                    70, 70
            ) && entityContext != null) {
                String path = openDialog("", null);
                entityContext.get(OxyMaterial.class).albedoTexture = OxyTexture.loadImage(path);
            }
            ImGui.sameLine();
            if(albedoRadio.get() && entityContext != null) {
//                ImGui.sameLine();
                ImGui.colorEdit4("alb", albedo,
                        ImGuiColorEditFlags.NoSidePreview |
                                ImGuiColorEditFlags.NoSmallPreview |
                                ImGuiColorEditFlags.DisplayRGB |
                                ImGuiColorEditFlags.NoLabel
                );
                entityContext.get(OxyMaterial.class).diffuseColor.setColorRGBA(albedo);
            }
        }
        if (ImGui.collapsingHeader("Normals", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.colorButton("norm", normals,
                    ImGuiColorEditFlags.AlphaBar |
                            ImGuiColorEditFlags.AlphaPreview |
                            ImGuiColorEditFlags.NoBorder,
                    70, 70
            );
            ImGui.sameLine();
            ImGui.colorEdit4("norm", normals,
                    ImGuiColorEditFlags.NoSidePreview |
                            ImGuiColorEditFlags.NoSmallPreview |
                            ImGuiColorEditFlags.DisplayRGB |
                            ImGuiColorEditFlags.NoLabel
            );
        }
        if (ImGui.collapsingHeader("Metalness", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.colorButton("mness", metalness,
                    ImGuiColorEditFlags.AlphaBar |
                            ImGuiColorEditFlags.AlphaPreview |
                            ImGuiColorEditFlags.NoBorder,
                    70, 70
            );
            ImGui.sameLine();
            ImGui.colorEdit4("mness", metalness,
                    ImGuiColorEditFlags.NoSidePreview |
                            ImGuiColorEditFlags.NoSmallPreview |
                            ImGuiColorEditFlags.DisplayRGB |
                            ImGuiColorEditFlags.NoLabel
            );
        }
        if (ImGui.collapsingHeader("Roughness", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.colorButton("rness", roughness,
                    ImGuiColorEditFlags.AlphaBar |
                            ImGuiColorEditFlags.AlphaPreview |
                            ImGuiColorEditFlags.NoBorder,
                    70, 70
            );
            ImGui.sameLine();
            ImGui.colorEdit4("rness", roughness,
                    ImGuiColorEditFlags.NoSidePreview |
                            ImGuiColorEditFlags.NoSmallPreview |
                            ImGuiColorEditFlags.DisplayRGB |
                            ImGuiColorEditFlags.NoLabel
            );
        }*/

        ImGui.checkbox("Demo", helpWindowBool);
        if (helpWindowBool.get()) ImGui.showDemoWindow();

        ImGui.end();
    }
}
