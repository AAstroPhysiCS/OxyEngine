package OxyEngine.Scene.Objects.Model;

import OxyEngine.Components.*;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Renderer.Mesh.OxyVertex;
import OxyEngine.Core.Renderer.OxyRenderPass;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.PhysX.OxyPhysXComponent;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.Scene;
import OxyEngine.Scene.SceneRenderer;
import OxyEngine.Scene.SceneRuntime;
import OxyEngine.Scripting.OxyScript;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static OxyEngine.Utils.toPrimitiveInteger;

public class OxyModel extends OxyEntity {

    private List<OxyVertex> vertexList;
    private List<int[]> faces;

    public OxyModel(Scene scene, int id, List<OxyVertex> vertexList, List<int[]> faces) {
        super(scene);
        this.objectID = id;
        this.vertexList = vertexList;
        this.faces = faces;
    }

    public OxyModel(Scene scene, int id){
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
        if (this.has(BoundingBoxComponent.class)) {
            var boundingBox = get(BoundingBoxComponent.class);
            e.addComponent(new BoundingBoxComponent(boundingBox.min(), boundingBox.max()));
        }

        e.addComponent(
                new UUIDComponent(UUID.randomUUID()),
                new TransformComponent(this.get(TransformComponent.class)),
                new MeshPosition(get(MeshPosition.class).meshPos()),
                new TagComponent(get(TagComponent.class).tag() == null ? "Unnamed" : get(TagComponent.class).tag()),
                new RenderableComponent(RenderingMode.Normal),
                get(OxyMaterialIndex.class),
                new SelectedComponent(false)
        );

        if (this.has(OxyPhysXComponent.class)) {
            OxyPhysXComponent physXComponent = new OxyPhysXComponent(this.get(OxyPhysXComponent.class), e);
            e.addComponent(physXComponent);
            physXComponent.getGeometry().build();
            physXComponent.getActor().build();
        }

        e.setFamily(new EntityFamily(this.getFamily().root()));

        if (this.has(PointLight.class)) e.addComponent(this.get(PointLight.class));
        if (this.has(DirectionalLight.class)) e.addComponent(this.get(DirectionalLight.class));
        if (this.has(AnimationComponent.class))
            e.addComponent(new AnimationComponent(this.get(AnimationComponent.class)));

        //SCRIPTS (with GUINode-Script)
        for (OxyScript s : this.getScripts()) e.addScript(new OxyScript(s.getPath()));

        //adding all the parent gui nodes (except OxyScript, bcs that gui node is instance dependent)
        e.getGUINodes().addAll(this.getGUINodes().stream().filter(c -> !(c instanceof OxyScript)).collect(Collectors.toList()));

        SceneRuntime.stop();

        if (has(OpenGLMesh.class)) e.initMesh(get(OpenGLMesh.class).getPath());

        copyChildRecursive(e);

        return e;
    }

    private void copyChildRecursive(OxyEntity parent) {
        for (OxyEntity child : getEntitiesRelatedTo()) {
            OxyEntity copy = child.copyMe();
            copy.setFamily(new EntityFamily(parent.getFamily()));
        }
    }

    public void initMesh(String meshPath) {
        transformLocally();
        construct();
        OxyPipeline geometryPipeline = SceneRenderer.getInstance().getGeometryPipeline();
        OxyRenderPass geometryRenderPass = geometryPipeline.getRenderPass();
        addComponent(new ModelMeshOpenGL(geometryPipeline, meshPath, geometryRenderPass.getMeshRenderingMode(),
                vertices, indices, tcs, normals, tangents, biTangents));
        vertexList.clear();
        faces.clear();
    }

    @Override
    public void updateData() {
        transformLocally();
        construct();
        if (has(OpenGLMesh.class)) get(OpenGLMesh.class).updateSingleEntityData(0, vertices);
    }

    private void construct(){
        OxyModel e = this;
        e.vertices = new float[vertexList.size() * 12];
        e.normals = new float[vertexList.size() * 3];
        e.tcs = new float[vertexList.size() * 2];
        e.tangents = new float[vertexList.size() * 3];
        e.biTangents = new float[vertexList.size() * 3];
        List<Integer> indicesArr = new ArrayList<>();

        int vertPtr = 0;
        int nPtr = 0;
        int tcsPtr = 0;
        int tangentPtr = 0;
        int biTangentPtr = 0;
        for (OxyVertex o : vertexList) {
            e.vertices[vertPtr++] = o.vertices.x;
            e.vertices[vertPtr++] = o.vertices.y;
            e.vertices[vertPtr++] = o.vertices.z;
            e.vertices[vertPtr++] = e.getObjectId();

            e.vertices[vertPtr++] = o.m_BoneIDs[0];
            e.vertices[vertPtr++] = o.m_BoneIDs[1];
            e.vertices[vertPtr++] = o.m_BoneIDs[2];
            e.vertices[vertPtr++] = o.m_BoneIDs[3];

            e.vertices[vertPtr++] = o.m_Weights[0];
            e.vertices[vertPtr++] = o.m_Weights[1];
            e.vertices[vertPtr++] = o.m_Weights[2];
            e.vertices[vertPtr++] = o.m_Weights[3];

            Vector3f normals = o.normals;
            e.normals[nPtr++] = normals.x;
            e.normals[nPtr++] = normals.y;
            e.normals[nPtr++] = normals.z;

            Vector2f textureCoords = o.textureCoords;
            e.tcs[tcsPtr++] = textureCoords.x;
            e.tcs[tcsPtr++] = textureCoords.y;

            Vector3f tangents = o.tangents;
            e.tangents[tangentPtr++] = tangents.x;
            e.tangents[tangentPtr++] = tangents.y;
            e.tangents[tangentPtr++] = tangents.z;

            Vector3f biTangents = o.biTangents;
            e.biTangents[biTangentPtr++] = biTangents.x;
            e.biTangents[biTangentPtr++] = biTangents.y;
            e.biTangents[biTangentPtr++] = biTangents.z;
        }

        for (int[] face : faces) {
            for (int i : face) {
                indicesArr.add(i);
            }
        }
        e.indices = toPrimitiveInteger(indicesArr);
    }
}