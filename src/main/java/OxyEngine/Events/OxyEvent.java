package OxyEngine.Events;

public abstract class OxyEvent {

    private final String name;

    public enum EventType {
        KeyEvent, MouseEvent, ResizeEvent
    }

    public OxyEvent(String name){
        this.name = name;
    }

    public abstract EventType getEventType();

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getEventType().name() + "{" +
                "name='" + name + '\'' +
                '}';
    }
}
