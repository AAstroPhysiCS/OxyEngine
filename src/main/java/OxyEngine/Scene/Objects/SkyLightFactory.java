package OxyEngine.Scene.Objects;

import OxyEngine.Core.Renderer.Buffer.BufferLayoutAttributes;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutProducer;
import OxyEngine.Core.Renderer.Light.SkyLight;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Scene.Objects.Native.NativeObjectFactory;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class SkyLightFactory implements NativeObjectFactory {

    public static final OxyShader skyLightShader = new OxyShader("shaders/OxyHDR.glsl");
    public static final NativeObjectMeshOpenGL skyLightMesh =
            new NativeObjectMeshOpenGL(GL_TRIANGLES, BufferLayoutProducer.Usage.STATIC, new BufferLayoutAttributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 0, 0));

    @Override
    public void constructData(OxyNativeObject e, int size) {
        if (e.vertices == null) {
            e.vertices = SkyLight.skyboxVertices;
        }
    }

    public void initData(OxyNativeObject e, NativeObjectMeshOpenGL mesh) {
        e.indices = SkyLight.indices;
        mesh.addToQueue();
    }
}
