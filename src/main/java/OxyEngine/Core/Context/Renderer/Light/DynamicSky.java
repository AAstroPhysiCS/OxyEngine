package OxyEngine.Core.Context.Renderer.Light;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Mesh.FrameBuffer;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.FrameBufferSpecification;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.TextureFormat;
import OxyEngine.Core.Context.Renderer.Mesh.RenderBuffer;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Core.Context.Renderer.Texture.*;
import OxyEngine.System.OxyDisposable;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public final class DynamicSky extends SkyLight implements OxyDisposable {

    CubeTexture dynamicSkyTexture;
    final float[] azimuth = new float[]{0f};
    final float[] inclination = new float[]{0f};
    final float[] turbidity = new float[]{2.0f};
    Vector3f dynamicSkySunDir = new Vector3f(Math.sin(inclination[0]) * Math.cos(azimuth[0]), Math.cos(inclination[0]), Math.sin(inclination[0]) * Math.sin(azimuth[0]));

    private OxyIrradiance irradiance;
    private OxyPrefilter prefilter;
    private static OxyBDRF bdrf;

    public DynamicSky() {
    }

    @Override
    public void bind() {
        if (prefilter != null)
            glBindTextureUnit(prefilter.getTexture().getTextureSlot(), prefilter.getTexture().getTextureId());
        if (irradiance != null)
            glBindTextureUnit(irradiance.getTexture().getTextureSlot(), irradiance.getTexture().getTextureId());
        if (bdrf != null) glBindTextureUnit(bdrf.getTexture().getTextureSlot(), bdrf.getTexture().getTextureId());
        if (dynamicSkyTexture != null)
            glBindTextureUnit(dynamicSkyTexture.getTextureSlot(), dynamicSkyTexture.getTextureId());
    }

    public void load() {

        if (dynamicSkyTexture != null) return;

        RenderBuffer captureRBO = RenderBuffer.create(TextureFormat.DEPTHCOMPONENT24, 1024, 1024);
        FrameBuffer captureFBO = FrameBuffer.create(1024, 1024,
                FrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .useRenderBuffer(captureRBO));

        dynamicSkyTexture = OxyTexture.loadCubemap(TextureSlot.HDR, 1024, 1024, TexturePixelType.UByte, TextureFormat.RGB,
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

        OxyShader preethamBuilder = ShaderLibrary.get("OxyPreetham");
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
            OxyRenderer.clearBuffer();
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            preethamBuilder.end();
        }
        captureFBO.unbind();

        if (prefilter != null) prefilter.dispose();
        if (irradiance != null) irradiance.dispose();

        irradiance = new OxyIrradiance(dynamicSkyTexture, captureFBO, captureRBO, TextureSlot.IRRADIANCE);
        prefilter = new OxyPrefilter(dynamicSkyTexture, captureFBO, captureRBO, TextureSlot.PREFILTER);
        if (bdrf == null) bdrf = new OxyBDRF(captureFBO, captureRBO, TextureSlot.BDRF);

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
