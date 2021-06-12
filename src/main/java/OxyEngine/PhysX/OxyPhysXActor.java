package OxyEngine.PhysX;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Scene.OxyEntity;
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

import static OxyEngine.PhysX.OxyPhysX.matrix4fToPxTransform;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public final class OxyPhysXActor implements OxyDisposable {

    private float mass;
    private PhysXRigidBodyMode bodyMode;

    PxRigidActor pxActor;

    private final OxyEntity eReference;

    public OxyPhysXActor(PhysXRigidBodyMode bodyMode) {
        this(bodyMode, entityContext);
    }

    public OxyPhysXActor(PhysXRigidBodyMode bodyMode, OxyEntity eReference){
        this.bodyMode = bodyMode;
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

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxTransform pose = PxTransform.createAt(stack, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity);
            pose.setP(PxVec3.createAt(stack, MemoryStack::nmalloc, pos.x, pos.y, pos.z));
            pose.setQ(PxQuat.createAt(stack, MemoryStack::nmalloc, rot.x, rot.y, rot.z, rot.w));
            if (bodyMode == PhysXRigidBodyMode.Static)
                pxActor = OxyPhysX.getInstance().getPhysXEnv().pxPhysics.createRigidStatic(pose);
            else pxActor = OxyPhysX.getInstance().getPhysXEnv().pxPhysics.createRigidDynamic(pose);
            OxyPhysX.getInstance().getPhysXEnv().addActor(pxActor);
        }
        OxyPhysXGeometry oxyPhysXGeometry = eReference.get(OxyPhysXComponent.class).getGeometry();
        if(oxyPhysXGeometry != null && oxyPhysXGeometry.shape != null) pxActor.attachShape(oxyPhysXGeometry.shape);
    }

    public static final GUINode guiNode = () -> {
        if (entityContext == null) return;
        if (!entityContext.has(OxyPhysXComponent.class)) return;

        PhysXRigidBodyMode currentMode = entityContext.get(OxyPhysXComponent.class).getActor().getPhysXRigidBodyMode();

        if(ImGui.treeNodeEx("Rigid Body", ImGuiTreeNodeFlags.DefaultOpen)) {
            if (ImGui.beginCombo("##hideLabelRigidBodyMode", currentMode.name())) {
                for (var modes : PhysXRigidBodyMode.values()) {
                    String s = modes.name();
                    boolean isSelected = (currentMode.name().equals(s));
                    if (ImGui.selectable(s, isSelected)) {
                        entityContext.get(OxyPhysXComponent.class).getActor().setPhysXRigidBodyMode(modes);
                        entityContext.get(OxyPhysXComponent.class).getActor().build();
                    }
                }
                ImGui.endCombo();
            }
            ImGui.treePop();
        }

    };

    public void setMass(float mass) {
        this.mass = mass;
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
        pxActor.setGlobalPose(matrix4fToPxTransform(transform));
    }

    PhysXRigidBodyMode getPhysXRigidBodyMode() {
        return bodyMode;
    }

    void setPhysXRigidBodyMode(PhysXRigidBodyMode bodyMode){
        this.bodyMode = bodyMode;
    }

    @Override
    public void dispose() {
        pxActor.release();
        pxActor = null;
    }
}
