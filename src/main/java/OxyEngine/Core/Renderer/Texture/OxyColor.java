package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import org.joml.Vector4f;

import java.awt.*;

import static OxyEngine.System.Globals.Globals.normalizeColor;

public class OxyColor implements Cloneable {

    private final OxyShader shader;
    private float[] numbers;

    public OxyColor(String code, OxyShader shader) {
        //constructor overloading does not work here!
        Color color = Color.decode(code);
        float r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = color.getAlpha();
        float[] numbers = {r, g, b ,a};
        if (r > 1 || b > 1 || g > 1 || a > 1) {
            numbers = normalizeColor(r, g, b, a);
        }
        this.numbers = numbers;
        this.shader = shader;
    }

    public OxyColor(float r, float g, float b, float a, OxyShader shader) {
        float[] numbers = {r, g, b ,a};
        if (r > 1 || b > 1 || g > 1 || a > 1) {
            numbers = normalizeColor(r, g, b, a);
        }
        this.numbers = numbers;
        this.shader = shader;
    }

    public OxyColor(float[] numbers, OxyShader shader) {
        this.numbers = numbers;
        this.shader = shader;
    }

    public OxyColor(int r, int g, int b, int a, OxyShader shader) {
        this((float)r, (float)g, (float)b, (float)a, shader);
    }

    public void setColorRGBA(float[] numbers) {
        this.numbers = numbers;
    }

    public void init(){
        shader.enable();
        shader.setUniformVec4("colorInput", new Vector4f(numbers[0], numbers[1], numbers[2], numbers[3]));
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public OxyShader getShader() {
        return shader;
    }

    public float[] getNumbers() {
        return numbers;
    }
}
