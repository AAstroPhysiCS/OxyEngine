package Scripts;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Scripting.ScriptableEntity;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

public class PositionIteratorScript extends ScriptableEntity {

    public PositionIteratorScript(Scene scene, OxyEntity entity) {
        super(scene, entity);
    }

    public float Speed = 1f;
    public double SpeedDouble = 1.5d;
    public int SpeedInt = 2;
    public char SpeedChar;
    public short SpeedShort;
    public long SpeedLong;
    public boolean SpeedBoolean = false;
    public byte SpeedByte = 0;
    public String SpeedString = "UAHSDJHAD";
    TransformComponent TransformComponent;

    @Override
    public void onCreate() {
        TransformComponent = getComponent(TransformComponent.class);
    }

    @Override
    public void onUpdate(float ts) {
        TransformComponent.position.add(Speed, 0f, 0f);
        updateData();
    }
}
