package OxyEngine.Core.Window;

/*
 * Tagging interface that every event should inherit from
 */
public sealed interface OxyEvent permits OxyKeyEvent, OxyMouseEvent, WindowEvent {
}
