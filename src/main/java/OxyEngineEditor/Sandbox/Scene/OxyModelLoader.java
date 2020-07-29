package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.System.OxySystem;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.Globals.Globals.toPrimitiveInteger;

public class OxyModelLoader {

    private static final List<Vector3f> vertices = new ArrayList<>();
    private static final List<Vector2f> textureCoords = new ArrayList<>();
    private static final List<Vector3f> normals = new ArrayList<>();
    private static final List<int[]> faces = new ArrayList<>();

    private static String objPath;

    private static Scene scene;

    public static OxyModel load(Scene scene, String objPath) {
        OxyModelLoader.objPath = objPath;
        OxyModelLoader.scene = scene;

        OxyModel oxyModel = processData();
        oxyModel.initData(null);

        vertices.clear();
        textureCoords.clear();
        normals.clear();
        faces.clear();
        return oxyModel;
    }

    private static OxyModel processData() {
        return fillIn(OxySystem.FileSystem.load(objPath));
    }

    private static OxyModel fillIn(String content) {
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

    private static OxyModel constructData() {
        OxyModel e = scene.createModelEntity();
        e.addComponent(new ModelTemplate(vertices, textureCoords, normals, faces));
        return e;
    }
}
