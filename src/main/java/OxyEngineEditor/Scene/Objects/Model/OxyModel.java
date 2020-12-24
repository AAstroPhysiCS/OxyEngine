package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.Components.*;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutProducer;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.Scene.SceneRuntime;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class OxyModel extends OxyEntity {

    public ModelFactory factory;

    public OxyModel(Scene scene) {
        super(scene);
    }

    public OxyModel(OxyModel other) {
        super(other.scene);
        this.tangents = other.tangents.clone();
        this.biTangents = other.biTangents.clone();
        this.originPos = new Vector3f(other.originPos);
        this.normals = other.normals.clone();
        this.vertices = other.vertices.clone();
        this.tcs = other.tcs.clone();
        this.indices = other.indices.clone();
    }

    @Override
    public OxyEntity copyMe() {
        OxyModel e = new OxyModel(this);
        e.addToScene();
        var boundingBox = get(BoundingBoxComponent.class);
        var transform = get(TransformComponent.class);
        e.importedFromFile = this.importedFromFile;
        e.factory = this.factory;
        e.addComponent(
                get(UUIDComponent.class),
                get(OxyShader.class),
                new BoundingBoxComponent(
                        boundingBox.min(),
                        boundingBox.max()
                ),
                new TransformComponent(new TransformComponent(new Vector3f(0, 0, 0), transform.rotation, transform.scale)),
                new MeshPosition(get(MeshPosition.class).meshPos()),
                new TagComponent(get(TagComponent.class).tag() == null ? "Unnamed" : get(TagComponent.class).tag()),
                new RenderableComponent(RenderingMode.Normal),
                new OxyMaterial(get(OxyMaterial.class)),
                new SelectedComponent(false)
        );

        //SCRIPTS (with GUINode-Script)
        for (OxyScript s : this.getScripts()) e.addScript(new OxyScript(s.getPath()));

        //adding all the parent gui nodes (except OxyScript, bcs that gui node is instance dependent)
        e.getGUINodes().addAll(this.getGUINodes());
        var iterator = e.getGUINodes().iterator();
        for (OxyScript s : this.getScripts()) {
            while (iterator.hasNext()) {
                var guiNode = iterator.next();
                if (s.guiNode.equals(guiNode)) {
                    iterator.remove();
                    break;
                }
            }
        }

        SceneRuntime.onCreate();
        SceneRuntime.stop();

        e.initData(get(OpenGLMesh.class).getPath());
        return e;
    }

    @Override
    public void initData(String path) {
        assert factory != null : oxyAssert("Models should have a Model Template");
        translatePos();
        factory.constructData(this);
        addComponent(new ModelMeshOpenGL(path, get(OxyShader.class), GL_TRIANGLES, BufferLayoutProducer.Usage.DYNAMIC, vertices, indices, tcs, normals, tangents, biTangents));
    }

    @Override
    public void constructData() {
        translatePos();
        if (factory == null) return;
        factory.constructData(this);
        if (has(OpenGLMesh.class)) get(OpenGLMesh.class).updateSingleEntityData(0, vertices);
    }

    @Override
    public void updateData() {
        translatePos();
        if (factory == null) return;
        factory.updateData(this);
        if (has(OpenGLMesh.class)) get(OpenGLMesh.class).updateSingleEntityData(0, vertices);
    }

    private void translatePos() {
        TransformComponent c = get(TransformComponent.class);
        c.transform = new Matrix4f()
                .translate(c.position)
                .rotateX(c.rotation.x)
                .rotateY(c.rotation.y)
                .rotateZ(c.rotation.z)
                .scale(c.scale);
    }
}