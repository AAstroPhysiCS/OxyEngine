package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Scene.Scene;
import OxyEngine.TargetPlatform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.lwjgl.opengl.GL11.glDeleteTextures;

public abstract class CubeTexture extends Texture {

    protected static final float[] skyboxVertices = {
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

    protected static final List<String> fileStructure = Arrays.asList("right", "left", "bottom", "top", "front", "back");
    protected static final List<String> totalFiles = new ArrayList<>();

    protected final TextureSlot textureSlot;
    protected int textureId;

    protected CubeTexture(TextureSlot slot, String path) {
        super(path);
        this.textureSlot = slot;
    }

    static CubeTexture create(TextureSlot slot, String path, Scene scene){
        if(OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL){
            return new OpenGLCubeTexture(slot, path, scene);
        }
        throw new IllegalStateException("API not supported yet!");
    }

    @Override
    public void dispose() {
        glDeleteTextures(textureId);
    }

    public abstract void init(Set<OxyPipeline> allOtherPipelines);
}
