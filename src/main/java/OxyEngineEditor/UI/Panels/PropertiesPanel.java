package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.TagComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Components.UUIDComponent;
import OxyEngine.Core.Camera.Camera;
import OxyEngine.Core.Camera.SceneCamera;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.Light.SkyLight;
import OxyEngine.Core.Renderer.Mesh.OpenGLMesh;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Scene.Entity;
import OxyEngine.Core.Scene.SceneRuntime;
import OxyEngine.Core.Scene.SceneState;
import OxyEngine.PhysX.PhysXActor;
import OxyEngine.PhysX.PhysXComponent;
import OxyEngine.PhysX.PhysXGeometry;
import OxyEngine.Scripting.Script;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.UI.GUINode;
import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImString;

import java.util.List;

import static OxyEngine.Core.Scene.SceneRuntime.entityContext;
import static OxyEngine.Core.Scene.SceneRuntime.sceneContext;
import static OxyEngine.Utils.DEGREES_TO_RADIANS;

public final class PropertiesPanel extends Panel {

    private static PropertiesPanel INSTANCE = null;

    public static PropertiesPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new PropertiesPanel();
        return INSTANCE;
    }

    public static boolean focusedWindow = false;

    ImString name = new ImString(0);
    final ImString searchAddComponent = new ImString(100);

    private void renderTransformControl(String label, float[] x, float[] y, float[] z, float defaultValue, float speed) {
        ImGui.pushID(label);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 4);

        ImFont font = OxySystem.Font.allFonts.get(0);
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

        List<GUINode> nodeList = entityContext.getGUINodes();

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
        ImGui.textDisabled("ID: " + entityContext.get(UUIDComponent.class).getUUID());

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

            float[] rotationX = new float[]{(float) Math.toDegrees(t.rotation.x)};
            float[] rotationY = new float[]{(float) Math.toDegrees(t.rotation.y)};
            float[] rotationZ = new float[]{(float) Math.toDegrees(t.rotation.z)};

            float[] scaleX = new float[]{t.scale.x};
            float[] scaleY = new float[]{t.scale.y};
            float[] scaleZ = new float[]{t.scale.z};

            renderTransformControl("Translation Control", translationX, translationY, translationZ, 0.0f, 0.1f);
            renderTransformControl("Rotation Control", rotationX, rotationY, rotationZ, 0.0f, 0.1f);
            renderTransformControl("Scale Control", scaleX, scaleY, scaleZ, 1.0f, 0.1f);

            rotationX[0] *= DEGREES_TO_RADIANS;
            rotationY[0] *= DEGREES_TO_RADIANS;
            rotationZ[0] *= DEGREES_TO_RADIANS;

            if (!t.position.equals(translationX[0], translationY[0], translationZ[0]) ||
                    !t.rotation.equals(rotationX[0], rotationY[0], rotationZ[0]) ||
                    !t.scale.equals(scaleX[0], scaleY[0], scaleZ[0]) && SceneState.STOP == sceneContext.getState()) {

                t.set(translationX[0], translationY[0], translationZ[0],
                        rotationX[0], rotationY[0], rotationZ[0],
                        scaleX[0], scaleY[0], scaleZ[0]);

                if (entityContext.has(PhysXComponent.class)) {
                    PhysXComponent physXComponent = entityContext.get(PhysXComponent.class);
                    PhysXActor actor = physXComponent.getActor();
                    actor.setGlobalPose(entityContext.getTransform());
                }

                entityContext.updateTransform();
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
                    if (!nodeList.contains(OpenGLMesh.guiNode))
                        nodeList.add(OpenGLMesh.guiNode);
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Scripts")) {
                if (ImGui.menuItem("Basic Script")) {
                    SceneRuntime.runtimeStop();
                    entityContext.addScript(new Script(null));
                    sceneContext.setState(SceneState.STOP);
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Physics")) {
                if (ImGui.beginMenu("Colliders")) {

                    if (ImGui.menuItem("Mesh Collider")) {
                        if (!entityContext.has(PhysXComponent.class))
                            entityContext.addComponent(new PhysXComponent(entityContext));
                        //TODO: ConvexMesh must also be a thing
                        PhysXGeometry meshGeometry = new PhysXGeometry.TriangleMesh(entityContext);
                        entityContext.get(PhysXComponent.class).setGeometryAs(meshGeometry);
                        nodeList.add(PhysXGeometry.guiNode);
                    }

                    if (ImGui.menuItem("Box Collider")) {
                        if (!entityContext.has(PhysXComponent.class))
                            entityContext.addComponent(new PhysXComponent(entityContext));
                        PhysXGeometry boxGeometry = new PhysXGeometry.Box(entityContext);
                        entityContext.get(PhysXComponent.class).setGeometryAs(boxGeometry);
                        nodeList.add(PhysXGeometry.guiNode);
                    }

                    if (ImGui.menuItem("Sphere Collider")) {
                        if (!entityContext.has(PhysXComponent.class))
                            entityContext.addComponent(new PhysXComponent(entityContext));
                        PhysXGeometry sphereGeometry = new PhysXGeometry.Sphere(entityContext);
                        entityContext.get(PhysXComponent.class).setGeometryAs(sphereGeometry);
                        nodeList.add(PhysXGeometry.guiNode);
                    }

                    if (ImGui.menuItem("Capsule Collider")) {
                        if (!entityContext.has(PhysXComponent.class))
                            entityContext.addComponent(new PhysXComponent(entityContext));
                        PhysXGeometry capsuleGeometry = new PhysXGeometry.Capsule(entityContext);
                        entityContext.get(PhysXComponent.class).setGeometryAs(capsuleGeometry);
                        nodeList.add(PhysXGeometry.guiNode);
                    }

                    ImGui.endMenu();
                }

                if (ImGui.menuItem("Rigid Body")) {
                    if (!entityContext.has(PhysXComponent.class))
                        entityContext.addComponent(new PhysXComponent(entityContext));
                }
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
                    if (!entityContext.has(Camera.class)) {
                        entityContext.addComponent(new SceneCamera(entityContext.getTransform()));
                        if (!nodeList.contains(Camera.guiNode))
                            nodeList.add(Camera.guiNode);
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
                        PointLight pointLight = new PointLight(1.0f, 1.0f, 0.0f);
                        entityContext.addComponent(pointLight);
                        nodeList.add(PointLight.guiNode);
                        Renderer.submitPointLight(pointLight);
                    }
                    //error or hint that lights are single instanced. TODO
                }
                if (ImGui.menuItem("Directional Light")) {
                    if (!entityContext.has(Light.class)) {
                        DirectionalLight directionalLight = new DirectionalLight();
                        entityContext.addComponent(directionalLight);
                        nodeList.add(DirectionalLight.guiNode);
                        Renderer.submitDirectionalLight(directionalLight);
                    }
                    //error or hint that lights are single instanced. TODO
                }

                if (ImGui.menuItem("HDR Sky Light")) {
                    if (!entityContext.has(Light.class)) {
                        SceneRuntime.sceneContext.removeEntity(entityContext);
                        Entity skyLightEnt = sceneContext.createSkyLight();
                        Renderer.submitSkyLight(skyLightEnt.get(SkyLight.class));
                        entityContext = skyLightEnt;
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

        ImGui.end();
    }
}
