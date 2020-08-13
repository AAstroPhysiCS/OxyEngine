package OxyEngineEditor.Sandbox.Scene.Model;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Sandbox.OxyComponents.ModelMesh;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Scene;

import static OxyEngine.System.OxySystem.oxyAssert;
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
        assert has(ModelFactory.class) : oxyAssert("Models should have a Model Template");
        factory = get(ModelFactory.class);
        factory.constructData(this);
        addComponent(new ModelMesh.ModelMeshBuilderImpl()
                .setShader(get(OxyShader.class))
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
        //TODO: SHOULD NOT BE RECALCULATED EVERY FRAME!
        factory.constructData(this);
        get(Mesh.class).updateSingleEntityData(0, vertices);
    }


    public String getName() {
        return name;
    }
}