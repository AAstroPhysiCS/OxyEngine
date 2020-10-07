package OxyEngineEditor.Components;


import OxyEngine.System.OxySystem;
import OxyEngineEditor.UI.Panels.PropertyEntry;

import java.util.ArrayList;
import java.util.List;

public interface UIEditable {

    List<Class<? extends UIEditable>> allEntityComponentChildClasses = new ArrayList<>(OxySystem.getSubClasses(UIEditable.class));

    static String[] allUIEditableNames() {
        String[] names = new String[allEntityComponentChildClasses.size()];
        int ptr = 0;
        for (Class<? extends UIEditable> classes : allEntityComponentChildClasses) {
            names[ptr++] = classes.getSimpleName();
        }
        return names;
    }

    static String[] allUIEditableFullNames() {
        String[] names = new String[allEntityComponentChildClasses.size()];
        int ptr = 0;
        for (Class<? extends UIEditable> classes : allEntityComponentChildClasses) {
            names[ptr++] = classes.getName();
        }
        return names;
    }

    PropertyEntry node();
}
