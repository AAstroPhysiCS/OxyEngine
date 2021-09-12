package OxyEngine.Core.Context.Renderer.Light;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Core.Context.Scene.Entity;

public abstract class Light implements EntityComponent {

    public static final int LIGHT_SIZE = 4;

    protected float colorIntensity;

    public Light(float colorIntensity) {
        this.colorIntensity = colorIntensity;
    }

    public Light(){
        this(1);
    }

    public abstract void update(Entity e, int i);

    public float getColorIntensity() {
        return colorIntensity;
    }
}
