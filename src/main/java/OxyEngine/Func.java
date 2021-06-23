package OxyEngine;

@FunctionalInterface
public interface Func<T> {
    void func(T event);
}
