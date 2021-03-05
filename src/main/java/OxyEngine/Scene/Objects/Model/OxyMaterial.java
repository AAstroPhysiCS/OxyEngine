package OxyEngine.Scene.Objects.Model;

import OxyEngine.Components.OxyMaterialIndex;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.System.OxyDisposable;
import OxyEngine.TextureSlot;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.materialContext;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public class OxyMaterial implements OxyDisposable {

    public int assimpIndex, index;

    public ImageTexture albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture, emissiveTexture;
    public final OxyColor albedoColor;

    public String name = "Unnamed Material";

    public float[] metalness;
    public float[] roughness;
    public float[] aoStrength;
    public final float[] normalStrength;
    public float[] emissiveStrength;

    public OxyMaterial(String name, ImageTexture albedoTexture, ImageTexture normalTexture, ImageTexture roughnessTexture, ImageTexture metallicTexture, ImageTexture aoTexture, ImageTexture emissiveTexture,
                       OxyColor albedoColor) {
        this(name, albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture, emissiveTexture, albedoColor, 1.0f, 0.5f, 1.0f, 0.0f, 1.0f);
    }

    public OxyMaterial(OxyModelLoader.AssimpMaterial assimpMaterial) {
        this(assimpMaterial.name().isEmpty() ? "Material (%s)".formatted(UNKNOWN_MATERIAL_COUNT++) : assimpMaterial.name(),
                OxyTexture.loadImage(TextureSlot.ALBEDO, assimpMaterial.textPath()), OxyTexture.loadImage(TextureSlot.NORMAL, assimpMaterial.textPathNormals()),
                OxyTexture.loadImage(TextureSlot.ROUGHNESS, assimpMaterial.textPathRoughness()), OxyTexture.loadImage(TextureSlot.METALLIC, assimpMaterial.textPathMetallic()),
                OxyTexture.loadImage(TextureSlot.AO, assimpMaterial.textPathAO()), OxyTexture.loadImage(TextureSlot.EMISSIVE, assimpMaterial.textPathEmissive()), new OxyColor(assimpMaterial.diffuse()));
    }

    public OxyMaterial(String name, String albedoTexture, String normalTexture, String roughnessTexture, String metallicTexture, String aoTexture, String emissiveTexture,
                       OxyColor albedoColor) {
        this(name, OxyTexture.loadImage(TextureSlot.ALBEDO, albedoTexture), OxyTexture.loadImage(TextureSlot.NORMAL, normalTexture),
                OxyTexture.loadImage(TextureSlot.ROUGHNESS, roughnessTexture), OxyTexture.loadImage(TextureSlot.METALLIC, metallicTexture),
                OxyTexture.loadImage(TextureSlot.AO, aoTexture), OxyTexture.loadImage(TextureSlot.EMISSIVE, emissiveTexture), albedoColor,
                1.0f, 0.5f, 1.0f, 0.0f, 1.0f);
    }

    public OxyMaterial(String name, ImageTexture albedoTexture, ImageTexture normalTexture, ImageTexture roughnessTexture, ImageTexture metallicTexture, ImageTexture aoTexture, ImageTexture emissiveTexture,
                       OxyColor albedoColor, float m_normalStrength, float m_aoStrength, float m_roughness, float m_metalness, float m_emissiveStrength) {
        this.name = name;
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

    public OxyMaterial(Vector4f albedoColor) {
        this("Material (%s)".formatted(UNKNOWN_MATERIAL_COUNT++), (ImageTexture) null, null, null, null, null, null, new OxyColor(albedoColor));
    }

    public OxyMaterial(float r, float g, float b, float a) {
        this("Material (%s)".formatted(UNKNOWN_MATERIAL_COUNT++), (ImageTexture) null, null, null, null, null,null, new OxyColor(new Vector4f(r, g, b, a)));
    }

    public OxyMaterial(Vector4f albedoColor, float metalness, float roughness, float aoStrength, float emissiveStrength) {
        this("Material (%s)".formatted(UNKNOWN_MATERIAL_COUNT++), (ImageTexture) null, null, null, null, null,null, new OxyColor(albedoColor));
        this.metalness = new float[]{metalness};
        this.roughness = new float[]{roughness};
        this.aoStrength = new float[]{aoStrength};
        this.emissiveStrength = new float[]{emissiveStrength};
    }

    public OxyMaterial(OxyMaterial other) {
        if (other.roughnessTexture != null) this.roughnessTexture = new ImageTexture(other.roughnessTexture);
        if (other.aoTexture != null) this.aoTexture = new ImageTexture(other.aoTexture);
        if (other.normalTexture != null) this.normalTexture = new ImageTexture(other.normalTexture);
        if (other.albedoTexture != null) this.albedoTexture = new ImageTexture(other.albedoTexture);
        if (other.metallicTexture != null) this.metallicTexture = new ImageTexture(other.metallicTexture);
        if (other.emissiveTexture != null) this.emissiveTexture = new ImageTexture(other.emissiveTexture);
        this.albedoColor = new OxyColor(other.albedoColor.getNumbers().clone());
        this.metalness = other.metalness.clone();
        this.roughness = other.roughness.clone();
        this.aoStrength = other.aoStrength.clone();
        this.normalStrength = other.normalStrength.clone();
        this.emissiveStrength = other.emissiveStrength.clone();
        this.index = other.index;
        this.assimpIndex = other.assimpIndex;
    }

    public static List<OxyEntity> updateAllEntities(OxyMaterial m) {
        List<OxyEntity> list = new ArrayList<>();
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyNativeObject)) continue;
            if (!e.has(OxyMaterialIndex.class)) continue;
            if (e.get(OxyMaterialIndex.class).index() == m.index) {
                list.add(e);
            }
        }
        return list;
    }

    private void bindTextures() {
        if (roughnessTexture != null)
            glBindTextureUnit(roughnessTexture.getTextureSlot(), roughnessTexture.getTextureId());
        if (metallicTexture != null)
            glBindTextureUnit(metallicTexture.getTextureSlot(), metallicTexture.getTextureId());
        if (normalTexture != null) glBindTextureUnit(normalTexture.getTextureSlot(), normalTexture.getTextureId());
        if (aoTexture != null) glBindTextureUnit(aoTexture.getTextureSlot(), aoTexture.getTextureId());
        if (albedoTexture != null) glBindTextureUnit(albedoTexture.getTextureSlot(), albedoTexture.getTextureId());
        if (emissiveTexture != null) glBindTextureUnit(emissiveTexture.getTextureSlot(), emissiveTexture.getTextureId());
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
            shader.setUniform1f("metallicStrength", metalness[0]);
        }
        if (aoTexture != null) {
            shader.setUniform1i("aoSlot", aoTexture.getTextureSlot());
        } else {
            shader.setUniform1f("aoStrength", aoStrength[0]);
            shader.setUniform1i("aoSlot", 0);
        }
        if (roughnessTexture != null) {
            shader.setUniform1i("roughnessSlot", roughnessTexture.getTextureSlot());
        } else {
            shader.setUniform1f("roughnessStrength", roughness[0]);
            shader.setUniform1i("roughnessSlot", 0);
        }
        if(emissiveTexture != null){
            shader.setUniform1f("emissiveStrength", emissiveStrength[0]);
            shader.setUniform1i("emissiveSlot", emissiveTexture.getTextureSlot());
        } else {
            shader.setUniform1i("emissiveSlot", 0);
        }
        shader.disable();
    }

    private static String currentItem = "No Material";
    private static final ImString inputTextBuffer = new ImString(100);

    public static final GUINode guiNode = () -> {

        if (entityContext == null) return;

        if (ImGui.treeNodeEx("Materials", ImGuiTreeNodeFlags.DefaultOpen)) {

            assert entityContext != null;
            OxyMaterial m = OxyMaterialPool.getMaterial(entityContext);

            if (m != null) {
                final int imageButtonWidth = 85;
                ImGui.columns(2);
                ImGui.setColumnWidth(0, imageButtonWidth + 15);
                ImGui.setColumnWidth(1, ImGui.getWindowWidth());
                if (ImGui.imageButton(-1, imageButtonWidth, 70, 0, 1, 1, 0, 1)) {
                    entityContext = null;
                    materialContext = m;
                    //terminate
                    ImGui.treePop();
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
//                ImGui.alignTextToFramePadding();
//                ImGui.text("Materials:");
//                ImGui.sameLine();
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
            }
            ImGui.columns(1);
            ImGui.treePop();
            ImGui.spacing();
            ImGui.separator();

            /*previewBuffer.bind();
            rendererAPI.clearBuffer();
            oxyShader.enable();
            oxyShader.setUniformMatrix4fv("model", previewSphereEntity.get(TransformComponent.class).transform, false);
            int irradianceSlot = 0, prefilterSlot = 0, brdfLUTSlot = 0;
            if (hdrTexture != null) {
                irradianceSlot = hdrTexture.getIrradianceSlot();
                prefilterSlot = hdrTexture.getPrefilterSlot();
                brdfLUTSlot = hdrTexture.getBDRFSlot();
                hdrTexture.bindAll();
            }
            oxyShader.setUniform1i("iblMap", irradianceSlot);
            oxyShader.setUniform1i("prefilterMap", prefilterSlot);
            oxyShader.setUniform1i("brdfLUT", brdfLUTSlot);
            oxyShader.setUniform1f("gamma", EnvironmentPanel.gammaStrength[0]);
            oxyShader.setUniform1f("exposure", EnvironmentPanel.exposure[0]);
            oxyShader.disable();
            m.push(entityContext.get(OxyShader.class));
            ACTIVE_SCENE.getRenderer().render(SceneRuntime.TS, previewSphereEntity.get(OpenGLMesh.class), editorCameraEntity.get(OxyCamera.class), oxyShader);

            previewBuffer.unbind();
            if (ImGui.imageButton(previewBuffer.getColorAttachmentTexture(0), 80, 60)) {}
            */

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