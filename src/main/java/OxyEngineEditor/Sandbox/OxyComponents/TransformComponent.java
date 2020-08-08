package OxyEngineEditor.Sandbox.OxyComponents;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransformComponent implements EntityComponent {

    public final Vector3f position;
    public final Vector3f rotation;
    public final float scale;

    public Matrix4f transform;

    public TransformComponent(Vector3f position, Vector3f rotation, float scale){
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

    public TransformComponent(float scale){
        this(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), scale);
    }

    public TransformComponent(){
        this(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 1);
    }
}
