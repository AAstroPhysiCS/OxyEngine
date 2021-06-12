package OxyEngine.Components;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIScene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static OxyEngine.Core.Context.Renderer.Mesh.OxyVertex.MAX_BONES;
import static OxyEngine.Scene.Objects.Importer.OxyModelImporter.*;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.assimp.Assimp.aiReleaseImport;

@SuppressWarnings("ConstantConditions")
public class AnimationComponent implements EntityComponent {

    private List<Matrix4f> finalBoneMatrices = new ArrayList<>(MAX_BONES);
    private final Map<String, BoneInfo> boneInfoMap;
    private final Map<String, OxyNodeAnimation> nodeAnimMap;
    private final AIScene currentAIScene;

    private final List<OxyAnimation> animations;
    private OxyNode rootNode;

    private boolean stop;

    public AnimationComponent(AIScene scene, Map<String, BoneInfo> boneInfoMap) {
        this.boneInfoMap = boneInfoMap;
        this.currentAIScene = scene;
        for (int i = 0; i < MAX_BONES; i++) finalBoneMatrices.add(new Matrix4f().identity());
        nodeAnimMap = new HashMap<>(MAX_BONES);
        animations = new ArrayList<>(scene.mNumAnimations());
        init();
    }

    public AnimationComponent(AnimationComponent other) {
        this.finalBoneMatrices = new ArrayList<>();
        for (Matrix4f otherMatrix : other.finalBoneMatrices) {
            finalBoneMatrices.add(new Matrix4f(otherMatrix));
        }
        this.stop = other.stop;
        this.boneInfoMap = new HashMap<>(other.boneInfoMap);
        this.nodeAnimMap = new HashMap<>(other.nodeAnimMap);
        this.animations = new ArrayList<>(other.animations);
        this.rootNode = other.rootNode;
        this.currentAIScene = other.currentAIScene;
        this.currentTime = other.currentTime;
        init();
    }

    //Runs one time
    private void init() {
        for (int i = 0; i < currentAIScene.mNumAnimations(); i++) {
            animations.add(new OxyAnimation(AIAnimation.create(currentAIScene.mAnimations().get(i))));
            for (String nodeName : boneInfoMap.keySet())
                nodeAnimMap.put(nodeName, findNodeAnim(animations.get(i), nodeName));
        }
        AINode aiNode = currentAIScene.mRootNode();
        if(aiNode != null) {
            rootNode = new OxyNode(aiNode.mName().dataString(), convertAIMatrixToJOMLMatrix(aiNode.mTransformation()), aiNode.mNumChildren(), new ArrayList<>());
            addAllNodeChildren(aiNode, rootNode); //reading all the aiNodes and saving them to the oxyNode (begins with rootnode and recursively adds...)
            //Dont need the AiScene anymore, because we fetched all the necessary animation data and saved them to java objects
            aiReleaseImport(currentAIScene);
        }
    }

    public void stopAnimation(boolean stop) {
        this.stop = stop;
    }

    public OxyNodeAnimation findNodeAnim(OxyAnimation animation, String nodeName) {
        for (int i = 0; i < animation.nodeAnim.size(); i++) {
            OxyNodeAnimation nodeAnim = animation.nodeAnim.get(i);
            if (nodeName.equals(nodeAnim.name)) {
                return nodeAnim;
            }
        }
        return null;
    }

    public int findRotation(float animationTime, OxyNodeAnimation nodeAnim) {
        List<AnimationKey> rotationKeys = nodeAnim.animationKeyMap.get(AnimationKeyType.RotationKey);
        int num = rotationKeys.size();
        for (int i = 0; i < num - 1; i++) {
            if (animationTime < rotationKeys.get(i + 1).time) {
                return i;
            }
        }
        throw new IllegalStateException("Find Rotation returns 0");
    }

    public int findPosition(float animationTime, OxyNodeAnimation nodeAnim) {
        List<AnimationKey> positionKeys = nodeAnim.animationKeyMap.get(AnimationKeyType.PositionKey);
        int num = positionKeys.size();
        for (int i = 0; i < num - 1; i++) {
            if (animationTime < positionKeys.get(i + 1).time) {
                return i;
            }
        }
        throw new IllegalStateException("Find Position returns 0");
    }

    public int findScale(float animationTime, OxyNodeAnimation nodeAnim) {
        List<AnimationKey> scaleKeys = nodeAnim.animationKeyMap.get(AnimationKeyType.ScaleKey);
        int num = scaleKeys.size();
        for (int i = 0; i < num - 1; i++) {
            if (animationTime < scaleKeys.get(i + 1).time) {
                return i;
            }
        }
        throw new IllegalStateException("Find Scale returns 0");
    }

