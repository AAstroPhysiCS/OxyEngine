package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.SceneCamera;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Scripting.OxyScript;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyMaterialPool;
import OxyEngine.Scene.SceneRuntime;
import OxyEngine.System.OxyFontSystem;
import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImString;
import org.joml.Vector4f;

import static OxyEngine.Scene.OxyEntity.addParentTransformToChildren;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

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

    @Override
    public void renderPanel() {

        ImGui.begin("Properties");

        if (entityContext == null) {
            ImGui.end();
            return;
        }
        name = new ImString(entityContext.get(TagComponent.class).tag(), 100);

        ImGui.alignTextToFramePadding();
        ImGui.text("Name: ");
        ImGui.sameLine();
        if (ImGui.inputText("##hidelabel InputTextTag", name, ImGuiInputTextFlags.EnterReturnsTrue)) {
            if (name.get().length() == 0) name.set("Unnamed");
            entityContext.get(TagComponent.class).setTag(name.get());
        }
        ImGui.sameLine();
        if (ImGui.button("Add Component", ImGui.getContentRegionAvailX(), 25)) ImGui.openPopup("popupAddComponent", ImGuiPopupFlags.AnyPopup);
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

        if (entityContext == null) {
            ImGui.end();
            return;
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
                        SceneLayer.getInstance().updateAllEntities();
                    }
                    //error or hint that lights are single instanced. TODO
                }
                if (ImGui.menuItem("Directional Light")) {
                    if (!entityContext.has(Light.class)) {
                        DirectionalLight directionalLight = new DirectionalLight();
                        int index = OxyMaterialPool.addMaterial(new OxyMaterial(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
                        entityContext.addComponent(directionalLight, new OxyMaterialIndex(index));
                        entityContext.getGUINodes().add(DirectionalLight.guiNode);
                        SceneLayer.getInstance().updateAllEntities();
                    }
                    //error or hint that lights are single instanced. TODO
                }
                ImGui.endMenu();
            }
            ImGui.endPopup();
        }
        for (GUINode guiNode : entityContext.getGUINodes()) guiNode.runEntry();
        ImGui.popStyleColor();
        ImGui.end();
    }
}
