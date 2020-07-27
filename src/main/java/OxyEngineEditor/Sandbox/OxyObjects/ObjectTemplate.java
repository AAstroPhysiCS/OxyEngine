package OxyEngineEditor.Sandbox.OxyObjects;

import OxyEngine.Core.Renderer.Buffer.Mesh;

public interface ObjectTemplate {
    void constructData(OxyEntity e);

    void initData(OxyEntity e, Mesh mesh);
}
