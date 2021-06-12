package OxyEngine.Core.Context.Renderer.Light;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Scene.OxyEntity;

public abstract class Light implements EntityComponent {

    public static final int LIGHT_SIZE = 4;

    protected float colorIntensity;

    public Light(float colorIntensity) {
        this.colorIntensity = colorIntensity;
    }

    public Light(){
        this(1);
    }

    public abstract void update(OxyEntity e, int i);

    public float getColorIntensity() {
        return colorIntensity;
    }
}
