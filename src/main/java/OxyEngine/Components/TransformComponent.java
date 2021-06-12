package OxyEngine.Components;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TransformComponent implements EntityComponent {

    public Vector3f position;
    public Vector3f rotation;
    public Vector3f scale;

    public Vector3f worldSpacePosition = new Vector3f();

    public Matrix4f transform;

    public TransformComponent(TransformComponent t) {
        this.scale = new Vector3f(t.scale);
        this.position = new Vector3f(t.position);
        this.rotation = new Vector3f(t.rotation);
        this.transform = new Matrix4f(t.transform);
        this.worldSpacePosition = new Vector3f(t.position);
    }

    public void set(TransformComponent t) {
        this.scale = new Vector3f(t.scale);
        this.position = new Vector3f(t.position);
        this.rotation = new Vector3f(t.rotation);
        this.transform = new Matrix4f();
    }

    public TransformComponent(Vector3f position, Vector3f rotation, Vector3f scale) {
        this.scale = scale;
        this.position = position;
        this.rotation = rotation;
        this.transform = new Matrix4f();
        this.worldSpacePosition = new Vector3f(position);
    }

    public TransformComponent(Vector3f position, Vector3f rotation, Vector3f scale, Matrix4f transform) {
        this.scale = scale;
        this.position = position;
        this.rotation = rotation;
        this.transform = transform;
        this.worldSpacePosition = new Vector3f(position);
    }

    public TransformComponent(Vector3f position, Vector3f rotation, float scale) {
        this(position, rotation, new Vector3f(scale, scale, scale));
    }

    public TransformComponent(Vector3f position, Quaternionf rotation, Vector3f scale) {
        this.position = new Vector3f(position);
        this.rotation = new Vector3f();
        this.scale = new Vector3f(scale);
        rotation.getEulerAnglesXYZ(this.rotation);
        this.transform = new Matrix4f();
    }

    public void set(Vector3f position, Quaternionf rotation, Vector3f scale){
        this.position.set(position);
        this.scale.set(scale);
        rotation.getEulerAnglesXYZ(this.rotation);
        this.transform = new Matrix4f();
    }

    public TransformComponent(Matrix4f t){
        this.transform = t;
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        this.scale = new Vector3f();
        Quaternionf rot = new Quaternionf();
        transform.getTranslation(position);
        transform.getUnnormalizedRotation(rot);
        transform.getScale(scale);
        //Could function or could not function idk.
        if(scale.x < 0.01f && scale.y < 0.01f && scale.z < 0.01f) scale.set(1);
        else {
            scale.mul(0.01f);
            position.mul(0.01f);
        }
        rot.getEulerAnglesXYZ(this.rotation);
//        this.rotation.mul((float) (180 / Math.PI));
    }

    public TransformComponent(Vector3f position, Vector3f rotation) {
        this(position, rotation, new Vector3f(1, 1, 1));
    }

    public TransformComponent(Vector3f position, float scaleX, float scaleY, float scaleZ) {
        this(position, new Vector3f(0, 0, 0), new Vector3f(scaleX, scaleY, scaleZ));
    }

    public TransformComponent(Vector3f position, float scale) {
        this(position, new Vector3f(0, 0, 0), new Vector3f(scale, scale, scale));
    }

    public TransformComponent(Vector3f position) {
        this(position, new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
    }

    public TransformComponent(float scaleX, float scaleY, float scaleZ) {
        this(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(scaleX, scaleY, scaleZ));
    }

    public TransformComponent(float scale) {
        this(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(scale, scale, scale));
    }

    public TransformComponent() {
        this(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
    }

    @Override
    public String toString() {
        return """
                X: %s, Y: %s, Z: %s
                Rotation X: %s, Rotation Y: %s, Rotation Z: %s
                Scale X: %s, Scale Y: %s, Scale Z: %s
                """.formatted(position.x, position.y, position.z, rotation.x, rotation.y, rotation.z, scale.x, scale.y, scale.z);
    }
}