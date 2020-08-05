package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;

import static OxyEngine.System.OxySystem.logger;

public class OxyGameObject extends OxyEntity implements Cloneable {

    private GameObjectFactory template;

    OxyGameObject(Scene scene) {
        super(scene);
    }

    OxyGameObject(OxyGameObject other) {
        this(other.scene);
        this.vertices = other.vertices;
        this.template = other.template;
        this.tcs = other.tcs;
        this.indices = other.indices;
        this.normals = other.normals;
        this.type = other.type;
    }

    public void initData() {
        if (!has(GameObjectFactory.class) || !has(Mesh.class))
            throw new IllegalStateException("Game object need to have a template or a Mesh!");

        Mesh mesh = (Mesh) get(Mesh.class);

        template = (GameObjectFactory) get(GameObjectFactory.class);
        this.type = template.type;
        template.constructData(this);
        if (mesh instanceof GameObjectMesh gameObjectMesh) {
            template.initData(this, gameObjectMesh);
        } else {
            logger.severe("Game Objects needs to have a GameObjectMesh");
            throw new IllegalStateException("Game Objects needs to have a GameObjectMesh");
        }
    }

    @Override
    public void updateData() {
        template.updateData(this);
        ((Mesh) get(Mesh.class)).updateSingleEntityData(scene, this);
    }
}