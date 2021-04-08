package Scripts;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.Scene;
import OxyEngine.Scripting.ScriptableEntity;
import org.joml.Vector3f;

import static OxyEngine.System.OxyEventSystem.keyEventDispatcher;
import static org.lwjgl.glfw.GLFW.*;

public class CameraScript extends ScriptableEntity {

    public CameraScript(Scene scene, OxyEntity entity) {
        super(scene, entity);
    }

    TransformComponent transformComponent;

    @Override
    public void onCreate() {
        transformComponent = getComponent(TransformComponent.class);
    }

    @Override
    public void onUpdate(float ts) {
        Vector3f positionRef = transformComponent.position;
        Vector3f rotationRef = transformComponent.rotation;
        float horizontalSpeed = 0.05f;
        float verticalSpeed = 0.05f;

        float angle90 = (float) (-rotationRef.y + (Math.PI / 2));
        float angle = -rotationRef.y;
        if (keyEventDispatcher.getKeys()[GLFW_KEY_W]) {
            positionRef.x += Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.z += Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_S]) {
            positionRef.x -= Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.z -= Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_D]) {
            positionRef.x -= Math.cos(angle) * horizontalSpeed * ts;
            positionRef.z -= Math.sin(angle) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_A]) {
            positionRef.x += Math.cos(angle) * horizontalSpeed * ts;
            positionRef.z += Math.sin(angle) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_SPACE]) {
            positionRef.y -= verticalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_LEFT_SHIFT]) {
            positionRef.y += verticalSpeed * ts;
        }
    }
}
