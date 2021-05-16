package OxyEngine.Core.Renderer.Texture;

import org.joml.Vector4f;

import java.awt.*;

import static OxyEngine.Utils.normalizeColor;

public class OxyColor implements Cloneable {

    private float[] numbers;

    public OxyColor(String code) {
        //constructor overloading does not work here!
        Color color = Color.decode(code);
        float r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();
        float[] numbers = {r, g, b, a};
        if (r > 1 || b > 1 || g > 1 || a > 1) {
            numbers = normalizeColor(r, g, b, a);
        }
        this.numbers = numbers;
    }

    public OxyColor(float r, float g, float b, float a) {
        float[] numbers = {r, g, b, a};
        if (r > 1 || b > 1 || g > 1 || a > 1) {
            numbers = normalizeColor(r, g, b, a);
        }
        this.numbers = numbers;
    }

    public OxyColor(Vector4f rgba) {
        this(rgba.x, rgba.y, rgba.z, rgba.w);
    }

    public OxyColor(float[] numbers) {
        this(numbers[0], numbers[1], numbers[2], numbers[3]);
    }

    public OxyColor(int r, int g, int b, int a) {
        this((float) r, (float) g, (float) b, (float) a);
    }

    public void setColorRGBA(float[] numbers) {
        this.numbers = numbers;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public float[] getNumbers() {
        return numbers;
    }
}
