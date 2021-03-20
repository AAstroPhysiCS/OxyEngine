package OxyEngine.Scene.Objects.Native;

import OxyEngine.Components.RenderableComponent;
import OxyEngine.Components.RenderingMode;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Components.UUIDComponent;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.Mesh.OxyVertex;
import OxyEngine.Scene.Objects.Model.DefaultModelType;
import OxyEngine.Scene.Objects.Importer.ImporterType;
import OxyEngine.Scene.Objects.Importer.OxyModelImporter;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.Scene;
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
        initData();
    }

    public void pushVertexData(DefaultModelType type){
        OxyModelImporter loader = new OxyModelImporter(type.getPath(), ImporterType.MeshImporter); //JUST IMPORTING MESH FOR NOW
        List<OxyVertex> modelVertices = loader.getVertexList(0);
        vertices = new float[modelVertices.size() * 3];
        int vertPtr = 0;
        TransformComponent c = get(TransformComponent.class);
        for (OxyVertex o : modelVertices) {
            Vector4f transformed = new Vector4f(o.vertices, 1.0f).mul(c.transform);
            vertices[vertPtr++] = transformed.x;
            vertices[vertPtr++] = transformed.y;
            vertices[vertPtr++] = transformed.z;
        }
        initData();
    }

    @Override
    public OxyEntity copyMe() {
        OxyNativeObject e = new OxyNativeObject(scene, size);
        e.addToScene();
        e.addComponent(new UUIDComponent(UUID.randomUUID()), new TransformComponent(), new RenderableComponent(RenderingMode.Normal));
        return e;
    }

    public void initData() {
        assert has(OpenGLMesh.class) : oxyAssert("Game object need to have a template and a Mesh!");

        OpenGLMesh mesh = get(OpenGLMesh.class);

        factory.constructData(this, size);
        assert mesh instanceof NativeObjectMeshOpenGL : oxyAssert("Native Object needs to have a NativeObjectMesh");
        factory.initData(this, (NativeObjectMeshOpenGL) mesh);
    }

    @Override
    public void constructData() {
        if(factory == null) return;
        factory.constructData(this, size);
    }

    public void setFactory(NativeObjectFactory factory) {
        this.factory = factory;
    }

    public int getSize() {
        return size;
    }
}