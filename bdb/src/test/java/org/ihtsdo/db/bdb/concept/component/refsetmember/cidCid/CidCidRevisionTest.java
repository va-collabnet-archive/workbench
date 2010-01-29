package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCid;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CidCidRevisionTest {

    private CidCidRevision testObj1;
    private CidCidRevision testObj2;
    private CidCidRevision testObj3;
    
    
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
        try {
            testObj1 = makeTestObject1();
            testObj2 = makeTestObject1();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Put testComponent1 in a collection 
        Set<CidCidRevision> coll = new java.util.HashSet<CidCidRevision>();
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

    private CidCidRevision makeTestObject1() {
        
        // Create an object to test... 
        CidCidRevision obj = new CidCidRevision();
        obj.setC1id(1);
        obj.setC2id(1);

        CidCidMember member = new CidCidMember();
        member.additionalVersions = null;
        member.enclosingConcept = null; 
        member.nid = 1;
        member.primordialSapNid = 1; 
        member.primordialUNid = 1;
        member.setC1Nid(1);
        member.setC2Nid(1);
        member.setStatusAtPositionNid(1);

        obj.primordialComponent = member; 
        obj.sapNid = 1; 
        
        return obj; 
    }

    private CidCidRevision makeTestObject2() {
        
        // Create an object to test... 
        CidCidRevision obj = new CidCidRevision();
        obj.setC1id(2);
        obj.setC2id(2);

        CidCidMember member = new CidCidMember();
        member.additionalVersions = null;
        member.enclosingConcept = null; 
        member.nid = 2;
        member.primordialSapNid = 2; 
        member.primordialUNid = 2;
        member.setC1Nid(2);
        member.setC2Nid(2);
        member.setStatusAtPositionNid(2);

        obj.primordialComponent = member; 
        obj.sapNid = 2; 
        
        return obj; 
    }
}

