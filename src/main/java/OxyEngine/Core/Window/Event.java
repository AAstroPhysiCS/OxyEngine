package OxyEngine.Core.Window;

/*
 * Tagging interface that every event should inherit from
 */
public sealed interface Event permits KeyEvent, MouseEvent, WindowEvent {
}
