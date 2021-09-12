package OxyEngine.Core.Window;

import OxyEngine.Core.Window.WindowEvent.WindowResizeEvent;
import OxyEngine.Core.Window.WindowEvent.WindowCloseEvent;

public abstract sealed class WindowEvent implements Event permits WindowResizeEvent, WindowCloseEvent {

    public static final class WindowResizeEvent extends WindowEvent {

        private float width, height;

        WindowResizeEvent(){}

        void setWidth(float width) {
            this.width = width;
        }

        void setHeight(float height) {
            this.height = height;
        }

        public float getHeight() {
            return height;
        }

        public float getWidth() {
            return width;
        }
    }

    public static final class WindowCloseEvent extends WindowEvent {
        WindowCloseEvent(){}
    }
}
