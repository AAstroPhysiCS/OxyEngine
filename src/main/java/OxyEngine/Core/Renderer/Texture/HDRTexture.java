package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngineEditor.Components.NativeObjectMesh;
import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.Scene.SceneRuntime;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

import static OxyEngine.Core.Renderer.Texture.OxyTexture.allTextures;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class HDRTexture extends OxyTexture.AbstractTexture {

    private static final float[] skyboxVertices = {
            -1, 1, -1,
            -1, -1, -1,
            1, -1, -1,
            1, -1, -1,
            1, 1, -1,
            -1, 1, -1,

            -1, -1, 1,
            -1, -1, -1,
            -1, 1, -1,
            -1, 1, -1,
            -1, 1, 1,
            -1, -1, 1,

            1, -1, -1,
            1, -1, 1,
            1, 1, 1,
            1, 1, 1,
            1, 1, -1,
            1, -1, -1,

            -1, -1, 1,
            -1, 1, 1,
            1, 1, 1,
            1, 1, 1,
            1, -1, 1,
            -1, -1, 1,

            -1, 1, -1,
            1, 1, -1,
            1, 1, 1,
            1, 1, 1,
            -1, 1, 1,
            -1, 1, -1,

            -1, -1, -1,
            -1, -1, 1,
            1, -1, -1,
            1, -1, -1,
            -1, -1, 1,
            1, -1, 1
    };

    private final Scene scene;
    private static NativeObjectMesh mesh;

    private final Matrix4f[] captureViews;
    private final Matrix4f captureProjection;
    private final int hdrTexture;
    private final int captureFBO;
    private final int captureRBO;

    private IrradianceTexture irradianceTexture;
    private PrefilterTexture prefilterTexture;
    private BDRF bdrf;

    HDRTexture(int slot, String path, Scene scene) {
        super(slot, path);
        this.scene = scene;
        assert slot != 0 : oxyAssert("Slot can not be 0");

        stbi_set_flip_vertically_on_load(false);
        int[] width = new int[1];
        int[] height = new int[1];
        int[] nrComponents = new int[1];
        FloatBuffer data = stbi_loadf(path, width, height, nrComponents, 0);
        assert data != null : oxyAssert("HDR Texture failed!");
        hdrTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, hdrTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, width[0], height[0], 0, GL_RGB, GL_FLOAT, data);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        stbi_image_free(data);
        glBindTexture(GL_TEXTURE_2D, 0);

        textureId = glGenTextures();
        allTextures.add(this);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
        for (int i = 0; i < 6; i++) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F,
                    3840, 3840, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        }
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        captureFBO = glGenFramebuffers();
        captureRBO = glGenRenderbuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
        glBindRenderbuffer(GL_RENDERBUFFER, captureRBO);

        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 3840, 3840);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, captureRBO);
        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE : oxyAssert("Framebuffer is incomplete!");

        captureProjection = new Matrix4f().perspective((float) Math.toRadians(90), 1.0f, 0.478f, 10.0f);
        captureViews = new Matrix4f[]{
                new Matrix4f()
                        .lookAt(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
                new Matrix4f()
                        .lookAt(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
                new Matrix4f()
                        .rotateY((float) Math.toRadians(180))
                        .lookAt(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f),
                new Matrix4f()
                        .rotateY((float) Math.toRadians(180))
                        .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f),
                new Matrix4f()
                        .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f),
                new Matrix4f()
                        .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f),
        };
    }

    private static final OxyShader shader = new OxyShader("shaders/OxyHDR.glsl");

    public void captureFaces(float ts) {
        shader.enable();
        shader.setUniform1i("hdrTexture", 0);
        shader.setUniformMatrix4fv("projection", captureProjection, true);
        shader.disable();

        BufferTemplate.Attributes attributesVert = new BufferTemplate.Attributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 0, 0);

        if (mesh == null) {
            mesh = new NativeObjectMesh.NativeMeshBuilderImpl()
                    .setShader(shader)
                    .setMode(GL_TRIANGLES)
                    .setUsage(BufferTemplate.Usage.STATIC)
                    .setVerticesBufferAttributes(attributesVert)
                    .create();
            OxyNativeObject cube = scene.createNativeObjectEntity();
            cube.vertices = skyboxVertices;
            int[] indices = new int[skyboxVertices.length];
            for (int i = 0; i < skyboxVertices.length; i++) {
                indices[i] = i;
            }
            cube.indices = indices;
            cube.addComponent(mesh, shader);
            mesh.initList();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, hdrTexture);
        glViewport(0, 0, 3840, 3840);
        for (int i = 0; i < 6; i++) {
            shader.enable();
            shader.setUniformMatrix4fv("view", captureViews[i], true);
            shader.disable();
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, textureId, 0);
            OpenGLRendererAPI.clearBuffer();
            scene.getRenderer().render(ts, mesh, SceneRuntime.currentBoundedCamera);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        if (irradianceTexture == null || prefilterTexture == null || bdrf == null) {
            oxyAssert("HDR Texture Algorithms are null!");
            return;
        }
        irradianceTexture.captureFaces(ts);
        prefilterTexture.captureFaces(ts);
        bdrf.captureFaces(ts);
    }

    @Override
    public void dispose() {
        bdrf.dispose();
        prefilterTexture.dispose();
        irradianceTexture.dispose();
        glDeleteFramebuffers(captureFBO);
        glDeleteRenderbuffers(captureRBO);
        super.dispose();
    }

    public void setIrradianceTexture(IrradianceTexture irradianceTexture) {
        this.irradianceTexture = irradianceTexture;
    }

    public void setPrefilterTexture(PrefilterTexture prefilterTexture) {
        this.prefilterTexture = prefilterTexture;
    }

    public void setBdrf(BDRF bdrf) {
        this.bdrf = bdrf;
    }

    public int getIrradianceSlot() {
        return irradianceTexture.getTextureSlot();
    }

    public int getPrefilterSlot() {
        return prefilterTexture.getTextureSlot();
    }

    public int getBDRFSlot() {
        return bdrf.getTextureSlot();
    }

    static class BDRF extends OxyTexture.AbstractTexture {

        private final HDRTexture mainTexture;

        BDRF(int slot, String path, HDRTexture mainTexture) {
            super(slot, path);
            this.mainTexture = mainTexture;
            allTextures.add(this);
        }

        final static OxyShader shader = new OxyShader("shaders/OxyBDRF.glsl");

        public void captureFaces(float ts) {
            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RG16F, 512, 512, 0,
                    GL_RG, GL_FLOAT, (FloatBuffer) null);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glBindTexture(GL_TEXTURE_2D, 0);

            glBindFramebuffer(GL_FRAMEBUFFER, mainTexture.captureFBO);
            glBindRenderbuffer(GL_RENDERBUFFER, mainTexture.captureRBO);
            shader.enable();
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 512, 512);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
            glViewport(0, 0, 512, 512);
            shader.disable();
            OpenGLRendererAPI.clearBuffer();
            mainTexture.scene.getRenderer().render(ts, mesh, SceneRuntime.currentBoundedCamera, shader);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    static class IrradianceTexture extends OxyTexture.AbstractTexture {

        private final HDRTexture mainTexture;

        IrradianceTexture(int slot, String path, HDRTexture mainTexture) {
            super(slot, path);
            this.mainTexture = mainTexture;
            allTextures.add(this);
        }

        final static OxyShader shader = new OxyShader("shaders/OxyIBL.glsl");

        void captureFaces(float ts) {
            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
            for (int i = 0; i < 6; i++) {
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F, 32, 32, 0,
                        GL_RGB, GL_FLOAT, (FloatBuffer) null);
            }
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

            glBindFramebuffer(GL_FRAMEBUFFER, mainTexture.captureFBO);
            glBindRenderbuffer(GL_RENDERBUFFER, mainTexture.captureRBO);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 32, 32);

            shader.enable();
            shader.setUniform1i("skyBoxTexture", 0);
            shader.setUniformMatrix4fv("projection", mainTexture.captureProjection, true);
            shader.disable();

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_CUBE_MAP, mainTexture.textureId);
            glViewport(0, 0, 32, 32);
            for (int i = 0; i < 6; i++) {
                shader.enable();
                shader.setUniformMatrix4fv("view", mainTexture.captureViews[i], true);
                shader.disable();
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                        GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, textureId, 0);
                OpenGLRendererAPI.clearBuffer();
                mainTexture.scene.getRenderer().render(ts, mesh, SceneRuntime.currentBoundedCamera, shader);
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    static class PrefilterTexture extends OxyTexture.AbstractTexture {

        private final HDRTexture mainTexture;

        PrefilterTexture(int slot, String path, HDRTexture mainTexture) {
            super(slot, path);
            this.mainTexture = mainTexture;
            allTextures.add(this);
        }

        final static OxyShader shader = new OxyShader("shaders/OxyPrefiltering.glsl");

        public void captureFaces(float ts) {

            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
            for (int i = 0; i < 6; i++) {
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F, 128, 128, 0,
                        GL_RGB, GL_FLOAT, (FloatBuffer) null);
            }
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

            shader.enable();
            shader.setUniform1i("skyBoxTexture", 0);
            shader.setUniformMatrix4fv("projection", mainTexture.captureProjection, true);
            shader.disable();

            glBindFramebuffer(GL_FRAMEBUFFER, mainTexture.captureFBO);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_CUBE_MAP, mainTexture.textureId);
            int maxMipLevels = 5;
            for (int mip = 0; mip < maxMipLevels; mip++) {
                int mipWidth = (int) (128 * Math.pow(0.5f, mip));
                int mipHeight = (int) (128 * Math.pow(0.5f, mip));
                glBindRenderbuffer(GL_RENDERBUFFER, mainTexture.captureRBO);
                glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, mipWidth, mipHeight);
                glViewport(0, 0, mipWidth, mipHeight);
                float roughness = (float) mip / (float) (maxMipLevels - 1);
                shader.enable();
                shader.setUniform1f("roughness", roughness);
                shader.disable();
                for (int i = 0; i < 6; i++) {
                    shader.enable();
                    shader.setUniformMatrix4fv("view", mainTexture.captureViews[i], true);
                    shader.disable();
                    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                            GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, textureId, mip);
                    OpenGLRendererAPI.clearBuffer();
                    mainTexture.scene.getRenderer().render(ts, mesh, SceneRuntime.currentBoundedCamera, shader);
                }
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    public NativeObjectMesh getMesh() {
        return mesh;
    }
}
