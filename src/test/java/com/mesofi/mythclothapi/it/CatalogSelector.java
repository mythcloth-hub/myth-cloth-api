package com.mesofi.mythclothapi.it;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CatalogSelector {

  String distribution() default "";

  String lineUp();

  String series();

  String group();

  int anniversary() default 0;
}
