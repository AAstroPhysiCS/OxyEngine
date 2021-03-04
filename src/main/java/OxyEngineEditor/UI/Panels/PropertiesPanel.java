package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.SceneCamera;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scripting.OxyScript;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyMaterialPool;
import OxyEngine.Scene.SceneRuntime;
import OxyEngine.System.OxyFontSystem;
import OxyEngine.TextureSlot;
import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImString;
import org.joml.Vector4f;

import static OxyEngine.Scene.OxyEntity.addParentTransformToChildren;
import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.materialContext;
import static OxyEngineEditor.UI.Panels.ProjectPanel.dirAsset;
import static OxyEngineEditor.UI.Panels.SceneHierarchyPanel.materialPinkSphere;

public class PropertiesPanel extends Panel {

    private static PropertiesPanel INSTANCE = null;

    public static PropertiesPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new PropertiesPanel();
        return INSTANCE;
    }

    public static boolean focusedWindow = false;

    ImString name = new ImString(0);
    final ImString searchAddComponent = new ImString(100);

    @Override
    public void preload() {
    }

    private void renderTransformControl(String label, float[] x, float[] y, float[] z, float defaultValue, float speed) {
        ImGui.pushID(label);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 4);

        ImFont font = OxyFontSystem.getAllFonts().get(0);
        float lineHeight = font.getFontSize() + ImGui.getStyle().getFramePaddingY() * 2.0f;
        float buttonWidth = lineHeight + 3.0f;

        ImGui.pushStyleColor(ImGuiCol.Button, 0.64f, 0.4f, 0.38f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.74f, 0.4f, 0.38f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.64f, 0.4f, 0.38f, 1.0f);
        if (ImGui.button("X", buttonWidth, lineHeight))
            x[0] = defaultValue;
        ImGui.popStyleColor(3);

        ImGui.sameLine();

        ImGui.pushItemWidth(ImGui.calcItemWidth() / 3 + 15);
        ImGui.alignTextToFramePadding();
        ImGui.dragFloat("##hidelabel X", x, speed);
        ImGui.popItemWidth();

        ImGui.sameLine();

        ImGui.pushStyleColor(ImGuiCol.Button, 0.46f, 0.59f, 0.5f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.46f, 0.69f, 0.5f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.46f, 0.59f, 0.5f, 1.0f);
        if (ImGui.button("Y", buttonWidth, lineHeight))
            y[0] = defaultValue;
        ImGui.popStyleColor(3);

        ImGui.sameLine();

        ImGui.pushItemWidth(ImGui.calcItemWidth() / 3 + 15);
        ImGui.dragFloat("##hidelabel Y", y, speed);
        ImGui.popItemWidth();

        ImGui.sameLine();

        ImGui.pushStyleColor(ImGuiCol.Button, 0.33f, 0.48f, 0.6f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.33f, 0.48f, 0.7f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.33f, 0.48f, 0.6f, 1.0f);
        if (ImGui.button("Z", buttonWidth, lineHeight))
            z[0] = defaultValue;
        ImGui.popStyleColor(3);

        ImGui.sameLine();

        ImGui.pushItemWidth(ImGui.calcItemWidth() / 3 + 15);
        ImGui.dragFloat("##hidelabel Z", z, speed);
        ImGui.popItemWidth();

        ImGui.popStyleVar();
        ImGui.popID();
    }

    private static final ImString albedoInputBuffer = new ImString(200);
    private static final ImString metalnessInputBuffer = new ImString(200);
    private static final ImString reflectivityInputBuffer = new ImString(200);
    private static final ImString normalInputBuffer = new ImString(200);
    private static final ImString aoInputBuffer = new ImString(200);

    private String renderImageBesideTreeNode(String name, int textureId, final float sizeX, final float sizeY) {
        name = "\t " + name;
        float cursorPosX = ImGui.getCursorPosX();
        ImGui.setCursorPosX(cursorPosX + 20);
        ImGui.image(textureId, sizeX, sizeY, 0, 1, 1, 0);
        ImGui.sameLine(1);
        return name;
    }

    private void renderPreviewImage(ImageTexture t, float offsetY) {
        if (t != null) {
            float oldCursorPosY = ImGui.getCursorPosY();
            ImGui.setCursorPosY(oldCursorPosY + offsetY);
            ImGui.imageButton(t.getTextureId(), 20, 15, 0, 1, 1, 0, 1);
            if (ImGui.isItemHovered(ImGuiHoveredFlags.AnyWindow |
                    ImGuiHoveredFlags.AllowWhenBlockedByPopup |
                    ImGuiHoveredFlags.AllowWhenBlockedByActiveItem |
                    ImGuiHoveredFlags.AllowWhenOverlapped)) {
                ImGui.openPopup("previewPopup");
                if (ImGui.beginPopup("previewPopup")) {
                    ImGui.image(t.getTextureId(), 150, 150, 0, 1, 1, 0);
                    ImGui.endPopup();
                }
            }
            ImGui.sameLine();
            ImGui.setCursorPosY(oldCursorPosY);
        }
    }

    @Override
    public void renderPanel() {

        ImGui.begin("Properties");

        if (entityContext == null && materialContext == null) {
            ImGui.end();
            return;
        }

        if (entityContext != null) {
            name = new ImString(entityContext.get(TagComponent.class).tag(), 100);

            ImGui.alignTextToFramePadding();
            ImGui.text("Name: ");
            ImGui.sameLine();
            if (ImGui.inputText("##hidelabel InputTextTag", name, ImGuiInputTextFlags.EnterReturnsTrue)) {
                if (name.get().length() == 0) name.set("Unnamed");
                entityContext.get(TagComponent.class).setTag(name.get());
            }
            ImGui.sameLine();
            if (ImGui.button("Add Component", ImGui.getContentRegionAvailX(), 25))
                ImGui.openPopup("popupAddComponent", ImGuiPopupFlags.AnyPopup);
            ImGui.textDisabled("ID: " + entityContext.get(UUIDComponent.class).getUUIDString());

            focusedWindow = ImGui.isWindowFocused();

            ImGui.setNextItemOpen(true, ImGuiCond.Once);
            if (ImGui.treeNodeEx("Transform")) {
                ImGui.columns(2, "myColumns");
                ImGui.setColumnWidth(0, 95);
                ImGui.alignTextToFramePadding();
                ImGui.text("Translation:");
                ImGui.alignTextToFramePadding();
                ImGui.text("Rotation:");
                ImGui.alignTextToFramePadding();
                ImGui.text("Scale:");
                ImGui.nextColumn();

                TransformComponent t = entityContext.get(TransformComponent.class);
                float[] translationX = new float[]{t.position.x};
                float[] translationY = new float[]{t.position.y};
                float[] translationZ = new float[]{t.position.z};

                float[] rotationX = new float[]{t.rotation.x};
                float[] rotationY = new float[]{t.rotation.y};
                float[] rotationZ = new float[]{t.rotation.z};

                float[] scaleX = new float[]{t.scale.x};
                float[] scaleY = new float[]{t.scale.y};
                float[] scaleZ = new float[]{t.scale.z};

                renderTransformControl("Translation Control", translationX, translationY, translationZ, 0.0f, 0.1f);
                renderTransformControl("Rotation Control", rotationX, rotationY, rotationZ, 0.0f, 0.1f);
                renderTransformControl("Scale Control", scaleX, scaleY, scaleZ, 1.0f, 0.1f);

                if (!t.position.equals(translationX[0], translationY[0], translationZ[0]) ||
                        !t.rotation.equals(rotationX[0], rotationY[0], rotationZ[0]) ||
                        !t.scale.equals(scaleX[0], scaleY[0], scaleZ[0])) {

                    t.position.set(translationX[0], translationY[0], translationZ[0]);
                    t.rotation.set(rotationX[0], rotationY[0], rotationZ[0]);
                    t.scale.set(scaleX[0], scaleY[0], scaleZ[0]);
                    entityContext.transformLocally();
                    entityContext.updateVertexData();
                    addParentTransformToChildren(entityContext);
                }

                ImGui.columns(1);
                ImGui.separator();
                ImGui.treePop();
            }

            ImGui.pushStyleColor(ImGuiCol.PopupBg, Panel.bgC[0], Panel.bgC[1], Panel.bgC[2], 1f);
            if (ImGui.beginPopup("popupAddComponent")) {
                ImGui.alignTextToFramePadding();
                ImGui.text("Search:");
                ImGui.sameLine();
                ImGui.inputText("##hidelabel comp_popup_search", searchAddComponent);
                if (ImGui.beginMenu("Mesh")) {
                    if (ImGui.menuItem("Mesh Renderer"))
                        if (!entityContext.getGUINodes().contains(ModelMeshOpenGL.guiNode))
                            entityContext.getGUINodes().add(ModelMeshOpenGL.guiNode);
                    if (ImGui.menuItem("Material"))
                        if (!entityContext.getGUINodes().contains(OxyMaterial.guiNode)) {
                            int index = OxyMaterialPool.addMaterial(new OxyMaterial(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), 1.0f, 1.0f, 1.0f));
                            entityContext.addComponent(new OxyMaterialIndex(index));
                            entityContext.getGUINodes().add(OxyMaterial.guiNode);
                        }
                    ImGui.endMenu();
                }
                if (ImGui.beginMenu("Scripts")) {
                    if (ImGui.menuItem("Basic Script")) {
                        SceneRuntime.stop();
                        entityContext.addScript(new OxyScript(null));
                    }
                    ImGui.endMenu();
                }
                if (ImGui.beginMenu("Physics")) {
                    ImGui.menuItem("Mesh Collider");
                    ImGui.endMenu();
                }
                if (ImGui.beginMenu("UI")) {
                    ImGui.endMenu();
                }
                if (ImGui.beginMenu("Audio")) {
                    ImGui.endMenu();
                }
                if (ImGui.beginMenu("Camera")) {
                    if (ImGui.menuItem("Perspective Camera")) {
                        if (!entityContext.has(OxyCamera.class)) {
                            entityContext.addComponent(new SceneCamera());
                            if (!entityContext.getGUINodes().contains(OxyCamera.guiNode))
                                entityContext.getGUINodes().add(OxyCamera.guiNode);
                            SceneLayer.getInstance().updateCameraEntities();
                        }
                    }

                    if (ImGui.menuItem("Orthographic Camera")) {
                        //Later
                    }
                    //error or hint that lights are single instanced. TODO
                    ImGui.endMenu();
                }
                if (ImGui.beginMenu("Light")) {
                    if (ImGui.menuItem("Point Light")) {
                        if (!entityContext.has(Light.class)) {
                            PointLight pointLight = new PointLight(1.0f, 0.027f, 0.0028f);
                            int index = OxyMaterialPool.addMaterial(new OxyMaterial(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
                            entityContext.addComponent(pointLight, new OxyMaterialIndex(index));
                            if (!entityContext.getGUINodes().contains(OxyMaterial.guiNode))
                                entityContext.getGUINodes().add(OxyMaterial.guiNode);
                            entityContext.getGUINodes().add(PointLight.guiNode);
                            SceneLayer.getInstance().updateModelEntities();
                        }
                        //error or hint that lights are single instanced. TODO
                    }
                    if (ImGui.menuItem("Directional Light")) {
                        if (!entityContext.has(Light.class)) {
                            DirectionalLight directionalLight = new DirectionalLight();
                            int index = OxyMaterialPool.addMaterial(new OxyMaterial(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
                            entityContext.addComponent(directionalLight, new OxyMaterialIndex(index));
                            entityContext.getGUINodes().add(DirectionalLight.guiNode);
                            SceneLayer.getInstance().updateModelEntities();
                        }
                        //error or hint that lights are single instanced. TODO
                    }
                    ImGui.endMenu();
                }
                ImGui.endPopup();
            }
            for (GUINode guiNode : entityContext.getGUINodes())
                guiNode.runEntry();
            ImGui.popStyleColor();

        } else if (materialContext != null) {

            /*
             * NOTE: SINCE ALL THE SUB NODES NEED SEPARATE VALUES AND FIELDS, WE CANT SUM UP THESE NODES
             * THE DIFFERENTIATION BETWEEN THESE SUB NODES WILL BE VISIBLE IN THE FUTURE/LATER
             */

            OxyMaterial m = materialContext;

            ImGui.pushStyleColor(ImGuiCol.ChildBg, Panel.masterCardColor[0], Panel.masterCardColor[1], Panel.masterCardColor[2], Panel.masterCardColor[3]);
            ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 12);
            ImGui.spacing();
            if (ImGui.beginChild("MasterMaterialCard", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                ImGui.spacing();

                String text = renderImageBesideTreeNode(m.name, materialPinkSphere.getTextureId(), 20, 20);

                if (ImGui.treeNodeEx(text, ImGuiTreeNodeFlags.DefaultOpen)) {
                    {

                        ImGui.pushStyleColor(ImGuiCol.ChildBg, Panel.childCardBgC[0], Panel.childCardBgC[1], Panel.childCardBgC[2], Panel.childCardBgC[3]);
                        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 12);
                        ImGui.beginChild("AlbedoChild", ImGui.getContentRegionAvailX(), 100);

                        ImGui.dummy(0, 5);

                        if (ImGui.treeNodeEx("Albedo", ImGuiTreeNodeFlags.DefaultOpen)) {

                            ImGui.columns(2);
                            ImGui.setColumnWidth(0, 160);

                            ImGui.alignTextToFramePadding();
                            ImGui.text("Base Color");

                            ImGui.dummy(0, 2);

                            renderPreviewImage(m.albedoTexture, 0);
                            ImGui.text("Albedo Map");

                            ImGui.nextColumn();

                            ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                            float[] albedo = m.albedoColor.getNumbers();
                            { // BASE COLOR
                                if (ImGui.colorEdit4("Base Color", albedo,
                                        ImGuiColorEditFlags.AlphaBar |
                                                ImGuiColorEditFlags.AlphaPreview |
                                                ImGuiColorEditFlags.NoBorder |
                                                ImGuiColorEditFlags.NoDragDrop |
                                                ImGuiColorEditFlags.DisplayRGB |
                                                ImGuiColorEditFlags.NoLabel
                                ) && entityContext != null) {
                                    m.albedoColor.setColorRGBA(albedo);
                                }
                            }
                            ImGui.popItemWidth();

                            {
                                ImGui.spacing();

                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX() - 50);
                                albedoInputBuffer.set("");
                                if (m.albedoTexture != null) albedoInputBuffer.set(m.albedoTexture.getPath());
                                if (ImGui.inputText("##hideLabelAlbedoMapInput", albedoInputBuffer, ImGuiInputTextFlags.EnterReturnsTrue)) {
                                    String path = albedoInputBuffer.get();
                                    if (path != null) {
                                        if (m.albedoTexture != null) m.albedoTexture.dispose();
                                        m.albedoTexture = OxyTexture.loadImage(TextureSlot.ALBEDO, path);
                                        for (OxyEntity e : OxyMaterial.updateAllEntities(m)) e.updateVertexData();
                                    }
                                }
                                ImGui.popItemWidth();

                                ImGui.sameLine();
                                ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
                                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1.0f, 1.0f, 1.0f, 0.2f);
                                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 1.0f, 1.0f, 0.2f);
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());

                                if (ImGui.imageButton(dirAsset.getTextureId(), 20, 20, 0, 1, 1, 0, 0)) {
                                    String path = openDialog("", null);
                                    albedoInputBuffer.set(path);
                                    if (path != null) {
                                        if (m.albedoTexture != null) m.albedoTexture.dispose();
                                        m.albedoTexture = OxyTexture.loadImage(TextureSlot.ALBEDO, path);
                                        for (OxyEntity e : OxyMaterial.updateAllEntities(m)) e.updateVertexData();
                                    }
                                }

                                ImGui.popStyleColor(3);
                                ImGui.popItemWidth();

                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                                ImGui.sameLine();
                                if (ImGui.button("A")) {
                                    m.albedoTexture.dispose();
                                    m.albedoTexture = null;
                                }
                                ImGui.popItemWidth();
                            }

                            ImGui.treePop();
                        }
                        ImGui.endChild();
                        ImGui.popStyleColor();
                        ImGui.popStyleVar();
                    }

                    {

                        ImGui.pushStyleColor(ImGuiCol.ChildBg, Panel.childCardBgC[0], Panel.childCardBgC[1], Panel.childCardBgC[2], Panel.childCardBgC[3]);
                        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 12);
                        ImGui.beginChild("NormalChild", ImGui.getContentRegionAvailX(), 100);

                        ImGui.dummy(0, 5);

                        if (ImGui.treeNodeEx("Normals", ImGuiTreeNodeFlags.DefaultOpen)) {

                            ImGui.columns(2);
                            ImGui.setColumnWidth(0, 160);

                            renderPreviewImage(m.normalTexture, 3);
                            ImGui.alignTextToFramePadding();
                            ImGui.text("Normal Map");

                            ImGui.dummy(0, 5);

                            ImGui.text("Normal Strength");
                            ImGui.nextColumn();

                            {
                                ImGui.spacing();
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX() - 50);
                                normalInputBuffer.set("");
                                if (m.normalTexture != null) normalInputBuffer.set(m.normalTexture.getPath());
                                if (ImGui.inputText("##hideLabelNormalInput", normalInputBuffer, ImGuiInputTextFlags.EnterReturnsTrue)) {
                                    String path = normalInputBuffer.get();
                                    normalInputBuffer.set(path);
                                    if (path != null) {
                                        if (m.normalTexture != null) m.normalTexture.dispose();
                                        m.normalTexture = OxyTexture.loadImage(TextureSlot.NORMAL, path);
                                        for (OxyEntity e : OxyMaterial.updateAllEntities(m)) e.updateVertexData();
                                    }
                                }
                                ImGui.popItemWidth();

                                ImGui.sameLine();
                                ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
                                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1.0f, 1.0f, 1.0f, 0.2f);
                                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 1.0f, 1.0f, 0.2f);
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());

                                if (ImGui.imageButton(dirAsset.getTextureId(), 20, 20, 0, 1, 1, 0, 0)) {
                                    String path = openDialog("", null);
                                    if (path != null) {
                                        if (m.normalTexture != null) m.normalTexture.dispose();
                                        m.normalTexture = OxyTexture.loadImage(TextureSlot.NORMAL, path);
                                        for (OxyEntity e : OxyMaterial.updateAllEntities(m)) e.updateVertexData();
                                    }
                                }

                                ImGui.popStyleColor(3);
                                ImGui.popItemWidth();

                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                                ImGui.sameLine();
                                if (ImGui.button("N")) {
                                    m.normalTexture.dispose();
                                    m.normalTexture = null;
                                }
                                ImGui.popItemWidth();
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                                ImGui.dummy(0, 1);
                                ImGui.sliderFloat("###hidelabel normals", m.normalStrength, 0, 5);
                                ImGui.popItemWidth();
                            }

                            ImGui.treePop();
                        }
                        ImGui.endChild();
                        ImGui.popStyleColor();
                        ImGui.popStyleVar();
                    }


                    {

                        ImGui.pushStyleColor(ImGuiCol.ChildBg, Panel.childCardBgC[0], Panel.childCardBgC[1], Panel.childCardBgC[2], Panel.childCardBgC[3]);
                        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 12);
                        ImGui.beginChild("MetalnessChild", ImGui.getContentRegionAvailX(), 100);

                        ImGui.dummy(0, 5);

                        if (ImGui.treeNodeEx("Metalness", ImGuiTreeNodeFlags.DefaultOpen)) {

                            ImGui.columns(2);
                            ImGui.setColumnWidth(0, 160);

                            ImGui.alignTextToFramePadding();
                            renderPreviewImage(m.metallicTexture, 3);
                            ImGui.text("Metalness Map");

                            ImGui.dummy(0, 5);

                            ImGui.text("Metalness Strength");
                            ImGui.nextColumn();

                            {
                                ImGui.spacing();
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX() - 50);
                                metalnessInputBuffer.set("");
                                if (m.metallicTexture != null) metalnessInputBuffer.set(m.metallicTexture.getPath());
                                if (ImGui.inputText("##hideLabelMetalnessInput", metalnessInputBuffer, ImGuiInputTextFlags.EnterReturnsTrue)) {
                                    String path = metalnessInputBuffer.get();
                                    metalnessInputBuffer.set(path);
                                    if (path != null) {
                                        if (m.metallicTexture != null) m.metallicTexture.dispose();
                                        m.metallicTexture = OxyTexture.loadImage(TextureSlot.METALLIC, path);
                                        for (OxyEntity e : OxyMaterial.updateAllEntities(m)) e.updateVertexData();
                                    }
                                }
                                ImGui.popItemWidth();

                                ImGui.sameLine();
                                ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
                                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1.0f, 1.0f, 1.0f, 0.2f);
                                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 1.0f, 1.0f, 0.2f);
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());

                                if (ImGui.imageButton(dirAsset.getTextureId(), 20, 20, 0, 1, 1, 0, 0)) {
                                    String path = openDialog("", null);
                                    if (path != null) {
                                        if (m.metallicTexture != null) m.metallicTexture.dispose();
                                        m.metallicTexture = OxyTexture.loadImage(TextureSlot.METALLIC, path);
                                        for (OxyEntity e : OxyMaterial.updateAllEntities(m)) e.updateVertexData();
                                    }
                                }

                                ImGui.popStyleColor(3);
                                ImGui.popItemWidth();

                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                                ImGui.sameLine();
                                if (ImGui.button("M")) {
                                    m.metallicTexture.dispose();
                                    m.metallicTexture = null;
                                }
                                ImGui.popItemWidth();
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                                ImGui.dummy(0, 1);
                                ImGui.sliderFloat("###hidelabel metallic", m.metalness, 0, 1);
                                ImGui.popItemWidth();
                            }

                            ImGui.treePop();
                        }
                        ImGui.endChild();
                        ImGui.popStyleColor();
                        ImGui.popStyleVar();
                    }

                    {

                        ImGui.pushStyleColor(ImGuiCol.ChildBg, Panel.childCardBgC[0], Panel.childCardBgC[1], Panel.childCardBgC[2], Panel.childCardBgC[3]);
                        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 12);
                        ImGui.beginChild("ReflectivityChild", ImGui.getContentRegionAvailX(), 100);

                        ImGui.dummy(0, 5);

                        if (ImGui.treeNodeEx("Reflectivity", ImGuiTreeNodeFlags.DefaultOpen)) {

                            ImGui.columns(2);
                            ImGui.setColumnWidth(0, 160);

                            ImGui.alignTextToFramePadding();
                            renderPreviewImage(m.roughnessTexture, 3);
                            ImGui.text("Reflectivity Map");

                            ImGui.dummy(0, 5);

                            ImGui.text("Reflectivity Strength");
                            ImGui.nextColumn();

                            {
                                ImGui.spacing();
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX() - 50);
                                reflectivityInputBuffer.set("");
                                if (m.roughnessTexture != null)
                                    reflectivityInputBuffer.set(m.roughnessTexture.getPath());
                                if (ImGui.inputText("##hideLabelReflectivityInput", reflectivityInputBuffer, ImGuiInputTextFlags.EnterReturnsTrue)) {
                                    String path = reflectivityInputBuffer.get();
                                    reflectivityInputBuffer.set(path);
                                    if (path != null) {
                                        if (m.roughnessTexture != null) m.roughnessTexture.dispose();
                                        m.roughnessTexture = OxyTexture.loadImage(TextureSlot.ROUGHNESS, path);
                                        for (OxyEntity e : OxyMaterial.updateAllEntities(m)) e.updateVertexData();
                                    }
                                }
                                ImGui.popItemWidth();

                                ImGui.sameLine();
                                ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
                                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1.0f, 1.0f, 1.0f, 0.2f);
                                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 1.0f, 1.0f, 0.2f);
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());

                                if (ImGui.imageButton(dirAsset.getTextureId(), 20, 20, 0, 1, 1, 0, 0)) {
                                    String path = openDialog("", null);
                                    if (path != null) {
                                        if (m.roughnessTexture != null) m.roughnessTexture.dispose();
                                        m.roughnessTexture = OxyTexture.loadImage(TextureSlot.ROUGHNESS, path);
                                        for (OxyEntity e : OxyMaterial.updateAllEntities(m)) e.updateVertexData();
                                    }
                                }

                                ImGui.popStyleColor(3);
                                ImGui.popItemWidth();

                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                                ImGui.sameLine();
                                if (ImGui.button("R")) {
                                    m.roughnessTexture.dispose();
                                    m.roughnessTexture = null;
                                }
                                ImGui.popItemWidth();
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                                ImGui.dummy(0, 1);
                                ImGui.sliderFloat("###hidelabel reflectivity", m.roughness, 0, 1);
                                ImGui.popItemWidth();
                            }

                            ImGui.treePop();
                        }
                        ImGui.endChild();
                        ImGui.popStyleColor();
                        ImGui.popStyleVar();
                    }

                    {

                        ImGui.pushStyleColor(ImGuiCol.ChildBg, Panel.childCardBgC[0], Panel.childCardBgC[1], Panel.childCardBgC[2], Panel.childCardBgC[3]);
                        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 12);
                        ImGui.beginChild("AOChild", ImGui.getContentRegionAvailX(), 100);

                        ImGui.dummy(0, 5);

                        if (ImGui.treeNodeEx("Ambient Occulusion", ImGuiTreeNodeFlags.DefaultOpen)) {

                            ImGui.columns(2);
                            ImGui.setColumnWidth(0, 160);

                            ImGui.alignTextToFramePadding();
                            renderPreviewImage(m.aoTexture, 3);
                            ImGui.text("AO Map");

                            ImGui.dummy(0, 5);

                            ImGui.text("AO Strength");
                            ImGui.nextColumn();

                            {
                                ImGui.spacing();
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX() - 50);
                                aoInputBuffer.set("");
                                if (m.aoTexture != null) aoInputBuffer.set(m.aoTexture.getPath());
                                if (ImGui.inputText("##hideLabelAOInput", aoInputBuffer, ImGuiInputTextFlags.EnterReturnsTrue)) {
                                    String path = aoInputBuffer.get();
                                    aoInputBuffer.set(path);
                                    if (path != null) {
                                        if (m.aoTexture != null) m.aoTexture.dispose();
                                        m.aoTexture = OxyTexture.loadImage(TextureSlot.AO, path);
                                        for (OxyEntity e : OxyMaterial.updateAllEntities(m)) e.updateVertexData();
                                    }
                                }
                                ImGui.popItemWidth();

                                ImGui.sameLine();
                                ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
                                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1.0f, 1.0f, 1.0f, 0.2f);
                                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 1.0f, 1.0f, 0.2f);
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());

                                if (ImGui.imageButton(dirAsset.getTextureId(), 20, 20, 0, 1, 1, 0, 0)) {
                                    String path = openDialog("", null);
                                    if (path != null) {
                                        if (m.aoTexture != null) m.aoTexture.dispose();
                                        m.aoTexture = OxyTexture.loadImage(TextureSlot.AO, path);
                                        for (OxyEntity e : OxyMaterial.updateAllEntities(m)) e.updateVertexData();
                                    }
                                }

                                ImGui.popStyleColor(3);
                                ImGui.popItemWidth();

                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                                ImGui.sameLine();
                                if (ImGui.button("O")) {
                                    m.aoTexture.dispose();
                                    m.aoTexture = null;
                                }
                                ImGui.popItemWidth();
                                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                                ImGui.dummy(0, 1);
                                ImGui.sliderFloat("###hidelabel normals", m.aoStrength, 0, 1);
                                ImGui.popItemWidth();
                            }

                            ImGui.treePop();
                        }
                        ImGui.endChild();
                        ImGui.popStyleColor();
                        ImGui.popStyleVar();
                    }

                    ImGui.treePop();
                }
            }
            ImGui.endChild();
            ImGui.popStyleVar();
            ImGui.popStyleColor();
        }
        ImGui.end();
    }
}
