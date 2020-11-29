package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static OxyEngine.Components.BoundingBoxComponent.*;
import static org.lwjgl.assimp.Assimp.*;

public class OxyModelLoader {

    public static class AssimpOxyMesh {
        public final List<Vector3f> vertices = new ArrayList<>();
        public final List<Vector2f> textureCoords = new ArrayList<>();
        public final List<Vector3f> normals = new ArrayList<>();
        public final List<Vector3f> tangents = new ArrayList<>();
        public final List<Vector3f> biTangents = new ArrayList<>();
        public final List<int[]> faces = new ArrayList<>();

        public OxyMaterial material;
        public Vector3f pos;

        public final String name;
        public Vector3f min, max;

        public AssimpOxyMesh(String name) {
            this.name = name;
        }
    }

    public final List<AssimpOxyMesh> meshes = new ArrayList<>();

    final String objPath;
    AIScene aiScene;

    public OxyModelLoader(String objPath) {
        this.objPath = objPath;
        processData();
    }

    void processData() {
        int flag = aiProcess_Triangulate
                | aiProcess_FixInfacingNormals
                | aiProcess_OptimizeMeshes
                | aiProcess_FlipUVs
                | aiProcess_CalcTangentSpace;

        aiScene = aiImportFile(objPath, flag);
        PointerBuffer materials = Objects.requireNonNull(aiScene).mMaterials();
        PointerBuffer meshes = Objects.requireNonNull(aiScene).mMeshes();
        for (int i = 0; i < aiScene.mNumMeshes(); i++) {
            AIMesh aiMesh = AIMesh.create(Objects.requireNonNull(meshes).get(i));
            AssimpOxyMesh oxyMesh = new AssimpOxyMesh(aiMesh.mName().dataString());
            AIMaterial material = AIMaterial.create(Objects.requireNonNull(materials).get(aiMesh.mMaterialIndex()));
            addMesh(aiMesh, oxyMesh);
            addMaterial(material, oxyMesh);
            float[][] sortedVertices = sort(oxyMesh);
            calcPos(oxyMesh, sortedVertices);
            calcMax(oxyMesh, sortedVertices);
            calcMin(oxyMesh, sortedVertices);
            this.meshes.add(oxyMesh);
        }

        //transforming to 0,0,0
        for (AssimpOxyMesh oxyMesh : this.meshes) {
            Matrix4f transform = new Matrix4f()
                    .translate(new Vector3f(oxyMesh.pos).negate());
            for (int j = 0; j < oxyMesh.vertices.size(); j++) {
                Vector3f vertices3f = oxyMesh.vertices.get(j);
                Vector4f t4f = new Vector4f(vertices3f, 1.0f).mul(transform);
                vertices3f.set(t4f.x, t4f.y, t4f.z);
            }
        }
    }

    private void addMesh(AIMesh mesh, AssimpOxyMesh oxyMesh) {

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
        while (Objects.requireNonNull(bufferNor).hasRemaining()) {
            AIVector3D normals = bufferNor.get();
            oxyMesh.normals.add(new Vector3f(normals.x(), normals.y(), normals.z()));
        }

        AIVector3D.Buffer textCoords = mesh.mTextureCoords(0);
        while (Objects.requireNonNull(textCoords).hasRemaining()) {
            AIVector3D textCoord = textCoords.get();
            oxyMesh.textureCoords.add(new Vector2f(textCoord.x(), 1 - textCoord.y()));
        }

        AIVector3D.Buffer tangent = mesh.mTangents();
        while (Objects.requireNonNull(tangent).hasRemaining()) {
            AIVector3D tangentC = tangent.get();
            oxyMesh.tangents.add(new Vector3f(tangentC.x(), tangentC.y(), tangentC.z()));
        }

        AIVector3D.Buffer bitangent = mesh.mBitangents();
        while (Objects.requireNonNull(bitangent).hasRemaining()) {
            AIVector3D biTangentC = bitangent.get();
            oxyMesh.biTangents.add(new Vector3f(biTangentC.x(), biTangentC.y(), biTangentC.z()));
        }
    }

    private void addMaterial(AIMaterial aiMaterial, AssimpOxyMesh oxyMesh) {
        AIString path = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        ImageTexture albedoTexture = null;
        if (!textPath.equals(""))
            albedoTexture = OxyTexture.loadImage(textPath);
        path.clear();

        AIString pathNormals = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_HEIGHT, 0, pathNormals, (IntBuffer) null, null, null, null, null, null);
        String textPathNormals = pathNormals.dataString();
        ImageTexture normalTexture = null;
        if (!textPathNormals.equals("")) {
            normalTexture = OxyTexture.loadImage(textPathNormals);
        }
        pathNormals.clear();

        AIString pathRoughness = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, AI_MATKEY_GLTF_PBRMETALLICROUGHNESS_METALLICROUGHNESS_TEXTURE, 0, pathRoughness, (IntBuffer) null, null, null, null, null, null);
        String textPathRoughness = pathRoughness.dataString();
        ImageTexture roughnessTexture = null;
        if (!textPathRoughness.equals("")) {
            roughnessTexture = OxyTexture.loadImage(textPathRoughness);
        }
        pathRoughness.clear();

        AIString pathMetallic = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_UNKNOWN, 0, pathMetallic, (IntBuffer) null, null, null, null, null, null);
        String textPathMetallic = pathMetallic.dataString();
        ImageTexture metallicTexture = null;
        if (!textPathMetallic.equals("")) {
            metallicTexture = OxyTexture.loadImage(textPathMetallic);
        }
        pathMetallic.clear();

        AIString pathAO = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_LIGHTMAP, 0, pathAO, (IntBuffer) null, null, null, null, null, null);
        String textPathAO = pathAO.dataString();
        ImageTexture aoTexture = null;
        if (!textPathAO.equals("")) {
            aoTexture = OxyTexture.loadImage(textPathAO);
        }
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

        oxyMesh.material = new OxyMaterial(albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture, new OxyColor(diffuse));
    }

    public String getPath() {
        return objPath;
    }
}
