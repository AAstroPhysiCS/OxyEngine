package OxyEngine.Core.Renderer.Light;

import OxyEngine.Components.EntityComponent;
import OxyEngineEditor.Scene.OxyEntity;
import org.joml.Vector3f;

public abstract class Light implements EntityComponent {

    public static final int LIGHT_SIZE = 4;

    protected final Vector3f ambient;
    protected final Vector3f specular;

    protected float colorIntensity = 1;

    public Light(Vector3f ambient, Vector3f specular) {
        this.ambient = ambient;
        this.specular = specular;
    }

    public abstract void update(OxyEntity e, int i);
}
