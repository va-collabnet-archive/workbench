package org.ihtsdo.etypes;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EConceptAttributesVersionTest {

    private EConceptAttributesVersion testComponent1;
    private EConceptAttributesVersion testComponent2;
    private EConceptAttributesVersion testComponent3;
    protected long myTime = Long.MIN_VALUE;
    
    @Before
    public void setUp() throws Exception {
        this.myTime = System.currentTimeMillis(); 
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testEqualsObject() {
        // Set the current time to use for object creation
        this.myTime = System.currentTimeMillis(); 
        
        // The contract of the equals method in Object 
        // specifies that equals must implement an equivalence 
        // relation on non-null objects:
        
        // Make all the components be the same 
        testComponent1 = makeTestComponent1();
        testComponent2 = makeTestComponent1();
        testComponent3 = makeTestComponent1();

        // Test for equality (2 objects created the same) 
        assertTrue(testComponent1.equals(testComponent2)); 
        
        // It is reflexive: 
        // for any non-null value x, the expression x.equals(x) should return true.
        assertTrue(testComponent1.equals(testComponent1)); 
        
        // It is symmetric: 
        // for any non-null values x and y, the expression x.equals(y) should return true 
        // if and only if y.equals(x) returns true.
        assertTrue(testComponent1.equals(testComponent2) && testComponent2.equals(testComponent1));
        
        // It is transitive: 
        // for any non-null values x, y, and z, if x.equals(y) returns true and 
        // y.equals(z) returns true, then x.equals(z) should return true.
        assertTrue(testComponent1.equals(testComponent2) && testComponent2.equals(testComponent3)
            && testComponent3.equals(testComponent1));
        
        // It is consistent: 
        // for any non-null values x and y, multiple invocations of x.equals(y) 
        // should consistently return true or consistently return false, 
        // provided no information used in equals comparisons on the objects is modified.
        assertTrue(testComponent1.equals(testComponent2)); 
        assertTrue(testComponent1.equals(testComponent2)); 
        assertTrue(testComponent1.equals(testComponent2)); 
        assertTrue(testComponent1.equals(testComponent2)); 
        assertTrue(testComponent1.equals(testComponent2)); 
        assertTrue(testComponent1.equals(testComponent2)); 
        assertTrue(testComponent1.equals(testComponent2)); 
        assertTrue(testComponent1.equals(testComponent2)); 
        assertTrue(testComponent1.equals(testComponent2)); 
        assertTrue(testComponent1.equals(testComponent2)); 

        // For any non-null value x, x.equals(null) should return false.
        assertFalse(testComponent1.equals(null)); 
       
        // Two objects known to be different should return false.
        testComponent1 = makeTestComponent1();
        testComponent2 = makeTestComponent2();
        assertFalse(testComponent1.equals(testComponent2)); 
       
    }

    @Test
    public void testEqualsInACollection() {
        // Make both the components be the same 
        testComponent1 = makeTestComponent1();
        testComponent2 = makeTestComponent1();
        
        // Put testComponent1 in a collection 
        Set<EConceptAttributesVersion> coll = new java.util.HashSet<EConceptAttributesVersion>();
        coll.add(testComponent1);

        // Test for the presence of testComponent1 by using the  
        // equivalent object testComponent2
        assertTrue(coll.contains(testComponent2));         

    }

    private EConceptAttributesVersion makeTestComponent1() {
        EConceptAttributesVersion testComponent = new EConceptAttributesVersion();
        testComponent.defined = false;
        testComponent.pathUuid = new UUID(4, 5);
        testComponent.statusUuid = new UUID(8, 9);
        testComponent.time = this.myTime;
        return testComponent;
    }

    private EConceptAttributesVersion makeTestComponent2() {
        EConceptAttributesVersion testComponent = new EConceptAttributesVersion();
        testComponent.defined = true;
        testComponent.pathUuid = new UUID(8, 7);
        testComponent.statusUuid = new UUID(6, 5);
        testComponent.time = this.myTime;
        return testComponent;
    }

}
