package OxyEngineEditor.Scene.NativeObjects;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Components.NativeObjectMesh;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

import static OxyEngine.System.OxySystem.oxyAssert;

public class OxyNativeObject extends OxyEntity {

    public ObjectType type;

    public OxyNativeObject(Scene scene) {
        super(scene);
    }

    @Override
    public void initData(String path) {
        assert has(NativeObjectFactory.class) && has(Mesh.class) : oxyAssert("Game object need to have a template or a Mesh!");

        Mesh mesh = get(Mesh.class);

        NativeObjectFactory factory = get(NativeObjectFactory.class);
        this.type = factory.type;
        factory.constructData(this);
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