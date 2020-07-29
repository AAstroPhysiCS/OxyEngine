package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;
import OxyEngineEditor.Sandbox.OxyComponents.ModelMesh;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.Globals.Globals.toPrimitiveInteger;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class ModelTemplate implements EntityComponent {

    static ObjectType type;
    private ModelMesh mesh;
    private final List<Vector3f> vertices;
    private final List<Vector2f> textureCoords;
    private final List<Vector3f> normals;
    private final List<int[]> faces;

    public ModelTemplate(List<Vector3f> vertices, List<Vector2f> textureCoords, List<Vector3f> normals, List<int[]> faces) {
        type = ObjectType.Model;
        this.vertices = vertices;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.faces = faces;
    }

    public void constructData(OxyModel e) {

        e.vertices = new float[vertices.size() * 4];
        e.normals = new float[vertices.size() * 3];
        e.tcs = new float[vertices.size() * 2];
        List<Integer> indicesArr = new ArrayList<>();

        OxyTexture texture = (OxyTexture) e.get(OxyTexture.class);
        TransformComponent c = (TransformComponent) e.get(TransformComponent.class);

        Matrix4f transform = new Matrix4f()
                .scale(c.scale)
                .translate(c.position)
                .rotateX((float) Math.toRadians(c.rotation.x))
                .rotateY((float) Math.toRadians(c.rotation.y))
                .rotateZ((float) Math.toRadians(c.rotation.z));

        int slot = 0;
        if (texture != null) slot = texture.getTextureSlot();

        int vertPtr = 0;
        for (Vector3f v : vertices) {
            Vector4f transformed = new Vector4f(v, 1.0f).mul(transform);
            e.vertices[vertPtr++] = transformed.x;
            e.vertices[vertPtr++] = transformed.y;
            e.vertices[vertPtr++] = transformed.z;
            e.vertices[vertPtr++] = slot;
        }

        int nPtr = 0;
        for (Vector3f n : normals) {
            e.normals[nPtr++] = n.x;
            e.normals[nPtr++] = n.y;
            e.normals[nPtr++] = n.z;
        }

        for (int[] face : faces) {
            int vertexPtr = face[0] - 1;
            indicesArr.add(vertexPtr);

            Vector2f textureCoords2f = textureCoords.get(face[1] - 1);
            e.tcs[vertexPtr * 2] = textureCoords2f.x;
            e.tcs[vertexPtr * 2 + 1] = 1 - textureCoords2f.y;

            Vector3f normals3f = normals.get(face[2] - 1);
            e.normals[vertexPtr * 3] = normals3f.x;
            e.normals[vertexPtr * 3 + 1] = normals3f.y;
            e.normals[vertexPtr * 3 + 2] = normals3f.z;
        }

        e.indices = toPrimitiveInteger(indicesArr);
        this.mesh = new ModelMesh.ModelMeshBuilderImpl()
                .setMode(GL_TRIANGLES)
                .setUsage(BufferTemplate.Usage.DYNAMIC)
                .setVertices(e.vertices)
                .setIndices(e.indices)
                .setTextureCoords(e.tcs)
                .setNormals(e.normals)
                .create();
    }

    public void updateData(OxyModel e) {

        OxyTexture texture = (OxyTexture) e.get(OxyTexture.class);
        TransformComponent c = (TransformComponent) e.get(TransformComponent.class);

        c.transform = new Matrix4f()
                .scale(c.scale)
                .translate(c.position)
                .rotateX(c.rotation.x)
                .rotateY(c.rotation.y)
                .rotateZ(c.rotation.z);

        int vertPtr = 0;
        int slot = 0;
        if (texture != null) slot = texture.getTextureSlot();
        for (int i = 0; i < vertices.size() / 3; i++) {
            Vector3f v = vertices.get(i);
            Vector4f transformed = new Vector4f(v, 1.0f).mul(c.transform);
            e.vertices[vertPtr++] = transformed.x;
            e.vertices[vertPtr++] = transformed.y;
            e.vertices[vertPtr++] = transformed.z;
            e.vertices[vertPtr++] = slot;
        }
    }

    public ModelMesh getMesh() {
        return mesh;
    }
}
