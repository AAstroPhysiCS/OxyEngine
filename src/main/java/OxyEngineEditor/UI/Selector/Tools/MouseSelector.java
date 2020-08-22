package OxyEngineEditor.UI.Selector.Tools;

import OxyEngine.Core.Camera.OxyCamera;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MouseSelector implements ObjectSelector {

    private static MouseSelector INSTANCE = null;
    private static final Vector4f clipPos = new Vector4f();
    private static final Matrix4f invProjectionMatrix = new Matrix4f();
    private static final Matrix4f invModelMatrix = new Matrix4f();

    public static MouseSelector getInstance() {
        if (INSTANCE == null) INSTANCE = new MouseSelector();
        return INSTANCE;
    }

    @Override
    public Vector3f toClipSpace(float width, float height, Vector2f mousePos) {
        float x = (2 * mousePos.x) / width - 1.0f;
        float y = 1.0f - (2 * mousePos.y) / height;
        float z = -1.0f;
        return new Vector3f(x, y, z);
    }

    @Override
    public Vector3f getObjectPosRelativeToCamera(float width, float height, Vector2f mousePos, OxyCamera camera) {
        if(camera.getProjectionMatrix() == null || camera.getModelMatrix() == null) return new Vector3f(0,0,0);

        Vector3f clipSpace = toClipSpace(width, height, mousePos);
        float x = clipSpace.x;
        float y = clipSpace.y;
        float z = clipSpace.z;

        invProjectionMatrix.set(camera.getProjectionMatrix());
        invProjectionMatrix.invert();

        clipPos.set(x, y, z, 1.0f);
        clipPos.mul(invProjectionMatrix);
        clipPos.z = -1.0f;
        clipPos.w = 0.0f;

        invModelMatrix.set(camera.getModelMatrix());
        invModelMatrix.invert();
        clipPos.mul(invModelMatrix);
        clipPos.normalize();

        return new Vector3f(clipPos.x, clipPos.y, clipPos.z);
    }
}
