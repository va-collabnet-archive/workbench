package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidStr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CidCidStrMemberTest {

    private CidCidStrMember testObj1;
    private CidCidStrMember testObj2;
    private CidCidStrMember testObj3;
    
    
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
        Set<CidCidStrMember> coll = new java.util.HashSet<CidCidStrMember>();
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

    private CidCidStrMember makeTestObject1() {
        
        // Create an object to test... 
        CidCidStrMember obj = new CidCidStrMember();
        obj.setC1Nid(1);
        obj.setC2Nid(1);
        obj.setStrValue("Test object 1");
        obj.enclosingConcept = null; 
        obj.nid = 1;
        obj.primordialSapNid = 1; 
        obj.primordialUNid = 1;
        obj.additionalVersions = new ArrayList<CidCidStrRevision>(1);

        CidCidStrRevision rev = new CidCidStrRevision(); 
        rev.primordialComponent = obj; 
        rev.sapNid = 1;
        obj.additionalVersions.add(rev); 
        
        return obj; 
    }

    private CidCidStrMember makeTestObject2() {
        
        // Create an object to test... 
        CidCidStrMember obj = new CidCidStrMember();
        obj.setC1Nid(2);
        obj.setC2Nid(2);
        obj.setStrValue("Test object 2");
        obj.enclosingConcept = null; 
        obj.nid = 2;
        obj.primordialSapNid = 2; 
        obj.primordialUNid = 2;
        obj.additionalVersions = new ArrayList<CidCidStrRevision>(1);

        CidCidStrRevision rev = new CidCidStrRevision(); 
        rev.primordialComponent = obj; 
        rev.sapNid = 2;
        obj.additionalVersions.add(rev); 
        
        return obj; 
    }
}

