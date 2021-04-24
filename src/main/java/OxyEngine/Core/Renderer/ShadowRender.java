package OxyEngine.Core.Renderer;

import OxyEngine.Components.AnimationComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Camera.OrthographicCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Buffer.Platform.FrameBufferSpecification;
import OxyEngine.Core.Renderer.Buffer.Platform.FrameBufferTextureFormat;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Renderer.Pipeline.OxyShader;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.SceneRenderer;
import OxyEngine.Scene.SceneRuntime;
import OxyEngineEditor.UI.Panels.Panel;
import imgui.ImGui;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

import static OxyEngine.Core.Renderer.Context.OxyRenderCommand.rendererAPI;
import static OxyEngine.Scene.SceneRuntime.currentBoundedCamera;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;

public class ShadowRender {

    public static final int NUMBER_CASCADES = 4;
    private static final int FRUSTUM_CORNERS = 8;

    private static OpenGLFrameBuffer shadowFrameBuffer = null;

    private static final ShadowMapCamera[] cascadedCamArr = new ShadowMapCamera[NUMBER_CASCADES];
    private static final float[] cascadeSplit = new float[NUMBER_CASCADES];
    private static float nearPlaneOffset = -150f;

    static {

        for (int i = 0; i < cascadedCamArr.length; i++) {
            ShadowMapCamera cam = new ShadowMapCamera(0, 0, 0, 0, 0, 0, true);
            cascadedCamArr[i] = cam;
        }

        cascadeSplit[0] = 150f;
        cascadeSplit[1] = 300f;
        cascadeSplit[2] = 600f;
        cascadeSplit[3] = 10000f;
    }

    public static boolean cascadeIndicatorToggle;

    private static final OxyShader shadowMapDepthShader = OxyShader.createShader("OxyDepthMap", "shaders/OxyDepthMap.glsl");
    private static final Vector3f[] frustumCorners = new Vector3f[FRUSTUM_CORNERS];
    private static OxyPipeline shadowMapPipeline = null;

    static {
        for (int i = 0; i < frustumCorners.length; i++) {
            frustumCorners[i] = new Vector3f();
        }
    }

    private ShadowRender() {

    }

    public static void initPipeline() {
        shadowFrameBuffer = FrameBuffer.create(1024, 1024,
                OpenGLFrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setAttachmentIndex(0)
                        .setTextureCount(NUMBER_CASCADES)
                        .setSizeForTextures(0, 4096, 4096)
                        .setSizeForTextures(1, 2048, 2048)
                        .setSizeForTextures(2, 1024, 1024)
                        .setSizeForTextures(3, 512, 512)
                        .setFormats(FrameBufferTextureFormat.DEPTHCOMPONENT32)
                        .setFilter(GL_NEAREST, GL_NEAREST)
                        .wrapSTR(GL_REPEAT, GL_REPEAT, -1)
                        .disableReadWriteBuffer(true)
        );

        shadowMapPipeline = OxyPipeline.createNewPipeline(OxyPipeline.createNewSpecification()
                .setRenderPass(OxyRenderPass.createBuilder(shadowFrameBuffer)
                        .setCullFace(CullMode.BACK)
                        .create())
                .setDebugName("Shadow Map Rendering Pipeline")
                .setShader(shadowMapDepthShader));
    }

    public static void shadowPass(DirectionalLight d) {

        OxyRenderPass shadowRenderPass = shadowMapPipeline.getRenderPass();
        shadowRenderPass.beginRenderPass();
        for (int i = 0; i < cascadeSplit.length; i++) {
            cascadedCamArr[i].setDirectionalLight(d);
            cascadedCamArr[i].prepare((PerspectiveCamera) currentBoundedCamera, cascadeSplit[i]);
            shadowFrameBuffer.bindDepthAttachment(0, i);
            rendererAPI.clearBuffer();
        }

        for (OxyEntity e : SceneRenderer.getInstance().allModelEntities) {

            ShadowMapCamera cam = null;
            int camIndex = 0;

            for (int i = 0; i < NUMBER_CASCADES; i++) {
                if (currentBoundedCamera.origin.distance(e.get(TransformComponent.class).position) - 20f < cascadeSplit[i]) {
                    cam = cascadedCamArr[i];
                    camIndex = i;
                    cam.finalizeCamera(SceneRuntime.TS);

                    /*
                     * so in order to properly render shadows, we have to render the next split too,
                     * because then it would be a horrible thing for a scene like sponza (model that is very big in size)
                     */
                    if (i != NUMBER_CASCADES - 1) {
                        cascadedCamArr[i + 1].finalizeCamera(SceneRuntime.TS);
                    }
                    if (i != 0) {
                        cascadedCamArr[i - 1].finalizeCamera(SceneRuntime.TS);
                    }
                    break;
                }
            }
            shadowFrameBuffer.bindDepthAttachment(0, camIndex);

            if (cam == null)
                return;

            OpenGLMesh mesh = e.get(OpenGLMesh.class);

            shadowMapDepthShader.begin();
            shadowMapDepthShader.setUniformMatrix4fv("model", e.get(TransformComponent.class).transform, false);
            shadowMapDepthShader.setUniformMatrix4fv("lightSpaceMatrix", cam.getViewMatrix(), false);
            shadowMapDepthShader.setUniform1i("animatedModel", 0);
            if (e.has(AnimationComponent.class)) {
                shadowMapDepthShader.setUniform1i("animatedModel", 1);
                AnimationComponent animComp = e.get(AnimationComponent.class);
                List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                for (int j = 0; j < matrix4fList.size(); j++) {
                    shadowMapDepthShader.setUniformMatrix4fv("finalBonesMatrices[" + j + "]", matrix4fList.get(j), false);
                }
            }
            shadowMapDepthShader.end();

            OxyRenderer.renderMesh(shadowMapPipeline, mesh);

            if (camIndex != NUMBER_CASCADES - 1) {
                shadowFrameBuffer.bindDepthAttachment(0, camIndex + 1);
                shadowMapDepthShader.begin();
                shadowMapDepthShader.setUniformMatrix4fv("lightSpaceMatrix", cascadedCamArr[camIndex + 1].getViewMatrix(), false);
                shadowMapDepthShader.end();
                OxyRenderer.renderMesh(shadowMapPipeline, mesh);
            }
            if (camIndex != 0) {
                shadowFrameBuffer.bindDepthAttachment(0, camIndex - 1);
                shadowMapDepthShader.begin();
                shadowMapDepthShader.setUniformMatrix4fv("lightSpaceMatrix", cascadedCamArr[camIndex - 1].getViewMatrix(), false);
                shadowMapDepthShader.end();
                OxyRenderer.renderMesh(shadowMapPipeline, mesh);
            }
        }
        shadowRenderPass.endRenderPass();
    }

