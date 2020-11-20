package OxyEngine.Components;

import OxyEngine.System.OxySystem;

import java.util.ArrayList;
import java.util.List;

/*
 * A tagging interface
 */
public interface EntityComponent {

    List<Class<? extends EntityComponent>> allEntityComponentChildClasses = new ArrayList<>(OxySystem.getSubClasses(EntityComponent.class));

    static String[] allComponentNames() {
        String[] names = new String[allEntityComponentChildClasses.size()];
        int ptr = 0;
        for (Class<? extends EntityComponent> classes : allEntityComponentChildClasses) {
            names[ptr++] = classes.getSimpleName();
        }
        return names;
    }

    static String[] allComponentFullNames() {
        String[] names = new String[allEntityComponentChildClasses.size()];
        int ptr = 0;
        for (Class<? extends EntityComponent> classes : allEntityComponentChildClasses) {
            names[ptr++] = classes.getName();
        }
        return names;
    }
}
