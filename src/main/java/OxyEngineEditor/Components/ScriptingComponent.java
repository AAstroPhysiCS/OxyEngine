package OxyEngineEditor.Components;

import OxyEngine.System.OxySystem;
import OxyEngineEditor.Scene.OxyScriptItem;
import OxyEngineEditor.Scene.ScriptableEntity;

import java.io.File;

import static OxyEngine.System.OxySystem.oxyAssert;

public class ScriptingComponent implements EntityComponent {

    private OxyScriptItem scriptItem;

    public ScriptingComponent(String path) {

        record CustomFileClassLoader() {
            public Object getObjectFromFile(String classBinName) {
                try {
                    return this.getClass().getClassLoader().loadClass(classBinName).getConstructor().newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        record PackageParser(String path) {
            public String getPackage() {
                File file = new File(path);
                String[] loadedStringInLines = OxySystem.FileSystem.load(path).split("\n");
                for (String s : loadedStringInLines) {
                    if (s.startsWith("package")) {
                        return s.split(" ")[1].replace(";", "") + "." + file.getName().replace(".java", "");
                    }
                }
                return null;
            }
        }

        CustomFileClassLoader loader = new CustomFileClassLoader();
        if (loader.getObjectFromFile(new PackageParser(path).getPackage()) instanceof ScriptableEntity obj) {
            Class<?> classObj = obj.getClass();
            scriptItem = new OxyScriptItem(obj, classObj.getFields(), classObj.getMethods());
        } else oxyAssert("The script must implement ScriptableEntity interface!");
    }

    public OxyScriptItem getScriptItem() {
        return scriptItem;
    }
}
