package OxyEngine.PhysX;

import physx.physics.PxRigidDynamicLockFlagEnum;

public enum PhysXAxisLocking {
    eLOCK_LINEAR_X(PxRigidDynamicLockFlagEnum.eLOCK_LINEAR_X),
    eLOCK_LINEAR_Y(PxRigidDynamicLockFlagEnum.eLOCK_LINEAR_Y),
    eLOCK_LINEAR_Z(PxRigidDynamicLockFlagEnum.eLOCK_LINEAR_Z),
    eLOCK_ANGULAR_X(PxRigidDynamicLockFlagEnum.eLOCK_ANGULAR_X),
    eLOCK_ANGULAR_Y(PxRigidDynamicLockFlagEnum.eLOCK_ANGULAR_Y),
    eLOCK_ANGULAR_Z(PxRigidDynamicLockFlagEnum.eLOCK_ANGULAR_Z);

    private final int byteValue;

    PhysXAxisLocking(int byteValue){
        this.byteValue = byteValue;
    }

    public int getByteValue() {
        return byteValue;
    }
}
