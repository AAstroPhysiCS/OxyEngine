package OxyEngineEditor.Sandbox.OxyObjects;

import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import org.joml.Vector3f;

public class ModelSpec {

    private final String objName;
    private final Vector3f position, rotation;
    private final float scale;
    private OxyTexture texture;
    private OxyColor color;

    private ModelSpec(String objName, Vector3f position, Vector3f rotation, float scale) {
        this.objName = objName;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public ModelSpec(String objName, OxyTexture texture, Vector3f position, Vector3f rotation, float scale) {
        this(objName, position, rotation, scale);
        this.texture = texture;
    }

    public ModelSpec(String objName, OxyTexture texture, Vector3f position, Vector3f rotation) {
        this(objName, position, rotation, 1);
        this.texture = texture;
    }

    public ModelSpec(String objName, OxyColor color, Vector3f position, Vector3f rotation, float scale) {
        this(objName, position, rotation, scale);
        this.color = color;
    }

    public ModelSpec(String objName, OxyColor color, Vector3f position, Vector3f rotation) {
        this(objName, position, rotation, 1);
        this.color = color;
    }

    public ModelSpec(String objName, OxyColor color) {
        this.objName = objName;
        this.color = color;
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = 1;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public OxyTexture getTexture() {
        return texture;
    }

    public String getObjName() {
        return objName;
    }

    public float getScale() {
        return scale;
    }

    public OxyColor getColor() {
        return color;
    }
}
