package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Components.ModelMesh;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class OxyModel extends OxyEntity {

    private ModelFactory factory;

    public OxyModel(Scene scene) {
        super(scene);
    }

    @Override
    public void initData(String path) {
        assert has(ModelFactory.class) : oxyAssert("Models should have a Model Template");
        factory = get(ModelFactory.class);
        factory.constructData(this);
        addComponent(new ModelMesh.ModelMeshBuilderImpl()
                .setPath(path)
                .setShader(get(OxyShader.class))
                .setMode(GL_TRIANGLES)
                .setUsage(BufferTemplate.Usage.DYNAMIC)
                .setVertices(vertices)
                .setIndices(indices)
                .setTextureCoords(tcs)
                .setNormals(normals)
                .setTangents(tangents)
                .setBiTangents(biTangents)
                .create());
    }

    @Override
    public void constructData() {
        factory.constructData(this);
        get(Mesh.class).updateSingleEntityData(0, vertices);
    }

    @Override
    public void updateData() {
        factory.updateData(this);
        get(Mesh.class).updateSingleEntityData(0, vertices);
    }
}