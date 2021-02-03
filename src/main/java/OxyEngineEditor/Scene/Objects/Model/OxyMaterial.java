package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector4f;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public class OxyMaterial implements OxyDisposable {

    /*
     * Albedo 1
     * Normal 2
     * Roughness 3
     * Metallic 4
     * AO 5
     * HDR 6, 7, 8, 9
     */

    public int assimpIndex, index;

    public ImageTexture albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture;
    public OxyColor albedoColor;

    public String name = "Unnamed Material";

    public float[] metalness;
    public float[] roughness;
    public float[] aoStrength;
    public final float[] normalStrength;

    public OxyMaterial(String name, ImageTexture albedoTexture, ImageTexture normalTexture, ImageTexture roughnessTexture, ImageTexture metallicTexture, ImageTexture aoTexture,
                       OxyColor albedoColor) {
        this(name, albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture, albedoColor, 1.0f, 0.1f, 1.0f, 0.0f);
    }

    public OxyMaterial(OxyModelLoader.AssimpMaterial assimpMaterial) {
        this(assimpMaterial.name(), OxyTexture.loadImage(1, assimpMaterial.textPath()), OxyTexture.loadImage(2, assimpMaterial.textPathNormals()),
                OxyTexture.loadImage(3, assimpMaterial.textPathRoughness()), OxyTexture.loadImage(4, assimpMaterial.textPathMetallic()),
                OxyTexture.loadImage(5, assimpMaterial.textPathAO()), new OxyColor(assimpMaterial.diffuse()));
    }

    public OxyMaterial(String name, String albedoTexture, String normalTexture, String roughnessTexture, String metallicTexture, String aoTexture,
                       OxyColor albedoColor) {
        this(name, OxyTexture.loadImage(1, albedoTexture), OxyTexture.loadImage(2, normalTexture),
                OxyTexture.loadImage(3, roughnessTexture), OxyTexture.loadImage(4, metallicTexture),
                OxyTexture.loadImage(5, aoTexture), albedoColor, 1.0f, 0.1f, 1.0f, 0.0f);
    }

    public OxyMaterial(String name, ImageTexture albedoTexture, ImageTexture normalTexture, ImageTexture roughnessTexture, ImageTexture metallicTexture, ImageTexture aoTexture,
                       OxyColor albedoColor, float m_normalStrength, float m_aoStrength, float m_roughness, float m_metalness) {
        this.name = name;
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

    private static int UNKNOWN_MATERIAL_COUNT = 0;

    public OxyMaterial(Vector4f albedoColor) {
        this("Unknown Material (" + (UNKNOWN_MATERIAL_COUNT++) + ")", (ImageTexture) null, null, null, null, null, new OxyColor(albedoColor));
    }

    public OxyMaterial(float r, float g, float b, float a) {
        this("Unknown Material (" + (UNKNOWN_MATERIAL_COUNT++) + ")", (ImageTexture) null, null, null, null, null, new OxyColor(new Vector4f(r, g, b, a)));
    }

    public OxyMaterial(Vector4f albedoColor, float metalness, float roughness, float aoStrength) {
        this("Unknown Material (" + (UNKNOWN_MATERIAL_COUNT++) + ")", (ImageTexture) null, null, null, null, null, new OxyColor(albedoColor));
        this.metalness = new float[]{metalness};
        this.roughness = new float[]{roughness};
        this.aoStrength = new float[]{aoStrength};
    }

    public OxyMaterial(OxyMaterial other) {
        if (other.roughnessTexture != null) this.roughnessTexture = new ImageTexture(other.roughnessTexture);
        if (other.aoTexture != null) this.aoTexture = new ImageTexture(other.aoTexture);
        if (other.normalTexture != null) this.normalTexture = new ImageTexture(other.normalTexture);
        if (other.albedoTexture != null) this.albedoTexture = new ImageTexture(other.albedoTexture);
        if (other.metallicTexture != null) this.metallicTexture = new ImageTexture(other.metallicTexture);
        this.albedoColor = new OxyColor(other.albedoColor.getNumbers().clone());
        this.metalness = other.metalness.clone();
        this.roughness = other.roughness.clone();
        this.aoStrength = other.aoStrength.clone();
        this.normalStrength = other.normalStrength.clone();
        this.index = other.index;
        this.assimpIndex = other.assimpIndex;
    }

    private void bindTextures() {
        if (roughnessTexture != null)
            glBindTextureUnit(roughnessTexture.getTextureSlot(), roughnessTexture.getTextureId());
        if (metallicTexture != null)
            glBindTextureUnit(metallicTexture.getTextureSlot(), metallicTexture.getTextureId());
        if (normalTexture != null) glBindTextureUnit(normalTexture.getTextureSlot(), normalTexture.getTextureId());
        if (aoTexture != null) glBindTextureUnit(aoTexture.getTextureSlot(), aoTexture.getTextureId());
        if (albedoTexture != null) glBindTextureUnit(albedoTexture.getTextureSlot(), albedoTexture.getTextureId());
    }

    public void push(OxyShader shader) {
        shader.enable();
        bindTextures();
        if (albedoColor != null) {
            shader.setUniformVec3("material.diffuse", albedoColor.getNumbers()[0], albedoColor.getNumbers()[1], albedoColor.getNumbers()[2]);
            shader.setUniformVec3("colorOut", albedoColor.getNumbers()[0], albedoColor.getNumbers()[1], albedoColor.getNumbers()[2]);
            shader.setUniformVec4("colorOut4f", albedoColor.getNumbers()[0], albedoColor.getNumbers()[1], albedoColor.getNumbers()[2], albedoColor.getNumbers()[3]);
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

            assert entityContext != null;
            OxyMaterial m = OxyMaterialPool.getMaterial(entityContext);

            ImGui.text("Name: " + m.name);

            float[] albedo = m.albedoColor.getNumbers();

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
                    m.albedoColor.setColorRGBA(albedo);
                    entityContext.updateData();
                }
            }

            {
                ImGui.spacing();
                ImGui.alignTextToFramePadding();
                ImGui.text("Albedo: (Base Texture): ");
                ImGui.sameLine(ImGui.getContentRegionAvailX() - 130);
                boolean nullT = m.albedoTexture == null;
                if (ImGui.imageButton(nullT ? -1 : m.albedoTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullT) m.albedoTexture.dispose();
                        m.albedoTexture = OxyTexture.loadImage(1, path);
                        entityContext.updateData();
                    }
                }
                ImGui.sameLine(ImGui.getContentRegionAvailX() - 30);
                if (ImGui.button("Remove A")) {
                    m.albedoTexture.dispose();
                    m.albedoTexture = null;
                }

                ImGui.alignTextToFramePadding();
                ImGui.text("Normal Map: ");
                ImGui.sameLine(ImGui.getContentRegionAvailX() - 130);
                boolean nullN = m.normalTexture == null;
                if (ImGui.imageButton(nullN ? -2 : m.normalTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullN) m.normalTexture.dispose();
                        m.normalTexture = OxyTexture.loadImage(2, path);
                        entityContext.updateData();
                    }
                }
                ImGui.sameLine(ImGui.getContentRegionAvailX() - 30);
                if (ImGui.button("Remove N")) {
                    m.normalTexture.dispose();
                    m.normalTexture = null;
                }
                ImGui.alignTextToFramePadding();
                ImGui.text("Normal map strength:");
                ImGui.sameLine();
                ImGui.sliderFloat("###hidelabel n", m.normalStrength, 0, 5);

                ImGui.alignTextToFramePadding();
                ImGui.text("Roughness Map: ");
                ImGui.sameLine(ImGui.getContentRegionAvailX() - 130);
                boolean nullR = m.roughnessTexture == null;
                if (ImGui.imageButton(nullR ? -3 : m.roughnessTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullR) m.roughnessTexture.dispose();
                        m.roughnessTexture = OxyTexture.loadImage(3, path);
                        entityContext.updateData();
                    }
                }
                ImGui.sameLine(ImGui.getContentRegionAvailX() - 30);
                if (ImGui.button("Remove R")) {
                    m.roughnessTexture.dispose();
                    m.roughnessTexture = null;
                }
                ImGui.alignTextToFramePadding();
                ImGui.text("Roughness strength:");
                ImGui.sameLine();
                ImGui.sliderFloat("###hidelabel roughness", m.roughness, 0, 1);

                ImGui.alignTextToFramePadding();
                ImGui.text("Metallic Map: ");
                ImGui.sameLine(ImGui.getContentRegionAvailX() - 130);
                boolean nullM = m.metallicTexture == null;
                if (ImGui.imageButton(nullM ? -4 : m.metallicTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullM) m.metallicTexture.dispose();
                        m.metallicTexture = OxyTexture.loadImage(4, path);
                        entityContext.updateData();
                    }
                }
                ImGui.sameLine(ImGui.getContentRegionAvailX() - 30);
                if (ImGui.button("Remove M")) {
                    m.metallicTexture.dispose();
                    m.metallicTexture = null;
                }
                ImGui.alignTextToFramePadding();
                ImGui.text("Metallic Strength:");
                ImGui.sameLine();
                ImGui.sliderFloat("###hidelabel metallic", m.metalness, 0, 1);

                ImGui.alignTextToFramePadding();
                ImGui.text("AO Map: ");
                ImGui.sameLine(ImGui.getContentRegionAvailX() - 130);
                boolean nullAO = m.aoTexture == null;
                if (ImGui.imageButton(nullAO ? -5 : m.aoTexture.getTextureId(), 80, 60)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (!nullAO) m.aoTexture.dispose();
                        m.aoTexture = OxyTexture.loadImage(5, path);
                        entityContext.updateData();
                    }
                }
                ImGui.sameLine(ImGui.getContentRegionAvailX() - 30);
                if (ImGui.button("Remove AO")) {
                    m.aoTexture.dispose();
                    m.aoTexture = null;
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
        albedoTexture = null;
        aoTexture = null;
        normalTexture = null;
        metallicTexture = null;
        roughnessTexture = null;
    }
}
