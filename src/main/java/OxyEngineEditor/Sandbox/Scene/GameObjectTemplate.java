package OxyEngineEditor.Sandbox.Scene;

import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;

public abstract class GameObjectTemplate implements EntityComponent {

    ObjectType type;

    abstract void constructData(OxyGameObject e);

    abstract void updateData(OxyGameObject e);

    abstract void initData(OxyGameObject e, GameObjectMesh mesh);
}
