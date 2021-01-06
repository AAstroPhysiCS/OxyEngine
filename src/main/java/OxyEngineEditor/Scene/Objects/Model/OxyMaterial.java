package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public class OxyMaterial implements EntityComponent, OxyDisposable {

    /*
     * Albedo 1
     * Normal 2
     * Roughness 3
     * Metallic 4
     * AO 5
     * HDR 6, 7, 8, 9
     */

    public ImageTexture albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture;
    public final OxyColor albedoColor;

    public float[] metalness;
    public float[] roughness;
    public float[] aoStrength;
    public final float[] normalStrength;

    public OxyMaterial(ImageTexture albedoTexture, ImageTexture normalTexture, ImageTexture roughnessTexture, ImageTexture metallicTexture, ImageTexture aoTexture,
                       OxyColor albedoColor) {
        this(albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture, albedoColor, 1.0f, 1.0f, 1.0f, 0.0f);
    }

    public OxyMaterial(ImageTexture albedoTexture, ImageTexture normalTexture, ImageTexture roughnessTexture, ImageTexture metallicTexture, ImageTexture aoTexture,
                       OxyColor albedoColor, float m_normalStrength, float m_aoStrength, float m_roughness, float m_metalness) {
        this.albedoTexture = albedoTexture;
        this.roughnessTexture = roughnessTexture;
        this.metallicTexture = metallicTexture;
        this.aoTexture = aoTexture;
        this.normalTexture = normalTexture;
        this.albedoColor = albedoColor;
        metalness = new float[]{m_metalness};
        roughness = new float[]{m_roughness};
        aoStrength = new float[]{m_aoStrength};
        normalStrength = new float[]{m_normalStrength};
    }

    public OxyMaterial(Vector4f albedoColor) {
        this(null, null, null, null, null, new OxyColor(albedoColor));
    }

    public OxyMaterial(float r, float g, float b, float a) {
        this(null, null, null, null, null, new OxyColor(new Vector4f(r, g, b, a)));
    }

    public OxyMaterial(Vector4f albedoColor, float metalness, float roughness, float aoStrength) {
        this(null, null, null, null, null, new OxyColor(albedoColor));
        this.metalness = new float[]{metalness};
        this.roughness = new float[]{roughness};
        this.aoStrength = new float[]{aoStrength};
    }

    public OxyMaterial(OxyMaterial other) {
        if(other.roughnessTexture != null) this.roughnessTexture = new ImageTexture(other.roughnessTexture);
        if(other.aoTexture != null) this.aoTexture = new ImageTexture(other.aoTexture);
        if(other.normalTexture != null) this.normalTexture = new ImageTexture(other.normalTexture);
        if(other.albedoTexture != null) this.albedoTexture = new ImageTexture(other.albedoTexture);
        if(other.metallicTexture != null) this.metallicTexture = new ImageTexture(other.metallicTexture);
        this.albedoColor = new OxyColor(other.albedoColor.getNumbers().clone());
        this.metalness = other.metalness.clone();
        this.roughness = other.roughness.clone();
        this.aoStrength = other.aoStrength.clone();
        this.normalStrength = other.normalStrength.clone();
    }

    private void bindTextures() {
        if (roughnessTexture != null) glBindTextureUnit(roughnessTexture.getTextureSlot(), roughnessTexture.getTextureId());
        if (metallicTexture != null) glBindTextureUnit(metallicTexture.getTextureSlot(), metallicTexture.getTextureId());
        if (normalTexture != null) glBindTextureUnit(normalTexture.getTextureSlot(), normalTexture.getTextureId());
        if (aoTexture != null) glBindTextureUnit(aoTexture.getTextureSlot(), aoTexture.getTextureId());
        if (albedoTexture != null) glBindTextureUnit(albedoTexture.getTextureSlot(), albedoTexture.getTextureId());
    }

    public void push(OxyShader shader) {
        shader.enable();
        bindTextures();
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
        shader.disable();
    }

    public static final GUINode guiNode = () -> {

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
                        entityContext.get(OxyMaterial.class).albedoTexture = OxyTexture.loadImage(1, path);
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
                        entityContext.get(OxyMaterial.class).normalTexture = OxyTexture.loadImage(2, path);
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
                ImGui.sliderFloat("###hidelabel n", m.normalStrength, 0, 5);

                ImGui.alignTextToFramePadding();
                ImGui.text("Roughness Map: ");
                ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 130);
                boolean nullR = entityContext.get(OxyMaterial.class).roughnessTexture == null;
                if (ImGui.imageButton(nullR ? -3 : entityContext.get(OxyMaterial.class).roughnessTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullR) entityContext.get(OxyMaterial.class).roughnessTexture.dispose();
                        entityContext.get(OxyMaterial.class).roughnessTexture = OxyTexture.loadImage(3, path);
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
                        entityContext.get(OxyMaterial.class).metallicTexture = OxyTexture.loadImage(4, path);
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
                        entityContext.get(OxyMaterial.class).aoTexture = OxyTexture.loadImage(5, path);
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
    }
}
