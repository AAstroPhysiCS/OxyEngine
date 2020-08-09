package OxyEngineEditor.Sandbox.Scene.Model;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Sandbox.OxyComponents.ModelMesh;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Scene;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class OxyModel extends OxyEntity {

    private ModelFactory factory;
    private ModelType type;

    public OxyModel(Scene scene) {
        super(scene);
    }

    public OxyModel(OxyModel other) {
        this(other.scene);
        this.factory = other.factory;
        this.vertices = other.vertices;
        this.tcs = other.tcs;
        this.indices = other.indices;
        this.normals = other.normals;
        this.type = other.type;
        this.name = other.name;
    }

    @Override
    public void initData() {
        if (!has(ModelFactory.class)) throw new IllegalStateException("Models should have a Model Template");
        factory = (ModelFactory) get(ModelFactory.class);
        factory.constructData(this);
        addComponent(new ModelMesh.ModelMeshBuilderImpl()
                .setMode(GL_TRIANGLES)
                .setUsage(BufferTemplate.Usage.DYNAMIC)
                .setVertices(vertices)
                .setIndices(indices)
                .setTextureCoords(tcs)
                .setNormals(normals)
                .create());
    }

    @Override
    public void updateData() {
        factory.constructData(this);
        ((Mesh) get(Mesh.class)).updateSingleEntityData(0, vertices);
    }

    public String getName(){
        return name;
    }
}