package OxyEngineEditor.Scene;

import OxyEngineEditor.Components.EntityComponent;

import java.util.Set;

public interface RegistryEach {
    interface Group<U, K extends EntityComponent> {
        void each(U entity, Set<K> objects);
    }
    interface View<U, K extends EntityComponent>{
        void each(U entity, K obj);
    }
    interface Single<U>{
        void each(U entity);
    }
}
