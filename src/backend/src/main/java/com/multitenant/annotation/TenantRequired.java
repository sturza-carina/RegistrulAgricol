package com.multitenant.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method or all methods within the annotated class 
 * require a specific tenant context to be executed.
 * If the current tenant is "public", a 400 Bad Request exception will be thrown.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantRequired {
}
