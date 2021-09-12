package OxyEngine.Core.Camera;

public abstract class OrthographicCamera extends Camera {

    public float left, right, bottom, top, zNear, zFar;

    public OrthographicCamera(float left, float right, float bottom, float top, float zNear, float zFar, boolean transpose) {
        super(0.05f, 7f, 7f, transpose);
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.zNear = zNear;
        this.zFar = zFar;
    }
}
