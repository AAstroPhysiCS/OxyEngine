package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.Components.*;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutProducer;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.Scene.SceneRuntime;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class OxyModel extends OxyEntity {

    public ModelFactory factory;

    public OxyModel(Scene scene, int id) {
        super(scene);
        this.objectID = id;
    }

    public OxyModel(OxyModel other, int id) {
        super(other.scene);
        if(other.tangents != null) this.tangents = other.tangents.clone();
        if(other.biTangents != null) this.biTangents = other.biTangents.clone();
        if(other.normals != null) this.normals = other.normals.clone();
        if(other.vertices != null) this.vertices = other.vertices.clone();
        if(other.tcs != null) this.tcs = other.tcs.clone();
        if(other.indices != null) this.indices = other.indices.clone();
        this.objectID = id;
    }

    @Override
    public OxyEntity copyMe() {
        OxyModel e = new OxyModel(this, ++Scene.OBJECT_ID_COUNTER);
        e.addToScene();
        e.importedFromFile = this.importedFromFile;
        e.factory = this.factory;
        if(this.has(BoundingBoxComponent.class)){
            var boundingBox = get(BoundingBoxComponent.class);
            e.addComponent(new BoundingBoxComponent(boundingBox.min(), boundingBox.max()));
        }
        e.addComponent(
                get(UUIDComponent.class),
                get(OxyShader.class),
                new TransformComponent(new TransformComponent(this.get(TransformComponent.class))),
                new MeshPosition(get(MeshPosition.class).meshPos()),
                new TagComponent(get(TagComponent.class).tag() == null ? "Unnamed" : get(TagComponent.class).tag()),
                new RenderableComponent(RenderingMode.Normal),
                get(OxyMaterialIndex.class),
                new SelectedComponent(false)
        );

        if(this.has(FamilyComponent.class)) e.addComponent(this.get(FamilyComponent.class));
        if(this.has(PointLight.class)) e.addComponent(this.get(PointLight.class));
        if(this.has(DirectionalLight.class)) e.addComponent(this.get(DirectionalLight.class));

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

        if(has(OpenGLMesh.class)){
            e.transformLocally();
            e.initData(get(OpenGLMesh.class).getPath());
        }
        return e;
    }

    @Override
    public void initData(String path) {
        assert factory != null : oxyAssert("Models should have a Model Template");
        factory.constructData(this);
        addComponent(new ModelMeshOpenGL(path, GL_TRIANGLES, BufferLayoutProducer.Usage.DYNAMIC, vertices, indices, tcs, normals, tangents, biTangents));
    }

    @Override
    public void constructData() {
        transformLocally();
        if (factory == null) return;
        factory.constructData(this);
        if (has(OpenGLMesh.class)) get(OpenGLMesh.class).updateSingleEntityData(0, vertices);
    }

    @Override
    public void updateData() {
        if (factory == null) return;
        factory.updateData(this);
        if (has(OpenGLMesh.class)) get(OpenGLMesh.class).updateSingleEntityData(0, vertices);
    }
}