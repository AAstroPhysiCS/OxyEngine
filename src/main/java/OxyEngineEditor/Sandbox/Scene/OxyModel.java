package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Sandbox.OxyComponents.ModelMesh;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class OxyModel extends OxyEntity {

    private ModelTemplate template;

    OxyModel(Scene scene) {
        super(scene);
        this.type = ObjectType.Model;
    }

    OxyModel(OxyModel other) {
        this(other.scene);
        this.template = other.template;
        this.vertices = other.vertices;
        this.tcs = other.tcs;
        this.indices = other.indices;
        this.normals = other.normals;
        this.type = other.type;
    }

    @Override
    void initData() {
        if (!has(ModelTemplate.class)) throw new IllegalStateException("Models should have a Model Template");
        template = (ModelTemplate) get(ModelTemplate.class);
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