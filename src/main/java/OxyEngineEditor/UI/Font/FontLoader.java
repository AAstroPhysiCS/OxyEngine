package OxyEngineEditor.UI.Font;

import OxyEngine.System.OxySystem;
import OxyEngine.Tools.Loader;

import java.io.File;

public class FontLoader implements Loader<File> {

    private FontLoader(){}

    private static FontLoader INSTANCE = null;

    public static FontLoader getInstance(){
        if(INSTANCE == null) INSTANCE = new FontLoader();
        return INSTANCE;
    }

    @Override
    public File[] load() {
        return new File(OxySystem.FileSystem.getResourceByPath("/fonts/")).listFiles();
    }
}
