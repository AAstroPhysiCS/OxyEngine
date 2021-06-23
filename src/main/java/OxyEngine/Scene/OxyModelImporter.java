package OxyEngine.Scene;

import OxyEngine.Components.AnimationComponent;
import OxyEngine.Components.TagComponent;
import OxyEngine.Core.Context.Renderer.Mesh.OxyVertex;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIQuaternion;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.assimp.Assimp.*;

public class OxyModelImporter {

    public static final int DEFAULT_ASSIMP_FLAG = aiProcess_CalcTangentSpace |
            aiProcess_GenSmoothNormals |
            aiProcess_FixInfacingNormals |
            aiProcess_JoinIdenticalVertices |
            aiProcess_ImproveCacheLocality |
            aiProcess_LimitBoneWeights |
            aiProcess_RemoveRedundantMaterials |
            aiProcess_Triangulate |
            aiProcess_GenUVCoords |
            aiProcess_FlipUVs |
//            aiProcess_PreTransformVertices | (animations wouldn't work, if you enable this)
//            aiProcess_SplitLargeMeshes |
            aiProcess_FindInvalidData |
            aiProcess_OptimizeMeshes;

    public OxyModelImporter(String scenePath, ImporterType... types) {
        processData(scenePath, null, DEFAULT_ASSIMP_FLAG, types);
    }

    public OxyModelImporter(String scenePath, OxyEntity root, ImporterType... types) {
        processData(scenePath, root, DEFAULT_ASSIMP_FLAG, types);
    }

    public OxyModelImporter(String scenePath, int flags, ImporterType... types) {
        processData(scenePath, null, flags, types);
    }

    public OxyModelImporter(String scenePath, OxyEntity root, int flags, ImporterType... types) {
        processData(scenePath, root, flags, types);
    }

    private static final List<ModelImporterFactory> factories = new ArrayList<>();
    private MeshImporter meshImporter;
    private AnimationImporter animationImporter;
    private AIScene aiScene;

    void processData(String scenePath, OxyEntity root, int flags, ImporterType... types) {
        if (types.length == 0) {
            logger.warning("No ImporterType selected! Returning...");
            return;
        }

        aiScene = aiImportFile(scenePath, flags);
        if (aiScene == null) {
            logger.warning("Scene could not be imported!");
            return;
        }

        String rootName = aiScene.mRootNode().mName().dataString();
        if (root == null) {
            root = ACTIVE_SCENE.createEmptyModel();
            // rootEnt.setFamily(new EntityFamily()); default behaviour from a entity, once it is created!
            root.addComponent(new TagComponent(rootName));
        }

        for (var type : types) factories.add(ModelImporterFactory.getInstance(type));

        meshImporter = tryMeshImporter();
        animationImporter = tryAnimationImporter();

        boolean meshImporterNull = meshImporter == null;
        boolean animationImporterNull = animationImporter == null;

        if (!animationImporterNull && meshImporterNull) {
            aiReleaseImport(aiScene);
            factories.clear();
            throw new IllegalStateException("Mesh Importer must be included in order to have animations.");
        }

        if (!meshImporterNull) meshImporter.process(aiScene, scenePath, root);
        if (!animationImporterNull) {
            animationImporter.setMeshes(meshImporter.meshes);
            animationImporter.process(aiScene, scenePath, root);
        } else { //no animation => we dont need the instance anymore
            aiReleaseImport(aiScene);
        }

        factories.clear();
    }

    private static MeshImporter tryMeshImporter() {
        for (var factory : factories) {
            if (factory instanceof MeshImporter) return (MeshImporter) factory;
        }
        return null;
    }

    private static AnimationImporter tryAnimationImporter() {
        for (var factory : factories) {
            if (factory instanceof AnimationImporter) return (AnimationImporter) factory;
        }
        return null;
    }

    private void checkAnimationImporter() {
        if (animationImporter == null) throw new IllegalStateException("Animation Importer is null");
    }

    private void checkMeshImporter() {
        if (meshImporter == null) throw new IllegalStateException("Mesh Importer is null");
    }

    public AIScene getScene() {
        return aiScene;
    }

    public Map<String, AnimationComponent.BoneInfo> getBoneInfoMap() {
        checkAnimationImporter();
        return animationImporter.boneInfoMap;
    }

    public int getBoneCounter() {
        checkAnimationImporter();
        return animationImporter.boneCounter;
    }

    public int getMaterialIndex(int index) {
        checkMeshImporter();
        return meshImporter.meshes.get(index).materialIndex;
    }

    public int getMeshSize() {
        checkMeshImporter();
        return meshImporter.meshes.size();
    }

    public String getMaterialName(int index) {
        checkMeshImporter();
        String name = meshImporter.materials.get(index).name();
        if (name == null) return "Unnamed";
        return name;
    }

    public String[] getMaterialPaths(int index) {
        checkMeshImporter();
        MeshImporter.AssimpMaterial assimpMaterial = meshImporter.materials.get(index);
        return new String[]{assimpMaterial.textPath(), assimpMaterial.textPathNormals(),
                assimpMaterial.textPathRoughness(), assimpMaterial.textPathMetallic(),
                assimpMaterial.textPathAO(), assimpMaterial.textPathEmissive(), assimpMaterial.diffuse().toString()};
    }

    public String getMeshName(int index) {
        checkMeshImporter();
        return meshImporter.meshes.get(index).name;
    }

    public Vector3f getBoundingBoxMin(int index) {
        checkMeshImporter();
        return meshImporter.meshes.get(index).min;
    }

    public Vector3f getBoundingBoxMax(int index) {
        checkMeshImporter();
        return meshImporter.meshes.get(index).max;
    }

    public Matrix4f getTransformation(int index) {
        checkMeshImporter();
        return meshImporter.meshes.get(index).transformation;
    }

    public OxyEntity getRootEntity(int index) {
        checkMeshImporter();
        return meshImporter.meshes.get(index).rootEntity;
    }

    public List<OxyVertex> getVertexList(int index) {
        checkMeshImporter();
        return meshImporter.meshes.get(index).vertexList;
    }

    public static Matrix4f convertAIMatrixToJOMLMatrix(AIMatrix4x4 matrix4x4) {
        return new Matrix4f(matrix4x4.a1(), matrix4x4.b1(), matrix4x4.c1(), matrix4x4.d1(),
                matrix4x4.a2(), matrix4x4.b2(), matrix4x4.c2(), matrix4x4.d2(),
                matrix4x4.a3(), matrix4x4.b3(), matrix4x4.c3(), matrix4x4.d3(),
                matrix4x4.a4(), matrix4x4.b4(), matrix4x4.c4(), matrix4x4.d4());
    }

    public static Vector3f convertAIVector3DToJOMLVector3f(AIVector3D vector3D) {
        return new Vector3f(vector3D.x(), vector3D.y(), vector3D.z());
    }

    public static Quaternionf convertAIQuaternionToJOMLQuaternion(AIQuaternion quaternion) {
        return new Quaternionf(quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w());
    }

    public List<int[]> getFaces(int index) {
        checkMeshImporter();
        return meshImporter.meshes.get(index).faces;
    }
}