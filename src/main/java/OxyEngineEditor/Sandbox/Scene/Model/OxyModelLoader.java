package OxyEngineEditor.Sandbox.Scene.Model;

import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.assimp.Assimp.*;

public class OxyModelLoader {

    public static class AssimpOxyMesh {
        public final List<Vector3f> vertices = new ArrayList<>();
        public final List<Vector2f> textureCoords = new ArrayList<>();
        public final List<Vector3f> normals = new ArrayList<>();
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

    public OxyModelLoader(String objPath) {
        this.objPath = objPath;
        processData();
    }

    void processData() {
        int flag = aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_FixInfacingNormals;
        AIScene aiScene = aiImportFile(objPath, flag);
        PointerBuffer materials = Objects.requireNonNull(aiScene).mMaterials();
        PointerBuffer meshes = Objects.requireNonNull(aiScene).mMeshes();

        for (int i = 0; i < aiScene.mNumMeshes(); i++) {
            AIMesh aiMesh = AIMesh.create(Objects.requireNonNull(meshes).get(i));
            AssimpOxyMesh oxyMesh = new AssimpOxyMesh(aiMesh.mName().dataString());
            AIMaterial material = AIMaterial.create(Objects.requireNonNull(materials).get(aiMesh.mMaterialIndex()));
            addMesh(aiMesh, oxyMesh);
            addMaterial(material, oxyMesh);
            calcPos(oxyMesh);
            this.meshes.add(oxyMesh);
        }
    }

    private void calcPos(AssimpOxyMesh oxyMesh) {
        float[] allVerticesX = new float[oxyMesh.vertices.size()];
        int ptr = 0;
        for(Vector3f v : oxyMesh.vertices){
            allVerticesX[ptr++] = v.x;
        }
        Arrays.sort(allVerticesX);

        float[] allVerticesY = new float[oxyMesh.vertices.size()];
        int ptr2 = 0;
        for(Vector3f v : oxyMesh.vertices){
            allVerticesY[ptr2++] = v.y;
        }
        Arrays.sort(allVerticesY);

        float[] allVerticesZ = new float[oxyMesh.vertices.size()];
        int ptr3 = 0;
        for(Vector3f v : oxyMesh.vertices){
            allVerticesZ[ptr3++] = v.z;
        }
        Arrays.sort(allVerticesZ);

        oxyMesh.pos = new Vector3f(
                (allVerticesX[0] + allVerticesX[allVerticesX.length - 1]) / 2,
                (allVerticesY[0] + allVerticesY[allVerticesY.length - 1]) / 2,
                (allVerticesZ[0] + allVerticesZ[allVerticesZ.length - 1]) / 2
        );
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
    }

    private void addMaterial(AIMaterial aiMaterial, AssimpOxyMesh oxyMesh) {
        AIColor4D color = AIColor4D.create();
        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        ImageTexture texture = null;
        if (!textPath.equals("")) {
            texture = OxyTexture.loadImage(textPath);
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
        oxyMesh.material = new OxyMaterial(texture, ambient, diffuse, specular, 128f);
    }
}
