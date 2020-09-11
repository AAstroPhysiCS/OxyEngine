package OxyEngineEditor.Scene.Objects.Native;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Components.NativeObjectMesh;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

import static OxyEngine.System.OxySystem.oxyAssert;

public class OxyNativeObject extends OxyEntity {

    public ObjectType type;
    final int size;

    public OxyNativeObject(Scene scene, int size) {
        super(scene);
        this.size = size;
    }

    public void pushVertexData(TransformComponent t) {
        TransformComponent tOld = get(TransformComponent.class);
        tOld.set(t);
        initData(null);
    }

    @Override
    public void initData(String path) {
        assert has(NativeObjectFactory.class) && has(Mesh.class) : oxyAssert("Game object need to have a template and a Mesh!");

        Mesh mesh = get(Mesh.class);
        NativeObjectFactory factory = get(NativeObjectFactory.class);

        this.type = factory.type;
        factory.constructData(this, size);
        assert mesh instanceof NativeObjectMesh : oxyAssert("Native Object needs to have a NativeObjectMesh");
        factory.initData(this, (NativeObjectMesh) mesh);
    }

    @Override
    public void constructData() {
        throw new NullPointerException("CONSTRUCT DATA NOT IMPLEMENTED");
    }

    @Override
    public void updateData() {
        throw new NullPointerException("UPDATE DATA NOT IMPLEMENTED");
    }

    public ObjectType getType() {
        return type;
    }
}