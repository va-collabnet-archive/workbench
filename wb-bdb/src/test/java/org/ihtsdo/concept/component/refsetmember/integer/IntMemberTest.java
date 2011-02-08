package org.ihtsdo.concept.component.refsetmember.integer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntMemberTest {

    private IntMember testObj1;
    private IntMember testObj2;
    private IntMember testObj3;
    
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testEqualsObject() {
        // The contract of the equals method in Object 
        // specifies that equals must implement an equivalence 
        // relation on non-null objects:
        
        // Make all the test objects be the same 
        testObj1 = makeTestObject1();
        testObj2 = makeTestObject1();
        testObj3 = makeTestObject1();

        // Test for equality (2 objects created the same) 
        assertTrue(testObj1.equals(testObj2)); 
        
        // It is reflexive: 
        // for any non-null value x, the expression x.equals(x) should return true.
        assertTrue(testObj1.equals(testObj1)); 
        
        // It is symmetric: 
        // for any non-null values x and y, the expression x.equals(y) should return true 
        // if and only if y.equals(x) returns true.
        assertTrue(testObj1.equals(testObj2) && testObj2.equals(testObj1));
        
        // It is transitive: 
        // for any non-null values x, y, and z, if x.equals(y) returns true and 
        // y.equals(z) returns true, then x.equals(z) should return true.
        assertTrue(testObj1.equals(testObj2) && testObj2.equals(testObj3)
            && testObj3.equals(testObj1));
        
        // It is consistent: 
        // for any non-null values x and y, multiple invocations of x.equals(y) 
        // should consistently return true or consistently return false, 
        // provided no information used in equals comparisons on the objects is modified.
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 

        // For any non-null value x, x.equals(null) should return false.
        assertFalse(testObj1.equals(null)); 
       
    }

    @Test
    public void testEqualsInACollection() {
        // Make both the objects be the same 
        testObj1 = makeTestObject1();
        testObj2 = makeTestObject1();
        
        // Put testComponent1 in a collection 
        Set<IntMember> coll = new java.util.HashSet<IntMember>();
        coll.add(testObj1);

        // Test for the presence of testComponent1 by using the  
        // equivalent object testComponent2
        assertTrue(coll.contains(testObj2));         

    }

    @Test
    public void testDifferentObjectsNotEqual() {
        // Make two different objects 
        try {
            testObj1 = makeTestObject1();
            testObj2 = makeTestObject2();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Test that they are not equal
        assertFalse(testObj1.equals(testObj2));         
        assertFalse(testObj2.equals(testObj1));         

    }

    private IntMember makeTestObject1() {
        
        // Create an object to test... 
        IntMember obj = new IntMember();
        obj.setIntValue(1);
        obj.setStatusAtPositionNid(1);
         
        obj.nid = 1;
        obj.primordialSapNid = 1; 
        obj.revisions = new CopyOnWriteArrayList<IntRevision>();

        IntRevision rev = new IntRevision(); 
        rev.primordialComponent = obj; 
        rev.sapNid = 1;
        rev.setIntValue(1);
        rev.setStatusAtPositionNid(1);
        obj.revisions.add(rev); 
        
        return obj; 
    }

    private IntMember makeTestObject2() {
        
        // Create an object to test... 
        IntMember obj = new IntMember();
        obj.setIntValue(2);
        obj.setStatusAtPositionNid(2);
         
        obj.nid = 2;
        obj.primordialSapNid = 2; 
        obj.revisions = new CopyOnWriteArrayList<IntRevision>();

        IntRevision rev = new IntRevision(); 
        rev.primordialComponent = obj; 
        rev.sapNid = 2;
        rev.setIntValue(2);
        rev.setStatusAtPositionNid(2);
        obj.revisions.add(rev); 
        
        return obj; 
    }
}


