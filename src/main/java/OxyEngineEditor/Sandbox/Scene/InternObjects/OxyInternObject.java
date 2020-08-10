package OxyEngineEditor.Sandbox.Scene.InternObjects;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Sandbox.OxyComponents.InternObjectMesh;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Scene;

import static OxyEngine.System.OxySystem.logOut;

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
        assert has(InternObjectFactory.class) && has(Mesh.class) : logOut("Game object need to have a template or a Mesh!");

        Mesh mesh = (Mesh) get(Mesh.class);

        factory = (InternObjectFactory) get(InternObjectFactory.class);
        this.type = factory.type;
        factory.constructData(this);
        assert mesh instanceof InternObjectMesh : logOut("Intern Object needs to have a InternObjectMesh");
        factory.initData(this, (InternObjectMesh) mesh);
    }

    @Override
    public void updateData() {
        factory.constructData(this);
        ((Mesh) get(Mesh.class)).updateSingleEntityData(scene, this);
    }

    public ObjectType getType() {
        return type;
    }
}