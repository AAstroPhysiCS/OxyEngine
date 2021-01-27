package OxyEngineEditor.Scene.Objects.Native;

import OxyEngine.Components.RenderableComponent;
import OxyEngine.Components.RenderingMode;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Components.UUIDComponent;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

import java.util.UUID;

import static OxyEngine.System.OxySystem.oxyAssert;

public class OxyNativeObject extends OxyEntity {

    final int size;
    public NativeObjectFactory factory;

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
        assert has(OpenGLMesh.class) : oxyAssert("Game object need to have a template and a Mesh!");

        OpenGLMesh mesh = get(OpenGLMesh.class);

        factory.constructData(this, size);
        assert mesh instanceof NativeObjectMeshOpenGL : oxyAssert("Native Object needs to have a NativeObjectMesh");
        factory.initData(this, (NativeObjectMeshOpenGL) mesh);
    }

    @Override
    public void constructData() {
        factory.constructData(this, size);
    }

    public void setFactory(NativeObjectFactory factory) {
        this.factory = factory;
    }

    public int getSize() {
        return size;
    }

    @Override
    public void updateData() {
        constructData();
    }
}