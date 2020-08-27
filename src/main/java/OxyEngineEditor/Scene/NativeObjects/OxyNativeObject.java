package OxyEngineEditor.Scene.NativeObjects;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Components.NativeObjectMesh;
import OxyEngineEditor.Components.RenderableComponent;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

import static OxyEngine.System.OxySystem.oxyAssert;

public class OxyNativeObject extends OxyEntity implements Cloneable {

    private NativeObjectFactory factory;
    public ObjectType type;

    public OxyNativeObject(Scene scene) {
        super(scene);
    }

    public void initData() {
        assert has(NativeObjectFactory.class) && has(Mesh.class) : oxyAssert("Game object need to have a template or a Mesh!");

        Mesh mesh = get(Mesh.class);

        factory = get(NativeObjectFactory.class);
        this.type = factory.type;
        factory.constructData(this);
        assert mesh instanceof NativeObjectMesh : oxyAssert("Native Object needs to have a NativeObjectMesh");
        factory.initData(this, (NativeObjectMesh) mesh);
    }

    @Override
    public void updateData() {
        get(Mesh.class).renderableComponent.noZBufferRendering = get(RenderableComponent.class).noZBufferRendering;
        get(Mesh.class).renderableComponent.renderable = get(RenderableComponent.class).renderable;
        factory.constructData(this);
        get(Mesh.class).updateSingleEntityData(scene, this);
    }

    public ObjectType getType() {
        return type;
    }
}