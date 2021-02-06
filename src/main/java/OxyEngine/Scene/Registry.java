package OxyEngine.Scene;

import OxyEngine.Components.EntityComponent;

import java.util.*;

/*
 * Entity Component System (ECS)
 * More info: https://github.com/skypjack/entt
 */
public class Registry {

    final Map<OxyEntity, Set<EntityComponent>> entityList = new LinkedHashMap<>();

    /*
     * add component to the registry
     */
    public final void addComponent(OxyEntity entity, EntityComponent... component) {
        Set<EntityComponent> entityComponentSet = entityList.get(entity);
        for (EntityComponent c : component) {
            if (c != null) {
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

    public void removeComponent(OxyEntity entity, EntityComponent c) {
        entityList.get(entity).remove(c);
    }

    public <T extends EntityComponent> OxyEntity getRoot(OxyEntity entity, Class<T> destClass) {
        EntityComponent familyComponent = entity.get(destClass);
        for (OxyEntity eList : entityList.keySet()) {
            if (!eList.isRoot()) continue;
            if (!eList.has(destClass)) continue;
            if (eList.get(destClass) == familyComponent) return eList;
        }
        return null;
    }
}
