package OxyEngine.Core.Window;

@FunctionalInterface
public interface EventFunc<T> {
    void func(T event);
}
