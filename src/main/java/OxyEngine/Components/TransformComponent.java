package OxyEngine.Components;

import OxyEngineEditor.Scene.OxyEntity;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransformComponent implements EntityComponent {

    public Vector3f position;
    public Vector3f rotation;
    public Vector3f scale;

    public Matrix4f transform;

    public TransformComponent(TransformComponent t) {
        this.scale = new Vector3f(t.scale);
        this.position = new Vector3f(t.position);
        this.rotation = new Vector3f(t.rotation);
    }

    public void set(TransformComponent t) {
        this.scale = new Vector3f(t.scale);
        this.position = new Vector3f(t.position);
        this.rotation = new Vector3f(t.rotation);
    }

    public TransformComponent(Vector3f position, Vector3f rotation, Vector3f scale) {
        this.scale = scale;
        this.position = position;
        this.rotation = rotation;
    }

    public TransformComponent(Vector3f position, Vector3f rotation, float scale) {
        this(position, rotation, new Vector3f(scale, scale, scale));
    }

    @Deprecated
    public TransformComponent(Matrix4f t){
        this.transform = t;
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        this.scale = new Vector3f();
        AxisAngle4f rot = new AxisAngle4f();
        t.getTranslation(position);
        t.getRotation(rot);
        t.getScale(scale);
        rot.transform(rotation);
    }

    public void validate(OxyEntity entity) {
        if (entity != null && entity.has(BoundingBoxComponent.class) && entity.has(TransformComponent.class)) { // safety
            entity.get(TransformComponent.class).position.add(entity.originPos).mul(new Vector3f(entity.get(TransformComponent.class).scale));
        }
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