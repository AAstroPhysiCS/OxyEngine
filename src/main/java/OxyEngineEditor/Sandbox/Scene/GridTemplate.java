package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class GridTemplate extends GameObjectTemplate {

    private static OxyColor color;

    private static final float[] vertexPos = new float[]{
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
    };

    public GridTemplate(OxyShader shader) {
        type = ObjectType.Grid;
        color = new OxyColor(1.0f, 1.0f, 1.0f, 0.2f, shader);
        color.init();
    }

    public static OxyColor getColor() {
        return color;
    }

    @Override
    public void constructData(OxyGameObject e) {
        TransformComponent c = (TransformComponent) e.get(TransformComponent.class);

        c.transform = new Matrix4f()
                .scale(c.scale)
                .translate(c.position)
                .rotateX(c.rotation.x)
                .rotateY(c.rotation.y)
                .rotateZ(c.rotation.z);

        Vector4f[] vec4Vertices = new Vector4f[4];
        int vecPtr = 0;
        for (int i = 0; i < vec4Vertices.length; i++) {
            vec4Vertices[i] = new Vector4f(vertexPos[vecPtr++], vertexPos[vecPtr++], vertexPos[vecPtr++], 1.0f).mul(c.transform);
        }

        e.vertices = new float[ObjectType.Grid.n_Vertices()];
        int ptr = 0;
        for (int i = 0; i < ObjectType.Grid.n_Vertices(); ) {
            e.vertices[i++] = vec4Vertices[ptr].x;
            e.vertices[i++] = vec4Vertices[ptr].y;
            e.vertices[i++] = vec4Vertices[ptr].z;
            i += 2;
            e.vertices[i++] = 0;
            ptr++;
        }
    }

    @Override
    public void initData(OxyGameObject e, GameObjectMesh mesh) {
        e.indices = new int[]{
                mesh.indicesX, 1 + mesh.indicesY, 3 + mesh.indicesZ,
                3 + mesh.indicesX, mesh.indicesY, 2 + mesh.indicesZ,
        };
        mesh.indicesX += 4;
        mesh.indicesY += 4;
        mesh.indicesZ += 4;
    }
}
