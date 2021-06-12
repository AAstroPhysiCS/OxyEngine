package OxyEngine.Components;

import OxyEngine.System.OxySystem;

import java.util.ArrayList;
import java.util.List;

/*
 * A tagging interface
 */
public interface EntityComponent {
    List<Class<? extends EntityComponent>> allEntityComponentChildClasses = new ArrayList<>(OxySystem.getSubClasses(EntityComponent.class));
}
