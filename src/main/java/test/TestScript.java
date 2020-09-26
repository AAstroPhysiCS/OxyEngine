package test;

import OxyEngineEditor.Scene.ScriptableEntity;

public class TestScript implements ScriptableEntity {

    public final float x;
    public final int y;

    public TestScript(){
        x = 100;
        y = 25;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onUpdate(float ts) {

    }
}
