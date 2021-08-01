package OxyEngine.Core.Context.Scene;

import OxyEngine.Components.AnimationComponent;
import OxyEngine.Components.TagComponent;
import OxyEngine.Core.Context.Renderer.Mesh.OxyVertex;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIScene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static OxyEngine.Core.Context.Scene.SceneRuntime.ACTIVE_SCENE;
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
//            aiProcess_PreTransformVertices | (animations wouldn't work, if you enable this)
            aiProcess_SplitLargeMeshes |
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
        return meshImporter.meshes.get(index).minAABB;
    }

    public Vector3f getBoundingBoxMax(int index) {
        checkMeshImporter();
        return meshImporter.meshes.get(index).maxAABB;
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

    public List<int[]> getFaces(int index) {
        checkMeshImporter();
        return meshImporter.meshes.get(index).faces;
    }
}