package OxyEngineEditor.Scene.Objects.Native;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.RenderingMode;
import OxyEngineEditor.Components.NativeObjectMesh;
import OxyEngineEditor.Components.RenderableComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Components.UUIDComponent;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.OxySerializable;
import OxyEngineEditor.Scene.Scene;

import java.util.UUID;

import static OxyEngine.System.OxySystem.oxyAssert;

@OxySerializable(info = """
        \t\tOxyNativeObject %s {
               \tID: %s
               \tMesh Position: %s
               \tName: %s
               \tGrouped: %s
               \tEmitting: %s
               \tPosition: X %s, Y %s, Z %s
               \tRotation: X %s, Y %s, Z %s
               \tScale: X %s, Y %s, Z %s
               \tBounds Min: X %s, Y %s, Z %s
               \tBounds Max: X %s, Y %s, Z %s
               \tColor: %s
               \tScripts: [%s]
               \tAlbedo Texture: %s
               \tNormal Map Texture: %s
               \tRoughness Map Texture: %s
               \tAO Map Texture: %s
               \tMetallic Map Texture: %s
               \tMesh: %s
            }"""
)
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
    public OxyEntity copyMe() {
        OxyNativeObject e = new OxyNativeObject(scene, size);
        e.addToScene();
        e.addComponent(new UUIDComponent(UUID.randomUUID()), new TransformComponent(), new RenderableComponent(RenderingMode.Normal));
        return e;
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