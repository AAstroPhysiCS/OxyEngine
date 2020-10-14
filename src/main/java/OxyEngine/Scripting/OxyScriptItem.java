package OxyEngine.Scripting;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OxyScriptItem {

    private final ScriptableEntity e;

    private final Field[] fields;
    private final Method[] methods;

    public OxyScriptItem(ScriptableEntity e, Field[] allFields, Method[] allMethods) {
        this.e = e;
        this.fields = allFields;
        this.methods = allMethods;
    }

    public static final record ScriptEntry(String name, Object e) {}

    public ScriptEntry[] getFieldsAsObject() {
        ScriptEntry[] objects = new ScriptEntry[fields.length];
        for (int i = 0; i < fields.length; i++) {
            try {
                objects[i] = new ScriptEntry(fields[i].getName(), fields[i].get(e));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

    private static final ExecutorService scriptExecutor = Executors.newSingleThreadExecutor();

    public void invokeMethod(String nameOfMethod, Object... args) {
        scriptExecutor.submit(() -> {
            for (Method m : methods) {
                if (m.getName().equals(nameOfMethod)) {
                    try {
                        m.invoke(e, args);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void suspendAll(){
        scriptExecutor.shutdown();
    }
}
