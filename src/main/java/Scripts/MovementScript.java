package Scripts;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.KeyCode;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.Scene;
import OxyEngine.Scripting.ScriptableEntity;
import org.joml.Vector3f;

public class MovementScript extends ScriptableEntity {

    public MovementScript(Scene scene, OxyEntity entity) {
        super(scene, entity);
    }

    TransformComponent transformComponent;

    public float SpeedHorizontal = 0.05f;
    public float SpeedVertical = 0.05f;

    @Override
    public void onCreate() {
        transformComponent = getComponent(TransformComponent.class);
    }

    @Override
    public void onUpdate(float ts) {
        Vector3f positionRef = transformComponent.position;
        Vector3f rotationRef = transformComponent.rotation;

        float angle90 = (float) (-rotationRef.y + (Math.PI / 2));
        float angle = -rotationRef.y;
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
    }
}
