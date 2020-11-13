package OxyEngine.Scripting;

import OxyEngine.System.OxySystem;
import OxyEngineEditor.Components.UUIDComponent;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.UI.Panels.GUIProperty;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngine.System.OxySystem.oxyAssert;
import static OxyEngineEditor.UI.Selector.OxySelectHandler.entityContext;

public class OxyScript {

    private Item scriptItem;
    private Scene scene;
    private OxyEntity entity;

    private static final ExecutorService scriptExecutor = Executors.newSingleThreadExecutor();

    private final String path;

    public OxyScript(String path) {
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
            scriptItem = new Item(obj, classObj.getFields(), classObj.getMethods());
        } else oxyAssert("The script must extend ScriptableEntity class!");
    }

    public static class Item {

        private final ScriptableEntity e;

        private final Field[] fields;
        private final Method[] methods;

        public Item(ScriptableEntity e, Field[] allFields, Method[] allMethods) {
            this.e = e;
            this.fields = allFields;
            this.methods = allMethods;
        }

        public static final record ScriptEntry(String name, Object e) {}

        public Item.ScriptEntry[] getFieldsAsObject() {
            Item.ScriptEntry[] objects = new Item.ScriptEntry[fields.length];
            for (int i = 0; i < fields.length; i++) {
                try {
                    objects[i] = new Item.ScriptEntry(fields[i].getName(), fields[i].get(e));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return objects;
        }


        public void invokeMethod(String nameOfMethod, Object... args) {
            scriptExecutor.submit(() -> {
                for (Method m : methods) {
                    if (m.getName().equals(nameOfMethod)) {
                        try {
                            m.invoke(e, args);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public static void suspendAll(){
        scriptExecutor.shutdown();
    }

    public static final class ScriptingClassGUI implements GUIProperty {

        final ImString buffer = new ImString(100);

        @Override
        public void runEntry() {
            if (ImGui.collapsingHeader("Scripts", ImGuiTreeNodeFlags.DefaultOpen)) {
                ImGui.alignTextToFramePadding();
                ImGui.text("Script Path:");
                ImGui.sameLine();
                ImGui.inputText("##hidelabel" + entityContext.get(UUIDComponent.class).id(), buffer, ImGuiInputTextFlags.ReadOnly);
                ImGui.sameLine();
                ImGui.pushID(entityContext.get(UUIDComponent.class).id().hashCode());
                if (ImGui.button("...")) {
                    String path = openDialog("java", null);
                    if(path != null){
                        buffer.set(path);
                        entityContext.addScript(new OxyScript(path));
                    }
                }
                ImGui.popID();
                for(OxyScript s : entityContext.getScripts()){
                    Item item = s.getScriptItem();
                    Item.ScriptEntry[] entries = item.getFieldsAsObject();
                    for(var entry : entries){
                        ImGui.text(entry.name());
                    }
                }
                ImGui.button("Run Script");
            }
        }
    }

    public Item getScriptItem() {
        return scriptItem;
    }

    public String getPath() {
        return path;
    }
}
