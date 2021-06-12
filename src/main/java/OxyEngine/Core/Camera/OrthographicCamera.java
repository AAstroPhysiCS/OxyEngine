package OxyEngine.Core.Camera;

import org.joml.Vector3f;

public abstract class OrthographicCamera extends OxyCamera {

    public float left, right, bottom, top, zNear, zFar;

    public OrthographicCamera(float left, float right, float bottom, float top, float zNear, float zFar, boolean transpose) {
        this(left, right, bottom, top, zNear, zFar, transpose, new Vector3f(0, 0, 0));
    }

    public OrthographicCamera(float left, float right, float bottom, float top, float zNear, float zFar, boolean transpose, Vector3f translation) {
        super(0.05f, 7f, 7f, transpose);
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.zNear = zNear;
        this.zFar = zFar;
        this.positionRef = translation;
    }
}
