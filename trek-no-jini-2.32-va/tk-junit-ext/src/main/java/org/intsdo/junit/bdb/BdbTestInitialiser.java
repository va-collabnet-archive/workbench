package org.intsdo.junit.bdb;

/**
 * Defines a class which will perform an initialisation routine to be called when the test class is created.
 * @see BdbTestRunnerConfig
 */
public interface BdbTestInitialiser {

    void init();
    
}
