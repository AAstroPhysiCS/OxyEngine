package OxyEngineEditor.Scene.Objects.Native;

import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;

public abstract class NativeObjectFactory{

    protected int vertexSize;

    public abstract void constructData(OxyNativeObject e, int size);

    public abstract void initData(OxyNativeObject e, NativeObjectMeshOpenGL mesh);

    public int getVertexSize() {
        return vertexSize;
    }
}

