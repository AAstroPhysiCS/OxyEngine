package OxyEngine.Scene.Objects.Native;

import OxyEngine.Components.RenderableComponent;
import OxyEngine.Components.RenderingMode;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Components.UUIDComponent;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Scene.Objects.Model.ModelType;
import OxyEngine.Scene.Objects.Model.OxyModelLoader;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.Scene;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
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

    public void pushVertexData(ModelType type){
        OxyModelLoader loader = new OxyModelLoader(type.getPath());
        List<Vector3f> modelVertices = loader.meshes.get(0).vertices;
        vertices = new float[modelVertices.size() * 3];
        int vertPtr = 0;
        TransformComponent c = get(TransformComponent.class);
        for (Vector3f v : modelVertices) {
            Vector4f transformed = new Vector4f(v, 1.0f).mul(c.transform);
            vertices[vertPtr++] = transformed.x;
            vertices[vertPtr++] = transformed.y;
            vertices[vertPtr++] = transformed.z;
        }
        initData(type.getPath());
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
    public void updateVertexData() {
        constructData();
    }
}