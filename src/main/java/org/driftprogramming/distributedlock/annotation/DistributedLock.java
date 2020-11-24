package org.driftprogramming.distributedlock.annotation;

import org.driftprogramming.distributedlock.LockType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    LockType lockType() default LockType.Default;

    String value() default "";

    String[] key() default {};

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    int expireTime() default 30;

    int waitTime() default 30;
}
