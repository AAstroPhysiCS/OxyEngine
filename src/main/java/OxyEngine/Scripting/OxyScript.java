package OxyEngine.Scripting;

import OxyEngine.System.OxySystem;
import OxyEngine.Components.UUIDComponent;
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
import java.util.Objects;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngine.System.OxySystem.oxyAssert;
import static OxyEngineEditor.UI.Selector.OxySelectHandler.entityContext;

public class OxyScript {

    private Scene scene;
    private OxyEntity entity;
    private EntityInfoProvider provider;

    private String path;

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

    public static class EntityInfoProvider {

        private final ScriptableEntity obj;

        private final Field[] allFields;
        private final Method[] allMethods;

        public EntityInfoProvider(ScriptableEntity obj) {
            this.obj = obj;
            this.allFields = obj.getClass().getDeclaredFields();
            for (Field f : allFields) f.setAccessible(true);
            this.allMethods = obj.getClass().getDeclaredMethods();
        }

        public Runnable invokeMethod(String nameOfMethod, Object... args) {
            return () -> {
                for (Method m : allMethods) {
                    if (m.getName().equals(nameOfMethod)) {
                        try {
                            m.invoke(obj, args);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        }
    }

    public void loadAssembly() {
        if (path == null) return;
        if (getObjectFromFile(getPackage(), scene, entity) instanceof ScriptableEntity obj) {
            provider = new EntityInfoProvider(obj);
        } else oxyAssert("The script must extend ScriptableEntity class!");
    }

    private final ImString bufferPath = new ImString(100);
    public final GUIProperty guiNode = () -> {
        bufferPath.set(Objects.requireNonNullElse(path, ""));
        final int hashCode = entityContext.hashCode();

        if (ImGui.collapsingHeader("Scripts", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.alignTextToFramePadding();
            ImGui.text("Script Path:");
            ImGui.sameLine();
            ImGui.inputText("##hidelabel oxyScript" + hashCode, bufferPath, ImGuiInputTextFlags.ReadOnly);
            ImGui.sameLine();
            ImGui.pushID(entityContext.get(UUIDComponent.class).getUUIDString() + hashCode());
            if (ImGui.button("...")) {
                String pathDialog = openDialog("java", null);
                if (pathDialog != null) {
                    if (this.path == null) {
                        this.path = pathDialog;
                        loadAssembly();
                    }
                    bufferPath.set(pathDialog);
                }
            }
            ImGui.popID();
            if (provider != null) {
                Field[] allFields = provider.allFields;
                for (var entry : allFields) {
                    Object obj = null;
                    try {
                        obj = entry.get(provider.obj);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    ImGui.columns(2, "myColumns" + hashCode);
                    ImGui.alignTextToFramePadding();
                    ImGui.text(entry.getName());
                    ImGui.nextColumn();
                    ImGui.pushItemWidth(ImGui.getContentRegionAvailWidth());
                    try {
                        if (obj instanceof Number n) {
                            double convertedD = Double.parseDouble(n.toString());
                            float[] buffer = new float[]{(float) convertedD};
                            ImGui.dragFloat("##hidelabel entrySlider" + entityContext.hashCode() + entry.getName(), buffer);
                            //int has an speciality
                            if (obj instanceof Integer) entry.set(provider.obj, Float.valueOf(buffer[0]).intValue());
                            else if (obj instanceof Long) entry.set(provider.obj, Float.valueOf(buffer[0]).longValue());
                            else if (obj instanceof Short)
                                entry.set(provider.obj, Float.valueOf(buffer[0]).shortValue());
                            else if (obj instanceof Byte) entry.set(provider.obj, Float.valueOf(buffer[0]).byteValue());
                            else entry.set(provider.obj, buffer[0]);
                        }
                        if (obj instanceof Boolean b) {
                            if (ImGui.radioButton("##hidelabel entryRadioButton" + entityContext.hashCode() + entry.getName(), b)) {
                                entry.set(provider.obj, !b);
                            }
                        }
                        if (obj instanceof Character c) {
                            ImString buffer = new ImString(1);
                            buffer.set(String.valueOf(c));
                            ImGui.inputText("##hidelabel entryCharacter" + entityContext.hashCode() + entry.getName(), buffer);
                            if (buffer.getLength() > 0) entry.set(provider.obj, buffer.get().charAt(0));
                        }
                        if (obj instanceof String s) {
                            ImString buffer = new ImString(100);
                            buffer.set(s);
                            ImGui.inputText("##hidelabel entryString" + entityContext.hashCode() + entry.getName(), buffer);
                            entry.set(provider.obj, buffer.get());
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    ImGui.popItemWidth();
                    ImGui.columns(1);
                }
            }
            ImGui.button("Run Script");
            ImGui.sameLine(ImGui.getContentRegionAvailWidth() - 100);
            if (ImGui.button("Reload Assembly")) {
                loadAssembly();
            }
        }
    };

    public EntityInfoProvider getProvider() {
        return provider;
    }

    public String getPath() {
        return path;
    }
}
