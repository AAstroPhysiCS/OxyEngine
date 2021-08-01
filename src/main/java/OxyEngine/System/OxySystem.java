package OxyEngine.System;

import OxyEngine.OxyApplication;
import OxyEngineEditor.EditorApplication;
import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGuiIO;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.Assimp;
import org.reflections.Reflections;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    List<String> extensionList = new ArrayList<>();

    static void init() {
        for (Handler handlers : logger.getHandlers())
            logger.removeHandler(handlers);
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new OxyLogger());
        logger.addHandler(handler);

        AIString extensionString = new AIString(ByteBuffer.allocateDirect(1032));
        Assimp.aiGetExtensionList(extensionString);
        String[] extensionArray = extensionString.dataString().split(";");
        for (String s : extensionArray) {
            s = s.replace(".", "").replace("*", "");
            extensionList.add(s);
        }
    }

    static OxyApplication createOxyApplication() {
        //returning just the editor application for now.
        return new EditorApplication();
    }

    static String oxyAssert(String msg) {
        logger.severe(msg);
        return msg;
    }

    static <T> Set<Class<? extends T>> getSubClasses(Class<T> type) {
        return reflections.getSubTypesOf(type);
    }

    static boolean isValidPath(String path) {
        return new File(path).exists();
    }

    interface Font {

        List<ImFont> allFonts = new ArrayList<>();

        static void load(ImGuiIO io, String path, final int size, String name) {
            ImFontAtlas atlas = io.getFonts();
            ImFontConfig config = new ImFontConfig();

            config.setGlyphRanges(atlas.getGlyphRangesCyrillic());
            atlas.addFontDefault();

            config.setMergeMode(false);
            config.setPixelSnapH(false);

            ImFont font = atlas.addFontFromFileTTF(path, size, config);

            config.setName(name + ", " + size);
            config.destroy();

            allFonts.add(font);
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

    static String removeFileExtension(String filename) {
        //regex from internet
        return filename.replaceFirst("[.][^.]+$", "");
    }

    static String getExtension(String filePath) {
        String[] splitted = filePath.split("\\.");
        return splitted[splitted.length - 1];
    }

    static boolean isSupportedModelFileExtension(String extensionToSupport) {
        for (String extensions : extensionList) {
            if (extensions.equalsIgnoreCase(extensionToSupport)) return true;
        }
        return false;
    }

    static boolean isSupportedTextureFile(String extensionToSupport) {
        return extensionToSupport.equalsIgnoreCase("jpg") ||
                extensionToSupport.equalsIgnoreCase("png") ||
                extensionToSupport.equalsIgnoreCase("jpeg") ||
                extensionToSupport.equalsIgnoreCase("tga");
    }
}
