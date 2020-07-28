package OxyEngineEditor.Sandbox.OxyObjects;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static OxyEngine.System.OxySystem.logger;

public class CubeTemplate extends ObjectTemplate {

    private static final float[] cubeVertexPos = new float[]{
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,

            //back
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,

            //left
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,

            //right
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,

            //top
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,

            //bottom
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f
    };

    public CubeTemplate(){
        type = GameObjectType.Cube;
    }

    @Override
    public void constructData(OxyEntity e) {
        OxyTexture texture = (OxyTexture) e.get(OxyTexture.class);
        TransformComponent c = (TransformComponent) e.get(TransformComponent.class);

        c.transform = new Matrix4f()
                .scale(c.scale)
                .translate(c.position)
                .rotateX(c.rotation.x)
                .rotateY(c.rotation.y)
                .rotateZ(c.rotation.z);

        Vector4f[] vec4Vertices = new Vector4f[24];
        int vecPtr = 0;
        for (int i = 0; i < vec4Vertices.length; i++) {
            vec4Vertices[i] = new Vector4f(cubeVertexPos[vecPtr++], cubeVertexPos[vecPtr++], cubeVertexPos[vecPtr++], 1.0f).mul(c.transform);
        }
        int slot = 0; // 0 => color
        float[] tcs = null;

        if (texture != null) {
            slot = texture.getTextureSlot();
            tcs = texture.getTextureCoords();
        }

        e.vertices = new float[GameObjectType.Cube.n_Vertices()];
        int ptr = 0, texIndex = 0;
        for (int i = 0; i < GameObjectType.Cube.n_Vertices(); ) {
            e.vertices[i++] = vec4Vertices[ptr].x;
            e.vertices[i++] = vec4Vertices[ptr].y;
            e.vertices[i++] = vec4Vertices[ptr].z;
            if (texture != null) {
                e.vertices[i++] = tcs[texIndex++];
                e.vertices[i++] = tcs[texIndex++];
            } else i += 2;
            e.vertices[i++] = slot;
            ptr++;
        }
    }

    @Override
    public void initData(OxyEntity e, Mesh m) {
        if (m instanceof GameObjectMesh mesh) {
            e.indices = new int[]{
                    mesh.indicesX, 1 + mesh.indicesY, 3 + mesh.indicesZ,
                    3 + mesh.indicesX, mesh.indicesY, 2 + mesh.indicesZ,

                    4 + mesh.indicesX, 5 + mesh.indicesY, 7 + mesh.indicesZ,
                    7 + mesh.indicesX, 4 + mesh.indicesY, 6 + mesh.indicesZ,

                    8 + mesh.indicesX, 9 + mesh.indicesY, 11 + mesh.indicesZ,
                    11 + mesh.indicesX, 8 + mesh.indicesY, 10 + mesh.indicesZ,

                    12 + mesh.indicesX, 13 + mesh.indicesY, 15 + mesh.indicesZ,
                    15 + mesh.indicesX, 12 + mesh.indicesY, 14 + mesh.indicesZ,

                    16 + mesh.indicesX, 17 + mesh.indicesY, 19 + mesh.indicesZ,
                    19 + mesh.indicesX, 16 + mesh.indicesY, 18 + mesh.indicesZ,

                    20 + mesh.indicesX, 21 + mesh.indicesY, 23 + mesh.indicesZ,
                    23 + mesh.indicesX, 20 + mesh.indicesY, 22 + mesh.indicesZ,
            };
            mesh.indicesX += 24;
            mesh.indicesY += 24;
            mesh.indicesZ += 24;
        } else {
            logger.severe("Cube must have an GameObjectMesh!");
            throw new IllegalStateException("Cube must have an GameObjectMesh!");
        }
    }
}
