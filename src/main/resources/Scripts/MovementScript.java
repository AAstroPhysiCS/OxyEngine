package Scripts;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Context.Scene.OxyEntity;
import OxyEngine.Core.Context.Scene.Scene;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.KeyCode;
import OxyEngine.PhysX.OxyPhysXActor;
import OxyEngine.PhysX.OxyPhysXComponent;
import OxyEngine.Scripting.ScriptableEntity;
import org.joml.Vector3f;

import javax.xml.crypto.dsig.Transform;

public final class MovementScript extends ScriptableEntity {

    public MovementScript(Scene scene, OxyEntity entity) {
        super(scene, entity);
    }

    TransformComponent transformComponent;
    OxyPhysXComponent physXComponent;
    OxyEntity cameraEntity;
    OxyEntity playerEntity;

    public float SpeedHorizontal = 10000f;
    public float SpeedVertical = 5000f;

    @Override
    public void onCreate() {
        transformComponent = getComponent(TransformComponent.class);
        physXComponent = getComponent(OxyPhysXComponent.class);
        cameraEntity = getEntityByName("Camera");
        playerEntity = getEntityByName("Sphere");
    }

    @Override
    public void onUpdate(float ts) {
        //basic movement system without physX
        Vector3f positionRef = transformComponent.position;
        Vector3f rotationRef = transformComponent.rotation;

        float angle90 = (float) (-rotationRef.y + (Math.PI / 2));
        float angle = -rotationRef.y;
        if (physXComponent == null) {
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_W)) {
                positionRef.x += Math.cos(angle90) * SpeedHorizontal * ts;
                positionRef.z += Math.sin(angle90) * SpeedHorizontal * ts;
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_S)) {
                positionRef.x -= Math.cos(angle90) * SpeedHorizontal * ts;
                positionRef.z -= Math.sin(angle90) * SpeedHorizontal * ts;
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_D)) {
                positionRef.x -= Math.cos(angle) * SpeedHorizontal * ts;
                positionRef.z -= Math.sin(angle) * SpeedHorizontal * ts;
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_A)) {
                positionRef.x += Math.cos(angle) * SpeedHorizontal * ts;
                positionRef.z += Math.sin(angle) * SpeedHorizontal * ts;
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_SPACE)) {
                positionRef.y -= SpeedVertical * ts;
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_LEFT_SHIFT)) {
                positionRef.y += SpeedVertical * ts;
            }
        } else {
            OxyPhysXActor actor = physXComponent.getActor();
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_W)) {
                actor.addForce(new Vector3f((float) -Math.cos(angle90) * SpeedHorizontal * ts, 0f, (float) Math.sin(angle90) * SpeedHorizontal * ts));
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_S)) {
                actor.addForce(new Vector3f((float) Math.cos(angle90) * SpeedHorizontal * ts, 0f, (float) -Math.sin(angle90) * SpeedHorizontal * ts));
            }
            /*if (Input.isKeyPressed(KeyCode.GLFW_KEY_D)) {
                actor.addForce(new Vector3f((float) Math.cos(angle) * SpeedHorizontal * ts, 0f, (float) -Math.sin(angle) * SpeedHorizontal * ts));
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_A)) {
                actor.addForce(new Vector3f((float) -Math.cos(angle) * SpeedHorizontal * ts, 0f, (float) Math.sin(angle) * SpeedHorizontal * ts));
            }*/
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_SPACE)) {
                actor.addForce(new Vector3f(0f, (float) SpeedVertical * ts * 10, 0f));
            }
            /*if (Input.isKeyPressed(KeyCode.GLFW_KEY_LEFT_SHIFT)) {
                actor.addForce(new Vector3f(0f, (float) -SpeedVertical * ts, 0f));
            }*/
            if(cameraEntity != null) cameraEntity.transformLocallyWithoutRotation();
        }
    }
}
