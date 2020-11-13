package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.Components.EntityComponent;
import OxyEngineEditor.Components.UIEditable;
import OxyEngineEditor.UI.Panels.GUIProperty;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngineEditor.UI.Selector.OxySelectHandler.entityContext;

public class OxyMaterial implements EntityComponent, UIEditable, OxyDisposable {

    public ImageTexture albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture, heightTexture;
    public final OxyColor albedoColor;

    public float[] metalness;
    public float[] roughness;
    public float[] aoStrength;
    public final float[] normalStrength;
    public final float[] heightScale;

    float[] albedo = {0, 0, 0, 0};

    public OxyMaterial(ImageTexture albedoTexture, ImageTexture normalTexture, ImageTexture roughnessTexture, ImageTexture metallicTexture, ImageTexture aoTexture,
                       ImageTexture heightTexture, OxyColor albedoColor) {
        this.albedoTexture = albedoTexture;
        this.roughnessTexture = roughnessTexture;
        this.metallicTexture = metallicTexture;
        this.aoTexture = aoTexture;
        this.normalTexture = normalTexture;
        this.heightTexture = heightTexture;
        this.albedoColor = albedoColor;
        metalness = new float[]{0.0f};
        roughness = new float[]{1.0f};
        aoStrength = new float[]{1.0f};
        normalStrength = new float[]{1.0f};
        heightScale = new float[]{1.0f};
    }

    public OxyMaterial(Vector4f albedoColor) {
        this(null, null, null, null, null, null, new OxyColor(albedoColor));
    }

    public OxyMaterial(float r, float g, float b, float a) {
        this(null, null, null, null, null, null, new OxyColor(new Vector4f(r, g, b, a)));
    }

    public OxyMaterial(Vector4f albedoColor, float metalness, float roughness, float aoStrength) {
        this(null, null, null, null, null, null, new OxyColor(albedoColor));
        this.metalness = new float[]{metalness};
        this.roughness = new float[]{roughness};
        this.aoStrength = new float[]{aoStrength};
    }

    public OxyMaterial(OxyMaterial other) {
        this.roughnessTexture = other.roughnessTexture;
        this.aoTexture = other.aoTexture;
        this.normalTexture = other.normalTexture;
        this.albedoTexture = other.albedoTexture;
        this.metallicTexture = other.metallicTexture;
        this.heightTexture = other.heightTexture;
        this.albedoColor = new OxyColor(other.albedoColor.getNumbers().clone());
        this.metalness = other.metalness.clone();
        this.roughness = other.roughness.clone();
        this.aoStrength = other.aoStrength.clone();
        this.normalStrength = other.normalStrength.clone();
        this.heightScale = other.heightScale.clone();
    }

    public void push(OxyShader shader) {
        shader.enable();
        if (albedoColor != null) {
            shader.setUniformVec3("material.diffuse", new Vector3f(albedoColor.getNumbers()[0], albedoColor.getNumbers()[1], albedoColor.getNumbers()[2]));
            shader.setUniformVec3("colorOut", new Vector3f(albedoColor.getNumbers()[0], albedoColor.getNumbers()[1], albedoColor.getNumbers()[2]));
            shader.setUniformVec4("colorOut4f", new Vector4f(albedoColor.getNumbers()[0], albedoColor.getNumbers()[1], albedoColor.getNumbers()[2], albedoColor.getNumbers()[3]));
        }
        if (normalTexture != null) {
            shader.setUniform1i("normalMapSlot", normalTexture.getTextureSlot());
            shader.setUniform1f("normalMapStrength", normalStrength[0]);
        } else {
            shader.setUniform1i("normalMapSlot", 0);
        }
        if (metallicTexture != null) {
            shader.setUniform1i("metallicSlot", metallicTexture.getTextureSlot());
        } else {
            shader.setUniform1i("metallicSlot", 0);
            shader.setUniform1f("metallicFloat", metalness[0]);
        }
        if (aoTexture != null) {
            shader.setUniform1i("aoSlot", aoTexture.getTextureSlot());
        } else {
            shader.setUniform1f("aoFloat", aoStrength[0]);
            shader.setUniform1i("aoSlot", 0);
        }
        if (roughnessTexture != null) {
            shader.setUniform1i("roughnessSlot", roughnessTexture.getTextureSlot());
        } else {
            shader.setUniform1f("roughnessFloat", roughness[0]);
            shader.setUniform1i("roughnessSlot", 0);
        }
        if (heightTexture != null) {
            shader.setUniform1i("heightSlot", heightTexture.getTextureSlot());
            shader.setUniform1f("heightScale", heightScale[0]);
        } else {
            shader.setUniform1i("heightSlot", 0);
        }
        shader.disable();
    }

    public static final GUIProperty guiNode = () -> {

        if (entityContext == null) return;

        if (ImGui.collapsingHeader("Material", ImGuiTreeNodeFlags.DefaultOpen)) {

            float[] albedo = entityContext.get(OxyMaterial.class).albedoColor.getNumbers();

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
                    entityContext.get(OxyMaterial.class).albedoColor.setColorRGBA(albedo);
                }
            }

