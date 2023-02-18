
package icu.helltab.itool.common.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Topic flag of page request
 * @author helltab
 * @version 1.0
 * @date 2022/2/27 15:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Paged {
}
