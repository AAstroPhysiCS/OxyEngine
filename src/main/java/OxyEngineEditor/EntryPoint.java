package OxyEngineEditor;

import OxyEngine.System.OxySystem;

public class EntryPoint {
    public static void main(String[] args) {
        OxySystem.init();
        new EditorApplication();
    }
}
