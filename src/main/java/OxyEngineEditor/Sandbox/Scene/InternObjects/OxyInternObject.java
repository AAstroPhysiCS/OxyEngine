package OxyEngineEditor.Sandbox.Scene.InternObjects;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Scene;

import static OxyEngine.System.OxySystem.logger;

public class OxyInternObject extends OxyEntity implements Cloneable {

    private InternObjectFactory factory;
    public ObjectType type;

    public OxyInternObject(Scene scene) {
        super(scene);
    }

    OxyInternObject(OxyInternObject other) {
        this(other.scene);
        this.vertices = other.vertices;
        this.factory = other.factory;
        this.tcs = other.tcs;
        this.indices = other.indices;
        this.normals = other.normals;
        this.type = other.type;
    }

    public void initData() {
        if (!has(InternObjectFactory.class) || !has(Mesh.class))
            throw new IllegalStateException("Game object need to have a template or a Mesh!");

        Mesh mesh = (Mesh) get(Mesh.class);

        factory = (InternObjectFactory) get(InternObjectFactory.class);
        this.type = factory.type;
        factory.constructData(this);
        if (mesh instanceof GameObjectMesh gameObjectMesh) {
            factory.initData(this, gameObjectMesh);
        } else {
            logger.severe("Game Objects needs to have a GameObjectMesh");
            throw new IllegalStateException("Game Objects needs to have a GameObjectMesh");
        }
    }

    @Override
    public void updateData() {
        factory.updateData(this);
        ((Mesh) get(Mesh.class)).updateSingleEntityData(scene, this);
    }

    public ObjectType getType() {
        return type;
    }
}