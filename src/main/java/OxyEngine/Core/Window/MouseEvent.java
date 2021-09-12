package OxyEngine.Core.Window;

import OxyEngine.Core.Window.MouseEvent.Scroll;
import OxyEngine.Core.Window.MouseEvent.Press;
import OxyEngine.Core.Window.MouseEvent.Release;
import OxyEngine.Core.Window.MouseEvent.Moved;

public sealed abstract class MouseEvent implements Event permits Press, Scroll, Release, Moved {

    MouseCode mouseCode;

    MouseEvent(){}

    public static final class Scroll extends MouseEvent {

        float xOffset, yOffset;

        Scroll(){}

        void setXOffset(float xOffset) {
            this.xOffset = xOffset;
        }

        void setYOffset(float yOffset) {
            this.yOffset = yOffset;
        }

        public float getXOffset() {
            return xOffset;
        }

        public float getYOffset() {
            return yOffset;
        }
    }

    public static final class Press extends MouseEvent {
        Press(){}
    }

    public static final class Release extends MouseEvent {
        Release(){}
    }

    public static final class Moved extends MouseEvent {
        float x, y;

        Moved(){}

        void setX(float xOffset) {
            this.x = xOffset;
        }

        void setY(float yOffset) {
            this.y = yOffset;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

}
