package OxyEngine.System;

import imgui.*;

import java.util.ArrayList;
import java.util.List;

public class OxyFontSystem {

    private static final List<ImFont> allFonts = new ArrayList<>();

    public static void load(ImGuiIO io, String path, final int size, String name) {
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

    public static List<ImFont> getAllFonts() {
        return allFonts;
    }
}
