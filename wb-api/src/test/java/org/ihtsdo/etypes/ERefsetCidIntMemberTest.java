package org.ihtsdo.etypes;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.refset.cidint.TkRefsetCidIntRevision;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ERefsetCidIntMemberTest {

    private ERefsetCidIntMember testComponent1;
    private ERefsetCidIntMember testComponent2;
    private ERefsetCidIntMember testComponent3;
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
        Set<ERefsetCidIntMember> coll = new java.util.HashSet<ERefsetCidIntMember>();
        coll.add(testComponent1);

        // Test for the presence of testComponent1 by using the  
        // equivalent object testComponent2
        assertTrue(coll.contains(testComponent2));         

    }

    @Test
    public void testEqualsForTwoArrayLists() {
        // Make two ArrayLists with the same components in each 
        List<ERefsetCidIntMember> list1 = new ArrayList<ERefsetCidIntMember>();
        List<ERefsetCidIntMember> list2 = new ArrayList<ERefsetCidIntMember>();
        
        // Add components to list 1
        list1.add(makeTestComponent1());
        list1.add(makeTestComponent2());
        
        // Add components to list 2
        list2.add(makeTestComponent1());
        list2.add(makeTestComponent2());
        
        // Test to see if the two arrays are equivalent 
        assertTrue(list1.equals(list2));               

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

    
    private ERefsetCidIntMember makeTestComponent1() {
        ERefsetCidIntMember cidIntMember = new ERefsetCidIntMember();
        cidIntMember.c1Uuid = new UUID(4386, 5497);
        cidIntMember.intValue = 33;
        cidIntMember.refsetUuid = new UUID(14386, 65497);
        cidIntMember.componentUuid = new UUID(64386, 75497);
        cidIntMember.pathUuid = new UUID(4350, 5469);
        cidIntMember.statusUuid = new UUID(5386, 4497);
        cidIntMember.time = this.myTime;
        cidIntMember.primordialUuid = new UUID(320, 230);
        cidIntMember.revisions = new ArrayList<TkRefsetCidIntRevision>();
        ERefsetCidIntRevision rciv = new ERefsetCidIntRevision();
        rciv.c1Uuid = new UUID(114386, 656497);
        rciv.intValue = 99;
        rciv.pathUuid = new UUID(4350, 5469);
        rciv.statusUuid = new UUID(5386, 4497);
        rciv.time = this.myTime;
        cidIntMember.revisions.add(rciv);
        
        return cidIntMember;
    }

    private ERefsetCidIntMember makeTestComponent2() {
        ERefsetCidIntMember cidIntMember = new ERefsetCidIntMember();
        cidIntMember.c1Uuid = new UUID(111, 5497);
        cidIntMember.intValue = 17;
        cidIntMember.refsetUuid = new UUID(222, 65497);
        cidIntMember.componentUuid = new UUID(333, 75497);
        cidIntMember.pathUuid = new UUID(444, 5469);
        cidIntMember.statusUuid = new UUID(555, 4497);
        cidIntMember.time = this.myTime;
        cidIntMember.primordialUuid = new UUID(666, 230);
        cidIntMember.revisions = new ArrayList<TkRefsetCidIntRevision>();
        ERefsetCidIntRevision rciv = new ERefsetCidIntRevision();
        rciv.c1Uuid = new UUID(777, 656497);
        rciv.intValue = 16;
        rciv.pathUuid = new UUID(888, 5469);
        rciv.statusUuid = new UUID(999, 4497);
        rciv.time = this.myTime;
        cidIntMember.revisions.add(rciv);
        
        return cidIntMember;
    }


}


