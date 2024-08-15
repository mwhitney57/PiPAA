package dev.mwhitney.main;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to indicate that the targeted method needs further testing.
 * 
 * @author mwhitney57
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface NeedsTesting {
    public String info() default "More testing needs to be done on this.";
}
