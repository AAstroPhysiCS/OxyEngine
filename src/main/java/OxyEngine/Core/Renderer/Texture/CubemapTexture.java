package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Components.EntityComponent;
import OxyEngineEditor.Components.NativeObjectMesh;
import OxyEngineEditor.Scene.NativeObjects.OxyNativeObject;
import OxyEngineEditor.Scene.Scene;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;

import static OxyEngine.Core.Renderer.Texture.OxyTexture.allTextures;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

public class CubemapTexture extends OxyTexture.Texture {

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
    private OxyShader shader;
    private static final List<String> fileStructure = Arrays.asList("right", "left", "bottom", "top", "front", "back");
    private static final List<String> totalFiles = new ArrayList<>();

    CubemapTexture(int slot, String path, Scene scene) {
        this.textureSlot = slot;
        this.path = path;
        this.scene = scene;

        assert slot != 0 : oxyAssert("Slot can not be 0");

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
            ByteBuffer buffer = loadTextureFile(totalFiles.get(i), width, height, channels);
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

        allTextures.add(this);
    }

    private OxyNativeObject cube;

    public void init(Set<EntityComponent> allOtherShaders) {

        for (EntityComponent s : allOtherShaders) {
            OxyShader ss = (OxyShader) s;
            ss.enable();
            ss.setUniform1i("skyBoxTexture", textureSlot);
            ss.disable();
        }

        if (shader == null) {

            shader = new OxyShader("shaders/skybox.glsl");
            shader.enable();
            shader.setUniform1i("skyBoxTexture", textureSlot);
            shader.disable();

            BufferTemplate.Attributes attributesVert = new BufferTemplate.Attributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 0, 0);

            NativeObjectMesh mesh = new NativeObjectMesh.NativeMeshBuilderImpl()
                    .setShader(shader)
                    .setMode(GL_TRIANGLES)
                    .setUsage(BufferTemplate.Usage.STATIC)
                    .setVerticesBufferAttributes(attributesVert)
                    .create();

            cube = scene.createNativeObjectEntity();
            cube.vertices = skyboxVertices;
            int[] indices = new int[skyboxVertices.length];
            for (int i = 0; i < skyboxVertices.length; i++) {
                indices[i] = i;
            }
            cube.indices = indices;
            cube.addComponent(mesh, shader);
            mesh.initList();
        }
    }

    public OxyNativeObject getCube() {
        return cube;
    }
}
