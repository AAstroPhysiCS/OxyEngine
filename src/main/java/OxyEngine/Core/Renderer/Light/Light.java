package OxyEngine.Core.Renderer.Light;

import OxyEngine.Components.EntityComponent;

public abstract class Light implements EntityComponent {

    public static final int LIGHT_SIZE = 4;

    protected float colorIntensity;

    public Light(float colorIntensity) {
        this.colorIntensity = colorIntensity;
    }

    public Light(){
        this(1);
    }

    public float getColorIntensity() {
        return colorIntensity;
    }
}
