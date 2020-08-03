package OxyEngine.System;

public class OxyTimestep {

    private float time;
    private double deltaTime;

    public OxyTimestep(float time){
        this.time = time;
    }

    public void setDeltaTime(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    public void setTimestep(float time) {
        this.time = time;
    }

    public float getSeconds() {
        return time;
    }

    public float getMilliseconds(){
        return time * 1000;
    }

    public double getDeltaTime() {
        return deltaTime;
    }
}
