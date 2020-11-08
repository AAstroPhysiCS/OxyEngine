package OxyEngineEditor.Components;

import OxyEngine.Scripting.OxyScriptItem;
import OxyEngine.Scripting.ScriptableEntity;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.UI.Panels.PropertyEntry;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;

import java.io.File;

import static OxyEngine.System.OxySystem.oxyAssert;

public class ScriptingComponent {

    private OxyScriptItem scriptItem;
    private Scene scene;
    private OxyEntity entity;

    private final String path;

    public ScriptingComponent(String path) {
        this.path = path;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void setEntity(OxyEntity entity) {
        this.entity = entity;
    }

    private Object getObjectFromFile(String classBinName, Scene scene, OxyEntity entity) {
        try {
            return this.getClass().getClassLoader().loadClass(classBinName).getConstructor(Scene.class, OxyEntity.class).newInstance(scene, entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPackage() {
        File file = new File(path);
        String[] loadedStringInLines = OxySystem.FileSystem.load(path).split("\n");
        for (String s : loadedStringInLines) {
            if (s.startsWith("package")) {
                return s.split(" ")[1].replace(";", "") + "." + file.getName().replace(".java", "");
            }
        }
        return null;
    }

    public void finalizeComponent() {
        if (getObjectFromFile(getPackage(), scene, entity) instanceof ScriptableEntity obj) {
            Class<?> classObj = obj.getClass();
            scriptItem = new OxyScriptItem(obj, classObj.getFields(), classObj.getMethods());
        } else oxyAssert("The script must extend ScriptableEntity class!");
    }

    private static final ImString textBuffer = new ImString(100);
    public static final PropertyEntry node = () -> {
        if (ImGui.collapsingHeader("Scripts", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.button("Add Script", 120, 25);

            ImGui.text("Script 1");
            ImGui.alignTextToFramePadding();
            ImGui.text("Script Path:");
            ImGui.sameLine();
            ImGui.inputText("##hidelabel", textBuffer, ImGuiInputTextFlags.ReadOnly);
            ImGui.sameLine();
            ImGui.button("...");

            ImGui.button("Run Script");
        }
    };

    public OxyScriptItem getScriptItem() {
        return scriptItem;
    }

    public String getPath() {
        return path;
    }
}
