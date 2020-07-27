package OxyEngineEditor.Sandbox.OxyComponents;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransformComponent implements EntityComponent {

    public Vector3f position, rotation;
    public float scale;

    public Matrix4f transform;

    public TransformComponent(Matrix4f transform){
        this.transform = transform;
    }

    public TransformComponent(Vector3f position, Vector3f rotation, float scale){
        this(new Matrix4f().scale(scale).translate(position).rotateX(rotation.x).rotateY(rotation.y).rotateZ(rotation.z));
        this.scale = scale;
        this.position = position;
        this.rotation = rotation;
    }

    public TransformComponent(Vector3f position, Vector3f rotation){
        this(position, rotation, 1);
    }

    public TransformComponent(Vector3f position, float scale){
        this(position, new Vector3f(0, 0, 0), scale);
    }

    public TransformComponent(Vector3f position){
        this(position, new Vector3f(0, 0, 0), 1);
    }

    public TransformComponent(){
        this(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 1);
    }
}
