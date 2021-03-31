package OxyEngine.Scene.Objects;

import OxyEngine.Core.Renderer.Buffer.BufferLayoutAttributes;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutConstructor;
import OxyEngine.Core.Renderer.Light.SkyLight;
import OxyEngine.Core.Renderer.Mesh.MeshRenderMode;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Scene.Objects.Native.NativeObjectFactory;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;

import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class SkyLightFactory implements NativeObjectFactory {

    public static final OxyShader skyLightShader = OxyShader.createShader("OxyHDR", "shaders/OxyHDR.glsl");
    public static final NativeObjectMeshOpenGL skyLightMesh =
            new NativeObjectMeshOpenGL(MeshRenderMode.TRIANGLES, BufferLayoutConstructor.Usage.STATIC, new BufferLayoutAttributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 0, 0));

    public static final float[] skyboxVertices = {
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

    @Override
    public void constructData(OxyNativeObject e, int size) {
        if (e.vertices == null) {
            e.vertices = skyboxVertices;
        }
    }

    public void initData(OxyNativeObject e, NativeObjectMeshOpenGL mesh) {
        e.indices = SkyLight.indices;
        mesh.addToBuffer();
    }
}
