package OxyEngine;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIQuaternion;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.system.MemoryStack;
import physx.common.PxQuat;
import physx.common.PxTransform;
import physx.common.PxVec3;

import java.util.List;

public interface Utils {

    double DEGREES_TO_RADIANS = 0.017453292519943295;

    static float[] toPrimitiveFloat(List<Float> list){
        float[] buffer = new float[list.size()];
        for(int i = 0; i < buffer.length; i++) buffer[i] = list.get(i);
        return buffer;
    }

    static int[] toPrimitiveInteger(List<Integer> list){
        int[] buffer = new int[list.size()];
        for(int i = 0; i < buffer.length; i++) buffer[i] = list.get(i);
        return buffer;
    }

    static float[] copy(float[] src, float[] dest) {
        float[] newObjVert = new float[src.length + dest.length];
        System.arraycopy(src, 0, newObjVert, 0, src.length);
        System.arraycopy(dest, 0, newObjVert, src.length, dest.length);
        return newObjVert;
    }

    static int[] copy(int[] src, int[] dest) {
        int[] newObjVert = new int[src.length + dest.length];
        System.arraycopy(src, 0, newObjVert, 0, src.length);
        System.arraycopy(dest, 0, newObjVert, src.length, dest.length);
        return newObjVert;
    }

    static Matrix4f convertAIMatrixToJOMLMatrix(AIMatrix4x4 matrix4x4) {
        return new Matrix4f(matrix4x4.a1(), matrix4x4.b1(), matrix4x4.c1(), matrix4x4.d1(),
                matrix4x4.a2(), matrix4x4.b2(), matrix4x4.c2(), matrix4x4.d2(),
                matrix4x4.a3(), matrix4x4.b3(), matrix4x4.c3(), matrix4x4.d3(),
                matrix4x4.a4(), matrix4x4.b4(), matrix4x4.c4(), matrix4x4.d4());
    }

    static Vector3f convertAIVector3DToJOMLVector3f(AIVector3D vector3D) {
        return new Vector3f(vector3D.x(), vector3D.y(), vector3D.z());
    }

    static Quaternionf convertAIQuaternionToJOMLQuaternion(AIQuaternion quaternion) {
        return new Quaternionf(quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w());
    }

    static Vector3f toJOMLVector3f(PxVec3 vec3) {
        return new Vector3f(vec3.getX(), vec3.getY(), vec3.getZ());
    }

    static PxVec3 toPxVec3(Vector3f vec3) {
        return new PxVec3(vec3.x(), vec3.y(), vec3.z());
    }

    static Quaternionf toJOMLQuaternionf(PxQuat quat) {
        return new Quaternionf(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }

    static PxQuat ToPxQuat(Quaternionf quat) {
        return new PxQuat(quat.x(), quat.y(), quat.z(), quat.w());
    }

    static PxTransform toPxTransform(Matrix4f transform) {
        Vector3f pos = new Vector3f();
        Quaternionf rot = new Quaternionf();
        transform.getTranslation(pos);
        transform.getUnnormalizedRotation(rot);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxVec3 vec3 = PxVec3.createAt(stack, MemoryStack::nmalloc, pos.x, pos.y, pos.z);
            PxQuat quat = PxQuat.createAt(stack, MemoryStack::nmalloc, rot.x, rot.y, rot.z, rot.w);
            return new PxTransform(vec3, quat);
        }
    }
}
