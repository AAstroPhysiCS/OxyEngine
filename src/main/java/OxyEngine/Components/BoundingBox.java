package OxyEngine.Components;

import org.joml.Vector3f;

public record BoundingBox(Vector3f min, Vector3f max) {
    public BoundingBox(BoundingBox other){
        this(new Vector3f(other.min), new Vector3f(other.max));
    }
}
