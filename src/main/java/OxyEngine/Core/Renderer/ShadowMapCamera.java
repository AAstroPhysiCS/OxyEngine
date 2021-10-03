package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OrthographicCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Window.Event;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static OxyEngine.Core.Renderer.Renderer.FRUSTUM_CORNERS;

public final class ShadowMapCamera extends OrthographicCamera {

    static float nearPlaneOffset = -15f;

    private static final Vector3f[] frustumCorners = new Vector3f[FRUSTUM_CORNERS];

    static {
        for (int i = 0; i < frustumCorners.length; i++) {
            frustumCorners[i] = new Vector3f();
        }
    }

    private DirectionalLight directionalLight;
    private float minZ, maxZ;
    private final Vector3f centerOfFrustum = new Vector3f();

    public ShadowMapCamera(float left, float right, float bottom, float top, float zNear, float zFar, boolean transpose) {
        super(left, right, bottom, top, zNear, zFar, transpose);
        viewMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
    }

    public ShadowMapCamera(){
        this(0, 0, 0, 0, 0, 0, false);
    }

    void setDirectionalLight(DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;
    }

    void setMinZMaxZ(float minZ, float maxZ) {
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    void prepare(PerspectiveCamera p, float cascadeSplit) {

        ShadowMapCamera camera = this;
        DirectionalLight d = directionalLight;

        camera.setDirectionalLight(d);

        Matrix4f projViewMatrix = new Matrix4f().setPerspective(p.getFovY(), p.getAspect(), nearPlaneOffset, cascadeSplit);
        projViewMatrix.mul(p.getModelMatrix());

        float maxZ = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE;

        Vector3f centerOfFrustum = new Vector3f();

        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            Vector3f corner = frustumCorners[i];
            corner.set(0);
            projViewMatrix.frustumCorner(i, corner);
            centerOfFrustum.add(corner);
            centerOfFrustum.div(FRUSTUM_CORNERS);
            minZ = Math.min(minZ, corner.z);
            maxZ = Math.max(maxZ, corner.z);
        }

        camera.setMinZMaxZ(minZ, maxZ);
        camera.setModelMatrix();

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MIN_VALUE;
        minZ = Float.MAX_VALUE;
        maxZ = -Float.MIN_VALUE;

        Vector4f tmpVec = new Vector4f();

        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            Vector3f corner = frustumCorners[i];
            tmpVec.set(corner, 1);
            tmpVec.mul(camera.getModelMatrix());
            minX = Math.min(tmpVec.x, minX);
            maxX = Math.max(tmpVec.x, maxX);
            minY = Math.min(tmpVec.y, minY);
            maxY = Math.max(tmpVec.y, maxY);
            minZ = Math.min(tmpVec.z, minZ);
            maxZ = Math.max(tmpVec.z, maxZ);
        }
        float distz = maxZ - minZ;

        camera.left = minX;
        camera.right = maxX;
        camera.bottom = minY;
        camera.top = maxY;
        camera.zNear = nearPlaneOffset;
        camera.zFar = distz;
        camera.setProjectionMatrix();
    }

    private void setProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.setOrtho(left, right, bottom, top, zNear, zFar);
    }

    private void setModelMatrix() {
        Vector3f lightDirection = new Vector3f(directionalLight.getDirection());
        Vector3f lightPosInc = new Vector3f().set(lightDirection);
        float distance = maxZ - minZ;
        lightPosInc.mul(distance);
        Vector3f lightPosition = new Vector3f();
        lightPosition.set(centerOfFrustum);
        lightPosition.add(lightPosInc);

        float lightAngleX = (float) Math.acos(lightDirection.z);
        float lightAngleY = (float) Math.asin(lightDirection.x);

        modelMatrix.identity()
                .rotateX(lightAngleX)
                .rotateY(lightAngleY)
                .translate(-lightPosition.x, -lightPosition.y, -lightPosition.z);
    }

    @Override
    public void update() {
        viewMatrix.set(projectionMatrix);
        viewMatrix.mul(modelMatrix);
    }

    //NOT USING THE EVENTS FOR NOW
    @Override
    public void onEvent(Event event) {
    }

    public DirectionalLight getDirectionalLight() {
        return directionalLight;
    }
}
