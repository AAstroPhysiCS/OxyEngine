package OxyEngineEditor.Sandbox.Components;

import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransformComponent implements EntityComponent {

    public final Vector3f position;
    public final Vector3f rotation;
    public final Vector3f scale;

    public Matrix4f transform;

    public TransformComponent(Vector3f position, Vector3f rotation, Vector3f scale){
        this.scale = scale;
        this.position = position;
        this.rotation = rotation;
    }

    public TransformComponent(Vector3f position, Vector3f rotation, float scale){
        this(position, rotation, new Vector3f(scale, scale, scale));
    }

    public void validate(OxyEntity entity){
        if(entity != null && entity.has(BoundingBoxComponent.class) && entity.has(TransformComponent.class)){ // safety
            Vector3f scaledPos = new Vector3f(entity.get(TransformComponent.class).position);
            entity.get(BoundingBoxComponent.class).pos().mul(entity.get(TransformComponent.class).scale).add(scaledPos);
        }
    }

    public TransformComponent(Vector3f position, Vector3f rotation){
        this(position, rotation, new Vector3f(1, 1, 1));
    }

    public TransformComponent(Vector3f position, float scaleX, float scaleY, float scaleZ){
        this(position, new Vector3f(0, 0, 0), new Vector3f(scaleX, scaleY, scaleZ));
    }

    public TransformComponent(Vector3f position, float scale){
        this(position, new Vector3f(0, 0, 0), new Vector3f(scale, scale, scale));
    }

    public TransformComponent(Vector3f position){
        this(position, new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
    }

    public TransformComponent(float scaleX, float scaleY, float scaleZ){
        this(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(scaleX, scaleY, scaleZ));
    }

    public TransformComponent(float scale){
        this(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(scale, scale, scale));
    }

    public TransformComponent(){
        this(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
    }
}
