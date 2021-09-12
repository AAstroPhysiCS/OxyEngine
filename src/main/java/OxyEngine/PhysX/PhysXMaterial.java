package OxyEngine.PhysX;

import OxyEngine.System.Disposable;
import physx.physics.PxMaterial;

public final class PhysXMaterial implements Disposable {

    public final float[] staticFriction, dynamicFriction, restitution;

    private final PxMaterial pxMaterialHandle;

    public PhysXMaterial(float staticFriction, float dynamicFriction, float restitution) {
        this.staticFriction = new float[]{staticFriction};
        this.dynamicFriction = new float[]{dynamicFriction};
        this.restitution = new float[]{restitution};
        var envInstance = OxyPhysX.getPhysXEnv();
        pxMaterialHandle = envInstance.createMaterial(staticFriction, dynamicFriction, restitution);
    }

    public PxMaterial getPxMaterial() {
        return pxMaterialHandle;
    }

    @Override
    public void dispose() {
        pxMaterialHandle.release();
    }
}
