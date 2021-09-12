package OxyEngine.Core.Context.Scene;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Components.EntityFamily;
import OxyEngine.Components.UUIDComponent;
import OxyEngine.System.Disposable;

import java.util.*;

/*
 * Entity Component System (ECS)
 * More info: https://github.com/skypjack/entt
 */
public final class Registry {

    final Map<Entity, List<EntityComponent>> entityList = new LinkedHashMap<>();

    /*
     * add component to the registry
     */
    public final void addComponent(Entity entity, EntityComponent... component) {
        List<EntityComponent> entityComponentSet = entityList.get(entity);
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

    public boolean has(Entity entity, Class<? extends EntityComponent> destClass) {
        List<EntityComponent> set = entityList.get(entity);
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
    public <T extends EntityComponent> T get(Entity entity, Class<T> destClass) {
        List<EntityComponent> set = entityList.get(entity);
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
     * gets all the entities associated with this class
     */

    public Set<Entity> view(Class<? extends EntityComponent> destClass) {
        Set<Entity> list = new LinkedHashSet<>();
        for (var entrySet : entityList.entrySet()) {
            List<EntityComponent> value = entrySet.getValue();
            Entity entity = entrySet.getKey();
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

    public void removeComponent(Entity entity, EntityComponent c) {
        entityList.get(entity).remove(c);
        if(c instanceof Disposable d) d.dispose();
    }

    public Entity getRoot(Entity entity) {
        EntityFamily familyComponent = entity.getFamily();
        EntityFamily rootFamilyComponent = familyComponent.root();
        for (Entity eList : entityList.keySet()) {
            if (eList.getFamily() == rootFamilyComponent) return eList;
        }
        return null;
    }

    public Entity getEntityByUUID(UUIDComponent uuidComponent) {
        for(Entity e : entityList.keySet()){
            if(e.get(UUIDComponent.class).getUUID().equals(uuidComponent.getUUID())) return e;
        }
        return null;
    }

    public Entity getEntityByUUID(String uuid) {
        for(Entity e : entityList.keySet()){
            if(e.get(UUIDComponent.class).getUUID().equals(uuid)) return e;
        }
        return null;
    }
}
