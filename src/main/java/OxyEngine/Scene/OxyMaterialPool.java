package OxyEngine.Scene;

import OxyEngine.Components.OxyMaterialIndex;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Core.Context.Renderer.Texture.OxyColor;
import OxyEngine.Core.Context.Renderer.Texture.OxyTexture;
import OxyEngine.Core.Context.Renderer.Texture.TexturePixelType;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Core.Context.Renderer.Texture.TextureSlot;
import OxyEngine.Scene.OxyMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static OxyEngineEditor.UI.AssetManager.DEFAULT_TEXTURE_PARAMETER;

public class OxyMaterialPool {

    private static boolean[] MATERIAL_SLOT = new boolean[0];
    private static int MATERIAL_SLOT_SIZE = 0;

    private static void checkGrow() {
        if (MATERIAL_SLOT.length == MATERIAL_SLOT_SIZE) {
            boolean[] newMaterialSlot = new boolean[MATERIAL_SLOT.length + 10];
            System.arraycopy(MATERIAL_SLOT, 0, newMaterialSlot, 0, MATERIAL_SLOT.length);
            MATERIAL_SLOT = newMaterialSlot;
        }
    }

    private static int getLatestSlot() {
        checkGrow();
        MATERIAL_SLOT_SIZE++;
        for (int i = 0; i < MATERIAL_SLOT.length; i++) {
            if (!MATERIAL_SLOT[i]) {
                MATERIAL_SLOT[i] = true;
                return i;
            }
        }
        throw new IllegalStateException("getLatestSlot returned -1"); //should be never reached
    }

    static final List<OxyMaterial> materialPool = new ArrayList<>();

    public static List<OxyMaterial> getMaterialPool() {
        return materialPool;
    }

    public static int addMaterial(OxyMaterial m) {
        materialPool.add(m);
        int latest = getLatestSlot();
        return m.index = latest;
    }

    public static int addMaterial(String name, String albedoTexture, String normalTexture, String roughnessTexture, String metallicTexture, String aoTexture, String emissiveTexture,
                                  OxyColor albedoColor, float m_normalStrength, float m_aoStrength, float m_roughness, float m_metalness, float m_emissive) {

        for (OxyMaterial pooled : materialPool) {

            if (pooled.index != -1)
                if (!MATERIAL_SLOT[pooled.index]) continue;

            //checkers => i need to do that... there's no other way.

            boolean albedoCheck = false, roughnessCheck = false, normalCheck = false, metallicCheck = false, aoCheck = false, emissiveCheck = false;
            if (pooled.albedoTexture != null) {
                if (pooled.albedoTexture.getPath().equals(albedoTexture)) {
                    albedoCheck = true;
                }
            } else {
                if (albedoTexture == null || albedoTexture.equals("null")) albedoCheck = true;
            }

            if (pooled.normalTexture != null) {
                if (pooled.normalTexture.getPath().equals(normalTexture)) {
                    normalCheck = true;
                }
            } else {
                if (normalTexture == null || normalTexture.equals("null")) normalCheck = true;
            }

            if (pooled.metallicTexture != null) {
                if (pooled.metallicTexture.getPath().equals(metallicTexture)) {
                    metallicCheck = true;
                }
            } else {
                if (metallicTexture == null || metallicTexture.equals("null")) metallicCheck = true;
            }

            if (pooled.aoTexture != null) {
                if (pooled.aoTexture.getPath().equals(aoTexture)) {
                    aoCheck = true;
                }
            } else {
                if (aoTexture == null || aoTexture.equals("null")) aoCheck = true;
            }

            if (pooled.roughnessTexture != null) {
                if (pooled.roughnessTexture.getPath().equals(roughnessTexture)) {
                    roughnessCheck = true;
                }
            } else {
                if (roughnessTexture == null || roughnessTexture.equals("null")) roughnessCheck = true;
            }

            if (pooled.emissiveTexture != null) {
                if (pooled.emissiveTexture.getPath().equals(emissiveTexture)) {
                    emissiveCheck = true;
                }
            } else {
                if (emissiveTexture == null || emissiveTexture.equals("null")) emissiveCheck = true;
            }

            float[] diffuseArr = albedoColor.getNumbers();
            if (albedoCheck && normalCheck && roughnessCheck && metallicCheck && aoCheck && emissiveCheck
                    && Arrays.equals(pooled.albedoColor.getNumbers(), diffuseArr)
                    && m_normalStrength == pooled.normalStrength[0]
                    && m_aoStrength == pooled.aoStrength[0]
                    && m_metalness == pooled.metalness[0]
                    && m_roughness == pooled.roughness[0]
                    && m_emissive == pooled.emissiveStrength[0])
                return pooled.index;
        }

        OxyMaterial material = new OxyMaterial(name, ShaderLibrary.get("OxyPBR"), OxyTexture.loadImage(TextureSlot.ALBEDO, albedoTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER), OxyTexture.loadImage(TextureSlot.NORMAL, normalTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER),
                OxyTexture.loadImage(TextureSlot.ROUGHNESS, roughnessTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER), OxyTexture.loadImage(TextureSlot.METALLIC, metallicTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER),
                OxyTexture.loadImage(TextureSlot.AO, aoTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER), OxyTexture.loadImage(TextureSlot.EMISSIVE, emissiveTexture, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER),
                albedoColor, m_normalStrength, m_aoStrength, m_roughness, m_metalness, m_emissive);
        materialPool.add(material);
        int latest = getLatestSlot();
        return material.index = latest;
    }

    public static int addMaterial(String name, int meshMaterialIndex, String... assimpMaterialPaths) {
        for (OxyMaterial oxyM : materialPool) if (oxyM.assimpIndex == meshMaterialIndex) return oxyM.index;
        OxyMaterial material = new OxyMaterial(name, ShaderLibrary.get("OxyPBR"), assimpMaterialPaths);
        material.assimpIndex = meshMaterialIndex;
        materialPool.add(material);
        int latest = getLatestSlot();
        return material.index = latest;
    }

    public static Optional<OxyMaterial> getMaterial(int index) {
        if (index == -1) return Optional.empty();
        for (OxyMaterial m : materialPool) {
            if (m.index == index) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public static Optional<OxyMaterial> getMaterial(OxyEntity e) {
        if (materialPool.size() == 0) return Optional.empty();
        if (!e.has(OxyMaterialIndex.class))
            throw new NullPointerException("Entity does not have any OxyMaterialIndex component (no Material)");
        int index = e.get(OxyMaterialIndex.class).index();
        for (OxyMaterial m : materialPool) {
            if (m.index == index) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public static void removeMaterial(OxyMaterial m) {
        MATERIAL_SLOT[m.index] = false;
        m.index = -1;
        materialPool.remove(m);
        MATERIAL_SLOT_SIZE--;
    }

    public static void clear() {
        materialPool.clear();
        Arrays.fill(MATERIAL_SLOT, false);
    }

    public static void newBatch() {
        for (OxyMaterial m : materialPool) {
            m.assimpIndex = -1;
        }
    }
}