            {
                assert entityContext != null;
                OxyMaterial m = entityContext.get(OxyMaterial.class);

                ImGui.spacing();
                ImGui.alignTextToFramePadding();
                ImGui.text("Albedo: (Base Texture): ");
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 130);
                boolean nullT = entityContext.get(OxyMaterial.class).albedoTexture == null;
                if (ImGui.imageButton(nullT ? -1 : entityContext.get(OxyMaterial.class).albedoTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullT) entityContext.get(OxyMaterial.class).albedoTexture.dispose();
                        entityContext.get(OxyMaterial.class).albedoTexture = OxyTexture.loadImage(path);
                    }
                }
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 30);
                if (ImGui.button("Remove A")) {
                    entityContext.get(OxyMaterial.class).albedoTexture.dispose();
                    entityContext.get(OxyMaterial.class).albedoTexture = null;
                }

                ImGui.alignTextToFramePadding();
                ImGui.text("Normal Map: ");
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 130);
                boolean nullN = entityContext.get(OxyMaterial.class).normalTexture == null;
                if (ImGui.imageButton(nullN ? -2 : entityContext.get(OxyMaterial.class).normalTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullN) entityContext.get(OxyMaterial.class).normalTexture.dispose();
                        entityContext.get(OxyMaterial.class).normalTexture = OxyTexture.loadImage(path);
                    }
                }
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 30);
                if (ImGui.button("Remove N")) {
                    entityContext.get(OxyMaterial.class).normalTexture.dispose();
                    entityContext.get(OxyMaterial.class).normalTexture = null;
                }
                ImGui.alignTextToFramePadding();
                ImGui.text("Normal map strength:");
                ImGui.sameLine();
                ImGui.sliderFloat("###hidelabel n", m.normalStrength, 0, 100);

                ImGui.alignTextToFramePadding();
                ImGui.text("Roughness Map: ");
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 130);
                boolean nullR = entityContext.get(OxyMaterial.class).roughnessTexture == null;
                if (ImGui.imageButton(nullR ? -3 : entityContext.get(OxyMaterial.class).roughnessTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullR) entityContext.get(OxyMaterial.class).roughnessTexture.dispose();
                        entityContext.get(OxyMaterial.class).roughnessTexture = OxyTexture.loadImage(path);
                    }
                }
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 30);
                if (ImGui.button("Remove R")) {
                    entityContext.get(OxyMaterial.class).roughnessTexture.dispose();
                    entityContext.get(OxyMaterial.class).roughnessTexture = null;
                }
                ImGui.alignTextToFramePadding();
                ImGui.text("Roughness strength:");
                ImGui.sameLine();
                ImGui.sliderFloat("###hidelabel roughness", m.roughness, 0, 1);

                ImGui.alignTextToFramePadding();
                ImGui.text("Metallic Map: ");
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 130);
                boolean nullM = entityContext.get(OxyMaterial.class).metallicTexture == null;
                if (ImGui.imageButton(nullM ? -4 : entityContext.get(OxyMaterial.class).metallicTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullM) entityContext.get(OxyMaterial.class).metallicTexture.dispose();
                        entityContext.get(OxyMaterial.class).metallicTexture = OxyTexture.loadImage(path);
                    }
                }
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 30);
                if (ImGui.button("Remove M")) {
                    entityContext.get(OxyMaterial.class).metallicTexture.dispose();
                    entityContext.get(OxyMaterial.class).metallicTexture = null;
                }
                ImGui.alignTextToFramePadding();
                ImGui.text("Metallic Strength:");
                ImGui.sameLine();
                ImGui.sliderFloat("###hidelabel metallic", m.metalness, 0, 1);

                ImGui.alignTextToFramePadding();
                ImGui.text("AO Map: ");
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 130);
                boolean nullAO = entityContext.get(OxyMaterial.class).aoTexture == null;
                if (ImGui.imageButton(nullAO ? -5 : entityContext.get(OxyMaterial.class).aoTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullAO) entityContext.get(OxyMaterial.class).aoTexture.dispose();
                        entityContext.get(OxyMaterial.class).aoTexture = OxyTexture.loadImage(path);
                    }
                }
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 30);
                if (ImGui.button("Remove AO")) {
                    entityContext.get(OxyMaterial.class).aoTexture.dispose();
                    entityContext.get(OxyMaterial.class).aoTexture = null;
                }
                ImGui.alignTextToFramePadding();
                ImGui.text("AO strength:");
                ImGui.sameLine();
                ImGui.sliderFloat("###hidelabel ao", m.aoStrength, 0, 1);

                ImGui.alignTextToFramePadding();
                ImGui.text("Height Map: ");
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 130);
                boolean nullHeight = entityContext.get(OxyMaterial.class).heightTexture == null;
                if (ImGui.imageButton(nullHeight ? -6 : entityContext.get(OxyMaterial.class).heightTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullHeight) entityContext.get(OxyMaterial.class).heightTexture.dispose();
                        entityContext.get(OxyMaterial.class).heightTexture = OxyTexture.loadImage(path);
                    }
                }
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 30);
                if (ImGui.button("Remove H")) {
                    entityContext.get(OxyMaterial.class).heightTexture.dispose();
                    entityContext.get(OxyMaterial.class).heightTexture = null;
                }
            }
        }
    };

    @Override
    public void dispose() {
        if (albedoTexture != null) albedoTexture.dispose();
        if (aoTexture != null) aoTexture.dispose();
        if (normalTexture != null) normalTexture.dispose();
        if (metallicTexture != null) metallicTexture.dispose();
        if (roughnessTexture != null) roughnessTexture.dispose();
        if (heightTexture != null) heightTexture.dispose();
    }
}
