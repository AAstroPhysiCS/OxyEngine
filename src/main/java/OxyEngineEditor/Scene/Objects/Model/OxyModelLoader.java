package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.Components.FamilyComponent;
import OxyEngine.Components.TagComponent;
import OxyEngineEditor.Scene.OxyEntity;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static OxyEngine.Components.BoundingBoxComponent.calcPos;
import static OxyEngine.Components.BoundingBoxComponent.sort;
import static OxyEngine.System.OxySystem.logger;
import static OxyEngineEditor.EditorApplication.oxyShader;
import static OxyEngineEditor.Scene.SceneRuntime.ACTIVE_SCENE;
import static org.lwjgl.assimp.Assimp.*;

public class OxyModelLoader {

    public static class AssimpMesh {
        public final List<Vector3f> vertices = new ArrayList<>();
        public final List<Vector2f> textureCoords = new ArrayList<>();
        public final List<Vector3f> normals = new ArrayList<>();
        public final List<Vector3f> tangents = new ArrayList<>();
        public final List<Vector3f> biTangents = new ArrayList<>();
        public final List<int[]> faces = new ArrayList<>();

        public int materialIndex;
        public Vector3f pos = new Vector3f();

        public final String name;
        public Vector3f min = new Vector3f(), max = new Vector3f();
        public final OxyEntity rootEntity;

        public AssimpMesh(OxyEntity rootEntity, String name) {
            this.name = name;
            this.rootEntity = rootEntity;
        }
    }

    public static record AssimpMaterial(String name, String textPath, String textPathMetallic,
                                        String textPathRoughness, String textPathNormals,
                                        String textPathAO, Vector4f diffuse) {
    }

    public final List<AssimpMesh> meshes = new ArrayList<>();
    public final List<AssimpMaterial> materials = new ArrayList<>();

    final String objPath;
    AIScene aiScene;
    private String rootName;
    private OxyEntity rootEnt;

    public OxyModelLoader(String objPath) {
        this.objPath = objPath;
        processData();
    }

    public OxyModelLoader(String objPath, OxyEntity root) {
        this.objPath = objPath;
        this.rootEnt = root;
        processData();
    }

    void processData() {
        int flag = aiProcess_Triangulate
                | aiProcess_FixInfacingNormals
                | aiProcess_OptimizeMeshes
                | aiProcess_FlipUVs
                | aiProcess_CalcTangentSpace
                | aiProcess_JoinIdenticalVertices
                | aiProcess_FindInvalidData
                | aiProcess_FlipWindingOrder
//                | aiProcess_PreTransformVertices
                | aiProcess_ValidateDataStructure
                | aiProcess_GenBoundingBoxes;

        aiScene = aiImportFile(objPath, flag);
        if (aiScene == null) {
            logger.warning("Mesh is null");
            return;
        }

        rootName = aiScene.mRootNode().mName().dataString();
        if(rootEnt == null) {
            rootEnt = ACTIVE_SCENE.createEmptyModel(oxyShader);
            rootEnt.setRoot(true);
            rootEnt.addComponent(new TagComponent(rootName), new FamilyComponent());
        }

        for (int i = 0; i < aiScene.mNumMeshes(); i++) {
            AIMesh mesh = AIMesh.create(Objects.requireNonNull(aiScene.mMeshes()).get(i));
            AssimpMesh oxyMesh = new AssimpMesh(rootEnt, mesh.mName().dataString());
            oxyMesh.materialIndex = mesh.mMaterialIndex();
            processMesh(mesh, oxyMesh);
            AIAABB aiaabb = mesh.mAABB();
            oxyMesh.min = new Vector3f(aiaabb.mMin().x(), aiaabb.mMin().y(), aiaabb.mMin().z());
            oxyMesh.max = new Vector3f(aiaabb.mMax().x(), aiaabb.mMax().y(), aiaabb.mMax().z());
            float[][] sortedVertices = sort(oxyMesh);
            calcPos(oxyMesh, sortedVertices);
            //transforming to 0,0,0
            Matrix4f transform = new Matrix4f()
                    .translate(new Vector3f(oxyMesh.pos).negate());
            for (int j = 0; j < oxyMesh.vertices.size(); j++) {
                Vector3f vertices3f = oxyMesh.vertices.get(j);
                Vector4f t4f = new Vector4f(vertices3f, 1.0f).mul(transform);
                oxyMesh.vertices.get(j).set(t4f.x, t4f.y, t4f.z);
            }
            this.meshes.add(oxyMesh);
        }

        for (int i = 0; i < aiScene.mNumMaterials(); i++) {
            AIMaterial material = AIMaterial.create(Objects.requireNonNull(aiScene.mMaterials()).get(i));
            addMaterial(material);
        }
        aiReleaseImport(aiScene);
    }

