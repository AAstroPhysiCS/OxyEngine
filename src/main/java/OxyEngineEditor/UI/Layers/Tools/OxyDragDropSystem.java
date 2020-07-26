package OxyEngineEditor.UI.Layers.Tools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OxyDragDropSystem {

    //could be used later... did it beforehand.

    private OxyDragDropSystem(){}

    public static byte[] pushFloat(float x, float y){
        byte[] xPos = ByteBuffer.allocate(Float.BYTES).order(ByteOrder.nativeOrder()).putFloat(x).array();
        byte[] yPos = ByteBuffer.allocate(Float.BYTES).order(ByteOrder.nativeOrder()).putFloat(y).array();
        byte[] allPos = new byte[xPos.length + yPos.length + 1];

        System.arraycopy(xPos, 0, allPos, 0, xPos.length);
        System.arraycopy(yPos, 0, allPos, xPos.length + 1, yPos.length);
        allPos[xPos.length] = 2;
        return allPos;
    }

    public static float[] getFloat(byte[] data){
        byte[] xPosBytes = new byte[Float.BYTES + 1];
        int index = 0;
        while(data[index] != 2){
            index++;
            xPosBytes[index] = data[index];
        }

        byte[] yPosBytes = new byte[Float.BYTES];
        System.arraycopy(data, index + 1, yPosBytes, 0, yPosBytes.length);

        float xPos = ByteBuffer.wrap(xPosBytes).order(ByteOrder.nativeOrder()).getFloat();
        float yPos = ByteBuffer.wrap(yPosBytes).order(ByteOrder.nativeOrder()).getFloat();
        return new float[]{xPos, yPos};
    }
}
