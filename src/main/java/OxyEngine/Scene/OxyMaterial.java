package OxyEngine.Scene;

import OxyEngine.Components.OxyMaterialIndex;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Core.Context.Renderer.Texture.*;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import org.joml.Vector4f;

import static OxyEngine.Scene.SceneRuntime.entityContext;
import static OxyEngine.Scene.SceneRuntime.materialContext;
import static OxyEngine.System.OxySystem.parseStringToVector4f;
import static OxyEngineEditor.UI.AssetManager.DEFAULT_TEXTURE_PARAMETER;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public class OxyMaterial implements OxyDisposable {

    public int assimpIndex, index;

    private OxyShader shader;

    public Image2DTexture albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture, emissiveTexture;
    public final OxyColor albedoColor;

    public String name;

    public float[] metalness, roughness, aoStrength, emissiveStrength;
    public final float[] normalStrength;

    public final float[] dynamicFriction = new float[]{0.5f}, staticFriction = new float[]{0.5f}, restitution = new float[]{0.5f};

    public OxyMaterial(String name, OxyShader shader, Image2DTexture albedoTexture, Image2DTexture normalTexture, Image2DTexture roughnessTexture, Image2DTexture metallicTexture, Image2DTexture aoTexture, Image2DTexture emissiveTexture,
                       OxyColor albedoColor) {
        this(name, shader, albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture, emissiveTexture, albedoColor, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f);
    }

    public OxyMaterial(String name, OxyShader shader, String... assimpMaterialPaths) {
        this(name.isEmpty() ? "Material (%s)".formatted(UNKNOWN_MATERIAL_COUNT++) : name, shader,
                OxyTexture.loadImage(TextureSlot.ALBEDO, assimpMaterialPaths[0], TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER), OxyTexture.loadImage(TextureSlot.NORMAL, assimpMaterialPaths[1], TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER),
                OxyTexture.loadImage(TextureSlot.ROUGHNESS, assimpMaterialPaths[2], TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER), OxyTexture.loadImage(TextureSlot.METALLIC, assimpMaterialPaths[3], TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER),
                OxyTexture.loadImage(TextureSlot.AO, assimpMaterialPaths[4], TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER), OxyTexture.loadImage(TextureSlot.EMISSIVE, assimpMaterialPaths[5], TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER), new OxyColor(parseStringToVector4f(assimpMaterialPaths[6])));
    }

    public OxyMaterial(String name, OxyShader shader, String albedoTexture, String normalTexture, String roughnessTexture, String metallicTexture, String aoTexture, String emissiveTexture,
                       OxyColor albedoColor) {
        this(name, shader, OxyTexture.loadImage(TextureSlot.ALBEDO, albedoTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER), OxyTexture.loadImage(TextureSlot.NORMAL, normalTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER),
                OxyTexture.loadImage(TextureSlot.ROUGHNESS, roughnessTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER), OxyTexture.loadImage(TextureSlot.METALLIC, metallicTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER),
                OxyTexture.loadImage(TextureSlot.AO, aoTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER), OxyTexture.loadImage(TextureSlot.EMISSIVE, emissiveTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER), albedoColor,
                1.0f, 1.0f, 1.0f, 0.0f, 1.0f);
    }

    public OxyMaterial(String name, OxyShader shader, Image2DTexture albedoTexture, Image2DTexture normalTexture, Image2DTexture roughnessTexture, Image2DTexture metallicTexture, Image2DTexture aoTexture, Image2DTexture emissiveTexture,
                       OxyColor albedoColor, float m_normalStrength, float m_aoStrength, float m_roughness, float m_metalness, float m_emissiveStrength) {
        this.name = name;
        this.shader = shader;
        this.albedoTexture = albedoTexture;
        this.roughnessTexture = roughnessTexture;
        this.metallicTexture = metallicTexture;
        this.aoTexture = aoTexture;
        this.normalTexture = normalTexture;
        this.emissiveTexture = emissiveTexture;
        this.albedoColor = albedoColor;
        metalness = new float[]{m_metalness};
        roughness = new float[]{m_roughness};
        aoStrength = new float[]{m_aoStrength};
        normalStrength = new float[]{m_normalStrength};
        emissiveStrength = new float[]{m_emissiveStrength};
    }

    private static int UNKNOWN_MATERIAL_COUNT = 0;

    public OxyMaterial(OxyShader shader, Vector4f albedoColor) {
        this("Material (%s)".formatted(UNKNOWN_MATERIAL_COUNT++), shader, (Image2DTexture) null, null, null, null, null, null, new OxyColor(albedoColor));
    }

    public OxyMaterial(OxyShader shader, float r, float g, float b, float a) {
        this("Material (%s)".formatted(UNKNOWN_MATERIAL_COUNT++), shader, (Image2DTexture) null, null, null, null, null, null, new OxyColor(new Vector4f(r, g, b, a)));
    }

    public OxyMaterial(OxyShader shader, Vector4f albedoColor, float metalness, float roughness, float aoStrength, float emissiveStrength) {
        this("Material (%s)".formatted(UNKNOWN_MATERIAL_COUNT++), shader, (Image2DTexture) null, null, null, null, null, null, new OxyColor(albedoColor));
        this.metalness = new float[]{metalness};
        this.roughness = new float[]{roughness};
        this.aoStrength = new float[]{aoStrength};
        this.emissiveStrength = new float[]{emissiveStrength};
    }

    public OxyMaterial(OxyMaterial other) {
        if (other.roughnessTexture != null) this.roughnessTexture = OxyTexture.loadImage(other.roughnessTexture);
        if (other.aoTexture != null) this.aoTexture = OxyTexture.loadImage(other.aoTexture);
        if (other.normalTexture != null) this.normalTexture = OxyTexture.loadImage(other.normalTexture);
        if (other.albedoTexture != null) this.albedoTexture = OxyTexture.loadImage(other.albedoTexture);
        if (other.metallicTexture != null) this.metallicTexture = OxyTexture.loadImage(other.metallicTexture);
        if (other.emissiveTexture != null) this.emissiveTexture = OxyTexture.loadImage(other.emissiveTexture);
        this.shader = other.shader;
        this.albedoColor = new OxyColor(other.albedoColor.getNumbers().clone());
        this.name = other.name + " Copy";
        this.metalness = other.metalness.clone();
        this.roughness = other.roughness.clone();
        this.aoStrength = other.aoStrength.clone();
        this.normalStrength = other.normalStrength.clone();
        this.emissiveStrength = other.emissiveStrength.clone();
        this.index = other.index;
        this.assimpIndex = other.assimpIndex;
    }

    void bindTextures() {
        if (roughnessTexture != null)
            glBindTextureUnit(roughnessTexture.getTextureSlot(), roughnessTexture.getTextureId());
        if (metallicTexture != null)
            glBindTextureUnit(metallicTexture.getTextureSlot(), metallicTexture.getTextureId());
        if (normalTexture != null) glBindTextureUnit(normalTexture.getTextureSlot(), normalTexture.getTextureId());
        if (aoTexture != null) glBindTextureUnit(aoTexture.getTextureSlot(), aoTexture.getTextureId());
        if (albedoTexture != null) glBindTextureUnit(albedoTexture.getTextureSlot(), albedoTexture.getTextureId());
        if (emissiveTexture != null)
            glBindTextureUnit(emissiveTexture.getTextureSlot(), emissiveTexture.getTextureId());
    }

    public void push(OxyShader shader) {
        bindTextures();

        if (albedoColor != null) {
            shader.setUniformVec3("Material.diffuse", albedoColor.getNumbers()[0], albedoColor.getNumbers()[1], albedoColor.getNumbers()[2]);
        }
        if (albedoTexture != null) {
            shader.setUniform1i("Material.albedoMapSlot", albedoTexture.getTextureSlot());
        } else {
            shader.setUniform1i("Material.albedoMapSlot", 0);
        }

        if (normalTexture != null) {
            shader.setUniform1i("Material.normalMapSlot", normalTexture.getTextureSlot());
            shader.setUniform1f("Material.normalMapStrength", normalStrength[0]);
        } else {
            shader.setUniform1i("Material.normalMapSlot", 0);
        }
        if (metallicTexture != null) {
            shader.setUniform1i("Material.metallicSlot", metallicTexture.getTextureSlot());
        } else {
            shader.setUniform1i("Material.metallicSlot", 0);
            shader.setUniform1f("Material.metallicStrength", metalness[0]);
        }
        if (aoTexture != null) {
            shader.setUniform1i("Material.aoSlot", aoTexture.getTextureSlot());
        } else {
            shader.setUniform1f("Material.aoStrength", aoStrength[0]);
            shader.setUniform1i("Material.aoSlot", 0);
        }
        if (roughnessTexture != null) {
            shader.setUniform1i("Material.roughnessSlot", roughnessTexture.getTextureSlot());
        } else {
            shader.setUniform1f("Material.roughnessStrength", roughness[0]);
            shader.setUniform1i("Material.roughnessSlot", 0);
        }
        if (emissiveTexture != null) {
            shader.setUniform1f("Material.emissiveStrength", emissiveStrength[0]);
            shader.setUniform1i("Material.emissiveSlot", emissiveTexture.getTextureSlot());
        } else {
            shader.setUniform1i("Material.emissiveSlot", 0);
        }
    }

    private static String currentItem = "No Material";
    private static String currentShaderItem = "OxyPBR";
    private static final ImString inputTextBuffer = new ImString(100);

    public static final GUINode guiNode = () -> {

        if (entityContext == null) return;

        if (ImGui.treeNodeEx("Materials", ImGuiTreeNodeFlags.DefaultOpen)) {

            ImGui.text("Shader");
            ImGui.sameLine();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
            if (ImGui.beginCombo("##hideLabelMaterialShader", currentShaderItem)) {
                for (OxyShader shaders : ShaderLibrary.getAllShaders()) {
                    String s = shaders.getName();
                    boolean isSelected = (currentShaderItem.equals(s));
                    if (ImGui.selectable(s, isSelected)) {
                        currentShaderItem = s;
                        OxyMaterialPool.getMaterial(entityContext).ifPresent((material) -> material.shader = ShaderLibrary.get(currentShaderItem));
                    }
                }
                ImGui.endCombo();
            }
            ImGui.popItemWidth();

            OxyMaterialPool.getMaterial(entityContext).ifPresent((m) -> {
                final int imageButtonWidth = 85;
                ImGui.columns(2);
                ImGui.setColumnWidth(0, imageButtonWidth + 15);
                ImGui.setColumnWidth(1, ImGui.getWindowWidth());
                if (ImGui.imageButton(-1, imageButtonWidth, 70, 0, 1, 1, 0, 1)) {
                    entityContext = null;
                    materialContext = m;
                    //terminate
                    return;
                }
                ImGui.nextColumn();
                inputTextBuffer.set(m.name);
                ImGui.alignTextToFramePadding();
                ImGui.text("Name:");
                ImGui.sameLine();
                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                if (ImGui.inputText("##hideLabelMaterialsInputText", inputTextBuffer, ImGuiInputTextFlags.EnterReturnsTrue)) {
                    m.name = inputTextBuffer.get();
                }
                ImGui.popItemWidth();
                ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                if (ImGui.beginCombo("##hideLabelMaterials", m.name)) {
                    for (OxyMaterial allMaterials : OxyMaterialPool.getMaterialPool()) {
                        String s = allMaterials.name;
                        boolean isSelected = (currentItem.equals(s));
                        if (ImGui.selectable(s, isSelected)) {
                            currentItem = s;
                            entityContext.addComponent(new OxyMaterialIndex(allMaterials.index));
                        }
                    }
                    ImGui.endCombo();
                }
                ImGui.popItemWidth();
            });

            ImGui.columns(1);
            ImGui.treePop();
            ImGui.spacing();
            ImGui.separator();
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

    public OxyShader getShader() {
        return shader;
    }
}
