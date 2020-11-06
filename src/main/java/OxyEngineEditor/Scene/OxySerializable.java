package OxyEngineEditor.Scene;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OxySerializable {
    String info() default "";
}
