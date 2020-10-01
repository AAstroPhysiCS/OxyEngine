package test;

import OxyEngine.Scripting.ScriptableEntity;

public class TestScript implements ScriptableEntity {

    public float x = 100;
    public int y = 25;

    @Override
    public void onCreate() {

    }

    @Override
    public void onUpdate(float ts) {
        x += 1;
        y += 1;
    }
}
