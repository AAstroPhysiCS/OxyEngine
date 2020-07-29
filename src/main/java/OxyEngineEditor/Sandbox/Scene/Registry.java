package OxyEngineEditor.Sandbox.Scene;

import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/*
 * Entity Component System (ECS)
 * More info: https://github.com/skypjack/entt
 */
public class Registry {

    final Map<OxyEntity, Set<EntityComponent>> componentList = new LinkedHashMap<>();

    /*
     * add component to the registry
     * returns true if it has been successfully added.
     */
    public final void addComponent(OxyEntity entity, EntityComponent... component) {
        for (EntityComponent c : component) {
            Set<EntityComponent> entityComponentSet = componentList.get(entity);
            entityComponentSet.removeIf(entityComponent -> entityComponent.getClass().equals(c.getClass()));
            componentList.get(entity).add(c);
        }
    }

    /*
     * returns true if the component is already in the set
     */
    public boolean has(OxyEntity entity, Class<? extends EntityComponent> destClass) {
        Set<EntityComponent> set = componentList.get(entity);
        for (EntityComponent c : set) {
            if (destClass.equals(c.getClass()))
                return true;
            if(destClass.isInstance(c))
                return true;
        }
        return false;
    }

    /*
     * gets the component from the set
     */
    public EntityComponent get(OxyEntity entity, Class<? extends EntityComponent> destClass) {
        Set<EntityComponent> set = componentList.get(entity);
        for (EntityComponent c : set) {
            if (c.getClass() == destClass) {
                return c;
            }
            if(destClass.isInstance(c)){
                return c;
            }
        }
        return null;
    }

    /*
     * gets all the entities associated with these classes
     */
    public Set<OxyEntity> view(Class<? extends EntityComponent> destClass) {
        Set<OxyEntity> list = new HashSet<>();
        for (var entrySet : componentList.entrySet()) {
            Set<EntityComponent> value = entrySet.getValue();
            OxyEntity entity = entrySet.getKey();
            for (EntityComponent c : value) {
                if (c.getClass() == destClass) {
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
        Set<OxyEntity> list = new HashSet<>();
        for (var entrySet : componentList.entrySet()) {
            Set<EntityComponent> value = entrySet.getValue();
            OxyEntity entity = entrySet.getKey();
            for (EntityComponent c : value) {
                for (var destClass : destClasses) {
                    if (c.getClass() == destClass) {
                        list.add(entity);
                    }
                }
            }
        }
        return list;
    }

    public Map<OxyEntity, Set<EntityComponent>> getComponentList() {
        return componentList;
    }
}
