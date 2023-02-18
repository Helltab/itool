package icu.helltab.itool.multablequery.config.db.multi.ds01;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@MapperScan
public @interface MyMapperScan {
	@AliasFor(
		annotation = MapperScan.class
	)
	Class value() default MyMapperScan.class;


}
