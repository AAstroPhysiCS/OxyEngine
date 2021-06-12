package OxyEngine.Events;

public class OxyMouseEvent extends OxyEvent {

    public OxyMouseEvent() {
        super("Mouse Event");
    }

    @Override
    public EventType getEventType() {
        return EventType.MouseEvent;
    }

    public void onMousePressed() {

    }
}
