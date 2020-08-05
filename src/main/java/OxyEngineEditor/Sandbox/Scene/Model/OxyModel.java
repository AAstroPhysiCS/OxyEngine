package OxyEngineEditor.Sandbox.Scene.Model;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Sandbox.OxyComponents.ModelMesh;
import OxyEngineEditor.Sandbox.Scene.ModelFactory;
import OxyEngineEditor.Sandbox.Scene.ObjectType;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Scene;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class OxyModel extends OxyEntity {

    private ModelFactory template;

    public OxyModel(Scene scene) {
        super(scene);
        this.type = ObjectType.Model;
    }

    public OxyModel(OxyModel other) {
        this(other.scene);
        this.template = other.template;
        this.vertices = other.vertices;
        this.tcs = other.tcs;
        this.indices = other.indices;
        this.normals = other.normals;
        this.type = other.type;
    }

    @Override
    public void initData() {
        if (!has(ModelFactory.class)) throw new IllegalStateException("Models should have a Model Template");
        template = (ModelFactory) get(ModelFactory.class);
        template.constructData(this);
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
        template.constructData(this);
        ((Mesh) get(Mesh.class)).updateSingleEntityData(0, vertices);
    }
}