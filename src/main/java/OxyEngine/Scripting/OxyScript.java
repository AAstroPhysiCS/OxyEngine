package OxyEngine.Scripting;

import OxyEngine.Components.UUIDComponent;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.Scene;
import OxyEngine.Scene.SceneRuntime;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngine.System.OxySystem.oxyAssert;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;
import static OxyEngineEditor.UI.Panels.ProjectPanel.dirAssetGrey;

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
//        compiler.run(System.in, System.out, System.err, "--enable-preview", "--release", "15",
//                "-classpath", System.getProperty("java.class.path"), "-d", System.getProperty("user.dir") + "\\target\\classes", path);
        try {
            return SceneRuntime.loadClass(classBinName, scene, entity);
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

    public static final class EntityInfoProvider implements OxyProvider {

        private final ScriptableEntity obj;

        private final Field[] allFields;

        public EntityInfoProvider(ScriptableEntity obj) {
            this.obj = obj;
            this.allFields = obj.getClass().getDeclaredFields();
//            System.out.println(Arrays.toString(allFields));
//            for (Field f : allFields) f.setAccessible(true);
        }

        @Override
        public void invokeCreate() {
            obj.onCreate();
        }

        @Override
        public void invokeUpdate(float ts) {
            if (!ACTIVE_SCENE.isValid(obj.entity)) return;
            obj.updateScript(ts);
        }
    }

    public void invokeCreate() {
        if (provider == null) throw new IllegalStateException("Provider is null!");
        provider.invokeCreate();
    }

    public void loadAssembly() {
        if (provider != null) {
            ScriptEngine.removeProvider(provider);
            provider = null;
        }
        if (path == null) return;
        if (getObjectFromFile(getPackage(), scene, entity) instanceof ScriptableEntity obj) {
            provider = new EntityInfoProvider(obj);
            ScriptEngine.addProvider(provider);
        } else oxyAssert("The script must extend ScriptableEntity class!");
    }

    private final ImString bufferPath = new ImString(100);
    public final GUINode guiNode = () -> {
        if (entityContext == null) return;

        bufferPath.set(Objects.requireNonNullElse(path, ""));
        final int hashCode = entityContext.hashCode();

        if (ImGui.treeNodeEx("Scripts", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.alignTextToFramePadding();
            ImGui.text("Script Path:");
            ImGui.sameLine();
            ImGui.inputText("##hidelabel oxyScript" + hashCode, bufferPath, ImGuiInputTextFlags.ReadOnly);
            ImGui.sameLine();
            ImGui.pushID(entityContext.get(UUIDComponent.class).getUUIDString() + hashCode());
            if (ImGui.imageButton(dirAssetGrey.getTextureId(), 20, 20, 0, 1, 1, 0, 0)) {
                String pathDialog = openDialog("java", null);
                if (pathDialog != null) {
                    this.path = pathDialog;
                    loadAssembly();
                    SceneRuntime.stop();
                    bufferPath.set(pathDialog);
                }
            }
            ImGui.popID();
            if (provider != null) {
                Field[] allFields = provider.allFields;
                for (var entry : allFields) {
                    if (entry.getModifiers() != Modifier.PUBLIC) continue;
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
                    ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
                    try {
                        if (obj instanceof Number n) {
                            double convertedD = Double.parseDouble(n.toString());
                            float[] buffer = new float[]{(float) convertedD};
                            ImGui.dragFloat("##hidelabel entrySlider" + entityContext.hashCode() + entry.getName(), buffer);
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
            if (ImGui.button("Run Script")) {
                //TODO: run just the specific script
            }
            ImGui.treePop();
            ImGui.separator();
            ImGui.spacing();
            /*if (ImGui.button("Reload Assembly")) {
                SceneRuntime.stop();
                loadAssembly();
                SceneRuntime.onCreate();
            }*/
        }
    };

    public EntityInfoProvider getProvider() {
        return provider;
    }

    public String getPath() {
        return path;
    }
}
