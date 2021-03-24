package OxyEngine.Scene.Objects.Importer;

import OxyEngine.Components.TagComponent;
import OxyEngine.Core.Renderer.Mesh.OxyVertex;
import OxyEngine.Scene.OxyEntity;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static OxyEngine.Scene.Objects.Importer.OxyModelImporter.convertAIMatrixToJOMLMatrix;
import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.assimp.Assimp.*;

public non-sealed class MeshImporter implements ModelImporterFactory {

    static class AssimpMesh {
        public List<OxyVertex> vertexList;
        public List<int[]> faces;

        //FOR SORTING (NULL AFTER SORTING IS DONE)
        public List<Vector3f> verticesList;

        public int materialIndex;
        public Matrix4f transformation = new Matrix4f();

        public final String name;
        public Vector3f min = new Vector3f(), max = new Vector3f();
        public final OxyEntity rootEntity;

        int mNumBones;
        PointerBuffer mBones;

        public AssimpMesh(OxyEntity rootEntity, String name) {
            this.name = name;
            this.rootEntity = rootEntity;
        }
    }

    static record AssimpMaterial(String name, String textPath, String textPathMetallic,
                                 String textPathRoughness, String textPathNormals,
                                 String textPathAO, String textPathEmissive, Vector4f diffuse) {
    }

    final List<AssimpMesh> meshes = new ArrayList<>();
    final List<AssimpMaterial> materials = new ArrayList<>();

    private String scenePath;
    private String rootName;

    @Override
    public void process(AIScene aiScene, String scenePath, OxyEntity root) {
        this.scenePath = scenePath;
        rootName = root.get(TagComponent.class).tag();
        processNode(Objects.requireNonNull(aiScene.mRootNode()), aiScene, root);

        for (int i = 0; i < aiScene.mNumMaterials(); i++) {
            AIMaterial material = AIMaterial.create(Objects.requireNonNull(aiScene.mMaterials()).get(i));
            addMaterial(material);
        }
    }

    private void processNode(AINode node, AIScene scene, OxyEntity root) {
        Matrix4f transformation = convertAIMatrixToJOMLMatrix(node.mTransformation());
        for (int i = 0; i < node.mNumMeshes(); i++) {
            AIMesh mesh = AIMesh.create(scene.mMeshes().get(node.mMeshes().get(i)));
            AssimpMesh oxyMesh = new AssimpMesh(root, mesh.mName().dataString());
            oxyMesh.transformation = transformation;
            oxyMesh.mBones = mesh.mBones();
            oxyMesh.mNumBones = mesh.mNumBones();
            oxyMesh.materialIndex = mesh.mMaterialIndex();
            processMesh(mesh, oxyMesh);
            AIAABB aiaabb = mesh.mAABB();
            oxyMesh.min = new Vector3f(aiaabb.mMin().x(), aiaabb.mMin().y(), aiaabb.mMin().z());
            oxyMesh.max = new Vector3f(aiaabb.mMax().x(), aiaabb.mMax().y(), aiaabb.mMax().z());
            this.meshes.add(oxyMesh);
        }

        for (int i = 0; i < node.mNumChildren(); i++) {
            processNode(AINode.create(node.mChildren().get(i)), scene, root);
        }
    }

    private void processMesh(AIMesh mesh, AssimpMesh oxyMesh) {

        AIVector3D.Buffer bufferVert = mesh.mVertices();
        AIVector3D.Buffer bufferNor = mesh.mNormals();
        AIVector3D.Buffer textCoords = mesh.mTextureCoords(0);
        AIVector3D.Buffer tangent = mesh.mTangents();
        AIVector3D.Buffer bitangent = mesh.mBitangents();

        int size = mesh.mNumVertices();
        oxyMesh.vertexList = new ArrayList<>(size);
        oxyMesh.verticesList = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            AIVector3D vertices = bufferVert.get(i);
            OxyVertex vertex = new OxyVertex();

            Vector3f v = new Vector3f(vertices.x(), vertices.y(), vertices.z());

            vertex.vertices.set(v);
            oxyMesh.verticesList.add(v);

            if (bufferNor != null) {
                AIVector3D normals3 = bufferNor.get(i);
                vertex.normals.set(new Vector3f(normals3.x(), normals3.y(), normals3.z()));
            } else logger.info("Model: " + rootName + " has no normals");

            if (textCoords != null) {
                AIVector3D textCoord = textCoords.get(i);
                vertex.textureCoords.set(new Vector2f(textCoord.x(), 1 - textCoord.y()));
            } else logger.info("Model: " + rootName + " has no texture coordinates");

            if (tangent != null) {
                AIVector3D tangentC = tangent.get(i);
                vertex.tangents.set(new Vector3f(tangentC.x(), tangentC.y(), tangentC.z()));
            } else logger.info("Model: " + rootName + " has no tangent");

            if (bitangent != null) {
                AIVector3D biTangentC = bitangent.get(i);
                vertex.biTangents.set(new Vector3f(biTangentC.x(), biTangentC.y(), biTangentC.z()));
            } else logger.info("Model: " + rootName + " has no bitangent");

            oxyMesh.vertexList.add(vertex);
        }

        int numFaces = mesh.mNumFaces();
        oxyMesh.faces = new ArrayList<>(numFaces);
        AIFace.Buffer aiFaces = mesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.hasRemaining()) {
                oxyMesh.faces.add(new int[]{buffer.get(), buffer.get(), buffer.get()});
            }
        }

    }

    private void addMaterial(AIMaterial aiMaterial) {

        String parentPath = new File(scenePath).getParent();

        AIString nameMaterial = new AIString(ByteBuffer.allocateDirect(1032));
        aiGetMaterialString(aiMaterial, AI_MATKEY_NAME, aiTextureType_NONE, 0, nameMaterial);
        String matName = nameMaterial.dataString();
        nameMaterial.clear();

        AIString path = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        path.clear();

        AIString pathNormals = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_NORMALS, 0, pathNormals, (IntBuffer) null, null, null, null, null, null);
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

        AIString pathEmissive = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_EMISSIVE, 0, pathEmissive, (IntBuffer) null, null, null, null, null, null);
        String textPathEmissive = pathEmissive.dataString();
        pathEmissive.clear();

        AIColor4D color = AIColor4D.create();
        Vector4f diffuse = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);
        if (result == 0) {
            diffuse = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        float[] metallic = new float[1];
        result = aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_COLOR_REFLECTIVE, aiTextureType_NONE, 0, metallic, new int[]{1});
        if (result == 0) {
            System.out.println(Arrays.toString(metallic));
        }

        float[] roughness = new float[1];
        result = aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, roughness, new int[]{1});
        if (result == 0) {
            System.out.println(Arrays.toString(roughness));
        }

        String warningString = "For the file: %s, ".formatted(scenePath);

        if (textPath.isBlank() || textPath.isEmpty()) {
            logger.warning(warningString + "Albedo map is empty!");
            textPath = null;
        }
        else textPath = parentPath + "\\" + textPath;
        if (textPathNormals.isBlank() || textPathNormals.isEmpty()){
            logger.warning(warningString + "Normal map is empty!");
            textPathNormals = null;
        }
        else textPathNormals = parentPath + "\\" + textPathNormals;
        if (textPathRoughness.isBlank() || textPathRoughness.isEmpty()){
            logger.warning(warningString + "Roughness map is empty!");
            textPathRoughness = null;
        }
        else textPathRoughness = parentPath + "\\" + textPathRoughness;
        if (textPathMetallic.isBlank() || textPathMetallic.isEmpty()){
            logger.warning(warningString + "Metallic map is empty!");
            textPathMetallic = null;
        }
        else textPathMetallic = parentPath + "\\" + textPathMetallic;
        if (textPathAO.isBlank() || textPathAO.isEmpty()){
            logger.warning(warningString + "AO map is empty!");
            textPathAO = null;
        }
        else textPathAO = parentPath + "\\" + textPathAO;
        if (textPathEmissive.isBlank() || textPathEmissive.isEmpty()){
            logger.warning(warningString + "Emissive map is empty!");
            textPathEmissive = null;
        }
        else textPathEmissive = parentPath + "\\" + textPathEmissive;

        materials.add(new AssimpMaterial(matName, textPath, textPathMetallic, textPathRoughness, textPathNormals, textPathAO, textPathEmissive, diffuse));
    }
}