    public static final class DebugPanel extends Panel {

        private static DebugPanel INSTANCE = null;

        static final float[] cascadeSplit = new float[]{
                ShadowRender.cascadeSplit[0], ShadowRender.cascadeSplit[1], ShadowRender.cascadeSplit[2], ShadowRender.cascadeSplit[3]
        };
        static final float[] nearPlaneOffset = new float[]{
                ShadowRender.nearPlaneOffset
        };
        static final int[] shadowMapIndex = new int[]{0};


        public static DebugPanel getInstance() {
            if (INSTANCE == null) INSTANCE = new DebugPanel();
            return INSTANCE;
        }

        @Override
        public void preload() {

        }

        @Override
        public void renderPanel() {
            ImGui.begin("Shadow Map Debug");

            ImGui.sliderInt("##hideLabel ShadowMapIndex", shadowMapIndex, 0, NUMBER_CASCADES - 1);

            for (int i = 0; i < NUMBER_CASCADES; i++) {
                int textureID = getShadowMap(i);
                if (shadowMapIndex[0] == i) {
                    ImGui.image(textureID, ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY(), 0, 1, 1, 0);
                }
            }

            ImGui.dragFloat("##hideLabel ShadowMapNearPlaneOffset", nearPlaneOffset);
            ImGui.dragFloat4("##hideLabel ShadowMapSplit", cascadeSplit);

            if (ImGui.radioButton("##hideLabel ShadowMapCascadeToggle", cascadeIndicatorToggle)) {
                cascadeIndicatorToggle = !cascadeIndicatorToggle;
            }

            ShadowRender.cascadeSplit[0] = cascadeSplit[0];
            ShadowRender.cascadeSplit[1] = cascadeSplit[1];
            ShadowRender.cascadeSplit[2] = cascadeSplit[2];
            ShadowRender.cascadeSplit[3] = cascadeSplit[3];

            ShadowRender.nearPlaneOffset = nearPlaneOffset[0];

            ImGui.end();
        }
    }

    private static final class ShadowMapCamera extends OrthographicCamera {

        private DirectionalLight directionalLight;
        private float minZ, maxZ;
        private final Vector3f centerOfFrustum = new Vector3f();

        public ShadowMapCamera(float left, float right, float bottom, float top, float zNear, float zFar, boolean transpose) {
            super(left, right, bottom, top, zNear, zFar, transpose);

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

            //FOVY: 45 (If in the future, some weirdness happens with the shadows, then change the editor camera to 45 FOVY and this to p.getFOVY(); )
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

        @Override
        public Matrix4f setProjectionMatrix() {
            projectionMatrix = new Matrix4f();
            projectionMatrix.setOrtho(left, right, bottom, top, zNear, zFar);
            return projectionMatrix;
        }

        @Override
        public Matrix4f setModelMatrix() {
            Vector3f lightDirection = new Vector3f(directionalLight.getDirection()).mul(-1);
            Vector3f lightPosInc = new Vector3f().set(lightDirection);
            float distance = maxZ - minZ;
            lightPosInc.mul(distance);
            Vector3f lightPosition = new Vector3f();
            lightPosition.set(centerOfFrustum);
            lightPosition.add(lightPosInc);

            float lightAngleX = (float) Math.acos(lightDirection.z);
            float lightAngleY = (float) Math.asin(lightDirection.x);

            modelMatrix = new Matrix4f()
                    .rotateX(lightAngleX)
                    .rotateY(lightAngleY)
                    .translate(-lightPosition.x, -lightPosition.y, -lightPosition.z);

            return modelMatrix;
        }

        @Override
        public void finalizeCamera(float ts) {

            projectionMatrix = setProjectionMatrix();
            modelMatrix = setModelMatrix();

            viewMatrix = new Matrix4f(projectionMatrix);
            viewMatrix.mul(modelMatrix);
        }
    }

    public static int getShadowMap(int index) {
        return shadowFrameBuffer.getColorAttachmentTexture(0)[index];
    }

    public static Matrix4f getShadowViewMatrix(int index) {
        return cascadedCamArr[index].getViewMatrix();
    }

    public static boolean ready(int index) {
        return cascadedCamArr[index].directionalLight != null && cascadedCamArr[index].getViewMatrix() != null;
    }

    public static boolean castShadows() {
        if (!ready(0)) return false;
        return cascadedCamArr[0].directionalLight.isCastingShadows();
    }

    public static float getCascadeSplits(int index) {
        return cascadeSplit[index];
    }
}
