package org.ihtsdo.etypes;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.refset.cidcidcid.TkRefsetCidCidCidRevision;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ERefsetCidCidCidMemberTest {

    private ERefsetCidCidCidMember testComponent1;
    private ERefsetCidCidCidMember testComponent2;
    private ERefsetCidCidCidMember testComponent3;
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
        Set<ERefsetCidCidCidMember> coll = new java.util.HashSet<ERefsetCidCidCidMember>();
        coll.add(testComponent1);

        // Test for the presence of testComponent1 by using the  
        // equivalent object testComponent2
        assertTrue(coll.contains(testComponent2));         

    }

    @Test
    public void testEqualsForTwoArrayLists() {
        // Make two ArrayLists with the same components in each 
        List<ERefsetCidCidCidMember> list1 = new ArrayList<ERefsetCidCidCidMember>();
        List<ERefsetCidCidCidMember> list2 = new ArrayList<ERefsetCidCidCidMember>();
        
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

    
    private ERefsetCidCidCidMember makeTestComponent1() {
        
        ERefsetCidCidCidMember member = new ERefsetCidCidCidMember();
        member.c1Uuid = new UUID(1234, 1111);
        member.c2Uuid = new UUID(1234, 2222);
        member.c3Uuid = new UUID(1234, 3333);
        member.componentUuid = new UUID(64386, 75497);
        member.pathUuid = new UUID(4350, 5469);
        member.primordialUuid = new UUID(320, 230);
        member.refsetUuid = new UUID(14386, 65497);
        member.statusUuid = new UUID(5386, 4497);
        member.time = this.myTime;
        member.revisions = new ArrayList<TkRefsetCidCidCidRevision>();
        ERefsetCidCidCidRevision rsv = new ERefsetCidCidCidRevision();
        rsv.c1Uuid = new UUID(4444, 4444);
        rsv.c2Uuid = new UUID(5555, 5555);
        rsv.c3Uuid = new UUID(6666, 6666);
        rsv.pathUuid = new UUID(7777, 7777);
        rsv.statusUuid = new UUID(8888, 8888);
        rsv.time = this.myTime;
        member.revisions.add(rsv);
        
        return member;
    }

    private ERefsetCidCidCidMember makeTestComponent2() {
        
        ERefsetCidCidCidMember member = new ERefsetCidCidCidMember();
        member.c1Uuid = new UUID(4321, 1111);
        member.c2Uuid = new UUID(4321, 2222);
        member.c3Uuid = new UUID(4321, 3333);
        member.componentUuid = new UUID(4321, 4444);
        member.pathUuid = new UUID(4321, 5555);
        member.primordialUuid = new UUID(4321, 6666);
        member.refsetUuid = new UUID(4321, 7777);
        member.statusUuid = new UUID(4321, 8888);
        member.time = this.myTime;
        member.revisions = new ArrayList<TkRefsetCidCidCidRevision>();
        ERefsetCidCidCidRevision rsv = new ERefsetCidCidCidRevision();
        rsv.c1Uuid = new UUID(555, 777);
        rsv.c2Uuid = new UUID(444, 777);
        rsv.c3Uuid = new UUID(333, 777);
        rsv.pathUuid = new UUID(222, 777);
        rsv.statusUuid = new UUID(111, 777);
        rsv.time = this.myTime;
        member.revisions.add(rsv);
        
        return member;
    }


}


