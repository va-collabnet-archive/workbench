/**
 * 
 */
package org.dwfa.util.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kec
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Spec {
    String directory();
    String beanName() default "";
    BeanType type() default BeanType.GENERIC_BEAN;
    BeanFormat format() default BeanFormat.FILE;
    String[] constructArgs() default {};
}
