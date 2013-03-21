package org.intsdo.junit.bdb;

import java.util.List;

import org.junit.runners.model.FrameworkMethod;

/**
 * Indicates a class will provide ordering of the test cases within a class by observing the @{@link DependsOn} annotation on test methods.
 */
public interface OrderedDependencyRunner {

    /**
     * Returns a list of the test methods to be executed in ordered (which abides by the defined dependency order)
     * Must override {@link org.junit.runners.BlockJUnit4ClassRunner.computeTestMethods()}
     */
    List<FrameworkMethod> computeTestMethods();
    
}
