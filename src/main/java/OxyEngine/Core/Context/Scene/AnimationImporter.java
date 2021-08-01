package OxyEngine.Core.Context.Scene;

import OxyEngine.Components.AnimationComponent;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVertexWeight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static OxyEngine.Core.Context.Renderer.Mesh.OxyVertex.MAX_BONE_INFLUENCE;
import static OxyEngine.OxyUtils.convertAIMatrixToJOMLMatrix;

public non-sealed class AnimationImporter implements ModelImporterFactory {

    final Map<String, AnimationComponent.BoneInfo> boneInfoMap = new HashMap<>();
    private List<MeshImporter.AssimpMesh> meshes;
    int boneCounter;

    public void setMeshes(List<MeshImporter.AssimpMesh> meshes) {
        this.meshes = meshes;
    }

    @Override
    public void process(AIScene aiScene, String scenePath, OxyEntity root) {
        for (MeshImporter.AssimpMesh mesh : meshes) {
            for (int boneIndex = 0; boneIndex < mesh.mNumBones; boneIndex++) {
                int boneID;

                AIBone bone = AIBone.create(Objects.requireNonNull(mesh.mBones).get(boneIndex));
                String boneName = bone.mName().dataString();

                if (!boneInfoMap.containsKey(boneName)) {
                    AnimationComponent.BoneInfo newInfo = new AnimationComponent.BoneInfo(boneCounter, convertAIMatrixToJOMLMatrix(bone.mOffsetMatrix()));
                    boneInfoMap.put(boneName, newInfo);
                    boneID = boneCounter;
                    boneCounter++;
                } else {
                    boneID = boneInfoMap.get(boneName).id();
                }

                AIVertexWeight.Buffer weights = bone.mWeights();
                int numWeights = bone.mNumWeights();

                for (int weightIndex = 0; weightIndex < numWeights; weightIndex++) {
                    int vertexId = weights.get(weightIndex).mVertexId();
                    float weight = weights.get(weightIndex).mWeight();
                    for (int i = 0; i < MAX_BONE_INFLUENCE; i++) {
                        if (mesh.vertexList.get(vertexId).boneIDs[i] == -1) {
                            mesh.vertexList.get(vertexId).weights[i] = weight;
                            mesh.vertexList.get(vertexId).boneIDs[i] = boneID;
                            break;
                        }
                    }
                }
            }
        }
    }
}
