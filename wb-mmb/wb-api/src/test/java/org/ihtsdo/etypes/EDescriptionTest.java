package org.ihtsdo.etypes;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EDescriptionTest {

    private EDescription testComponent1;
    private EDescription testComponent2;
    private EDescription testComponent3;
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
       
    }

    @Test
    public void testEqualsInACollection() {
        // Make both the components be the same 
        testComponent1 = makeTestComponent1();
        testComponent2 = makeTestComponent1();
        
        // Put testComponent1 in a collection 
        Set<EDescription> coll = new java.util.HashSet<EDescription>();
        coll.add(testComponent1);

        // Test for the presence of testComponent1 by using the  
        // equivalent object testComponent2
        assertTrue(coll.contains(testComponent2));         

    }


    @Test
    public void testDifferentObjectsNotEqual() {
        // Make two different objects 
        testComponent1 = makeTestComponent1();
        testComponent2 = makeTestComponent2();
        
        // Test that they are not equal
        assertFalse(testComponent1.equals(testComponent2));         
        assertFalse(testComponent2.equals(testComponent1));         

    }

    private EDescription makeTestComponent1() {
        EDescription desc = new EDescription();
        desc.conceptUuid  = new UUID(11, 12);
        desc.initialCaseSignificant = false;
        desc.lang = "en";
        desc.text = "hello world";          
        desc.typeUuid = new UUID(13, 14);
        desc.pathUuid = new UUID(4, 5);
        desc.statusUuid = new UUID(8, 9);
        desc.time = this.myTime;
        desc.primordialUuid = new UUID(20, 30);
        desc.revisions = new ArrayList<EDescriptionRevision>();

        EDescriptionRevision edv = new EDescriptionRevision();
        edv.initialCaseSignificant = true;
        edv.lang = "en-uk";
        edv.text = "hello world 2";
        edv.typeUuid  = new UUID(13, 14);
        edv.pathUuid = new UUID(4, 5);
        edv.statusUuid = new UUID(8, 9);
        edv.time = this.myTime;
        desc.revisions.add(edv);
        return desc;
    }

    private EDescription makeTestComponent2() {
        EDescription desc = new EDescription();
        desc.conceptUuid  = new UUID(3, 9);
        desc.initialCaseSignificant = false;
        desc.lang = "en";
        desc.text = "goodbye world";          
        desc.typeUuid = new UUID(8, 6);
        desc.pathUuid = new UUID(3, 5);
        desc.statusUuid = new UUID(6, 2);
        desc.time = this.myTime;
        desc.primordialUuid = new UUID(2, 18);
        desc.revisions = new ArrayList<EDescriptionRevision>();
        EDescriptionRevision edv = new EDescriptionRevision();
        edv.initialCaseSignificant = true;
        edv.lang = "en-uk";
        edv.text = "goodbye world 2";
        edv.typeUuid  = new UUID(13, 14);
        edv.pathUuid = new UUID(4, 5);
        edv.statusUuid = new UUID(8, 9);
        edv.time = this.myTime;
        desc.revisions.add(edv);
        
        return desc;
    }


}
