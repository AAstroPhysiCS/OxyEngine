package OxyEngineEditor.UI;

import OxyEngine.System.OxySystem;

import java.io.File;

public class FontLoader {

    private FontLoader(){}

    private static FontLoader INSTANCE = null;

    public static FontLoader getInstance(){
        if(INSTANCE == null) INSTANCE = new FontLoader();
        return INSTANCE;
    }

    public File[] load() {
        return new File(OxySystem.FileSystem.getResourceByPath("/fonts/")).listFiles();
    }
}
