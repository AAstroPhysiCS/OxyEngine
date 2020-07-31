package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.System.OxySystem;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.Globals.Globals.toPrimitiveInteger;

public class OxyModelLoader {

    final List<Vector3f> vertices = new ArrayList<>();
    final List<Vector2f> textureCoords = new ArrayList<>();
    final List<Vector3f> normals = new ArrayList<>();
    final List<int[]> faces = new ArrayList<>();

    final String objPath;

    OxyModelLoader(String path){
        this.objPath = path;
        processData();
    }

    private void processData() {
        fillIn(OxySystem.FileSystem.load(objPath));
    }

    private void fillIn(String content) {
        String[] splitted = content.split("\n");
        for (String s : splitted) {
            if (s.startsWith("v ")) getValues3f(s, vertices);
            if (s.startsWith("vt ")) getValues2f(s, textureCoords);
            if (s.startsWith("vn ")) getValues3f(s, normals);
            if (s.startsWith("f ")) getFaces(s);
        }
    }

    private void getValues3f(String s, List<Vector3f> list) {
        String[] splitted = s.split(" ");
        for (int i = 1; i < splitted.length; )
            list.add(new Vector3f(Float.parseFloat(splitted[i++]), Float.parseFloat(splitted[i++]), Float.parseFloat(splitted[i++])));
    }

    private void getValues2f(String s, List<Vector2f> list) {
        String[] splitted = s.split(" ");
        for (int i = 1; i < splitted.length; )
            list.add(new Vector2f(Float.parseFloat(splitted[i++]), Float.parseFloat(splitted[i++])));
    }

    private void getFaces(String s) {
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
}
