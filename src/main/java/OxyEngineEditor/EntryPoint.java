package OxyEngineEditor;

import OxyEngine.OxyApplication;
import OxyEngine.System.OxySystem;

public final class EntryPoint {
    public static void main(String[] args) {
        OxySystem.init();
        OxyApplication editorApplication = OxySystem.createOxyApplication();
        editorApplication.start();
    }
}
