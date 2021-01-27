package OxyEngine;

import java.util.List;

public interface Globals {

    static float normalizeColor(float number) {
        return number / 255;
    }

    static float[] normalizeColor(float r, float g, float b, float a) {
        return new float[]{normalizeColor(r), normalizeColor(g), normalizeColor(b), normalizeColor(b)};
    }

    static float[] toPrimitiveFloat(List<Float> list){
        float[] buffer = new float[list.size()];
        for(int i = 0; i < buffer.length; i++) buffer[i] = list.get(i);
        return buffer;
    }

    static int[] toPrimitiveInteger(List<Integer> list){
        int[] buffer = new int[list.size()];
        for(int i = 0; i < buffer.length; i++) buffer[i] = list.get(i);
        return buffer;
    }
}
