package OxyEngineEditor.Sandbox.Scene.InternObjects;

import OxyEngineEditor.Sandbox.OxyComponents.InternObjectMesh;

public class GridFactory extends InternObjectFactory {

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
    public void initData(OxyInternObject e, InternObjectMesh mesh) {
        e.indices = new int[]{
                mesh.indicesX, 1 + mesh.indicesY, 3 + mesh.indicesZ,
                3 + mesh.indicesX, mesh.indicesY, 2 + mesh.indicesZ,
        };
        mesh.indicesX += 4;
        mesh.indicesY += 4;
        mesh.indicesZ += 4;
    }
}
