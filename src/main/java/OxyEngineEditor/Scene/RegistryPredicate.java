package OxyEngineEditor.Scene;

public interface RegistryPredicate<T, U>{
    T test(U u);
}
