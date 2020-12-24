package OxyEngineEditor.Scene.Objects.Native;

import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;

public class GridFactory extends NativeObjectFactory {

    private int indicesPtr = 0;

    public GridFactory() {
        type = ObjectType.Grid;
        vertexPos = new float[]{
                -0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
        };
    }

    @Override
    public void initData(OxyNativeObject e, NativeObjectMeshOpenGL mesh) {
        int[] indices = new int[]{
                mesh.indicesX, 1 + mesh.indicesY, 3 + mesh.indicesZ,
                3 + mesh.indicesX, mesh.indicesY, 2 + mesh.indicesZ,
        };
        if(e.indices == null) e.indices = new int[indices.length * e.size];
        for (int index : indices) {
            e.indices[indicesPtr++] = index;
        }
        mesh.indicesX += 4;
        mesh.indicesY += 4;
        mesh.indicesZ += 4;
    }
}
