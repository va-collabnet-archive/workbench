/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intsdo.junit.bdb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines configuration parameters to be passed to {@link BdbTestRunner} 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BdbTestRunnerConfig {

    /**
     * Sets the relative path of the working directory for the container. The default container work
     * path will be correct for Maven projects.
     * 
     * <p>
     * Default value: <code>test-resources/berkeley-db</code>
     * </p>
     */
    String bdbLocation() default "test-resources/berkeley-db";
    
    /**
     * Specifies classes to be executed to perform initialisation routines when the test runner is 
     * initialised (per test class)
     */
    Class<? extends BdbTestInitialiser>[] init() default {};
}
