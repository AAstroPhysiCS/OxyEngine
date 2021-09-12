package OxyEngine.Core.Window;

import OxyEngine.Core.Window.KeyEvent.Press;
import OxyEngine.Core.Window.KeyEvent.Release;
import OxyEngine.Core.Window.KeyEvent.Typed;

public sealed abstract class KeyEvent implements Event permits Press, Release, Typed {

    KeyCode keyCode;

    KeyEvent(){}

    public static final class Press extends KeyEvent {
        Press(){}
    }

    public static final class Release extends KeyEvent {
        Release(){}
    }

    public static final class Typed extends KeyEvent {
        Typed(){}
    }

    public KeyCode getKeyCode() {
        return keyCode;
    }
}