    public Vector3f calcInterpolatedPosition(float animationTime, OxyNodeAnimation nodeAnim) {
        List<AnimationKey> keys = nodeAnim.animationKeyMap.get(AnimationKeyType.PositionKey);
        if (keys.size() == 1)
            return (Vector3f) keys.get(0).value;
        int positionIndex = findPosition(animationTime, nodeAnim);
        int nextPositionIndex = positionIndex + 1;
        float deltaTime = (float) (keys.get(nextPositionIndex).time - keys.get(positionIndex).time);
        float factor = (float) ((animationTime - keys.get(positionIndex).time) / deltaTime);
        assert factor >= 0.0f && factor <= 1.0f : oxyAssert("Factor is bigger than 1 or smaller than 0");
        Vector3f start = (Vector3f) keys.get(positionIndex).value;
        Vector3f end = (Vector3f) keys.get(nextPositionIndex).value;
        Vector3f delta = new Vector3f(end).sub(start);

        Vector3f factoredDelta = new Vector3f(factor).mul(delta);
        return new Vector3f(start).add(factoredDelta);
    }

    public Quaternionf calcInterpolatedRotation(float animationTime, OxyNodeAnimation nodeAnim) {
        List<AnimationKey> keys = nodeAnim.animationKeyMap.get(AnimationKeyType.RotationKey);
        if (keys.size() == 1)
            return (Quaternionf) keys.get(0).value;
        int positionIndex = findRotation(animationTime, nodeAnim);
        int nextPositionIndex = positionIndex + 1;
        float deltaTime = (float) (keys.get(nextPositionIndex).time - keys.get(positionIndex).time);
        float factor = (float) ((animationTime - keys.get(positionIndex).time) / deltaTime);
        assert factor >= 0.0f && factor <= 1.0f : oxyAssert("Factor is bigger than 1 or smaller than 0");
        Quaternionf start = (Quaternionf) keys.get(positionIndex).value;
        Quaternionf end = (Quaternionf) keys.get(nextPositionIndex).value;
        return nlerp(start, end, factor);
    }

    public Vector3f calcInterpolatedScaling(float animationTime, OxyNodeAnimation nodeAnim) {
        List<AnimationKey> keys = nodeAnim.animationKeyMap.get(AnimationKeyType.ScaleKey);
        if (keys.size() == 1)
            return (Vector3f) keys.get(0).value;
        int positionIndex = findScale(animationTime, nodeAnim);
        int nextPositionIndex = positionIndex + 1;
        float deltaTime = (float) (keys.get(nextPositionIndex).time - keys.get(positionIndex).time);
        float factor = (float) ((animationTime - keys.get(positionIndex).time) / deltaTime);
        assert factor >= 0.0f && factor <= 1.0f : oxyAssert("Factor is bigger than 1 or smaller than 0");
        Vector3f start = (Vector3f) keys.get(positionIndex).value;
        Vector3f end = (Vector3f) keys.get(nextPositionIndex).value;
        Vector3f delta = new Vector3f(end).sub(start);

        Vector3f factoredDelta = new Vector3f(factor).mul(delta);
        return new Vector3f(start).add(factoredDelta);
    }

    Quaternionf nlerp(Quaternionf a, Quaternionf b, float blend) {
        a = a.normalize();
        b = b.normalize();

        Quaternionf result = new Quaternionf();
        float dot_product = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
        float one_minus_blend = 1.0f - blend;

        if (dot_product < 0.0f) {
            result.x = a.x * one_minus_blend + blend * -b.x;
            result.y = a.y * one_minus_blend + blend * -b.y;
            result.z = a.z * one_minus_blend + blend * -b.z;
            result.w = a.w * one_minus_blend + blend * -b.w;
        } else {
            result.x = a.x * one_minus_blend + blend * b.x;
            result.y = a.y * one_minus_blend + blend * b.y;
            result.z = a.z * one_minus_blend + blend * b.z;
            result.w = a.w * one_minus_blend + blend * b.w;
        }

        return result.normalize();
    }

    public void addAllNodeChildren(AINode aiNode, OxyNode parentNode) {
        for (int i = 0; i < aiNode.mNumChildren(); i++) {
            AINode children = AINode.create(aiNode.mChildren().get(i));
            OxyNode childrenOxy = new OxyNode(children.mName().dataString(), convertAIMatrixToJOMLMatrix(children.mTransformation()), children.mNumChildren(), new ArrayList<>());
            parentNode.children.add(childrenOxy);
            addAllNodeChildren(children, childrenOxy);
        }
    }

