package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Mesh.FrameBuffer;
import OxyEngine.Core.Renderer.Mesh.Platform.FrameBufferSpecification;
import OxyEngine.Core.Renderer.Mesh.Platform.TextureFormat;
import OxyEngine.Core.Renderer.Mesh.RenderBuffer;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Renderer.Shader;
import OxyEngine.Core.Renderer.Texture.*;
import OxyEngine.System.Disposable;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;

public final class DynamicSky extends SkyLight implements Disposable {

    CubeTexture dynamicSkyTexture;
    final float[] azimuth = new float[]{0f};
    final float[] inclination = new float[]{0f};
    final float[] turbidity = new float[]{2.0f};
    Vector3f dynamicSkySunDir = new Vector3f(Math.sin(inclination[0]) * Math.cos(azimuth[0]), Math.cos(inclination[0]), Math.sin(inclination[0]) * Math.sin(azimuth[0]));

    private Irradiance irradiance;
    private Prefilter prefilter;
    private static BDRF bdrf;

    public DynamicSky() {
    }

    @Override
    public void bind() {
        if (prefilter != null)
            prefilter.getTexture().bind();
        if (irradiance != null)
            irradiance.getTexture().bind();
        if (bdrf != null)
            bdrf.getTexture().bind();
        if (dynamicSkyTexture != null)
            dynamicSkyTexture.bind();
    }

    public void load() {

        if (dynamicSkyTexture != null) return;

        RenderBuffer captureRBO = RenderBuffer.create(TextureFormat.DEPTHCOMPONENT24, 1024, 1024);
        FrameBuffer captureFBO = FrameBuffer.create(1024, 1024,
                FrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .useRenderBuffer(captureRBO));

        dynamicSkyTexture = Texture.loadCubemap(TextureSlot.HDR, 1024, 1024, TexturePixelType.UByte, TextureFormat.RGB,
                TextureParameterBuilder.create()
                        .setMinFilter(TextureParameter.LINEAR)
                        .setMagFilter(TextureParameter.LINEAR)
                        .setWrapR(TextureParameter.CLAMP_TO_EDGE)
                        .setWrapT(TextureParameter.CLAMP_TO_EDGE)
                        .setWrapS(TextureParameter.CLAMP_TO_EDGE));

        final Matrix4f[] captureViews = new Matrix4f[]{
                new Matrix4f()
                        .lookAt(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
                new Matrix4f()
                        .lookAt(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
                new Matrix4f()
                        .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f),
                new Matrix4f()
                        .lookAt(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f),
                new Matrix4f()
                        .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f),
                new Matrix4f()
                        .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f),
        };
        Matrix4f captureProjection = new Matrix4f().setPerspective(Math.toRadians(90), 1.0f, 0.4768f, 10.0f);

        Shader preethamBuilder = Renderer.getShader("OxyPreetham");
        preethamBuilder.begin();
        preethamBuilder.setUniformVec3("u_Direction", dynamicSkySunDir);
        preethamBuilder.setUniform1f("u_Turbidity", turbidity[0]);
        preethamBuilder.setUniformMatrix4fv("u_Projection", captureProjection);
        preethamBuilder.end();

        glViewport(0, 0, 1024, 1024);
        captureFBO.bind();
        for (int i = 0; i < 6; i++) {
            preethamBuilder.begin();
            preethamBuilder.setUniformMatrix4fv("u_View", captureViews[i]);
            captureFBO.attachColorAttachment(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, dynamicSkyTexture.getTextureId());
            Renderer.clearBuffer();
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            preethamBuilder.end();
        }
        captureFBO.unbind();

        if (prefilter != null) prefilter.dispose();
        if (irradiance != null) irradiance.dispose();

        irradiance = new Irradiance(dynamicSkyTexture, captureFBO, captureRBO, TextureSlot.IRRADIANCE);
        prefilter = new Prefilter(dynamicSkyTexture, captureFBO, captureRBO, TextureSlot.PREFILTER);
        if (bdrf == null) bdrf = new BDRF(captureFBO, captureRBO, TextureSlot.BDRF);

        captureRBO.dispose();
        captureFBO.dispose();
    }

    public void setTurbidity(float turbidity) {
        this.turbidity[0] = turbidity;
    }

    public void setAzimuth(float turbidity) {
        this.azimuth[0] = turbidity;
    }

    public void setInclination(float turbidity) {
        this.inclination[0] = turbidity;
    }

    public void setDynamicSkySunDir(Vector3f dynamicSkySunDir) {
        this.dynamicSkySunDir = dynamicSkySunDir;
    }

    public float[] getTurbidity() {
        return turbidity;
    }

    public float[] getAzimuth() {
        return azimuth;
    }

    public float[] getInclination() {
        return inclination;
    }

    public Vector3f getDynamicSkySunDir() {
        return dynamicSkySunDir;
    }

    @Override
    public void dispose() {
        if (prefilter != null) prefilter.dispose();
        if (irradiance != null) irradiance.dispose();
//        if (bdrf != null) bdrf.dispose(); should not get destroyed
        if (dynamicSkyTexture != null) dynamicSkyTexture.dispose();
        dynamicSkyTexture = null;
        prefilter = null;
        irradiance = null;
    }
}
