package OxyEngine.Core.Renderer.Texture;

import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;

import java.awt.*;

import static OxyEngine.System.Globals.Globals.normalizeColor;

public class OxyColor implements Cloneable, EntityComponent {

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

    public OxyColor(float[] numbers) {
        this.numbers = numbers;
    }

    public OxyColor(int r, int g, int b, int a) {
        this((float) r, (float) g, (float) b, (float) a);
    }

    public void setColorRGBA(float[] numbers) {
        this.numbers = numbers;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public float[] getNumbers() {
        return numbers;
    }
}
