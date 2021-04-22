package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.Mesh.MeshRenderMode;
import OxyEngine.Core.Renderer.OxyRenderPass;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;
import OxyEngine.Scene.Scene;
import OxyEngine.Scene.SceneRenderer;
import OxyEngine.TextureSlot;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.stb.STBImage.*;

public class CubemapTexture extends OxyTexture.AbstractTexture {

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

    private final Scene scene;
    private OxyShader shader;
    private static final List<String> fileStructure = Arrays.asList("right", "left", "bottom", "top", "front", "back");
    private static final List<String> totalFiles = new ArrayList<>();

    CubemapTexture(TextureSlot slot, String path, Scene scene) {
        super(slot, path);
        this.scene = scene;

        assert slot.getValue() != 0 : oxyAssert("Slot can not be 0");

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);

        File[] files = Objects.requireNonNull(new File(path).listFiles());
        for (String structureName : fileStructure) {
            for (File f : files) {
                String name = f.getName().split("\\.")[0];
                if (structureName.equals(name)) {
                    totalFiles.add(f.getPath());
                }
            }
        }

        assert totalFiles.size() == 6 : oxyAssert("Cubemap directory needs to only have the texture files. Directory length: " + files.length);
        stbi_set_flip_vertically_on_load(true);
        for (int i = 0; i < totalFiles.size(); i++) {
            int[] width = new int[1];
            int[] height = new int[1];
            int[] channels = new int[1];
            ByteBuffer buffer = stbi_load(totalFiles.get(i), width, height, channels, 0);
            if (buffer == null) {
                logger.warning("Texture: " + path + " could not be loaded!");
                return;
            }
            int alFormat = GL_RGBA;
            if (channels[0] == 3)
                alFormat = GL_RGB;
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, alFormat, width[0], height[0], 0, alFormat, GL_UNSIGNED_BYTE, buffer);
            stbi_image_free(buffer);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
    }

    public void init(Set<OxyPipeline> allOtherPipelines) {

        for (OxyPipeline s : allOtherPipelines) {
            OxyShader shader = s.getShader();
            shader.begin();
            shader.setUniform1i("skyBoxTexture", textureSlot.getValue());
            shader.end();
        }

        if (shader == null) {

            shader = OxyShader.createShader("OxySkybox", "shaders/OxySkybox.glsl");
            OxyPipeline skyBoxPipeline = OxyPipeline.createNewPipeline(OxyPipeline.createNewSpecification()
                    .setRenderPass(OxyRenderPass.createBuilder(SceneRenderer.getInstance().getFrameBuffer())
                            .renderingMode(MeshRenderMode.TRIANGLES)
                            .create())
                    .setDebugName("Cube Map Texture Rendering Pipeline")
                    .setShader(shader));

            shader.begin();
            shader.setUniform1i("skyBoxTexture", textureSlot.getValue());
            shader.end();

            NativeObjectMeshOpenGL mesh = new NativeObjectMeshOpenGL(skyBoxPipeline);
            OxyNativeObject cube = scene.createNativeObjectEntity();
            cube.vertices = skyboxVertices;
            int[] indices = new int[skyboxVertices.length];
            for (int i = 0; i < skyboxVertices.length; i++) {
                indices[i] = i;
            }
            cube.indices = indices;
            cube.addComponent(mesh);
            mesh.addToBuffer(skyBoxPipeline);
        }
    }
}
