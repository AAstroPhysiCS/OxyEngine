package OxyEngine.System;

import OxyEngineEditor.EntryPoint;
import OxyEngine.Scene.SceneRuntime;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NativeFileDialog;
import org.reflections.Reflections;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
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

    Reflections reflections = new Reflections("OxyEngine");

    static void init() {
        for (Handler handlers : logger.getHandlers())
            logger.removeHandler(handlers);
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new OxyLogger());
        logger.addHandler(handler);
    }

    static String oxyAssert(String msg) {
        logger.severe(msg);
        return msg;
    }

    static <T> Set<Class<? extends T>> getSubClasses(Class<T> type) {
        return reflections.getSubTypesOf(type);
    }

    static File[] getCurrentProjectFiles(boolean hideHiddenFiles){
        File[] f = new File(BASE_PATH).listFiles();
        if(f == null) return null;
        if(hideHiddenFiles) return Arrays.stream(f).filter(file -> !file.isHidden()).toArray(File[]::new);
        return f;
    }

    static boolean isValidPath(String path) {
        return new File(path).exists();
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

        static String getResourceByPath(String path) {
            String fullPath = EntryPoint.class.getResource(path).getPath();
            return (String) fullPath.subSequence(1, fullPath.length());
        }

        static String openDialog(String filterList, String defaultPath) {
            SceneRuntime.stop();
            String path = null;
            PointerBuffer buffer = PointerBuffer.allocateDirect(16);
            int result = NativeFileDialog.NFD_OpenDialog(filterList, defaultPath, buffer);
            if (result == NativeFileDialog.NFD_OKAY) {
                path = buffer.getStringASCII();
            }
            NativeFileDialog.nNFD_Free(buffer.get());
            return path;
        }

        static String saveDialog(String filterList, String defaultPath){
            SceneRuntime.stop();
            String path = null;
            PointerBuffer buffer = PointerBuffer.allocateDirect(16);
            int result = NativeFileDialog.NFD_SaveDialog(filterList, defaultPath, buffer);
            if (result == NativeFileDialog.NFD_OKAY) {
                path = buffer.getStringASCII();
            }
            NativeFileDialog.nNFD_Free(buffer.get());
            return path;
        }
    }

    static Vector3f parseStringToVector3f(String sValue) {
        String[] splittedVector = sValue.replace("(", "").replace(")", "").split(" ");
        String[] valuesPos = new String[3];
        int ptr = 0;
        for (String s : splittedVector) {
            if (s.isBlank() || s.isEmpty()) continue;
            valuesPos[ptr++] = s;
        }
        return new Vector3f(Float.parseFloat(valuesPos[0]), Float.parseFloat(valuesPos[1]), Float.parseFloat(valuesPos[2]));
    }

    static Vector4f parseStringToVector4f(String sValue) {
        String[] splittedVector = sValue.replace("(", "").replace(")", "").split(" ");
        String[] valuesPos = new String[4];
        int ptr = 0;
        for (String s : splittedVector) {
            if (s.isBlank() || s.isEmpty()) continue;
            valuesPos[ptr++] = s;
        }
        return new Vector4f(Float.parseFloat(valuesPos[0]), Float.parseFloat(valuesPos[1]), Float.parseFloat(valuesPos[2]), Float.parseFloat(valuesPos[3]));
    }

    static float[] parseStringToFloatArray(String sValue, int len) {
        float[] valuesPos = new float[len];
        if (sValue.equals("null")) {
            Arrays.fill(valuesPos, 0f);
            return valuesPos;
        }
        String[] splittedVector = sValue.replace("[", "").replace("]", "").split(", ");
        for (int i = 0; i < valuesPos.length; i++) {
            valuesPos[i] = Float.parseFloat(splittedVector[i]);
        }
        return valuesPos;
    }
}
