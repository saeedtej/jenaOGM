package jenaOGM;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: saeed.y
 * Date: 8/16/13
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SubClassOf {
    public Class<?> value();
}
