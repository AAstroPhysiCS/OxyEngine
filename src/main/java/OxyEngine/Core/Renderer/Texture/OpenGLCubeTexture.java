package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.Mesh.MeshRenderMode;
import OxyEngine.Core.Renderer.Mesh.NativeMeshOpenGL;
import OxyEngine.Core.Renderer.OxyRenderPass;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Renderer.Pipeline.OxyShader;
import OxyEngine.Scene.Objects.Model.OxyNativeObject;
import OxyEngine.Scene.Scene;
import OxyEngine.Scene.SceneRenderer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Set;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.stb.STBImage.stbi_image_free;

public class OpenGLCubeTexture extends CubeTexture {

    private final Scene scene;
    private OxyShader shader;

    OpenGLCubeTexture(TextureSlot slot, String path, Scene scene) {
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
        for (int i = 0; i < totalFiles.size(); i++) {
            loadAsByteBuffer(totalFiles.get(i));
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, alFormat, width, height, 0, alFormat, GL_UNSIGNED_BYTE, (ByteBuffer) textureBuffer);
            stbi_image_free((ByteBuffer) textureBuffer);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
    }

    @Override
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

            NativeMeshOpenGL mesh = new NativeMeshOpenGL(skyBoxPipeline);
            int[] indices = new int[skyboxVertices.length];
            for (int i = 0; i < skyboxVertices.length; i++)
                indices[i] = i;
            OxyNativeObject cube = scene.createNativeObjectEntity(skyboxVertices, indices);
            cube.addComponent(mesh);
            mesh.load(skyBoxPipeline);
        }
    }
}
