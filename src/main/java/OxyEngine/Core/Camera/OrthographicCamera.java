package OxyEngine.Core.Camera;

import OxyEngine.System.OxyTimestep;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class OrthographicCamera extends OxyCamera {

    //if i happen to code the engine to 2D too, this class will be helpful.
    //But for now, it is left to die... (although i might use for something else ;) )

    private final int left, right, bottom, top, zNear, zFar;

    private final Vector3f translation;

    public OrthographicCamera(int left, int right, int bottom, int top, int zNear, int zFar, int location, boolean transpose) {
        this(left, right, bottom, top, zNear, zFar, location, transpose, new Vector3f(0, 0, 0));
    }

    public OrthographicCamera(int left, int right, int bottom, int top, int zNear, int zFar, int location, boolean transpose, Vector3f translation) {
        super(location, transpose);
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.zNear = zNear;
        this.zFar = zFar;
        this.translation = translation;
    }

    @Override
    public Matrix4f setProjectionMatrix() {
        Matrix4f m = new Matrix4f();
        m.identity();
        m.ortho(left, right, bottom, top, zNear, zFar);
        return m;
    }

    @Override
    public Matrix4f setModelMatrix() {
        Matrix4f m = new Matrix4f();
        m.identity();
        m.translate(-translation.x, -translation.y, -translation.z);
        return m;
    }

    @Override
    public void finalizeCamera(OxyTimestep ts) {
        projectionMatrix = setProjectionMatrix();
        modelMatrix = setModelMatrix();
        viewMatrix.set(projectionMatrix);
        viewMatrix.mul(modelMatrix);
    }

    public Vector3f getTranslation() {
        return translation;
    }
}
