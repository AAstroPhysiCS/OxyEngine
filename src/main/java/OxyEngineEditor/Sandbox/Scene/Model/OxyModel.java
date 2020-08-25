package OxyEngineEditor.Sandbox.Scene.Model;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Sandbox.Components.ModelMesh;
import OxyEngineEditor.Sandbox.Components.RenderableComponent;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Scene;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class OxyModel extends OxyEntity {

    private ModelFactory factory;

    public OxyModel(Scene scene) {
        super(scene);
    }

    @Override
    public void initData() {
        assert has(ModelFactory.class) : oxyAssert("Models should have a Model Template");
        factory = get(ModelFactory.class);
        factory.constructData(this);
        addComponent(new ModelMesh.ModelMeshBuilderImpl()
                .setShader(get(OxyShader.class))
                .setRenderableComponent(get(RenderableComponent.class))
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
        get(Mesh.class).renderableComponent.maskedRendering = get(RenderableComponent.class).maskedRendering;
        get(Mesh.class).renderableComponent.renderable = get(RenderableComponent.class).renderable;
        factory.constructData(this);
        get(Mesh.class).updateSingleEntityData(0, vertices);
    }
}