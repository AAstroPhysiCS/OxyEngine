package OxyEngine.PhysX;

import OxyEngine.Core.Scene.Entity;
import OxyEngine.System.Disposable;
import org.lwjgl.system.MemoryStack;
import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.cooking.PxCooking;
import physx.cooking.PxCookingParams;
import physx.extensions.PxDefaultAllocator;
import physx.physics.*;

import static OxyEngine.Core.Scene.SceneRuntime.sceneContext;
import static OxyEngine.PhysX.OxyPhysX.PHYSX_VERSION;

public final class PhysXEnvironment implements Disposable {

    private final PxDefaultAllocator allocator;
    private JavaErrorCallback errorCallback;

    private final PxFoundation foundation;
    final PxPhysics pxPhysics;

    final PxCooking pxCooking;
    private final PxCookingParams cookingParams;

    private PxScene scene;
    private final PxTolerancesScale tolerances;

    private final PhysXSpecification builder;

    private PhysXEnvironment(PhysXSpecification builder) {
        this.builder = builder;
        try {
            this.errorCallback = (JavaErrorCallback) builder.callbackClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        allocator = new PxDefaultAllocator();
        foundation = PxTopLevelFunctions.CreateFoundation(PHYSX_VERSION, allocator, errorCallback);

        tolerances = new PxTolerancesScale();
        pxPhysics = PxTopLevelFunctions.CreatePhysics(PHYSX_VERSION, foundation, tolerances);

        cookingParams = new PxCookingParams(tolerances);
        pxCooking = PxTopLevelFunctions.CreateCooking(PHYSX_VERSION, foundation, cookingParams);

        create(builder);
    }

    private void create(PhysXSpecification builder) {
        var cpuDispatcher = builder.cpuDispatcher;
        if (cpuDispatcher == null)
            cpuDispatcher = PxTopLevelFunctions.DefaultCpuDispatcherCreate(1);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PxSceneDesc sceneDesc = PxSceneDesc.createAt(stack, MemoryStack::nmalloc, tolerances);
            PxVec3 gravity = PxVec3.createAt(stack, MemoryStack::nmalloc, builder.xGravity, builder.yGravity, builder.zGravity);
            sceneDesc.setGravity(gravity);
            sceneDesc.setCpuDispatcher(cpuDispatcher);
            sceneDesc.setFilterShader(builder.filterShader);
            for (var flags : builder.sceneFlags) sceneDesc.getFlags().set(flags);
            scene = pxPhysics.createScene(sceneDesc);
        }
//        tolerances.destroy();
    }

    static PhysXEnvironment create(Builder builder) {
        return new PhysXEnvironment((PhysXSpecification) builder);
    }

    public static Builder createSpecification() {
        return new PhysXSpecification();
    }

    sealed interface Builder permits PhysXSpecification {

        Builder setGravity(float x, float y, float z);

        Builder setCpuDispatcher(PxCpuDispatcher dispatcher);

        Builder setFilterShader(PxSimulationFilterShader filterShader);

        Builder setFlags(int... flags);

        Builder setCallback(Class<? extends PxErrorCallback> callback);
    }

    static final class PhysXSpecification implements Builder {

        float xGravity, yGravity, zGravity;
        PxCpuDispatcher cpuDispatcher;
        PxSimulationFilterShader filterShader;
        int[] sceneFlags = new int[0];
        Class<? extends PxErrorCallback> callbackClass = PxDefaultErrorCallback.class;

        @Override
        public PhysXSpecification setGravity(float x, float y, float z) {
            this.xGravity = x;
            this.yGravity = y;
            this.zGravity = z;
            return this;
        }

        @Override
        public PhysXSpecification setCpuDispatcher(PxCpuDispatcher dispatcher) {
            if (this.cpuDispatcher != null) this.cpuDispatcher.destroy(); //destroying the default cpuDispatcher
            this.cpuDispatcher = dispatcher;
            return this;
        }

        @Override
        public PhysXSpecification setFilterShader(PxSimulationFilterShader filterShader) {
            if (this.filterShader != null) this.filterShader.destroy(); //destroying the default filterShader
            this.filterShader = filterShader;
            return this;
        }

        @Override
        public PhysXSpecification setFlags(int... sceneFlags) {
            this.sceneFlags = sceneFlags;
            return this;
        }

        @Override
        public PhysXSpecification setCallback(Class<? extends PxErrorCallback> callback) {
            this.callbackClass = callback;
            return this;
        }
    }

    PxMaterial createMaterial(float staticFriction, float dynamicFriction, float restitution) {
        return pxPhysics.createMaterial(staticFriction, dynamicFriction, restitution);
    }

    public PxFilterData createFilterData(int word0, int word1, int word2, int word3) {
        PxFilterData filterData = new PxFilterData();
        filterData.setWord0(word0);          // collision group: 0 (i.e. 1 << 0)
        filterData.setWord1(word1);          // collision mask: collide with everything
        filterData.setWord2(word2);          // no additional collision flags
        filterData.setWord3(word3);          // word3 is currently not used
        return filterData;
    }

    public void addActor(PxActor actor) {
        scene.addActor(actor);
    }

    public void removeActorFromScene(PhysXActor oxyActor) {
        if (oxyActor == null) return;
        PxActor pxActor = oxyActor.pxActor;
        if (pxActor == null) return;
        if (scene != null) scene.removeActor(pxActor);
    }

    public void lockRead() {
        scene.lockRead();
    }

    public void lockWrite() {
        scene.lockWrite();
    }

    public void unlockWrite() {
        scene.unlockWrite();
    }

    public void unlockRead() {
        scene.unlockRead();
    }

    public void simulatePhysics(float ts) {
        scene.simulate(ts);
        scene.fetchResults(true);
    }

    public void resetScene() {
        scene.release();
        scene = null;
        create(builder);
    }

    @Override
    public void dispose() {
        for (Entity e : sceneContext.view(PhysXComponent.class)) {
            e.get(PhysXComponent.class).dispose();
        }
//       TODO: invalid operation error
//        foundation.release();
        cookingParams.destroy();
        pxCooking.release();
        tolerances.destroy();
        scene.release();
        scene = null;
        errorCallback.destroy();
        pxPhysics.destroy();
        allocator.destroy();
    }
}
