package com.eappcat.sync.es.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于表示String类型的字段同步属性是text
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Text {
}
