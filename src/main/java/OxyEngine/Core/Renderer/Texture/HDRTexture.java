package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngineEditor.Components.NativeObjectMesh;
import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;
import OxyEngineEditor.Scene.Scene;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

import static OxyEngine.Core.Renderer.Texture.OxyTexture.allTextures;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class HDRTexture extends OxyTexture.Texture {

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
    private NativeObjectMesh mesh;

    private final Matrix4f[] captureViews;
    private final Matrix4f captureProjection;
    private final int captureFBO, captureRBO, hdrTexture;
    private final int[] width, height;

    public HDRTexture(int slot, String path, Scene scene) {
        super(slot, path);
        this.scene = scene;
        assert slot != 0 : oxyAssert("Slot can not be 0");

        stbi_set_flip_vertically_on_load(false);
        width = new int[1];
        height = new int[1];
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
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
        for (int i = 0; i < 6; i++) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F,
                    7680, 7680, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        }
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        allTextures.add(this);

        captureFBO = glGenFramebuffers();
        captureRBO = glGenRenderbuffers();

        glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
        glBindRenderbuffer(GL_RENDERBUFFER, captureRBO);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 7680, 7680);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, captureRBO);
        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE : oxyAssert("Framebuffer is incomplete!");

        captureProjection = new Matrix4f().perspective(70.0f, 1.0f, 1.0f, 10.0f);
        captureViews = new Matrix4f[]{
                new Matrix4f().lookAt(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, .0f, -1.0f, 0.0f),
                new Matrix4f().lookAt(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
                new Matrix4f().lookAt(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f),
                new Matrix4f().lookAt(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f),
                new Matrix4f().lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f),
                new Matrix4f().lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f),
        };
    }

    public void captureFaces(float ts) {

        OxyShader shader = new OxyShader("shaders/OxyHDR.glsl");

        shader.enable();
        shader.setUniform1i("hdrTexture", 0);
        shader.setUniformMatrix4fv("projection", captureProjection, true);
        shader.disable();

        BufferTemplate.Attributes attributesVert = new BufferTemplate.Attributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 0, 0);

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

        glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, hdrTexture);
        glViewport(0, 0, 7680, 7680);
        for (int i = 0; i < 6; i++) {
            shader.enable();
            shader.setUniformMatrix4fv("view", captureViews[i], true);
            shader.disable();

            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, textureId, 0);
            OpenGLRendererAPI.clearBuffer();
            scene.getRenderer().render(ts, mesh, OxyRenderer.currentBoundedCamera);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public NativeObjectMesh getMesh() {
        return mesh;
    }
}
