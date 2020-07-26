package OxyEngine.Core.OxyObjects;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.OxyComponents.GameObjectMeshComponent;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.Core.OxyComponents.GameObjectMeshComponent.BufferAttributes.*;
import static OxyEngine.Core.Renderer.OxyRenderer.MeshSystem.sandBoxMesh;
import static OxyEngine.Core.Renderer.OxyRenderer.MeshSystem.worldGridMesh;
import static org.lwjgl.opengl.GL11.GL_LINES;

public class WorldGrid {

    private static final List<OxyEntity> worldGrids = new ArrayList<>();

    private final OxyRenderer renderer;

    public WorldGrid(OxyRenderer renderer, int size) {
        this.renderer = renderer;
        worldGridMesh.obj = new GameObjectMeshComponent.GameObjectMeshBuilderImpl()
                .setMode(GL_LINES)
                .setUsage(BufferTemplate.Usage.STATIC)
                .setVerticesBufferAttributes(attributesVert, attributesTXCoords, attributesTXSlots)
                .setGameObjectType(GameObject.Type.Cube)
                .create();
        add(size);
        worldGridMesh.obj.add(GameObject.sumAllVertices(worldGrids, GameObject.Type.Grid), GameObject.sumAllIndices(worldGrids, GameObject.Type.Grid));
    }

    private static class Grid extends GameObject {

        private static OxyColor color;

        private static final float[] vertexPos = new float[]{
                -0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
        };

        public Grid(OxyColor color, Vector3f position, Vector3f rotation, float scale) {
            super(color, position, rotation, scale);
            color.init();
            Grid.color = color;
            transform = new Matrix4f()
                    .scale(scale)
                    .translate(position.x, position.y, position.z)
                    .rotateX(rotation.x)
                    .rotateY(rotation.y)
                    .rotateZ(rotation.z);
        }

        public Grid(OxyColor color, Vector3f position, Vector3f rotation) {
            this(color, position, rotation, 1);
        }

        public void initData(GameObjectMeshComponent mesh) {
            indices = new int[]{
                    mesh.indicesX, 1 + mesh.indicesY, 3 + mesh.indicesZ,
                    3 + mesh.indicesX, mesh.indicesY, 2 + mesh.indicesZ,
            };
            mesh.indicesX += 4;
            mesh.indicesY += 4;
            mesh.indicesZ += 4;
        }

        @Override
        protected void constructData() {
            Vector4f[] vec4Vertices = new Vector4f[4];
            int vecPtr = 0;
            for (int i = 0; i < vec4Vertices.length; i++) {
                vec4Vertices[i] = new Vector4f(vertexPos[vecPtr++], vertexPos[vecPtr++], vertexPos[vecPtr++], 1.0f).mul(transform);
            }

            vertices = new float[GameObject.Type.Grid.n_Vertices()];
            int ptr = 0;
            for (int i = 0; i < GameObject.Type.Grid.n_Vertices(); ) {
                vertices[i++] = vec4Vertices[ptr].x;
                vertices[i++] = vec4Vertices[ptr].y;
                vertices[i++] = vec4Vertices[ptr].z;
                i += 2;
                vertices[i++] = 0;
                ptr++;
            }
        }
    }

    private void add(int size) {
        int index = 0;
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                worldGrids.add(new Grid(new OxyColor(1.0f, 1.0f, 1.0f, 0.2f, renderer.getShader()), new Vector3f(x, 0, z), new Vector3f(0, 0, 0), 20f));
                ((Grid) worldGrids.get(index)).initData(worldGridMesh.obj);
                index++;
            }
        }
    }

    public void render() {
        sandBoxMesh.obj.getFrameBuffer().bind();
        Grid.color.init();
        renderer.render(worldGridMesh.obj);
        sandBoxMesh.obj.getFrameBuffer().unbind();
    }
}