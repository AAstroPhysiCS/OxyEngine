package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Sandbox.OxyComponents.ModelMesh;

public class OxyModel extends OxyEntity {

    private ModelTemplate template;
    private ModelMesh mesh;

    OxyModel(Scene scene) {
        super(scene);
        this.type = ObjectType.Model;
    }

    OxyModel(OxyModel other) {
        this(other.scene);
        this.mesh = other.mesh;
        this.template = other.template;
        this.vertices = other.vertices;
        this.tcs = other.tcs;
        this.indices = other.indices;
        this.normals = other.normals;
        this.type = other.type;
    }

    @Override
    public void initData(Mesh mesh) {
        if(!has(ModelTemplate.class)) throw new IllegalStateException("Models should have a Model Template");

        template = (ModelTemplate) get(ModelTemplate.class);
        template.constructData(this);
        this.mesh = template.getMesh();
    }

    @Override
    public void updateData() {
        template.updateData(this);
    }

    public ModelMesh getMesh() {
        return mesh;
    }
}
