package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Components.SelectedComponent;
import OxyEngineEditor.Components.TagComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.util.List;

import static OxyEngineEditor.UI.Selector.OxySelectSystem.entityContext;
import static OxyEngineEditor.UI.Selector.OxySelectSystem.gizmoEntityContextControl;

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

    static String lastTexturePath = null;
    static int lastTextureID = -1;
    static final ImString inputTextPath = new ImString();
    static final float[] diffuseColor = new float[]{0f, 0.0f, 0.0f, 0.0f};
    static final float[] specularColor = new float[]{0f, 0.0f, 0.0f, 0.0f};
    static final float[] ambientColor = new float[]{0f, 0.0f, 0.0f, 0.0f};
    final float[] NULL = new float[]{0, 0, 0};
    static boolean init = false;
    public static boolean focusedWindow = false;
    private static final ImBoolean helpWindowBool = new ImBoolean();

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

        if (entityContext != null) {
            name = new ImString(entityContext.get(TagComponent.class).tag(), 100);
            meshPath = new ImString(entityContext.get(Mesh.class).getPath());
            albedo = entityContext.get(OxyMaterial.class).diffuseColor.getNumbers();
            normals = entityContext.get(OxyMaterial.class).specularColor.getNumbers();
            metalness = entityContext.get(OxyMaterial.class).ambientColor.getNumbers();
            roughness = entityContext.get(OxyMaterial.class).diffuseColor.getNumbers();
        }

        ImGui.begin("Properties");

        ImGui.alignTextToFramePadding();
        ImGui.text("Name: ");
        ImGui.sameLine();
        if (ImGui.inputText("##hidelabel", name, ImGuiInputTextFlags.EnterReturnsTrue) && entityContext != null) {
            if (name.get().length() == 0) name.set("Unnamed");
            entityContext.get(TagComponent.class).setTag(name.get());
        }

        focusedWindow = ImGui.isWindowFocused();

        if (!init) ImGui.setNextItemOpen(true);
        if (ImGui.treeNode("Transform")) {
            ImGui.columns(2, "myColumns");
            if (!init) ImGui.setColumnOffset(0, -70f);
            ImGui.alignTextToFramePadding();
            ImGui.text("Translation:");
            ImGui.alignTextToFramePadding();
            ImGui.text("Rotation:");
            ImGui.alignTextToFramePadding();
            ImGui.text("Scale:");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailWidth());

            if (entityContext != null) {
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
            } else {
                ImGui.dragFloat3("##hidelabel T", NULL, 0.01f);
                ImGui.dragFloat3("##hidelabel R", NULL, 0.01f);
                ImGui.dragFloat3("##hidelabel S", NULL, 0.01f);
                albedo = NULL;
                normals = NULL;
                roughness = NULL;
                metalness = NULL;
            }
            gizmoEntityContextControl(entityContext);

            ImGui.popItemWidth();
            ImGui.columns(1);
            ImGui.separator();
            ImGui.treePop();
        }

        if (!init) ImGui.setNextItemOpen(true);
        if (ImGui.treeNode("Mesh")) {
            ImGui.columns(2, "myColumns");
            if (!init) {
                ImGui.setColumnOffset(0, -70f);
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
                PointerBuffer buffer = PointerBuffer.allocateDirect(16);
                int result = NativeFileDialog.NFD_OpenDialog("obj", null, buffer);
                if (result == NativeFileDialog.NFD_OKAY) {
                    if (entityContext != null) {
                        String path = buffer.getStringASCII();
                        List<OxyEntity> eList = sceneLayer.getScene().createModelEntities(path, entityContext.get(OxyShader.class));
                        for (OxyEntity e : eList) {
                            TransformComponent t = new TransformComponent(entityContext.get(TransformComponent.class));
                            e.addComponent(t, new SelectedComponent(true, false));
                            e.constructData();
                        }
                        sceneLayer.getScene().removeEntity(entityContext);
                        sceneLayer.rebuild();
                        meshPath = new ImString(path);
                        entityContext = null;
                    }
                }
                NativeFileDialog.nNFD_Free(buffer.get());
            }
            ImGui.columns(1);
            ImGui.separator();
            ImGui.treePop();
        }

        if (ImGui.beginPopupContextWindow("item context menu")) {
            if (ImGui.button("Add Component")) {
            }
            ImGui.endPopup();
        }

        ImGui.spacing();
        if (ImGui.collapsingHeader("Albedo", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.colorButton("alb", albedo,
                    ImGuiColorEditFlags.AlphaBar |
                            ImGuiColorEditFlags.AlphaPreview |
                            ImGuiColorEditFlags.NoBorder,
                    70, 70
            );
            ImGui.sameLine();
            ImGui.colorEdit4("alb", albedo,
                    ImGuiColorEditFlags.NoSidePreview |
                            ImGuiColorEditFlags.NoSmallPreview |
                            ImGuiColorEditFlags.DisplayRGB |
                            ImGuiColorEditFlags.NoLabel
            );
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
        }

        if (ImGui.collapsingHeader("Texture", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.inputText("###label", inputTextPath, ImGuiInputTextFlags.ReadOnly);
            ImGui.sameLine();
            if (ImGui.button("...")) {
                PointerBuffer buffer = PointerBuffer.allocateDirect(16);
                int result = NativeFileDialog.NFD_OpenDialog("", null, buffer);
                if (result == NativeFileDialog.NFD_OKAY) {
                    PropertiesPanel.lastTexturePath = buffer.getStringASCII();
                    PropertiesPanel.inputTextPath.set(PropertiesPanel.lastTexturePath);
                }
                NativeFileDialog.nNFD_Free(buffer.get());
            }
        }

        ImGui.checkbox("Demo", helpWindowBool);
        if (helpWindowBool.get()) ImGui.showDemoWindow();

        ImGui.end();
    }
}
