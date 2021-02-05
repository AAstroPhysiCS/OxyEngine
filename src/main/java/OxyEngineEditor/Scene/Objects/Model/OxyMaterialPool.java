package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.Components.OxyMaterialIndex;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.System.OxyFontSystem;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.UI.Panels.Panel;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static int addMaterial(String name, String albedoTexture, String normalTexture, String roughnessTexture, String metallicTexture, String aoTexture,
                                  OxyColor albedoColor, float m_normalStrength, float m_aoStrength, float m_roughness, float m_metalness) {

        for (OxyMaterial pooled : materialPool) {

            if (pooled.index != -1)
                if (!MATERIAL_SLOT[pooled.index]) continue;

            //checkers => i need to do that... there's no other way.

            boolean albedoCheck = false, roughnessCheck = false, normalCheck = false, metallicCheck = false, aoCheck = false;
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

            float[] diffuseArr = albedoColor.getNumbers();
            if (albedoCheck && normalCheck && roughnessCheck && metallicCheck && aoCheck
                    && Arrays.equals(pooled.albedoColor.getNumbers(), diffuseArr)
                    && m_normalStrength == pooled.normalStrength[0]
                    && m_aoStrength == pooled.aoStrength[0]
                    && m_metalness == pooled.metalness[0]
                    && m_roughness == pooled.roughness[0])
                return pooled.index;
        }

        OxyMaterial material = new OxyMaterial(name, OxyTexture.loadImage(1, albedoTexture), OxyTexture.loadImage(2, normalTexture),
                OxyTexture.loadImage(3, roughnessTexture), OxyTexture.loadImage(4, metallicTexture), OxyTexture.loadImage(5, aoTexture), albedoColor, m_normalStrength, m_aoStrength, m_roughness, m_metalness);
        materialPool.add(material);
        int latest = getLatestSlot();
        return material.index = latest;
    }

    public static int addMaterial(OxyModelLoader.AssimpMesh mesh, OxyModelLoader.AssimpMaterial m) {
        for(OxyMaterial oxyM : materialPool) if(oxyM.assimpIndex == mesh.materialIndex) return oxyM.index;
        OxyMaterial material = new OxyMaterial(m);
        material.assimpIndex = mesh.materialIndex;
        materialPool.add(material);
        int latest = getLatestSlot();
        return material.index = latest;
    }

    public static OxyMaterial getMaterial(int index) {
        if (index == -1) return null;
        for(OxyMaterial m : materialPool){
            if(m.index == index){
                return m;
            }
        }
        return null;
    }

    public static OxyMaterial getMaterial(OxyEntity e) {
        if (materialPool.size() == 0) return null;
        if (!e.has(OxyMaterialIndex.class)) throw new NullPointerException("Entity does not have any OxyMaterialIndex component (no Material)");
        int index = e.get(OxyMaterialIndex.class).index();
        for(OxyMaterial m : materialPool){
            if(m.index == index){
                return m;
            }
        }
        return null;
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

    private static OxyMaterialPoolPanel INSTANCE = null;

    public static OxyMaterialPoolPanel getPanelInstance() {
        if (INSTANCE == null) INSTANCE = new OxyMaterialPoolPanel();
        return INSTANCE;
    }

    public static void newBatch() {
        for(OxyMaterial m : materialPool){
            m.assimpIndex = -1;
        }
    }

    public static class OxyMaterialPoolPanel extends Panel {

        public OxyMaterialPoolPanel() {
        }

        @Override
        public void preload() {

        }

        @Override
        public void renderPanel() {
            ImGui.begin("Materials");

            ImGui.dummy(0, 10);
            {
                ImGui.setCursorPosX(25);
                ImGui.pushStyleColor(ImGuiCol.ChildBg, 30, 30, 30, 255);
                ImGui.beginChild("ChildM", ImGui.getContentRegionAvailX() * 0.9f, 80, false, ImGuiWindowFlags.HorizontalScrollbar);
                for (var mIndex : materialPool) {
                    OxyMaterial m = OxyMaterialPool.getMaterial(mIndex.index);
                    if (m != null) ImGui.selectable(m.name);
                    if (m == null) ImGui.selectable(m.name, true);
                }
                ImGui.popStyleColor();
                ImGui.endChild();
                ImGui.sameLine();
                float x = ImGui.getCursorPosX();
                float y = ImGui.getCursorPosY();
                ImGui.pushFont(OxyFontSystem.getAllFonts().get(1));
                ImGui.button("+", 25, 25);
                ImGui.setCursorPosX(x);
                ImGui.setCursorPosY(y + 35);
                ImGui.button("-", 25, 25);
                ImGui.popFont();
            }

            ImGui.end();
        }
    }
}
