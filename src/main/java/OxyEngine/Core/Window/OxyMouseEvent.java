package OxyEngine.Core.Window;

import OxyEngine.Core.Window.OxyMouseEvent.Scroll;
import OxyEngine.Core.Window.OxyMouseEvent.Press;
import OxyEngine.Core.Window.OxyMouseEvent.Release;
import OxyEngine.Core.Window.OxyMouseEvent.Moved;

public sealed abstract class OxyMouseEvent implements OxyEvent permits Press, Scroll, Release, Moved {

    MouseCode mouseCode;

    OxyMouseEvent(){}

    public static final class Scroll extends OxyMouseEvent {

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

    public static final class Press extends OxyMouseEvent {
        Press(){}
    }

    public static final class Release extends OxyMouseEvent {
        Release(){}
    }

    public static final class Moved extends OxyMouseEvent {
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
