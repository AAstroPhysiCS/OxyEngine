package OxyEngineEditor.Sandbox.OxyObjects;

import OxyEngine.Core.Renderer.Buffer.Mesh;

public abstract class ObjectTemplate {

    public GameObjectType type;

    public abstract void constructData(OxyEntity e);

    public abstract void initData(OxyEntity e, Mesh mesh);
}
