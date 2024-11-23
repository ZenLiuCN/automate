package cn.zenliu.automate.notation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.PARAMETER,
        ElementType.RECORD_COMPONENT,
})
public @interface Info {
    /**
     * the description of Action or parameter
     */
    String value();

    /**
     * the parameter is required or not.
     */
    boolean optional() default false;

    /**
     * values as enumeration.
     */
    String[] values() default {};

    /**
     * the config reader hold class
     */
    Class<?> read() default Void.class;

    /**
     * the static field on config reader class which is {@link ConfReader}
     */
    String from() default "";
}