    public void readNodeHierarchy(float animationTime, OxyNode node, Matrix4f parentTransformation) {

        String nodeName = node.name;
        Matrix4f nodeTransform = node.transformation;
        OxyNodeAnimation aiNodeAnim = nodeAnimMap.get(nodeName);

        if (aiNodeAnim != null) {
            Vector3f translate = calcInterpolatedPosition(animationTime, aiNodeAnim);
            Vector3f scale = calcInterpolatedScaling(animationTime, aiNodeAnim);
            Quaternionf rotate = calcInterpolatedRotation(animationTime, aiNodeAnim);

            nodeTransform = new Matrix4f()
                    .translate(translate)
                    .rotate(rotate)
                    .scale(scale);
        }

        Matrix4f globalTransform = new Matrix4f(parentTransformation).mul(nodeTransform);

        if (boneInfoMap.containsKey(nodeName)) {
            int boneIndex = boneInfoMap.get(nodeName).id;
            Matrix4f offset = boneInfoMap.get(nodeName).offset;
            finalBoneMatrices.get(boneIndex).set(new Matrix4f(rootNode.transformation).invert()).mul(globalTransform).mul(offset);
        }

        for (int i = 0; i < node.numChildren; i++) {
            readNodeHierarchy(animationTime, node.children.get(i), globalTransform);
        }
    }

    float currentTime = 0;

    public void updateAnimation(float dt) {
        if (stop) return;
        currentTime += animations.get(0).tickPerSecond * dt;
        currentTime = (float) (currentTime % animations.get(0).duration);
        readNodeHierarchy(currentTime, rootNode, new Matrix4f());
    }

    public List<Matrix4f> getFinalBoneMatrices() {
        return finalBoneMatrices;
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public void setTime(float currentTime) {
        this.currentTime = currentTime;
    }

    private static record OxyNode(String name, Matrix4f transformation, int numChildren, List<OxyNode> children) {
    }

    @SuppressWarnings("ConstantConditions")
    private static class OxyAnimation {

        private final double tickPerSecond;
        private final double duration;

        private final List<OxyNodeAnimation> nodeAnim;

        public OxyAnimation(AIAnimation animation) {
            this.tickPerSecond = animation.mTicksPerSecond();
            this.duration = animation.mDuration();
            nodeAnim = new ArrayList<>(animation.mNumChannels());

            for (int i = 0; i < animation.mNumChannels(); i++) {
                AINodeAnim aiNodeAnim = AINodeAnim.create(animation.mChannels().get(i));
                OxyNodeAnimation oxyNodeAnim = new OxyNodeAnimation(aiNodeAnim.mNodeName().dataString());
                for (int position = 0; position < aiNodeAnim.mNumPositionKeys(); position++) {
                    var key = aiNodeAnim.mPositionKeys().get(position);
                    oxyNodeAnim.addAnimationKey(AnimationKeyType.PositionKey, new AnimationKey(key.mTime(), convertAIVector3DToJOMLVector3f(key.mValue())));
                }
                for (int rotation = 0; rotation < aiNodeAnim.mNumRotationKeys(); rotation++) {
                    var key = aiNodeAnim.mRotationKeys().get(rotation);
                    oxyNodeAnim.addAnimationKey(AnimationKeyType.RotationKey, new AnimationKey(key.mTime(), convertAIQuaternionToJOMLQuaternion(key.mValue())));
                }
                for (int scale = 0; scale < aiNodeAnim.mNumScalingKeys(); scale++) {
                    var key = aiNodeAnim.mScalingKeys().get(scale);
                    oxyNodeAnim.addAnimationKey(AnimationKeyType.ScaleKey, new AnimationKey(key.mTime(), convertAIVector3DToJOMLVector3f(key.mValue())));
                }
                nodeAnim.add(oxyNodeAnim);
            }
        }
    }

    private enum AnimationKeyType {
        PositionKey, RotationKey, ScaleKey
    }

    private static record AnimationKey(double time, Object value) {
    }

    private static record OxyNodeAnimation(String name, Map<AnimationKeyType, List<AnimationKey>> animationKeyMap) {

        public OxyNodeAnimation(String name) {
            this(name, new HashMap<>());
        }

        public void addAnimationKey(AnimationKeyType type, AnimationKey key) {
            if (animationKeyMap.containsKey(type)) {
                animationKeyMap.get(type).add(key);
            } else {
                List<AnimationKey> list = new ArrayList<>();
                list.add(key);
                animationKeyMap.put(type, list);
            }
        }
    }

    public record BoneInfo(int id, Matrix4f offset) {
    }
}
