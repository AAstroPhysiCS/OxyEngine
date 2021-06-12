package OxyEngine.Core.Context.Renderer.Texture;

import org.joml.Vector4f;

import java.awt.*;

public class OxyColor {

    public static final OxyColor DEFAULT = new OxyColor(0.1f, 0.1f, 0.1f, 1.0f);

    private float[] numbers;

    public OxyColor(String code) {
        //constructor overloading does not work here!
        Color color = Color.decode(code);
        float r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();
        this.numbers = new float[]{r, g, b, a};
    }

    public OxyColor(float r, float g, float b, float a) {
        this.numbers = new float[]{r, g, b, a};
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

    public float[] getNumbers() {
        return numbers;
    }

    public float getRedChannel() {
        return numbers[0];
    }

    public float getGreenChannel() {
        return numbers[1];
    }

    public float getBlueChannel() {
        return numbers[2];
    }

    public float getAlphaChannel() {
        return numbers[3];
    }
}
