package OxyEngine.PhysX;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Scene.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import physx.PxTopLevelFunctions;
import physx.common.JavaErrorCallback;
import physx.common.PxErrorCodeEnum;
import physx.common.PxTransform;
import physx.physics.PxFilterData;
import physx.physics.PxSceneFlagEnum;

import java.util.HashMap;
import java.util.Map;

import static OxyEngine.Core.Scene.SceneRuntime.sceneContext;
import static OxyEngine.Utils.toJOMLQuaternionf;
import static OxyEngine.Utils.toJOMLVector3f;
import static OxyEngine.System.OxySystem.logger;

public final class OxyPhysX {

    private static PhysXEnvironment physXEnv = null;
    private static PxFilterData DEFAULT_FILTER_DATA;

    static final int PHYSX_VERSION = PxTopLevelFunctions.getPHYSICS_VERSION();

    static {
        logger.info("PhysX init version: " + PHYSX_VERSION);
    }

    private OxyPhysX() {
    }

    //From the PhysX example
    static final class CustomErrorCallback extends JavaErrorCallback {

        private final Map<Integer, String> codeNames = new HashMap<>() {{
            put(PxErrorCodeEnum.eDEBUG_INFO, "DEBUG_INFO");
            put(PxErrorCodeEnum.eDEBUG_WARNING, "DEBUG_WARNING");
            put(PxErrorCodeEnum.eINVALID_PARAMETER, "INVALID_PARAMETER");
            put(PxErrorCodeEnum.eINVALID_OPERATION, "INVALID_OPERATION");
            put(PxErrorCodeEnum.eOUT_OF_MEMORY, "OUT_OF_MEMORY");
            put(PxErrorCodeEnum.eINTERNAL_ERROR, "INTERNAL_ERROR");
            put(PxErrorCodeEnum.eABORT, "ABORT");
            put(PxErrorCodeEnum.ePERF_WARNING, "PERF_WARNING");
        }};

        @Override
        public void reportError(int code, String message, String file, int line) {
            String codeName = codeNames.getOrDefault(code, "code: " + code);
            logger.severe(String.format("Nvidia PhysX: [%s] %s (%s:%d)\n", codeName, message, file, line));
        }
    }

    public static void init() {
        physXEnv = PhysXEnvironment.create(PhysXEnvironment.createSpecification()
                .setFilterShader(PxTopLevelFunctions.DefaultFilterShader())
                .setFlags(PxSceneFlagEnum.eENABLE_CCD)
                .setGravity(0f, -9.81f, 0f)
                .setCallback(CustomErrorCallback.class));
        DEFAULT_FILTER_DATA = physXEnv.createFilterData(1, 1, 0, 0);
    }

    public static void simulate() {

        physXEnv.simulatePhysics(Renderer.TS);

        for (Entity physXEntities : sceneContext.view(PhysXComponent.class)) {
            PhysXActor actor = physXEntities.get(PhysXComponent.class).getActor();
            PhysXGeometry geometry = physXEntities.get(PhysXComponent.class).getGeometry();
            if (actor == null || geometry == null) continue;

            PxTransform globalPose = actor.getGlobalPose();
            Vector3f pos = toJOMLVector3f(globalPose.getP());
            Quaternionf rot = toJOMLQuaternionf(globalPose.getQ());

            physXEntities.get(TransformComponent.class).set(pos, rot);
            physXEntities.updateTransform();
        }
    }

    public static void resetSimulation() {
        for (Entity e : sceneContext.view(PhysXComponent.class))
            e.get(PhysXComponent.class).reset();

//        physXEnv.resetScene();
    }

    public static void buildComponents() {
        for (Entity e : sceneContext.view(PhysXComponent.class)) {
            PhysXComponent physXComponent = e.get(PhysXComponent.class);
            physXComponent.getActor().build();
            physXComponent.getGeometry().build();
        }
    }

    public static void dispose() {
        DEFAULT_FILTER_DATA.destroy();
        physXEnv.dispose();
        physXEnv = null;
    }

    static PhysXEnvironment getPhysXEnv() {
        return physXEnv;
    }

    static PxFilterData getDefaultFilterData() {
        return DEFAULT_FILTER_DATA;
    }
}
