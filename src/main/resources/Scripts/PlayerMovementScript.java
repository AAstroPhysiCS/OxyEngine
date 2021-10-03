package Scripts;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Scene.DefaultModelType;
import OxyEngine.Core.Scene.Entity;
import OxyEngine.Core.Scene.Scene;
import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.KeyCode;
import OxyEngine.Core.Window.MouseCode;
import OxyEngine.PhysX.*;
import OxyEngine.Scripting.ScriptableEntity;
import org.joml.Vector3f;

public final class PlayerMovementScript extends ScriptableEntity {

    public PlayerMovementScript(Scene scene, Entity entity) {
        super(scene, entity);
    }

    TransformComponent transformComponentPlayer, transformComponentCamera;
    PhysXComponent physXComponent;
    Entity cameraEntity;

    public float SpeedHorizontal = 10000f;
    public float SpeedVertical = 5000f;

    @Override
    public void onCreate() {
        transformComponentPlayer = getComponent(TransformComponent.class);
        physXComponent = getComponent(PhysXComponent.class);
        cameraEntity = getEntityByName("Player Camera");
        transformComponentCamera = cameraEntity.get(TransformComponent.class);
    }

    @Override
    public void onUpdate(float ts) {
        Vector3f positionRef = transformComponentPlayer.position;
        Vector3f rotationRef = transformComponentCamera.rotation;

        float angle90 = (float) (-rotationRef.y + (Math.PI / 2));
        float angle = -rotationRef.y;
        if (physXComponent != null) {
            PhysXActor actor = physXComponent.getActor();
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_W)) {
                actor.addForce(new Vector3f((float) Math.cos(angle) * SpeedHorizontal * ts, 0f, (float) -Math.sin(angle) * SpeedHorizontal * ts));
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_S)) {
                actor.addForce(new Vector3f((float) -Math.cos(angle) * SpeedHorizontal * ts, 0f, (float) Math.sin(angle) * SpeedHorizontal * ts));
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_D)) {
                actor.addForce(new Vector3f((float) Math.cos(angle) * SpeedHorizontal * ts, 0f, (float) -Math.sin(angle) * SpeedHorizontal * ts));
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_A)) {
                actor.addForce(new Vector3f((float) -Math.cos(angle) * SpeedHorizontal * ts, 0f, (float) Math.sin(angle) * SpeedHorizontal * ts));
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_SPACE)) {
                actor.addForce(new Vector3f(0f, (float) SpeedVertical * ts * 10, 0f));
            }
            if (Input.isKeyPressed(KeyCode.GLFW_KEY_LEFT_SHIFT)) {
                actor.addForce(new Vector3f(0f, (float) -SpeedVertical * ts, 0f));
            }
        }

        if (Input.isMouseButtonPressed(MouseCode.GLFW_MOUSE_BUTTON_1)) {
//                Vector3f forward = new Vector3f();
//                cameraEntity.get(TransformComponent.class).transform.getRow(2, forward);
            //Entity bullet = createEntity(DefaultModelType.Sphere);
            //bullet.get(TransformComponent.class).set(new Vector3f(0, 10f, 0), new Vector3f(), new Vector3f(0.01f));
            //bullet.updateTransform();
                /*PhysXComponent physXComponent = new PhysXComponent(bullet);
                physXComponent.setGeometryAs(new PhysXGeometry.Sphere(bullet));
                physXComponent.setMaterial(new PhysXMaterial(0.5f, 0.5f, 0.5f));
                physXComponent.getActor().setBodyType(PhysXRigidBodyType.Dynamic);
                physXComponent.getActor().build();
                physXComponent.getGeometry().build();

                physXComponent.getActor().addForce(forward.normalize());

                bullet.addComponent(physXComponent);*/
        }
    }
}
