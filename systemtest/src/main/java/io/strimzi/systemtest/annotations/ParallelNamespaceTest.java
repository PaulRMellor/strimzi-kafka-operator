/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.systemtest.annotations;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.strimzi.systemtest.TestTags.PARALLEL_NAMESPACE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/***
 * Annotation for running parallel tests in strimzi test suite
 * please be sure that you know laws of parallel execution and concurrent programming
 * be sure that you do not use shared resources, and if you use shared resources please work with synchronization
 *
 * The difference between ParallelTest and ParallelNamespaceTest is that ParallelNamespaceTest executing another
 * Kafka cluster inside different namespace. With that multiple ParallelNamespaceTests will have multiple Kafka clusters.
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Execution(ExecutionMode.CONCURRENT)
@ResourceLock(mode = ResourceAccessMode.READ, value = "global")
@Test
@Tag(PARALLEL_NAMESPACE)
public @interface ParallelNamespaceTest {

    String value() default "";
}
