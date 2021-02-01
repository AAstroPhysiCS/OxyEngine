package OxyEngineEditor.UI;

import OxyEngine.System.OxySystem;
import imgui.flag.ImGuiCol;

import java.lang.reflect.Field;

public class UIThemeLoader {

    private UIThemeLoader() {}

    private static UIThemeLoader INSTANCE = null;

    public static UIThemeLoader getInstance() {
        if (INSTANCE == null) INSTANCE = new UIThemeLoader();
        return INSTANCE;
    }

    public float[][] load() {
        return loadStyle(OxySystem.FileSystem.load(OxySystem.FileSystem.getResourceByPath("/theme/oxyTheme.txt")).split("\n"));
    }

    private float[][] loadStyle(String[] splittedContent){
        float[][] allThemeColors = new float[ImGuiCol.COUNT][4];
        for(int i = 0; i < splittedContent.length; i++){
            String s = splittedContent[i];
//            if(s.equals("ImVec4* colors = ImGui::GetStyle().Colors;")) continue;
//            int id = loadId(s, allFields);
            float[] value = loadColorValue(s);
            allThemeColors[i] = value;
        }
        return allThemeColors;
    }

    //I dont need it. But if i want to have the color values seperate, i'll have to use this
    //so i code it beforehand
    private int loadId(String content, Field[] allFields){
        String sequence = ((String) content.subSequence(content.indexOf("[") + 1, content.indexOf("]"))).replace("_", ".");
        for(Field f : allFields){
            String name = f.getName();
            if(name.equals(sequence.split("\\.")[1])){
                try {
                    return f.getInt(0);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    private float[] loadColorValue(String content){
        String[] sequence = ((String) content.subSequence(content.indexOf("(") + 1, content.indexOf(")"))).split(",");
        float[] value = new float[sequence.length];
        for(int i = 0; i < value.length; i++){
            value[i] = Float.parseFloat(sequence[i]);
        }
        return value;
    }
}
