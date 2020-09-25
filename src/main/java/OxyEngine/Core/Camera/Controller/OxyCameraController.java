package OxyEngine.Core.Camera.Controller;

import org.joml.Vector3f;

public abstract class OxyCameraController {

    protected final Vector3f rotationRef, positionRef;
    public final Vector3f origin = new Vector3f();

    protected double oldMouseX, oldMouseY;
    protected final float mouseSpeed, horizontalSpeed, verticalSpeed;

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

    /*public OxyCameraController(float mouseSpeed, float horizontalSpeed, float verticalSpeed) {
        this(new Ref<>(new Vector3f(0, 0, 0)), new Ref<>(new Vector3f(0, 0, 0)), mouseSpeed, horizontalSpeed, verticalSpeed);
    }*/

    public Vector3f getRotation() {
        return rotationRef;
    }

    public Vector3f getPosition() {
        return positionRef;
    }

    public float getMouseSpeed() {
        return mouseSpeed;
    }

    public abstract void update(float ts, Mode mode);
}
