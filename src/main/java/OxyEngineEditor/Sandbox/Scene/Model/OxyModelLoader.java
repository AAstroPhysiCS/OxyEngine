package OxyEngineEditor.Sandbox.Scene.Model;

import OxyEngine.Core.Renderer.Texture.OxyTexture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.assimp.Assimp.aiImportFile;

public class OxyModelLoader {

    public static class AssimpMesh {
        public final List<Vector3f> vertices = new ArrayList<>();
        public final List<Vector2f> textureCoords = new ArrayList<>();
        public final List<Vector3f> normals = new ArrayList<>();
        public final List<int[]> faces = new ArrayList<>();
        public OxyMaterial material;

        public final String name;

        public AssimpMesh(String name){
            this.name = name;
        }
    }
    public final List<AssimpMesh> meshes = new ArrayList<>();

    String objPath;

    public OxyModelLoader(String objPath) {
        this.objPath = objPath;
        processData();
    }

    void processData() {
        int flag = aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_FixInfacingNormals;
        AIScene aiScene = aiImportFile(objPath, flag);
        PointerBuffer meshes = Objects.requireNonNull(aiScene).mMeshes();
        for (int i = 0; i < aiScene.mNumMeshes(); i++) {
            AIMesh aiMesh = AIMesh.create(Objects.requireNonNull(meshes).get(i));
            AssimpMesh oxyMesh = new AssimpMesh(aiMesh.mName().dataString());
            addMesh(aiMesh, oxyMesh);
            this.meshes.add(oxyMesh);
        }

        PointerBuffer materials = aiScene.mMaterials();
        for(int i = 0; i < aiScene.mNumMaterials() - 1; i++){
            AIMaterial material = AIMaterial.create(Objects.requireNonNull(materials).get(i));
            addMaterial(material, this.meshes.get(i));
        }
    }

    private void addMesh(AIMesh mesh, AssimpMesh oxyMesh) {

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
        for (int i = 0; i < Objects.requireNonNull(textCoords).remaining(); i++) {
            AIVector3D textCoord = textCoords.get();
            oxyMesh.textureCoords.add(new Vector2f(textCoord.x(), 1 - textCoord.y()));
        }
    }

    private void addMaterial(AIMaterial aiMaterial, AssimpMesh oxyMesh){
        AIColor4D color = AIColor4D.create();
        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        OxyTexture texture = null;
        if(!textPath.equals("")){
            texture = OxyTexture.load(5, textPath);
        }
        Vector4f ambient = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, color);
        if (result == 0) {
            ambient = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        Vector4f diffuse = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);
        if (result == 0) {
            diffuse = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        Vector4f specular = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, color);
        if (result == 0) {
            specular = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        oxyMesh.material = new OxyMaterial(texture, ambient, diffuse, specular, 1.0f);
    }
}
