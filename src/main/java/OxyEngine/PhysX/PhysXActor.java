package OxyEngine.PhysX;

import OxyEngine.Core.Context.Scene.Entity;
import OxyEngine.System.Disposable;
import OxyEngineEditor.UI.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import physx.common.PxIDENTITYEnum;
import physx.common.PxQuat;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.physics.*;

import static OxyEngine.Core.Context.Scene.SceneRuntime.entityContext;
import static OxyEngine.Utils.toPxTransform;
import static OxyEngine.Utils.toPxVec3;


public final class PhysXActor implements Disposable {

    private PhysXRigidBodyType bodyType;

    PxRigidActor pxActor;

    private byte axisLocker;

    private final Entity eReference;

    public PhysXActor(PhysXRigidBodyType bodyType, Entity eReference) {
        this.bodyType = bodyType;
        this.eReference = eReference;
    }

    void build() {

        Matrix4f transform = eReference.getTransform();

        PhysXEnvironment physXEnv = OxyPhysX.getPhysXEnv();

        if (pxActor != null) {
            physXEnv.removeActorFromScene(this);
            dispose();
        }

        Vector3f pos = new Vector3f();
        Quaternionf rot = new Quaternionf();
        transform.getTranslation(pos);
        transform.getUnnormalizedRotation(rot);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxTransform pose = PxTransform.createAt(stack, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity);
            pose.setP(PxVec3.createAt(stack, MemoryStack::nmalloc, pos.x, pos.y, pos.z));
            pose.setQ(PxQuat.createAt(stack, MemoryStack::nmalloc, rot.x, rot.y, rot.z, rot.w));
            if (bodyType == PhysXRigidBodyType.Static) pxActor = physXEnv.pxPhysics.createRigidStatic(pose);
            else {
                pxActor = physXEnv.pxPhysics.createRigidDynamic(pose);
                setPhysXAxisLocking(axisLocker);
                setMass(1f);
            }
            physXEnv.addActor(pxActor);
        }
    }

    public void attachShape(PxShape shape) {
        pxActor.attachShape(shape);
    }

    public void detachShape(PxShape shape) {
        pxActor.detachShape(shape);
    }

    private boolean checkIfNull() {
        return pxActor != null;
    }

    public void setMass(float mass) {
        if (!checkIfNull()) return;
        ((PxRigidBody) pxActor).setMass(mass);
    }

    public float getMass() {
        if (!checkIfNull()) return 0;
        return ((PxRigidBody) pxActor).getMass();
    }

    PxTransform getGlobalPose() {
        return pxActor.getGlobalPose();
    }

    public void setGlobalPose(Vector3f pos, Quaternionf rot) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxVec3 pxVec3 = PxVec3.createAt(stack, MemoryStack::nmalloc, pos.x, pos.y, pos.z);
            PxQuat quat = PxQuat.createAt(stack, MemoryStack::nmalloc, rot.x, rot.y, rot.z, rot.w);
            PxTransform transform = PxTransform.createAt(stack, MemoryStack::nmalloc, pxVec3, quat);
            pxActor.setGlobalPose(transform);
        }
    }

    public void setGlobalPose(Matrix4f transform) {
        if (pxActor != null) pxActor.setGlobalPose(toPxTransform(transform));
    }

    public PhysXRigidBodyType getBodyType() {
        return bodyType;
    }

    void setPhysXRigidBodyType(PhysXRigidBodyType bodyType) {
        this.bodyType = bodyType;
    }

    void setPhysXAxisLocking(byte flags) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxRigidDynamicLockFlags pxFlags = PxRigidDynamicLockFlags.createAt(stack, MemoryStack::nmalloc, flags);
            ((PxRigidDynamic) pxActor).setRigidDynamicLockFlags(pxFlags);
        }
    }

    public void addForce(Vector3f force) {
        if (pxActor instanceof PxRigidDynamic d) {
            PxVec3 forcePxVec3 = toPxVec3(force);
            d.addForce(forcePxVec3);
            forcePxVec3.destroy();
        }
    }

    public void addTorque(Vector3f torque) {
        if (pxActor instanceof PxRigidDynamic d) {
            PxVec3 torquePxVec3 = toPxVec3(torque);
            d.addTorque(torquePxVec3);
            torquePxVec3.destroy();
        }
    }

    @Override
    public void dispose() {
        if (pxActor != null) {
            pxActor.release();
            pxActor = null;
        }
    }

    private static final float[] massArray = new float[1];
    public static final GUINode guiNode = () -> {
        if (entityContext == null) return;
        if (!entityContext.has(PhysXComponent.class)) return;

        PhysXActor actor = entityContext.get(PhysXComponent.class).getActor();
        PhysXRigidBodyType currentMode = actor.getBodyType();

        if (ImGui.treeNodeEx("Rigid Body", ImGuiTreeNodeFlags.DefaultOpen)) {
            if (ImGui.beginCombo("##hideLabelRigidBodyMode", currentMode.name())) {
                for (var modes : PhysXRigidBodyType.values()) {
                    String s = modes.name();
                    boolean isSelected = (currentMode.name().equals(s));
                    if (ImGui.selectable(s, isSelected)) {
                        actor.setPhysXRigidBodyType(modes);
                    }
                }
                ImGui.endCombo();
            }

            if (actor.bodyType == PhysXRigidBodyType.Dynamic) {
                massArray[0] = actor.getMass();
                if (ImGui.dragFloat("Rigid Body Mass", massArray)) {
                    actor.setMass(massArray[0]);
                }
            }

            if (currentMode == PhysXRigidBodyType.Dynamic && ImGui.treeNodeEx("Constraints")) {
                renderAxisLocking(actor);
                ImGui.treePop();
            }

            ImGui.treePop();
        }

    };

    private static void renderAxisLocking(PhysXActor actor) {
        ImGui.columns(2);
        ImGui.alignTextToFramePadding();
        ImGui.text("Translation:");
        ImGui.alignTextToFramePadding();
        ImGui.text("Rotation:");
        ImGui.nextColumn();

        ImGui.alignTextToFramePadding();
        ImGui.text("X:");
        ImGui.sameLine();
        boolean xLinearActive = (actor.axisLocker & PhysXAxisLocking.eLOCK_LINEAR_X.getByteValue()) == PhysXAxisLocking.eLOCK_LINEAR_X.getByteValue();
        if (ImGui.radioButton("##hideLabelXAxisLockingLinear", xLinearActive)) {
            if (xLinearActive) {
                actor.axisLocker &= ~PhysXAxisLocking.eLOCK_LINEAR_X.getByteValue();
            } else {
                actor.axisLocker |= PhysXAxisLocking.eLOCK_LINEAR_X.getByteValue();
            }
        }
        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        boolean yLinearActive = (actor.axisLocker & PhysXAxisLocking.eLOCK_LINEAR_Y.getByteValue()) == PhysXAxisLocking.eLOCK_LINEAR_Y.getByteValue();
        if (ImGui.radioButton("##hideLabelYAxisLockingLinear", yLinearActive)) {
            if (yLinearActive) {
                actor.axisLocker &= ~PhysXAxisLocking.eLOCK_LINEAR_Y.getByteValue();
            } else {
                actor.axisLocker |= PhysXAxisLocking.eLOCK_LINEAR_Y.getByteValue();
            }
        }
        ImGui.sameLine();
        ImGui.text("Z:");
        ImGui.sameLine();
        boolean zLinearActive = (actor.axisLocker & PhysXAxisLocking.eLOCK_LINEAR_Z.getByteValue()) == PhysXAxisLocking.eLOCK_LINEAR_Z.getByteValue();
        if (ImGui.radioButton("##hideLabelZAxisLockingLinear", zLinearActive)) {
            if (zLinearActive) {
                actor.axisLocker &= ~PhysXAxisLocking.eLOCK_LINEAR_Z.getByteValue();
            } else {
                actor.axisLocker |= PhysXAxisLocking.eLOCK_LINEAR_Z.getByteValue();
            }
        }

        ImGui.text("X:");
        ImGui.sameLine();
        boolean xAngularActive = (actor.axisLocker & PhysXAxisLocking.eLOCK_ANGULAR_X.getByteValue()) == PhysXAxisLocking.eLOCK_ANGULAR_X.getByteValue();
        if (ImGui.radioButton("##hideLabelXAxisLockingAngular", xAngularActive)) {
            if (xAngularActive) {
                actor.axisLocker &= ~PhysXAxisLocking.eLOCK_ANGULAR_X.getByteValue();
            } else {
                actor.axisLocker |= PhysXAxisLocking.eLOCK_ANGULAR_X.getByteValue();
            }
        }
        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        boolean yAngularActive = (actor.axisLocker & PhysXAxisLocking.eLOCK_ANGULAR_Y.getByteValue()) == PhysXAxisLocking.eLOCK_ANGULAR_Y.getByteValue();
        if (ImGui.radioButton("##hideLabelYAxisLockingAngular", yAngularActive)) {
            if (yAngularActive) {
                actor.axisLocker &= ~PhysXAxisLocking.eLOCK_ANGULAR_Y.getByteValue();
            } else {
                actor.axisLocker |= PhysXAxisLocking.eLOCK_ANGULAR_Y.getByteValue();
            }
        }
        ImGui.sameLine();
        ImGui.text("Z:");
        ImGui.sameLine();
        boolean zAngularActive = (actor.axisLocker & PhysXAxisLocking.eLOCK_ANGULAR_Z.getByteValue()) == PhysXAxisLocking.eLOCK_ANGULAR_Z.getByteValue();
        if (ImGui.radioButton("##hideLabelZAxisLockingAngular", zAngularActive)) {
            if (zAngularActive) {
                actor.axisLocker &= ~PhysXAxisLocking.eLOCK_ANGULAR_Z.getByteValue();
            } else {
                actor.axisLocker |= PhysXAxisLocking.eLOCK_ANGULAR_Z.getByteValue();
            }
        }
        ImGui.columns(1);
    }
}