    private void processMesh(AIMesh mesh, AssimpMesh oxyMesh) {
        AIVector3D.Buffer bufferVert = mesh.mVertices();
        while (bufferVert.hasRemaining()) {
            AIVector3D vertex = bufferVert.get();
            oxyMesh.vertices.add(new Vector3f(vertex.x(), vertex.y(), vertex.z()));
        }

        int numFaces = mesh.mNumFaces();
        AIFace.Buffer aiFaces = mesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.hasRemaining()) {
                oxyMesh.faces.add(new int[]{buffer.get(), buffer.get(), buffer.get()});
            }
        }

        AIVector3D.Buffer bufferNor = mesh.mNormals();
        if(bufferNor != null){
            while (Objects.requireNonNull(bufferNor).hasRemaining()) {
                AIVector3D normals = bufferNor.get();
                oxyMesh.normals.add(new Vector3f(normals.x(), normals.y(), normals.z()));
            }
        } else logger.info("Model: " + rootName + " has no normals");

        AIVector3D.Buffer textCoords = mesh.mTextureCoords(0);
        if(textCoords != null) {
            while (Objects.requireNonNull(textCoords).hasRemaining()) {
                AIVector3D textCoord = textCoords.get();
                oxyMesh.textureCoords.add(new Vector2f(textCoord.x(), 1 - textCoord.y()));
            }
        } else logger.info("Model: " + rootName + " has no texture coordinates");

        AIVector3D.Buffer tangent = mesh.mTangents();
        if(tangent != null) {
            while (Objects.requireNonNull(tangent).hasRemaining()) {
                AIVector3D tangentC = tangent.get();
                oxyMesh.tangents.add(new Vector3f(tangentC.x(), tangentC.y(), tangentC.z()));
            }
        } else logger.info("Model: " + rootName + " has no tangent");

        AIVector3D.Buffer bitangent = mesh.mBitangents();
        if(bitangent != null) {
            while (Objects.requireNonNull(bitangent).hasRemaining()) {
                AIVector3D biTangentC = bitangent.get();
                oxyMesh.biTangents.add(new Vector3f(biTangentC.x(), biTangentC.y(), biTangentC.z()));
            }
        } else logger.info("Model: " + rootName + " has no bitangent");
    }

    private void addMaterial(AIMaterial aiMaterial) {

        String parentPath = new File(objPath).getParent();

        AIString nameMaterial = new AIString(ByteBuffer.allocateDirect(1032));
        aiGetMaterialString(aiMaterial, AI_MATKEY_NAME, aiTextureType_NONE, 0, nameMaterial);
        String matName = nameMaterial.dataString();
        nameMaterial.clear();

        AIString path = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        path.clear();

        AIString pathNormals = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_HEIGHT, 0, pathNormals, (IntBuffer) null, null, null, null, null, null);
        String textPathNormals = pathNormals.dataString();
        pathNormals.clear();

        AIString pathRoughness = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, AI_MATKEY_GLTF_PBRMETALLICROUGHNESS_METALLICROUGHNESS_TEXTURE, 0, pathRoughness, (IntBuffer) null, null, null, null, null, null);
        String textPathRoughness = pathRoughness.dataString();
        pathRoughness.clear();

        AIString pathMetallic = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_UNKNOWN, 0, pathMetallic, (IntBuffer) null, null, null, null, null, null);
        String textPathMetallic = pathMetallic.dataString();
        pathMetallic.clear();

        AIString pathAO = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_LIGHTMAP, 0, pathAO, (IntBuffer) null, null, null, null, null, null);
        String textPathAO = pathAO.dataString();
        pathAO.clear();

        AIColor4D color = AIColor4D.create();
        Vector4f diffuse = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);
        if (result == 0) {
            diffuse = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        float[] metallic = new float[1];
        result = aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_REFLECTIVITY, aiTextureType_NONE, 0, metallic, new int[]{1});
        if (result == 0) {
//            System.out.println(Arrays.toString(metallic));
        }

        float[] roughness = new float[1];
        result = aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_SHININESS_STRENGTH, aiTextureType_NONE, 0, roughness, new int[]{1});
        if (result == 0) {
//            System.out.println(Arrays.toString(roughness));
        }

        if(textPath.isBlank() || textPath.isEmpty()) textPath = null;
        else textPath = parentPath + "\\" + textPath;
        if(textPathNormals.isBlank() || textPathNormals.isEmpty()) textPathNormals = null;
        else textPathNormals = parentPath + "\\" + textPathNormals;
        if(textPathRoughness.isBlank() || textPathRoughness.isEmpty()) textPathRoughness = null;
        else textPathRoughness = parentPath + "\\" + textPathRoughness;
        if(textPathMetallic.isBlank() || textPathMetallic.isEmpty()) textPathMetallic = null;
        else textPathMetallic = parentPath + "\\" + textPathMetallic;
        if(textPathAO.isBlank() || textPathAO.isEmpty()) textPathAO = null;
        else textPathAO = parentPath + "\\" + textPathAO;

        materials.add(new AssimpMaterial(matName, textPath, textPathMetallic, textPathRoughness, textPathNormals, textPathAO, diffuse));
    }

    public String getPath() {
        return objPath;
    }
}
