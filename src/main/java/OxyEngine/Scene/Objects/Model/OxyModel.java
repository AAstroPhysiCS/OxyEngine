package OxyEngine.Scene.Objects.Model;

import OxyEngine.Components.*;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutProducer;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.Scene;
import OxyEngine.Scene.SceneRuntime;
import OxyEngine.Scripting.OxyScript;

import java.util.UUID;
import java.util.stream.Collectors;

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
        if (other.tangents != null) this.tangents = other.tangents.clone();
        if (other.biTangents != null) this.biTangents = other.biTangents.clone();
        if (other.normals != null) this.normals = other.normals.clone();
        if (other.vertices != null) this.vertices = other.vertices.clone();
        if (other.tcs != null) this.tcs = other.tcs.clone();
        if (other.indices != null) this.indices = other.indices.clone();
        this.objectID = id;
    }

    @Override
    public OxyEntity copyMe() {
        OxyModel e = new OxyModel(this, ++Scene.OBJECT_ID_COUNTER);
        e.addToScene();
        e.importedFromFile = this.importedFromFile;
        e.factory = this.factory;
        if (this.has(BoundingBoxComponent.class)) {
            var boundingBox = get(BoundingBoxComponent.class);
            e.addComponent(new BoundingBoxComponent(boundingBox.min(), boundingBox.max()));
        }

        e.addComponent(
                new UUIDComponent(UUID.randomUUID()),
                get(OxyShader.class),
                new TransformComponent(this.get(TransformComponent.class)),
                new MeshPosition(get(MeshPosition.class).meshPos()),
                new TagComponent(get(TagComponent.class).tag() == null ? "Unnamed" : get(TagComponent.class).tag()),
                new RenderableComponent(RenderingMode.Normal),
                get(OxyMaterialIndex.class),
                new SelectedComponent(false)
        );

        e.setFamily(new EntityFamily(this.getFamily().root()));
        /*for (OxyEntity child : getEntitiesRelatedTo()) {
            child.copyMe();
            child.setFamily(new EntityFamily(e.getFamily()));
        }*/

        if (this.has(PointLight.class)) e.addComponent(this.get(PointLight.class));
        if (this.has(DirectionalLight.class)) e.addComponent(this.get(DirectionalLight.class));

        //SCRIPTS (with GUINode-Script)
        for (OxyScript s : this.getScripts()) e.addScript(new OxyScript(s.getPath()));

        //adding all the parent gui nodes (except OxyScript, bcs that gui node is instance dependent)
        e.getGUINodes().addAll(this.getGUINodes().stream().filter(c -> c instanceof OxyScript).collect(Collectors.toList()));

        SceneRuntime.stop();

        if (has(OpenGLMesh.class)) e.initData(get(OpenGLMesh.class).getPath());
        return e;
    }

    public void initData(String meshPath) {
        assert factory != null : oxyAssert("Models should have a Model Template");
        transformLocally();
        factory.constructData(this);
        addComponent(new ModelMeshOpenGL(meshPath, GL_TRIANGLES, BufferLayoutProducer.Usage.DYNAMIC, vertices, indices, tcs, normals, tangents, biTangents));
    }

    @Override
    public void constructData() {
        transformLocally();
        if (factory == null) return;
        factory.constructData(this);
        if (has(OpenGLMesh.class)) get(OpenGLMesh.class).updateSingleEntityData(0, vertices);
    }
}