package OxyEngine.Events;

import java.util.ArrayList;
import java.util.List;

public class OxyEventDispatcher {

    private final List<OxyEvent> eventPool = new ArrayList<>();

    public <T extends OxyEvent> void dispatch(Class<T> eventClass) {
        if(eventPool.size() != OxyEvent.EventType.values().length) {
            addClass(eventClass);
            return;
        }
        for(OxyEvent e : eventPool){
            if(e instanceof OxyKeyEvent e1){
                e1.onKeyPressed();
            } else if(e instanceof OxyMouseEvent e1){
                e1.onMousePressed();
            }
        }
    }

    private <T extends OxyEvent> void addClass(Class<T> eventClass) {
        try {
            eventPool.add(eventClass.getDeclaredConstructor().newInstance());
        } catch (Exception e) { e.printStackTrace(); }
    }
}
