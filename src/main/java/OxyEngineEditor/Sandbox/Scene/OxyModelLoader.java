package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class OxyModelLoader {
    final List<Vector3f> vertices = new ArrayList<>();
    final List<Vector2f> textureCoords = new ArrayList<>();
    final List<Vector3f> normals = new ArrayList<>();
    final List<int[]> faces = new ArrayList<>();
    final List<Mesh> meshes = new ArrayList<>();

    String objPath, mtlPath;

    abstract void processData();

    protected abstract void fillIn(String content);
}
