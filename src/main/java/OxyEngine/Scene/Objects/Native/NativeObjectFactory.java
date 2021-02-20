package OxyEngine.Scene.Objects.Native;

import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;

public interface NativeObjectFactory {

    void constructData(OxyNativeObject e, int size);

    void initData(OxyNativeObject e, NativeObjectMeshOpenGL mesh);
}

