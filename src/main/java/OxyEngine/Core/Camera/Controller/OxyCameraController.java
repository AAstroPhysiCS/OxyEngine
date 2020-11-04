package OxyEngine.Core.Camera.Controller;

import org.joml.Vector3f;

public abstract class OxyCameraController {

    protected final Vector3f rotationRef, positionRef;
    public final Vector3f origin = new Vector3f();

    protected double oldMouseX, oldMouseY;
    protected final float mouseSpeed, horizontalSpeed, verticalSpeed;

    protected static float ts;

    public OxyCameraController(Vector3f positionRef, Vector3f rotationRef, float mouseSpeed, float horizontalSpeed, float verticalSpeed) {
        this.rotationRef = rotationRef;
        this.mouseSpeed = mouseSpeed;
        this.horizontalSpeed = horizontalSpeed;
        this.verticalSpeed = verticalSpeed;
        this.positionRef = positionRef;
    }

    public enum Mode {
        SWIPE(), FREE()
    }

    public Vector3f getRotation() {
        return rotationRef;
    }

    public Vector3f getPosition() {
        return positionRef;
    }

    public float getMouseSpeed() {
        return mouseSpeed;
    }

    public static float getTs() {
        return ts;
    }

    public abstract void update(float ts, Mode mode);
}
