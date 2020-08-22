package OxyEngine.Events;

import org.joml.Vector2f;

//record class holder, that holds the button id
public class OxyMouseEvent {

    final int buttonId = -1; //not initialized
    final Vector2f lastRayPosition = new Vector2f();

    OxyMouseEvent() {
    }

    public int getButton() {
        return buttonId;
    }

    public Vector2f getLastRayPosition() {
        return lastRayPosition;
    }
}
