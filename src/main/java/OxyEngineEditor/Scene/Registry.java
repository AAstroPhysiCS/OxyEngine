package OxyEngineEditor.Scene;

import OxyEngineEditor.Components.EntityComponent;

import java.util.*;

/*
 * Entity Component System (ECS)
 * More info: https://github.com/skypjack/entt
 */
public class Registry {

    final Map<OxyEntity, Set<EntityComponent>> entityList = new LinkedHashMap<>();

    /*
     * add component to the registry
     * returns true if it has been successfully added.
     */
    public final void addComponent(OxyEntity entity, EntityComponent... component) {
        for (EntityComponent c : component) {
            if (c != null) {
                Set<EntityComponent> entityComponentSet = entityList.get(entity);
                entityComponentSet.removeIf(entityComponent -> entityComponent.getClass().equals(c.getClass()) || entityComponent.getClass().isInstance(c));
                entityComponentSet.add(c);
            }
        }
    }

    /*
     * returns true if the component is already in the set
     */
    public boolean has(OxyEntity entity, Class<? extends EntityComponent> destClass) {
        Set<EntityComponent> set = entityList.get(entity);
        for (EntityComponent c : set) {
            if (destClass.equals(c.getClass()))
                return true;
            if (destClass.isInstance(c))
                return true;
        }
        return false;
    }

    /*
     * gets the component from the set
     */
    public <T extends EntityComponent> T get(OxyEntity entity, Class<T> destClass) {
        Set<EntityComponent> set = entityList.get(entity);
        for (EntityComponent c : set) {
            if (c.getClass() == destClass) {
                return (T) c;
            }
            if (destClass.isInstance(c)) {
                return (T) c;
            }
        }
        return null;
    }

    /*
     * gets all the entities associated with these classes
     */
    public Set<OxyEntity> view(Class<? extends EntityComponent> destClass) {
        Set<OxyEntity> list = new LinkedHashSet<>();
        for (var entrySet : entityList.entrySet()) {
            Set<EntityComponent> value = entrySet.getValue();
            OxyEntity entity = entrySet.getKey();
            for (EntityComponent c : value) {
                if (c.getClass() == destClass) {
                    list.add(entity);
                } else if (destClass.isInstance(c)) {
                    list.add(entity);
                }
            }
        }
        return list;
    }

    /*
     * gets all the entities associated with multiple classes
     */
    @SafeVarargs
    public final Set<OxyEntity> group(Class<? extends EntityComponent>... destClasses) {
        Set<OxyEntity> list = new LinkedHashSet<>();
        for (var entrySet : entityList.entrySet()) {
            int counter = 0;
            Set<EntityComponent> value = entrySet.getValue();
            OxyEntity entity = entrySet.getKey();
            for (EntityComponent c : value) {
                for (var destClass : destClasses) {
                    if (c.getClass() == destClass) {
                        counter++;
                        if (counter == destClasses.length) {
                            list.add(entity);
                        }
                    }
                }
            }
        }
        return list;
    }

    @SafeVarargs
    public final <V extends EntityComponent> Set<V> distinct(Class<? super V>... classes) {
        Set<V> allDistinctComponents = new LinkedHashSet<>();
        for (var value : entityList.values()) {
            int counter = 0;
            for (EntityComponent c : value) {
                for (var destClass : classes) {
                    if (c.getClass() == destClass || destClass.isInstance(c)) {
                        counter++;
                        if (counter == classes.length) {
                            allDistinctComponents.add((V) c);
                        }
                    }
                }
            }
        }
        return allDistinctComponents;
    }

    @SafeVarargs
    public final <U extends EntityComponent> Set<U> distinct(RegistryPredicate<Boolean, U> predicate, Class<U> type, Class<? extends EntityComponent>... destClasses) {
        Set<U> allDistinctComponents = new LinkedHashSet<>();
        for (var entrySet : entityList.entrySet()) {
            var value = entrySet.getValue();
            var entity = entrySet.getKey();
            int counter = 0;
            for (EntityComponent c : value) {
                for (var destClass : destClasses) {
                    if (c.getClass() == destClass || destClass.isInstance(c)) {
                        counter++;
                        if (counter == destClasses.length && predicate.test(entity.get(type))) {
                            allDistinctComponents.add((U) c);
                        }
                    }
                }
            }
        }
        return allDistinctComponents;
    }
}
