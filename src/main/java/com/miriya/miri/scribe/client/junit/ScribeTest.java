package com.miriya.miri.scribe.client.junit;

import org.junit.platform.commons.annotation.Testable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated test classes to run scribe tests.
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Testable
public @interface ScribeTest {
    /**
     * Gets files of scribes to run. If empty then all scribes will be run.
     * @return the scribe files
     */
    String[] files() default {};
}
