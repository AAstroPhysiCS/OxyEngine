package Scripts;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Scripting.ScriptableEntity;
import OxyEngine.Core.Scene.Entity;
import OxyEngine.Core.Scene.Scene;

public final class PositionIteratorScript extends ScriptableEntity {

    public PositionIteratorScript(Scene scene, Entity entity) {
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
        TransformComponent.position.add(Speed * ts, 0f, 0f);
        updateData();
    }
}
