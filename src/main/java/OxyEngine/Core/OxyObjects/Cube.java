package OxyEngine.Core.OxyObjects;

import OxyEngine.Core.OxyComponents.GameObjectMeshComponent;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Cube extends GameObject {

    /**
     * Constructs a new object with texture
     */
    public Cube(OxyTexture texture, Vector3f position, Vector3f rotation, float scale) {
        super(texture, position, rotation, scale);
    }

    public Cube(OxyTexture texture, Vector3f position, Vector3f rotation) {
        super(texture, position, rotation);
    }

    /**
     * Reuses a already instanced texture
     */
    public Cube(int slot, Vector3f position, Vector3f rotation, float scale) {
        super(slot, position, rotation, scale);
    }

    public Cube(int slot, Vector3f position, Vector3f rotation) {
        super(slot, position, rotation);
    }

    /**
     * Constructs a new object with color
     */
    public Cube(OxyColor color, Vector3f position, Vector3f rotation, float scale) {
        super(color, position, rotation, scale);
    }

    public Cube(OxyColor color, Vector3f position, Vector3f rotation) {
        super(color, position, rotation);
    }

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


    @Override
    protected void constructData() {
        Vector4f[] vec4Vertices = new Vector4f[24];
        int vecPtr = 0;
        for (int i = 0; i < vec4Vertices.length; i++) {
            vec4Vertices[i] = new Vector4f(cubeVertexPos[vecPtr++], cubeVertexPos[vecPtr++], cubeVertexPos[vecPtr++], 1.0f).mul(transform);
        }
        int slot = 0; // 0 => color
        float[] tcs = null;

        if (texture != null) {
            slot = texture.getTextureSlot();
            tcs = texture.getTextureCoords();
        }

        vertices = new float[Type.Cube.n_Vertices()];
        int ptr = 0, texIndex = 0;
        for (int i = 0; i < Type.Cube.n_Vertices(); ) {
            vertices[i++] = vec4Vertices[ptr].x;
            vertices[i++] = vec4Vertices[ptr].y;
            vertices[i++] = vec4Vertices[ptr].z;
            if (texture != null) {
                vertices[i++] = tcs[texIndex++];
                vertices[i++] = tcs[texIndex++];
            } else i += 2;
            vertices[i++] = slot;
            ptr++;
        }
    }

    @Override
    public void initData(GameObjectMeshComponent mesh) {
        indices = new int[]{
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
    }
}
