package OxyEngine.PhysX;

import OxyEngine.Components.BoundingBox;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Context.Renderer.Mesh.OpenGLMesh;
import OxyEngine.Core.Context.Scene.Entity;
import OxyEngine.PhysX.PhysXGeometry.Box;
import OxyEngine.PhysX.PhysXGeometry.Sphere;
import OxyEngine.PhysX.PhysXGeometry.TriangleMesh;
import OxyEngineEditor.UI.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import physx.common.PxBoundedData;
import physx.common.PxQuat;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.cooking.PxTriangleMeshDesc;
import physx.geomutils.*;
import physx.physics.PxShape;
import physx.physics.PxShapeFlagEnum;
import physx.physics.PxShapeFlags;
import physx.support.Vector_PxU32;
import physx.support.Vector_PxVec3;

import static OxyEngine.Core.Context.Scene.SceneRuntime.entityContext;
import static OxyEngine.Utils.toPxTransform;
import static OxyEngine.Utils.toPxVec3;


public sealed abstract class PhysXGeometry
        permits Box, Sphere, TriangleMesh {

    protected final Entity eReference;

    public PhysXGeometry(Entity eReference) {
        this.eReference = eReference;
    }

    abstract void build();

    public String getColliderType() {
        return this.getClass().getSimpleName();
    }

    @SuppressWarnings("preview")
    static Class<?> getClassBasedOnType(String colliderType) {
        for (Class<?> classesThatInherit : PhysXGeometry.class.getPermittedSubclasses()) {
            String simpleName = classesThatInherit.getSimpleName();
            if (simpleName.equals(colliderType)) {
                return classesThatInherit;
            }
        }
        return null;
    }

    public static final GUINode guiNode = () -> {
        if (entityContext == null) return;
        if (!entityContext.has(PhysXComponent.class)) return;

        PhysXGeometry geometry = entityContext.get(PhysXComponent.class).getGeometry();

        if (geometry instanceof TriangleMesh t) {
            if (ImGui.treeNodeEx("Mesh Collider", ImGuiTreeNodeFlags.DefaultOpen)) {
                ImGui.columns(2);
                ImGui.text("Is Convex");
                ImGui.nextColumn();
                if (ImGui.button("##hideLabelConvexButton", 20, 20)) {
//                    TriangleMesh.isConvex = !TriangleMesh.isConvex;
                }
                ImGui.columns(1);
                ImGui.treePop();
            }
        } else if (geometry instanceof Box b) {
            if (ImGui.treeNodeEx("Box Collider", ImGuiTreeNodeFlags.DefaultOpen)) {
                float[] halfScalarArr = new float[3];
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
        } else if (geometry instanceof Sphere s) {
            if (ImGui.treeNodeEx("Sphere Collider", ImGuiTreeNodeFlags.DefaultOpen)) {
                float[] radiusArr = new float[1];
                radiusArr[0] = s.getRadius();
                ImGui.text("Radius:");
                ImGui.sameLine();
                ImGui.dragFloat("SphereColliderRadius", radiusArr);
                s.setRadius(radiusArr[0]);
                ImGui.treePop();
            }
        }
    };

    public static final class TriangleMesh extends PhysXGeometry {

        public TriangleMesh(Entity eReference) {
            super(eReference);
        }

        @Override
        void build() {
            if (eReference == null) return;
            if (!eReference.has(PhysXComponent.class)) return;

            PhysXMaterial physXMaterial = eReference.get(PhysXComponent.class).getMaterial();
            TransformComponent transformComponent = eReference.get(TransformComponent.class);
            Vector3f entityScale = new Vector3f();
            transformComponent.transform.getScale(entityScale);

            OpenGLMesh mesh = eReference.get(OpenGLMesh.class);
            float[] vertices = mesh.getVertices();
            int[] indices = mesh.getIndices();

            var envInstance = OxyPhysX.getPhysXEnv();
            var callback = envInstance.pxPhysics.getPhysicsInsertionCallback();

            PhysXActor physXActor = eReference.get(PhysXComponent.class).getActor();
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PxVec3 vec3VerticesTemp = PxVec3.createAt(stack, MemoryStack::nmalloc, 0f, 0f, 0f);
                Vector_PxVec3 pxVertices = new Vector_PxVec3();
                Vector_PxU32 pxIndices = new Vector_PxU32();

                int vertPtr = 0;
                for (OpenGLMesh.Submesh submesh : mesh.getSubmeshes()) {

                    int startVertex = submesh.baseVertex();
                    int lengthVertex = submesh.vertexCount();

                    for(int i = startVertex; i < startVertex + lengthVertex; i++){
                        vec3VerticesTemp.setX(vertices[vertPtr++]);
                        vec3VerticesTemp.setY(vertices[vertPtr++]);
                        vec3VerticesTemp.setZ(vertices[vertPtr++]);
                        vertPtr += 20;
                        pxVertices.push_back(vec3VerticesTemp);
                    }

                    int startIndex = submesh.baseIndex();
                    int lengthIndex = submesh.indexCount();

                    for (int i = startIndex; i < startIndex + lengthIndex; i++)
                        pxIndices.push_back(indices[i]);

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

                    PxTriangleMesh pxTriangleMesh = envInstance.pxCooking.createTriangleMesh(meshDesc, callback);
                    if (pxTriangleMesh == null) throw new IllegalStateException("PhysX Cooking failed!");

                    PxShapeFlags shapeFlags = PxShapeFlags.createAt(stack, MemoryStack::nmalloc, (byte) (PxShapeFlagEnum.eSCENE_QUERY_SHAPE | PxShapeFlagEnum.eSIMULATION_SHAPE));

                    Vector3f submeshScale = new Vector3f();
                    submesh.t().transform.getScale(submeshScale);

//                    new Matrix4f(transformComponent.transform).mulLocal(submesh.t().transform).getScale(submeshScale);

                    //TODO: I DONT KNOW WHY THIS DOES NOT FUCKING WORK WITH A CERTAIN AMOUNT OF SCALE! (WITH SCALE: 1, it does work)
                    PxVec3 localScale = toPxVec3(submeshScale.mul(entityScale));
                    PxQuat localQuat = PxQuat.createAt(stack, MemoryStack::nmalloc);

                    PxMeshScale pxMeshScale = PxMeshScale.createAt(stack, MemoryStack::nmalloc, localScale, localQuat);
                    localScale.destroy();

                    PxTriangleMeshGeometry meshGeometry = PxTriangleMeshGeometry.createAt(stack, MemoryStack::nmalloc, pxTriangleMesh, pxMeshScale);
                    PxShape shape = envInstance.pxPhysics.createShape(meshGeometry, physXMaterial.getPxMaterial(), true, shapeFlags);
                    PxTransform transform = toPxTransform(submesh.t().transform);
                    shape.setLocalPose(transform);
                    transform.destroy();
                    shape.setSimulationFilterData(OxyPhysX.getDefaultFilterData());

                    physXActor.attachShape(shape);
                    shape.release();

                    pxVertices.clear();
                    pxIndices.clear();
                }

                pxVertices.destroy();
                pxIndices.destroy();
                //dont need them
            }

        }
    }

    public static final class Box extends PhysXGeometry {

        private final Vector3f pxHalfScalar = new Vector3f(1f, 1f, 1f);

        public Box(Entity e) {
            super(e);
        }

        private void calculateBounds(){
            Vector3f scale = eReference.get(TransformComponent.class).scale;
            BoundingBox aabb = eReference.get(OpenGLMesh.class).getAABB();
            Vector3f min = aabb.min();
            Vector3f max = aabb.max();
            this.pxHalfScalar.set(new Vector3f(max).sub(min).div(2)).mul(scale);
        }

        @Override
        void build() {
            if (eReference == null) return;
            if (!eReference.has(PhysXComponent.class)) return;

            PhysXEnvironment env = OxyPhysX.getPhysXEnv();
            PhysXMaterial physXMaterial = eReference.get(PhysXComponent.class).getMaterial();

            calculateBounds();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                PxBoxGeometry geometry = PxBoxGeometry.createAt(stack, MemoryStack::nmalloc, pxHalfScalar.x, pxHalfScalar.y, pxHalfScalar.z);
                PxShapeFlags shapeFlags = PxShapeFlags.createAt(stack, MemoryStack::nmalloc, (byte) (PxShapeFlagEnum.eSCENE_QUERY_SHAPE | PxShapeFlagEnum.eSIMULATION_SHAPE));

                PxShape shape = env.pxPhysics.createShape(geometry, physXMaterial.getPxMaterial(), true, shapeFlags);
                shape.setSimulationFilterData(OxyPhysX.getDefaultFilterData());
                PhysXActor physXActor = eReference.get(PhysXComponent.class).getActor();
                physXActor.attachShape(shape);
                shape.release();
            }
        }

        public Vector3f getPxHalfScalar() {
            return pxHalfScalar;
        }
    }

    public static final class Sphere extends PhysXGeometry {

        private float r;

        public Sphere(Entity eReference) {
            super(eReference);
        }

        private void calculateRadius(){
            Vector3f scale = eReference.get(TransformComponent.class).scale;
            BoundingBox aabb = eReference.get(OpenGLMesh.class).getAABB();
            Vector3f min = aabb.min();
            Vector3f max = aabb.max();
            Vector3f scaleDest = new Vector3f(max).sub(min).div(2).mul(scale);

            if (Math.round(scaleDest.x) != Math.round(scaleDest.y) || Math.round(scaleDest.y) != Math.round(scaleDest.z))
                throw new IllegalStateException("Sphere must have the same scale");
            this.r = scaleDest.x;
        }

        @Override
        void build() {

            if (eReference == null) return;
            if (!eReference.has(PhysXComponent.class)) return;

            PhysXMaterial physXMaterial = eReference.get(PhysXComponent.class).getMaterial();
            calculateRadius();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                PxSphereGeometry geometry = PxSphereGeometry.createAt(stack, MemoryStack::nmalloc, r);
                PxShapeFlags shapeFlags = PxShapeFlags.createAt(stack, MemoryStack::nmalloc, (byte) (PxShapeFlagEnum.eSCENE_QUERY_SHAPE | PxShapeFlagEnum.eSIMULATION_SHAPE));

                PhysXEnvironment env = OxyPhysX.getPhysXEnv();

                PxShape shape = env.pxPhysics.createShape(geometry, physXMaterial.getPxMaterial(), true, shapeFlags);
                shape.setSimulationFilterData(OxyPhysX.getDefaultFilterData());
                PhysXActor physXActor = eReference.get(PhysXComponent.class).getActor();
                physXActor.attachShape(shape);
            }
        }

        public void setRadius(float radius) {
            this.r = radius;
        }

        public float getRadius() {
            return r;
        }
    }
}
