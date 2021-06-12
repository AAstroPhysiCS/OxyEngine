package OxyEngine.PhysX;

import OxyEngine.Components.MeshPosition;
import OxyEngine.Components.TransformComponent;
import OxyEngine.PhysX.OxyPhysXGeometry.Box;
import OxyEngine.PhysX.OxyPhysXGeometry.Sphere;
import OxyEngine.PhysX.OxyPhysXGeometry.TriangleMesh;
import OxyEngine.Scene.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyMaterialPool;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import physx.common.PxBoundedData;
import physx.common.PxVec3;
import physx.cooking.PxTriangleMeshDesc;
import physx.geomutils.PxBoxGeometry;
import physx.geomutils.PxSphereGeometry;
import physx.geomutils.PxTriangleMesh;
import physx.geomutils.PxTriangleMeshGeometry;
import physx.physics.PxMaterial;
import physx.physics.PxShape;
import physx.physics.PxShapeFlagEnum;
import physx.physics.PxShapeFlags;
import physx.support.Vector_PxU32;
import physx.support.Vector_PxVec3;

import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public sealed abstract class OxyPhysXGeometry implements OxyDisposable
        permits Box, Sphere, TriangleMesh {

    protected PxMaterial material;
    protected PxShape shape;

    protected final OxyEntity eReference;

    public OxyPhysXGeometry(OxyEntity eReference) {
        this.eReference = eReference;
    }

    public abstract void build();

    abstract void update();

    @Override
    public void dispose() {
        if (shape != null) shape.release();
        shape = null;
        OxyPhysXEnvironment.destroyMaterial(material);
        material = null;
    }

    public static final class TriangleMesh extends OxyPhysXGeometry {

        public TriangleMesh(OxyEntity eReference) {
            super(eReference);
        }

        @Override
        public void build() {
            if (eReference == null) return;
            if (!eReference.has(OxyPhysXComponent.class)) return;

            OxyMaterial oxyMaterial = OxyMaterialPool.getMaterial(eReference);
            if (oxyMaterial == null) throw new IllegalStateException("Geometry has no OxyMaterial");

            Vector_PxVec3 pxVertices = new Vector_PxVec3();
            Vector_PxU32 pxIndices = new Vector_PxU32();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                //preparing the buffer for vertices and indices
                PxVec3 vec3VerticesTemp = PxVec3.createAt(stack, MemoryStack::nmalloc, 0f, 0f, 0f);
                TransformComponent eReferenceTransform = eReference.get(TransformComponent.class);
                for (int i = 0; i < eReference.getVertices().length; ) {
                    Vector4f vec4fTemp = new Vector4f(eReference.getVertices()[i++], eReference.getVertices()[i++], eReference.getVertices()[i++], 1.0f)
                            .mul(eReferenceTransform.transform);
                    vec3VerticesTemp.setX(vec4fTemp.x);
                    vec3VerticesTemp.setY(vec4fTemp.y);
                    vec3VerticesTemp.setZ(vec4fTemp.z);
                    i += 9;
                    pxVertices.push_back(vec3VerticesTemp);
                }
                for (int i = 0; i < eReference.getIndices().length; i++)
                    pxIndices.push_back(eReference.getIndices()[i]);

                PxBoundedData pxMeshDataVertices = PxBoundedData.createAt(stack, MemoryStack::nmalloc);
                pxMeshDataVertices.setCount(pxVertices.size());
                pxMeshDataVertices.setStride(PxVec3.SIZEOF);
                pxMeshDataVertices.setData(pxVertices.data());

                PxBoundedData pxMeshDataTriangles = PxBoundedData.createAt(stack, MemoryStack::nmalloc);
                pxMeshDataTriangles.setCount(pxIndices.size() / 3);
                pxMeshDataTriangles.setStride(4 * 3);
                pxMeshDataTriangles.setData(pxIndices.data());

                PxTriangleMeshDesc meshDesc = PxTriangleMeshDesc.createAt(stack, MemoryStack::nmalloc);
                meshDesc.setPoints(pxMeshDataVertices);
                meshDesc.setTriangles(pxMeshDataTriangles);

                var envInstance = OxyPhysX.getInstance().getPhysXEnv();

                var callback = envInstance.pxPhysics.getPhysicsInsertionCallback();
                PxTriangleMesh pxTriangleMesh = envInstance.pxCooking.createTriangleMesh(meshDesc, callback);
                if (pxTriangleMesh == null) throw new IllegalStateException("PhysX Cooking failed!");

                PxShapeFlags shapeFlags = PxShapeFlags.createAt(stack, MemoryStack::nmalloc, (byte) (PxShapeFlagEnum.eSCENE_QUERY_SHAPE | PxShapeFlagEnum.eSIMULATION_SHAPE));
                material = envInstance.createMaterial(oxyMaterial.staticFriction[0], oxyMaterial.dynamicFriction[0], oxyMaterial.restitution[0]);

                PxTriangleMeshGeometry meshGeometry = PxTriangleMeshGeometry.createAt(stack, MemoryStack::nmalloc, pxTriangleMesh);
                shape = envInstance.pxPhysics.createShape(meshGeometry, material, true, shapeFlags);
                shape.setSimulationFilterData(OxyPhysX.getInstance().getDefaultFilterData());

                //dont need them
                pxIndices.destroy();
                pxVertices.destroy();
            }
            OxyPhysXActor physXActor = eReference.get(OxyPhysXComponent.class).getActor();
            if (physXActor != null) {
                if (physXActor.pxActor != null) physXActor.pxActor.attachShape(shape);
            }
        }

        private static boolean isConvex = false;

        public static final GUINode guiNode = () -> {

            if (entityContext == null) return;
            if (!entityContext.has(OxyPhysXComponent.class)) return;

            if (ImGui.treeNodeEx("Mesh Collider", ImGuiTreeNodeFlags.DefaultOpen)) {
                ImGui.columns(2);
                ImGui.text("Is Convex");
                ImGui.nextColumn();
                if (ImGui.button("##hideLabelConvexButton", 20, 20)) {
                    isConvex = !isConvex;
                }
                ImGui.columns(1);
                ImGui.treePop();
            }
        };

        @Override
        void update() {

        }
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
            OxyPhysXActor oxyActor = eReference.get(OxyPhysXComponent.class).getActor();
            if (oxyActor != null) {
                if (oxyActor.pxActor != null) oxyActor.pxActor.attachShape(shape);
            }
        }

        @Override
        void update() {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PxBoxGeometry geometry = PxBoxGeometry.createAt(stack, MemoryStack::nmalloc, pxHalfScalar.x, pxHalfScalar.y, pxHalfScalar.z);
                shape.setGeometry(geometry);
            }
        }

        public static final float[] halfScalarArr = new float[3];

        public static final GUINode guiNode = () -> {

            if (entityContext == null) return;
            if (!entityContext.has(OxyPhysXComponent.class)) return;

            OxyPhysXGeometry geometry = entityContext.get(OxyPhysXComponent.class).getGeometry();

            if (ImGui.treeNodeEx("Box Collider", ImGuiTreeNodeFlags.DefaultOpen)) {
                if(geometry instanceof Box b){
                    Vector3f halfScalar = b.getPxHalfScalar();
                    halfScalarArr[0] = halfScalar.x;
                    halfScalarArr[1] = halfScalar.y;
                    halfScalarArr[2] = halfScalar.z;
                    ImGui.text("Size:");
                    ImGui.sameLine();
                    ImGui.dragFloat3("BoxColliderScale", halfScalarArr);
                    halfScalar.set(halfScalarArr[0], halfScalarArr[1], halfScalarArr[2]);
                    ImGui.treePop();
                }
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
            if (physXActor != null) {
                if (physXActor.pxActor != null) physXActor.pxActor.attachShape(shape);
            }
        }

        @Override
        void update() {
//            r = eReference.get(TransformComponent.class).scale.x;
            try (MemoryStack stack = MemoryStack.stackPush()) {
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
