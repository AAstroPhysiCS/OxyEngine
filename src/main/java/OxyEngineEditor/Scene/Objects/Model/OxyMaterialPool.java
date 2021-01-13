package OxyEngineEditor.Scene.Objects.Model;

import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;

import java.util.ArrayList;
import java.util.List;

public class OxyMaterialPool {

    private static final List<OxyMaterial> materialPool = new ArrayList<>();

    public static void addMaterial(OxyMaterial m){
        if(!materialPool.contains(m)) materialPool.add(m);
    }

    public static void removeMaterial(OxyMaterial m) {
        materialPool.remove(m);
    }
    public static final GUINode guiNode = () -> {

        ImGui.begin("Materials");

        if (ImGui.collapsingHeader("Material", ImGuiTreeNodeFlags.DefaultOpen)) {

        }

        ImGui.end();
    };
}
