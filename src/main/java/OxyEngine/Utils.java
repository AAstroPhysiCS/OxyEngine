package OxyEngine;

import java.util.List;

public interface Utils {

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

    static float[] copy(float[] src, float[] dest) {
        float[] newObjVert = new float[src.length + dest.length];
        System.arraycopy(src, 0, newObjVert, 0, src.length);
        System.arraycopy(dest, 0, newObjVert, src.length, dest.length);
        return newObjVert;
    }
}
