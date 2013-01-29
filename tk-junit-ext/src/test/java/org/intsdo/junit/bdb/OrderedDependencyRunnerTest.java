package org.intsdo.junit.bdb;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the {@link BdbTestRunner}'s compliance as an {@link OrderedDependencyRunner} by running a number of test methods and 
 * checking each runs after any other dependant test methods. 
 */
@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig( bdbLocation = "" )
public class OrderedDependencyRunnerTest {

    private static ArrayList<String> executedTests = new ArrayList<>();
    
    @Test    
    public void testA() {
        System.out.println("Execute A");
        executedTests.add("A");
    }

    @Test
    @DependsOn("testA")
    public void testB() {
        System.out.println("Execute B");
        executedTests.add("B");
        
        assertTrue(executedTests.contains("A"));
    }

    @Test
    @DependsOn("testE")
    public void testC() {
        System.out.println("Execute C");
        executedTests.add("C");
        
        assertTrue(executedTests.contains("E"));
    }

    @Test
    @DependsOn("testA")
    public void testD() {
        System.out.println("Execute D");
        executedTests.add("D");
        
        assertTrue(executedTests.contains("A"));
        
    }

    @Test
    @DependsOn("testB")
    public void testE() {
        System.out.println("Execute E");
        executedTests.add("E");
        
        assertTrue(executedTests.contains("B"));
    }
}
