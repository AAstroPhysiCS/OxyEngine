package OxyEngine.Core.Context.Scene;

import OxyEngine.Components.TagComponent;
import OxyEngine.Core.Context.Renderer.Mesh.OxyVertex;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.OxyUtils.convertAIMatrixToJOMLMatrix;
import static org.lwjgl.assimp.Assimp.*;

public non-sealed class MeshImporter implements ModelImporterFactory {

    static final class AssimpMesh {
        final List<OxyVertex> vertexList = new ArrayList<>();
        final List<int[]> faces = new ArrayList<>();
        final OxyEntity rootEntity;
        final String name;

        Matrix4f transformation = new Matrix4f();
        Vector3f minAABB = new Vector3f(), maxAABB = new Vector3f();
        int materialIndex;

        int mNumBones;
        PointerBuffer mBones;

        AssimpMesh(OxyEntity rootEntity, String name) {
            this.rootEntity = rootEntity;
            this.name = name;
        }
    }

    static final record AssimpMaterial(String name, String textPath, String textPathMetallic,
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

        //Submeshes
        for (int i = 0; i < node.mNumMeshes(); i++) {
            AIMesh mesh = AIMesh.create(scene.mMeshes().get(node.mMeshes().get(i)));
            AssimpMesh subMesh = new AssimpMesh(root, node.mName().dataString());
            subMesh.transformation = transformation;
            subMesh.mBones = mesh.mBones();
            subMesh.mNumBones = mesh.mNumBones();
            subMesh.materialIndex = mesh.mMaterialIndex();
            AIAABB aiaabb = mesh.mAABB();
            subMesh.minAABB = new Vector3f(aiaabb.mMin().x(), aiaabb.mMin().y(), aiaabb.mMin().z());
            subMesh.maxAABB = new Vector3f(aiaabb.mMax().x(), aiaabb.mMax().y(), aiaabb.mMax().z());
            processMesh(mesh, subMesh);
            this.meshes.add(subMesh);
        }

        //Children node
        for (int i = 0; i < node.mNumChildren(); i++) {
            AINode childrenNode = AINode.create(node.mChildren().get(i));
            processNode(childrenNode, scene, root);
        }
    }

    private void processMesh(AIMesh mesh, AssimpMesh oxyMesh) {

        AIVector3D.Buffer bufferVert = mesh.mVertices();
        AIVector3D.Buffer bufferNor = mesh.mNormals();
        AIVector3D.Buffer textCoords = mesh.mTextureCoords(0);
        AIVector3D.Buffer tangent = mesh.mTangents();
        AIVector3D.Buffer bitangent = mesh.mBitangents();

        int size = mesh.mNumVertices();

        for (int i = 0; i < size; i++) {
            AIVector3D vertices = bufferVert.get(i);
            OxyVertex vertex = new OxyVertex();

            Vector3f v = new Vector3f(vertices.x(), vertices.y(), vertices.z());

            vertex.vertices.set(v);

            if (bufferNor != null) {
                AIVector3D normals3 = bufferNor.get(i);
                vertex.normals.set(normals3.x(), normals3.y(), normals3.z());
            } else logger.info("Model: " + rootName + " has no normals");

            if (textCoords != null) {
                AIVector3D textCoord = textCoords.get(i);
                vertex.textureCoords.set(textCoord.x(), textCoord.y());
            } else logger.info("Model: " + rootName + " has no texture coordinates");

            if (tangent != null) {
                AIVector3D tangentC = tangent.get(i);
                vertex.tangents.set(tangentC.x(), tangentC.y(), tangentC.z());
            } else logger.info("Model: " + rootName + " has no tangent");

            if (bitangent != null) {
                AIVector3D biTangentC = bitangent.get(i);
                vertex.biTangents.set(biTangentC.x(), biTangentC.y(), biTangentC.z());
            } else logger.info("Model: " + rootName + " has no bitangent");

            oxyMesh.vertexList.add(vertex);
        }

        int numFaces = mesh.mNumFaces();
        AIFace.Buffer aiFaces = mesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.hasRemaining()) {
                int f1 = buffer.get();
                int f2 = buffer.get();
                int f3 = buffer.get();
                oxyMesh.faces.add(new int[]{f1, f2, f3});
            }
        }
    }

    private void addMaterial(AIMaterial aiMaterial) {

        String parentPath = new File(scenePath).getParent();

        AIString nameMaterial = AIString.calloc();
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

        AIString pathMetallicAndRoughness = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, AI_MATKEY_GLTF_PBRMETALLICROUGHNESS_METALLICROUGHNESS_TEXTURE, 0, pathMetallicAndRoughness, (IntBuffer) null, null, null, null, null, null);
        String textPathRoughness = pathMetallicAndRoughness.dataString();
        pathMetallicAndRoughness.clear();

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
        } else textPath = parentPath + "\\" + textPath;
        if (textPathNormals.isBlank() || textPathNormals.isEmpty()) {
            logger.warning(warningString + "Normal map is empty!");
            textPathNormals = null;
        } else textPathNormals = parentPath + "\\" + textPathNormals;
        if (textPathRoughness.isBlank() || textPathRoughness.isEmpty()) {
            logger.warning(warningString + "Roughness map is empty!");
            textPathRoughness = null;
        } else textPathRoughness = parentPath + "\\" + textPathRoughness;
        if (textPathMetallic.isBlank() || textPathMetallic.isEmpty()) {
            logger.warning(warningString + "Metallic map is empty!");
            textPathMetallic = null;
        } else textPathMetallic = parentPath + "\\" + textPathMetallic;
        if (textPathAO.isBlank() || textPathAO.isEmpty()) {
            logger.warning(warningString + "AO map is empty!");
            textPathAO = null;
        } else textPathAO = parentPath + "\\" + textPathAO;
        if (textPathEmissive.isBlank() || textPathEmissive.isEmpty()) {
            logger.warning(warningString + "Emissive map is empty!");
            textPathEmissive = null;
        } else textPathEmissive = parentPath + "\\" + textPathEmissive;

        materials.add(new AssimpMaterial(matName, textPath, textPathMetallic, textPathRoughness, textPathNormals, textPathAO, textPathEmissive, diffuse));
    }
}