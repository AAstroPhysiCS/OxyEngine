package OxyEngineEditor.Scene.Model;

import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngineEditor.Components.EntityComponent;
import OxyEngineEditor.Components.TransformComponent;
import org.joml.*;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.Globals.Globals.toPrimitiveInteger;

public class ModelFactory implements EntityComponent {

    private final List<Vector3f> verticesNonTransformed, normals;
    private final List<int[]> faces;
    private final List<Vector2f> textureCoords;

    public ModelFactory(List<Vector3f> verticesNonTransformed, List<Vector2f> textureCoords, List<Vector3f> normals,
                        List<int[]> faces) {
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

        OxyColor color = e.get(OxyColor.class);
        ImageTexture texture = e.get(ImageTexture.class);
        TransformComponent c = e.get(TransformComponent.class);

        c.transform = new Matrix4f()
                .translate(c.position)
                .rotateX(c.rotation.x)
                .rotateY(c.rotation.y)
                .rotateZ(c.rotation.z)
                .scale(c.scale);

        int slot = 0;
        if (texture != null)
            slot = texture.getTextureSlot();

        int vertPtr = 0;
        for (Vector3f v : verticesNonTransformed) {
            Vector4f transformed = new Vector4f(v, 1.0f).mul(c.transform);
            e.vertices[vertPtr++] = transformed.x;
            e.vertices[vertPtr++] = transformed.y;
            e.vertices[vertPtr++] = transformed.z;
            e.vertices[vertPtr++] = slot;
            if (color != null) {
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
            for (int i : face) {
                indicesArr.add(i);
            }
        }

        int tcsPtr = 0;
        for (Vector2f v : textureCoords) {
            e.tcs[tcsPtr++] = v.x;
            e.tcs[tcsPtr++] = v.y;
        }
        e.indices = toPrimitiveInteger(indicesArr);
    }
}