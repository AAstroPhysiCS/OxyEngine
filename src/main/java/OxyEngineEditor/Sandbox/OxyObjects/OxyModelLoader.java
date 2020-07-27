package OxyEngineEditor.Sandbox.OxyObjects;

import OxyEngine.System.OxySystem;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.Scene;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.Globals.Globals.toPrimitiveInteger;

public class OxyModelLoader {

    private static final List<Vector3f> vertices = new ArrayList<>();
    private static final List<Vector2f> textureCoords = new ArrayList<>();
    private static final List<Vector3f> normals = new ArrayList<>();
    private static final List<int[]> faces = new ArrayList<>();

    private static ModelSpec spec;

    private static Scene scene;

    public static OxyEntity load(Scene scene, ModelSpec spec) {
        OxyModelLoader.spec = spec;
        OxyModelLoader.scene = scene;

        OxyEntity oxyModel = processData();

        textureCoords.clear();
        normals.clear();
        faces.clear();
        return oxyModel;
    }

    private static OxyEntity processData() {
        return fillIn(OxySystem.FileSystem.load(spec.getObjName()));
    }

    private static OxyEntity fillIn(String content) {
        String[] splitted = content.split("\n");
        for (String s : splitted) {
            if (s.startsWith("v ")) getValues3f(s, vertices);
            if (s.startsWith("vt ")) getValues2f(s, textureCoords);
            if (s.startsWith("vn ")) getValues3f(s, normals);
            if (s.startsWith("f ")) getFaces(s);
        }
        return constructData();
    }

    private static void getValues3f(String s, List<Vector3f> list) {
        String[] splitted = s.split(" ");
        for (int i = 1; i < splitted.length; )
            list.add(new Vector3f(Float.parseFloat(splitted[i++]), Float.parseFloat(splitted[i++]), Float.parseFloat(splitted[i++])));
    }

    private static void getValues2f(String s, List<Vector2f> list) {
        String[] splitted = s.split(" ");
        for (int i = 1; i < splitted.length; )
            list.add(new Vector2f(Float.parseFloat(splitted[i++]), Float.parseFloat(splitted[i++])));
    }

    private static void getFaces(String s) {
        List<Integer> vertexCollection = new ArrayList<>();
        String[] splitted = s.split(" ");
        for (int i = 1; i < splitted.length; i++) {
            String[] vertex = splitted[i].split("/");
            for (String value : vertex) {
                vertexCollection.add(Integer.valueOf(value));
            }
            faces.add(toPrimitiveInteger(vertexCollection));
            vertexCollection.clear();
        }
    }

    public static OxyEntity constructData() {

        float[] verticesArr = new float[vertices.size() * 4];
        float[] normalsArr = new float[vertices.size() * 3];
        float[] textureCoordsArr = new float[vertices.size() * 2];
        List<Integer> indicesArr = new ArrayList<>();

        Matrix4f transform = new Matrix4f()
                .scale(spec.getScale())
                .translate(spec.getPosition())
                .rotateX((float) Math.toRadians(spec.getRotation().x))
                .rotateY((float) Math.toRadians(spec.getRotation().y))
                .rotateZ((float) Math.toRadians(spec.getRotation().z));

        int slot = 0;
        if (spec.getTexture() != null) slot = spec.getTexture().getTextureSlot();

        int vertPtr = 0;
        for (Vector3f v : vertices) {
            Vector4f transformed = new Vector4f(v, 1.0f).mul(transform);
            verticesArr[vertPtr++] = transformed.x;
            verticesArr[vertPtr++] = transformed.y;
            verticesArr[vertPtr++] = transformed.z;
            verticesArr[vertPtr++] = slot;
        }

        int nPtr = 0;
        for (Vector3f n : normals) {
            normalsArr[nPtr++] = n.x;
            normalsArr[nPtr++] = n.y;
            normalsArr[nPtr++] = n.z;
        }

        for (int[] face : faces) {
            int vertexPtr = face[0] - 1;
            indicesArr.add(vertexPtr);

            Vector2f textureCoords2f = textureCoords.get(face[1] - 1);
            textureCoordsArr[vertexPtr * 2] = textureCoords2f.x;
            textureCoordsArr[vertexPtr * 2 + 1] = 1 - textureCoords2f.y;

            Vector3f normals3f = normals.get(face[2] - 1);
            normalsArr[vertexPtr * 3] = normals3f.x;
            normalsArr[vertexPtr * 3 + 1] = normals3f.y;
            normalsArr[vertexPtr * 3 + 2] = normals3f.z;
        }

        OxyEntity e = scene.createEntity(new ModelTemplate(vertices));
        e.vertices = verticesArr;
        e.indices = toPrimitiveInteger(indicesArr);
        e.tcs = textureCoordsArr;
        e.normals = normalsArr;

        if (spec.getColor() == null) e.addComponent(spec.getTexture());
        else e.addComponent(spec.getColor());
        e.addComponent(new TransformComponent(spec.getPosition(), spec.getRotation(), spec.getScale()));
        return e;
    }
}
