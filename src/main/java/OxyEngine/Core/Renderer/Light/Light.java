package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Sandbox.Components.EntityComponent;
import org.joml.Vector3f;

public abstract class Light implements EntityComponent {

    protected Vector3f position, direction;
    protected Vector3f ambient, diffuse, specular;

    public Light() {

    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }
    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }
    public void setAmbient(Vector3f ambient) {
        this.ambient = ambient;
    }
    public void setDiffuse(Vector3f diffuse) {
        this.diffuse = diffuse;
    }
    public void setSpecular(Vector3f specular) {
        this.specular = specular;
    }

    public abstract void update(OxyShader shader);
}
