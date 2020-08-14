package OxyEngine.Core.Camera.Controller;

import OxyEngine.Tools.Ref;
import org.joml.Vector3f;

public abstract class OxyCameraController {

    protected final Ref<Vector3f> rotationRef, positionRef;
    public final Vector3f origin = new Vector3f();

    protected double oldMouseX, oldMouseY;
    protected final float mouseSpeed, horizontalSpeed, verticalSpeed;

    public OxyCameraController(Ref<Vector3f> positionRef, Ref<Vector3f> rotationRef, float mouseSpeed, float horizontalSpeed, float verticalSpeed) {
        this.rotationRef = rotationRef;
        this.mouseSpeed = mouseSpeed;
        this.horizontalSpeed = horizontalSpeed;
        this.verticalSpeed = verticalSpeed;
        this.positionRef = positionRef;
    }

    public enum Mode {
        SWIPE(), FREE()
    }

    public OxyCameraController(float mouseSpeed, float horizontalSpeed, float verticalSpeed) {
        this(new Ref<>(new Vector3f(0, 0, 0)), new Ref<>(new Vector3f(0, 0, 0)), mouseSpeed, horizontalSpeed, verticalSpeed);
    }

    public void setRotation(Vector3f pos){
        rotationRef.obj = pos;
    }

    public void setPosition(Vector3f pos){
        rotationRef.obj = pos;
    }

    public Vector3f getRotation() {
        return rotationRef.obj;
    }

    public Vector3f getPosition() {
        return positionRef.obj;
    }

    public float getMouseSpeed() {
        return mouseSpeed;
    }

    public abstract void update(float ts, Mode mode);
}
