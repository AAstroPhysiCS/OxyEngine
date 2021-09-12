package OxyEngine.Components;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class TransformComponent implements EntityComponent {

    public final Vector3f position = new Vector3f();
    public final Vector3f rotation = new Vector3f();
    public final Vector3f scale = new Vector3f();

    public final Matrix4f transform = new Matrix4f();

    public TransformComponent(TransformComponent other) {
        this.scale.set(other.scale);
        this.position.set(other.position);
        this.rotation.set(other.rotation);
        this.transform.set(other.transform);
    }

    public TransformComponent(Vector3f position, Vector3f rotation, Vector3f scale) {
        this.scale.set(scale);
        this.position.set(position);
        this.rotation.set(rotation);
    }

    public TransformComponent(Vector3f position, Vector3f rotation, Vector3f scale, Matrix4f transform) {
        this.scale.set(scale);
        this.position.set(position);
        this.rotation.set(rotation);
        this.transform.set(transform);
    }

    public TransformComponent(Vector3f position, Vector3f rotation, float scale) {
        this(position, rotation, new Vector3f(scale, scale, scale));
    }

    public TransformComponent(Vector3f position, Quaternionf rotation, Vector3f scale) {
        this.position.set(position);
        this.scale.set(scale);
        rotation.getEulerAnglesXYZ(this.rotation);
    }

    public void set(TransformComponent t) {
        this.scale.set(t.scale);
        this.position.set(t.position);
        this.rotation.set(t.rotation);
        this.transform.set(t.transform);
    }

    public void set(Vector3f position, Quaternionf rotation, Vector3f scale) {
        this.position.set(position);
        this.scale.set(scale);
        rotation.getEulerAnglesXYZ(this.rotation);
    }

    public void set(Vector3f position, Vector3f rotation, Vector3f scale) {
        this.position.set(position);
        this.scale.set(scale);
        this.rotation.set(rotation);
    }

    public void set(float xPos, float yPos, float zPos, float xRot, float yRot, float zRot, float scaleX, float scaleY, float scaleZ) {
        this.position.set(xPos, yPos, zPos);
        this.scale.set(scaleX, scaleY, scaleZ);
        this.rotation.set(xRot, yRot, zRot);
    }

    public void set(Vector3f position, Quaternionf rotation) {
        this.position.set(position);
        rotation.getEulerAnglesXYZ(this.rotation);
    }

    public void set(Matrix4f t) {
        this.transform.set(t);
        Quaternionf rot = new Quaternionf();
        transform.getTranslation(this.position);
        transform.getUnnormalizedRotation(rot);
        transform.getScale(this.scale);
        rot.getEulerAnglesXYZ(this.rotation);
    }

    public TransformComponent(Matrix4f t) {
        set(t);
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