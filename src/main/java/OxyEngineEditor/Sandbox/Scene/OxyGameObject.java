package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;

import static OxyEngine.System.OxySystem.logger;

public class OxyGameObject extends OxyEntity implements Cloneable {

    private GameObjectTemplate template;
    //TODO: CHANGE MESH to MeshCOMPONENT
    private GameObjectMesh mesh;

    OxyGameObject(Scene scene) {
        super(scene);
    }

    OxyGameObject(OxyGameObject other){
        this(other.scene);
        this.mesh = other.mesh;
        this.vertices = other.vertices;
        this.template = other.template;
        this.tcs = other.tcs;
        this.indices = other.indices;
        this.normals = other.normals;
        this.type = other.type;
    }

    @Override
    public void initData(Mesh mesh) {
        if(!has(GameObjectTemplate.class)) throw new IllegalStateException("Game object need to have a template!");

        template = (GameObjectTemplate) get(GameObjectTemplate.class);
        template.constructData(this);
        if(mesh instanceof GameObjectMesh gameObjectMesh){
            this.mesh = gameObjectMesh;
            template.initData(this, gameObjectMesh);
        } else {
            logger.severe("Game Objects needs to have a GameObjectMesh");
            throw new IllegalStateException("Game Objects needs to have a GameObjectMesh");
        }
        this.type = template.type;
    }

    @Override
    public void updateData() {
        template.constructData(this);
        mesh.updateSingleEntityData(scene, this);
    }
}