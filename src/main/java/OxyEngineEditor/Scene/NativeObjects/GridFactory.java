package OxyEngineEditor.Scene.NativeObjects;

import OxyEngineEditor.Components.NativeObjectMesh;

public class GridFactory extends NativeObjectFactory {

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
    public void initData(OxyNativeObject e, NativeObjectMesh mesh) {
        e.indices = new int[]{
                mesh.indicesX, 1 + mesh.indicesY, 3 + mesh.indicesZ,
                3 + mesh.indicesX, mesh.indicesY, 2 + mesh.indicesZ,
        };
        mesh.indicesX += 4;
        mesh.indicesY += 4;
        mesh.indicesZ += 4;
    }
}
