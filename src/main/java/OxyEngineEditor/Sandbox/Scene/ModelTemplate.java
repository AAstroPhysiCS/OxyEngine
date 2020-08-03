package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.Globals.Globals.toPrimitiveInteger;

public class ModelTemplate implements EntityComponent {

    private final List<Vector3f> verticesNonTransformed;
    private final List<Vector2f> textureCoords;
    private final List<Vector3f> normals;
    private final List<int[]> faces;

    public ModelTemplate(List<Vector3f> verticesNonTransformed, List<Vector2f> textureCoords, List<Vector3f> normals, List<int[]> faces) {
        this.verticesNonTransformed = verticesNonTransformed;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.faces = faces;
    }

    public void constructData(OxyModel e) {
        e.vertices = new float[verticesNonTransformed.size() * 4 * 8];
        e.normals = new float[verticesNonTransformed.size() * 3 * 4];
        e.tcs = new float[verticesNonTransformed.size() * 2 * 4];
        List<Integer> indicesArr = new ArrayList<>();

        OxyColor color = (OxyColor) e.get(OxyColor.class);
        OxyTexture texture = (OxyTexture) e.get(OxyTexture.class);
        TransformComponent c = (TransformComponent) e.get(TransformComponent.class);

        c.transform = new Matrix4f()
                .translate(c.position)
                .rotateX(c.rotation.x)
                .rotateY(c.rotation.y)
                .rotateZ(c.rotation.z)
                .scale(c.scale);

        int slot = 0;
        if (texture != null) slot = texture.getTextureSlot();

        int vertPtr = 0;
        for (Vector3f v : verticesNonTransformed) {
            Vector4f transformed = new Vector4f(v, 1.0f).mul(c.transform);
            e.vertices[vertPtr++] = transformed.x;
            e.vertices[vertPtr++] = transformed.y;
            e.vertices[vertPtr++] = transformed.z;
            e.vertices[vertPtr++] = slot;
            if(color != null && slot == 0){
                e.vertices[vertPtr++] = color.getNumbers()[0];
                e.vertices[vertPtr++] = color.getNumbers()[1];
                e.vertices[vertPtr++] = color.getNumbers()[2];
                e.vertices[vertPtr++] = color.getNumbers()[3];
            } else vertPtr += 4;
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
    }
    //TODO: UPDATE DATA
}
