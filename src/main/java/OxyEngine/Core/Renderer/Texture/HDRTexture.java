package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.Buffer.BufferLayoutAttributes;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutProducer;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;
import OxyEngine.Scene.Scene;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

import static OxyEngine.Core.Renderer.Context.OxyRenderCommand.rendererAPI;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;
import static org.lwjgl.stb.STBImage.*;

public class HDRTexture extends OxyTexture.AbstractTexture {

    private static final float[] skyboxVertices = {
            -1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            // front face
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            // left face
            -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            // right face
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            // bottom face
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            // top face
            -1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
    };

    private static Scene scene;
    private static NativeObjectMeshOpenGL mesh;

    private static final Matrix4f[] captureViews = new Matrix4f[]{
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f),
    };

    private static final Matrix4f captureProjection = new Matrix4f().setPerspective((float) Math.toRadians(90), 1.0f, 0.4768f, 10.0f);
    private final int captureFBO;
    private final int captureRBO;

    private final IrradianceTexture irradianceTexture;
    private final PrefilterTexture prefilterTexture;
    private final BDRF bdrf;

    private static final OxyShader shader = new OxyShader("shaders/OxyHDR.glsl");

    HDRTexture(int slot, String path, Scene scene) {
        super(slot, path);
        HDRTexture.scene = scene;
        assert slot != 0 : oxyAssert("Slot can not be 0");

        captureFBO = glGenFramebuffers();
        captureRBO = glGenRenderbuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
        glBindRenderbuffer(GL_RENDERBUFFER, captureRBO);

        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 1920, 1920);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, captureRBO);
        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE : oxyAssert("Framebuffer is incomplete!");

        int[] width = new int[1];
        int[] height = new int[1];
        int[] nrComponents = new int[1];
        stbi_set_flip_vertically_on_load(true);
        FloatBuffer data = stbi_loadf(path, width, height, nrComponents, 0);
        assert data != null : oxyAssert("HDR Texture failed!");
        int hdrTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, hdrTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width[0], height[0], 0, GL_RGB, GL_FLOAT, data);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        stbi_image_free(data);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
        for (int i = 0; i < 6; ++i) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB32F,
                    1920, 1920, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        }
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        shader.enable();
        shader.setUniform1i("hdrTexture", 0);
        shader.setUniformMatrix4fv("projection", captureProjection, true);
        shader.disable();

        if (mesh == null) {
            mesh = new NativeObjectMeshOpenGL(GL_TRIANGLES, BufferLayoutProducer.Usage.STATIC, new BufferLayoutAttributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 0, 0));
            OxyNativeObject cube = scene.createNativeObjectEntity();
            cube.vertices = skyboxVertices;
            int[] indices = new int[skyboxVertices.length];
            for (int i = 0; i < skyboxVertices.length; i++) {
                indices[i] = i;
            }
            cube.indices = indices;
            cube.addComponent(mesh, shader);
            mesh.addToQueue();
        }

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, hdrTexture);
        glViewport(0, 0, 1920, 1920);
        glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
        for (int i = 0; i < 6; ++i) {
            shader.enable();
            shader.setUniformMatrix4fv("view", captureViews[i], true);
            shader.disable();
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, textureId, 0);
            rendererAPI.clearBuffer();
            scene.getRenderer().render(mesh, shader);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
        irradianceTexture = new IrradianceTexture(7, path, this);
        prefilterTexture = new PrefilterTexture(8, path, this);
        bdrf = new BDRF(9, path, this);
    }


    @Override
    public void dispose() {
        bdrf.dispose();
        prefilterTexture.dispose();
        irradianceTexture.dispose();
        glDeleteFramebuffers(captureFBO);
        glDeleteRenderbuffers(captureRBO);
        glDeleteVertexArrays(quadVAO);
        glDeleteBuffers(quadVBO);
        quadVAO = 0;
        quadVBO = 0;
        super.dispose();
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

    public void bindAll() {
        if (bdrf != null) glBindTextureUnit(bdrf.getTextureSlot(), bdrf.textureId);
        if (prefilterTexture != null) glBindTextureUnit(prefilterTexture.getTextureSlot(), prefilterTexture.textureId);
        if (irradianceTexture != null)
            glBindTextureUnit(irradianceTexture.getTextureSlot(), irradianceTexture.textureId);
        if (this.textureId != 0) glBindTextureUnit(this.getTextureSlot(), this.textureId);
    }

    static class IrradianceTexture extends OxyTexture.AbstractTexture {

        final static OxyShader shader = new OxyShader("shaders/OxyIBL.glsl");

        IrradianceTexture(int slot, String path, HDRTexture mainTexture) {
            super(slot, path);
            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
            for (int i = 0; i < 6; ++i) {
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB32F, 32, 32, 0,
                        GL_RGB, GL_FLOAT, (FloatBuffer) null);
            }
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            glBindFramebuffer(GL_FRAMEBUFFER, mainTexture.captureFBO);
            glBindRenderbuffer(GL_RENDERBUFFER, mainTexture.captureRBO);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 32, 32);

            shader.enable();
            shader.setUniform1i("skyBoxTexture", 0);
            shader.setUniformMatrix4fv("projection", captureProjection, true);
            shader.disable();

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_CUBE_MAP, mainTexture.textureId);
            glViewport(0, 0, 32, 32);
            glBindFramebuffer(GL_FRAMEBUFFER, mainTexture.captureFBO);
            for (int i = 0; i < 6; ++i) {
                shader.enable();
                shader.setUniformMatrix4fv("view", captureViews[i], true);
                shader.disable();
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                        GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, textureId, 0);
                rendererAPI.clearBuffer();
                scene.getRenderer().render(mesh, shader);
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    static class PrefilterTexture extends OxyTexture.AbstractTexture {

        final static OxyShader shader = new OxyShader("shaders/OxyPrefiltering.glsl");

        PrefilterTexture(int slot, String path, HDRTexture mainTexture) {
            super(slot, path);

            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
            for (int i = 0; i < 6; ++i) {
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB32F, 512, 512, 0,
                        GL_RGB, GL_FLOAT, (FloatBuffer) null);
            }
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

            shader.enable();
            shader.setUniform1i("skyBoxTexture", 0);
            shader.setUniformMatrix4fv("projection", captureProjection, true);
            shader.setUniform1f("faceSize", 1920);
            shader.disable();

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_CUBE_MAP, mainTexture.textureId);
            glBindFramebuffer(GL_FRAMEBUFFER, mainTexture.captureFBO);
            int maxMipLevels = 10;
            for (int mip = 0; mip < maxMipLevels; ++mip) {
                int mipWidth = (int) (512 * Math.pow(0.5f, mip));
                int mipHeight = (int) (512 * Math.pow(0.5f, mip));
                glBindRenderbuffer(GL_RENDERBUFFER, mainTexture.captureRBO);
                glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, mipWidth, mipHeight);
                glViewport(0, 0, mipWidth, mipHeight);
                float roughness = (float) mip / (float) (maxMipLevels - 1);
                shader.enable();
                shader.setUniform1f("roughness", roughness);
                shader.disable();
                for (int i = 0; i < 6; ++i) {
                    shader.enable();
                    shader.setUniformMatrix4fv("view", captureViews[i], true);
                    shader.disable();
                    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                            GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, textureId, mip);
                    rendererAPI.clearBuffer();
                    scene.getRenderer().render(mesh, shader);
                }
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    static class BDRF extends OxyTexture.AbstractTexture {

        final static OxyShader shader = new OxyShader("shaders/OxyBDRF.glsl");

        BDRF(int slot, String path, HDRTexture mainTexture) {
            super(slot, path);
            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RG16F, 512, 512, 0, GL_RG, GL_FLOAT, 0);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            glBindFramebuffer(GL_FRAMEBUFFER, mainTexture.captureFBO);
            glBindRenderbuffer(GL_RENDERBUFFER, mainTexture.captureRBO);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 512, 512);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
            glViewport(0, 0, 512, 512);
            rendererAPI.clearBuffer();
            shader.enable();
            renderQuad();
            shader.disable();
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    private static int quadVAO, quadVBO;

    //TODO: Rendering 2D
    private static void renderQuad() {
        if(quadVAO == 0){
            float[] quadVertices = {
                    -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                    -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                    1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            };

            quadVAO = glGenVertexArrays();
            quadVBO = glGenBuffers();
            glBindVertexArray(quadVAO);
            glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
            glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        }
        glBindVertexArray(quadVAO);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindVertexArray(0);
    }

    public NativeObjectMeshOpenGL getMesh() {
        return mesh;
    }
}