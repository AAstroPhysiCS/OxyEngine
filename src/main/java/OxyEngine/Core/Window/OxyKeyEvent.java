package OxyEngine.Core.Window;

import OxyEngine.Core.Window.OxyKeyEvent.Press;
import OxyEngine.Core.Window.OxyKeyEvent.Release;
import OxyEngine.Core.Window.OxyKeyEvent.Typed;

public sealed abstract class OxyKeyEvent implements OxyEvent permits Press, Release, Typed {

    KeyCode keyCode;

    OxyKeyEvent(){}

    public static final class Press extends OxyKeyEvent {
        Press(){}
    }

    public static final class Release extends OxyKeyEvent {
        Release(){}
    }

    public static final class Typed extends OxyKeyEvent {
        Typed(){}
    }

    public KeyCode getKeyCode() {
        return keyCode;
    }
}
