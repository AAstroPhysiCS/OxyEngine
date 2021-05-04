package OxyEngine.PhysX;

import OxyEngine.Components.MeshPosition;
import OxyEngine.Components.TransformComponent;
import OxyEngine.PhysX.OxyPhysXGeometry.Box;
import OxyEngine.PhysX.OxyPhysXGeometry.Sphere;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyMaterialPool;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import physx.geomutils.PxBoxGeometry;
import physx.geomutils.PxSphereGeometry;
import physx.physics.PxMaterial;
import physx.physics.PxShape;
import physx.physics.PxShapeFlagEnum;
import physx.physics.PxShapeFlags;

import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public sealed abstract class OxyPhysXGeometry implements OxyDisposable
        permits Box, Sphere {

    protected PxMaterial material;
    protected PxShape shape;

    protected final OxyEntity eReference;

    public OxyPhysXGeometry(OxyEntity eReference){
        this.eReference = eReference;
    }

    public abstract void build();

    abstract void update();

    @Override
    public void dispose() {
        if(shape != null) shape.release();
        shape = null;
        OxyPhysXEnvironment.destroyMaterial(material);
        material = null;
    }

    public static final class Box extends OxyPhysXGeometry {

        private final Vector3f pxHalfScalar;

        public Box(Vector3f scale, OxyEntity e) {
            super(e);
            this.pxHalfScalar = scale;
        }

        public Box() {
            this(new Vector3f(entityContext.get(TransformComponent.class).scale), entityContext);
        }

        @Override
        public void build() {
            if (eReference == null) return;
            if (!eReference.has(OxyPhysXComponent.class)) return;
            if (!eReference.has(MeshPosition.class)) return;

            OxyMaterial oxyMaterial = OxyMaterialPool.getMaterial(eReference);
            if (oxyMaterial == null) throw new IllegalStateException("Geometry has no OxyMaterial");

            try (MemoryStack stack = MemoryStack.stackPush()) {
                PxBoxGeometry geometry = PxBoxGeometry.createAt(stack, MemoryStack::nmalloc, pxHalfScalar.x, pxHalfScalar.y, pxHalfScalar.z);
                PxShapeFlags shapeFlags = PxShapeFlags.createAt(stack, MemoryStack::nmalloc, (byte) (PxShapeFlagEnum.eSCENE_QUERY_SHAPE | PxShapeFlagEnum.eSIMULATION_SHAPE));

                OxyPhysXEnvironment env = OxyPhysX.getInstance().getPhysXEnv();
                material = env.createMaterial(oxyMaterial.staticFriction[0], oxyMaterial.dynamicFriction[0], oxyMaterial.restitution[0]);

                shape = env.pxPhysics.createShape(geometry, material, true, shapeFlags);
                shape.setSimulationFilterData(OxyPhysX.getInstance().getDefaultFilterData());
            }
            OxyPhysXActor physXActor = eReference.get(OxyPhysXComponent.class).getActor();
            if(physXActor != null){
                if(physXActor.pxActor != null) physXActor.pxActor.attachShape(shape);
            }
        }

        @Override
        void update() {
            try(MemoryStack stack = MemoryStack.stackPush()){
                PxBoxGeometry geometry = PxBoxGeometry.createAt(stack, MemoryStack::nmalloc, pxHalfScalar.x, pxHalfScalar.y, pxHalfScalar.z);
                shape.setGeometry(geometry);
            }
        }

        public static final float[] halfScalarArr = new float[3];

        public static final GUINode guiNode = () -> {

            if (entityContext == null) return;
            if (!entityContext.has(OxyPhysXComponent.class)) return;

            if (ImGui.treeNodeEx("Box Collider", ImGuiTreeNodeFlags.DefaultOpen)) {
                Vector3f halfScalar = ((Box) entityContext.get(OxyPhysXComponent.class).getGeometry()).getPxHalfScalar();
                halfScalarArr[0] = halfScalar.x;
                halfScalarArr[1] = halfScalar.y;
                halfScalarArr[2] = halfScalar.z;
                ImGui.text("Size:");
                ImGui.sameLine();
                ImGui.dragFloat3("BoxColliderScale", halfScalarArr);
                halfScalar.set(halfScalarArr[0], halfScalarArr[1], halfScalarArr[2]);
                ImGui.treePop();
            }
        };

        public Vector3f getPxHalfScalar() {
            return pxHalfScalar;
        }
    }

    public static final class Sphere extends OxyPhysXGeometry {

        private float r;

        public Sphere(float r, OxyEntity eReference) {
            super(eReference);
            this.r = r;
            TransformComponent t = entityContext.get(TransformComponent.class);
            if (t.scale.x != t.scale.y || t.scale.y != t.scale.z)
                throw new IllegalStateException("Sphere must have the same scale");
            this.r = entityContext.get(TransformComponent.class).scale.x;
        }

        public Sphere() {
            this(0, entityContext);
        }

        @Override
        public void build() {

            if (eReference == null) return;
            if (!eReference.has(OxyPhysXComponent.class)) return;

            OxyMaterial oxyMaterial = OxyMaterialPool.getMaterial(eReference);
            if (oxyMaterial == null) return;

            try (MemoryStack stack = MemoryStack.stackPush()) {
                PxSphereGeometry geometry = PxSphereGeometry.createAt(stack, MemoryStack::nmalloc, r);
                PxShapeFlags shapeFlags = PxShapeFlags.createAt(stack, MemoryStack::nmalloc, (byte) (PxShapeFlagEnum.eSCENE_QUERY_SHAPE | PxShapeFlagEnum.eSIMULATION_SHAPE));

                OxyPhysXEnvironment env = OxyPhysX.getInstance().getPhysXEnv();
                material = env.createMaterial(oxyMaterial.staticFriction[0], oxyMaterial.dynamicFriction[0], oxyMaterial.restitution[0]);

                shape = env.pxPhysics.createShape(geometry, material, true, shapeFlags);
                shape.setSimulationFilterData(OxyPhysX.getInstance().getDefaultFilterData());
            }
            OxyPhysXActor physXActor = eReference.get(OxyPhysXComponent.class).getActor();
            if(physXActor != null){
                if(physXActor.pxActor != null) physXActor.pxActor.attachShape(shape);
            }
        }

        @Override
        void update() {
            r = eReference.get(TransformComponent.class).scale.x;
            try(MemoryStack stack = MemoryStack.stackPush()){
                PxSphereGeometry geometry = PxSphereGeometry.createAt(stack, MemoryStack::nmalloc, r);
                shape.setGeometry(geometry);
            }
        }

        public void setRadius(float radius) {
            this.r = radius;
        }

        public float getRadius() {
            return r;
        }

        public static final float[] radiusArr = new float[1];

        public static final GUINode guiNode = () -> {

            if (entityContext == null) return;
            if (!entityContext.has(OxyPhysXComponent.class)) return;

            if (ImGui.treeNodeEx("Sphere Collider", ImGuiTreeNodeFlags.DefaultOpen)) {
                Sphere s = ((Sphere) entityContext.get(OxyPhysXComponent.class).getGeometry());
                radiusArr[0] = s.getRadius();
                ImGui.text("Radius:");
                ImGui.sameLine();
                ImGui.dragFloat("SphereColliderRadius", radiusArr);
                s.setRadius(radiusArr[0]);
                ImGui.treePop();
            }
        };
    }
}
