package OxyEngine.System;

import OxyEngineEditor.EntryPoint;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public interface OxySystem {

    /**
     * @author Okan Güclü (Github: https://github.com/AAstroPhysiCS)
     * @since 11.04.2020
     **/


    Logger logger = Logger.getLogger(OxySystem.class.getName());

    String BASE_PATH = System.getProperty("user.dir");
    String gl_Version = "#version 460";

    static void init() {
        for (Handler handlers : logger.getHandlers())
            logger.removeHandler(handlers);
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new OxyLogger());
        logger.addHandler(handler);
    }

    static String oxyAssert(String msg){
        logger.severe(msg);
        return msg;
    }

    interface FileSystem {
        static String load(String path) {
            final StringBuilder builder = new StringBuilder();
            try {
                File file = new File(path);
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return builder.toString();
        }

        static String getResourceByPath(String path){
            String fullPath = EntryPoint.class.getResource(path).getPath();
            return (String) fullPath.subSequence(1, fullPath.length());
        }

        static String openDialog(String filterList, String defaultPath){
            String path = null;
            PointerBuffer buffer = PointerBuffer.allocateDirect(16);
            int result = NativeFileDialog.NFD_OpenDialog(filterList, defaultPath, buffer);
            if (result == NativeFileDialog.NFD_OKAY) {
                path = buffer.getStringASCII();
            }
            NativeFileDialog.nNFD_Free(buffer.get());
            return path;
        }
    }
}
