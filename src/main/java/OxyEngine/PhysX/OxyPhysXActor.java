package OxyEngine.PhysX;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Context.Scene.OxyEntity;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.UI.Panels.GUINode;
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
import physx.physics.PxRigidActor;
import physx.physics.PxRigidBody;
import physx.physics.PxRigidDynamic;

import static OxyEngine.Core.Context.Scene.SceneRuntime.entityContext;
import static OxyEngine.OxyUtils.*;


public final class OxyPhysXActor implements OxyDisposable {

    private PhysXRigidBodyType bodyType;

    PxRigidActor pxActor;

    private final OxyEntity eReference;

    public OxyPhysXActor(PhysXRigidBodyType bodyType) {
        this(bodyType, entityContext);
    }

    public OxyPhysXActor(PhysXRigidBodyType bodyType, OxyEntity eReference){
        this.bodyType = bodyType;
        this.eReference = eReference;
    }

    public void build() {

        Matrix4f transform = eReference.get(TransformComponent.class).transform;
        if(pxActor != null)
            pxActor.release();

        Vector3f scale = new Vector3f();
        transform.getScale(scale);

        Vector3f pos = new Vector3f();
        Quaternionf rot = new Quaternionf();
        transform.getTranslation(pos);
        transform.getUnnormalizedRotation(rot);

        OxyPhysXEnvironment physXEnv = OxyPhysX.getInstance().getPhysXEnv();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxTransform pose = PxTransform.createAt(stack, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity);
            pose.setP(PxVec3.createAt(stack, MemoryStack::nmalloc, pos.x, pos.y, pos.z));
            pose.setQ(PxQuat.createAt(stack, MemoryStack::nmalloc, rot.x, rot.y, rot.z, rot.w));
            if (bodyType == PhysXRigidBodyType.Static)
                pxActor = physXEnv.pxPhysics.createRigidStatic(pose);
            else pxActor = physXEnv.pxPhysics.createRigidDynamic(pose);
            physXEnv.addActor(pxActor);
        }
        OxyPhysXGeometry oxyPhysXGeometry = eReference.get(OxyPhysXComponent.class).getGeometry();
        if(oxyPhysXGeometry != null){
            if(oxyPhysXGeometry.shape != null) pxActor.attachShape(oxyPhysXGeometry.shape);
        }
    }

    public void setMass(float mass) {
        ((PxRigidBody)pxActor).setMass(mass);
    }

    PxTransform getGlobalPose() {
        return pxActor.getGlobalPose();
    }

    public void setGlobalPose(Vector3f pos, Quaternionf rot){
        try(MemoryStack stack = MemoryStack.stackPush()){
            PxVec3 pxVec3 = PxVec3.createAt(stack, MemoryStack::nmalloc, pos.x, pos.y, pos.z);
            PxQuat quat = PxQuat.createAt(stack, MemoryStack::nmalloc, rot.x, rot.y, rot.z, rot.w);
            PxTransform transform = PxTransform.createAt(stack, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity);
            transform.setP(pxVec3);
            transform.setQ(quat);
            pxActor.setGlobalPose(transform);
        }
    }

    public void setGlobalPose(Matrix4f transform){
        if(pxActor != null) pxActor.setGlobalPose(matrix4fToPxTransform(transform));
    }

    public PhysXRigidBodyType getBodyType() {
        return bodyType;
    }

    void setPhysXRigidBodytype(PhysXRigidBodyType bodytype){
        this.bodyType = bodytype;
    }

    public void addForce(Vector3f force){
        if(pxActor instanceof PxRigidDynamic d) {
            PxVec3 forcePxVec3 = vector3fToPxVec3(force);
            d.addForce(forcePxVec3);
            forcePxVec3.destroy();
        }
    }

    public void addTorque(Vector3f torque){
        if(pxActor instanceof PxRigidDynamic d) {
            PxVec3 torquePxVec3 = vector3fToPxVec3(torque);
            d.addTorque(torquePxVec3);
            torquePxVec3.destroy();
        }
    }

    @Override
    public void dispose() {
        if(pxActor != null){
            pxActor.release();
            pxActor = null;
        }
    }

    public static final GUINode guiNode = () -> {
        if (entityContext == null) return;
        if (!entityContext.has(OxyPhysXComponent.class)) return;

        PhysXRigidBodyType currentMode = entityContext.get(OxyPhysXComponent.class).getActor().getBodyType();

        if(ImGui.treeNodeEx("Rigid Body", ImGuiTreeNodeFlags.DefaultOpen)) {
            if (ImGui.beginCombo("##hideLabelRigidBodyMode", currentMode.name())) {
                for (var modes : PhysXRigidBodyType.values()) {
                    String s = modes.name();
                    boolean isSelected = (currentMode.name().equals(s));
                    if (ImGui.selectable(s, isSelected)) {
                        entityContext.get(OxyPhysXComponent.class).getActor().setPhysXRigidBodytype(modes);
                        entityContext.get(OxyPhysXComponent.class).getActor().build();
                    }
                }
                ImGui.endCombo();
            }
            ImGui.treePop();
        }

    };
}
