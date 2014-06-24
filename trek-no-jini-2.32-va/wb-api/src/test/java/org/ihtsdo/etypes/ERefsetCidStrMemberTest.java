package org.ihtsdo.etypes;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_string.TkRefexUuidStringRevision;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ERefsetCidStrMemberTest {

    private ERefsetCidStrMember testComponent1;
    private ERefsetCidStrMember testComponent2;
    private ERefsetCidStrMember testComponent3;
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
        Set<ERefsetCidStrMember> coll = new java.util.HashSet<ERefsetCidStrMember>();
        coll.add(testComponent1);

        // Test for the presence of testComponent1 by using the  
        // equivalent object testComponent2
        assertTrue(coll.contains(testComponent2));         

    }

    @Test
    public void testEqualsForTwoArrayLists() {
        // Make two ArrayLists with the same components in each 
        List<ERefsetCidStrMember> list1 = new ArrayList<ERefsetCidStrMember>();
        List<ERefsetCidStrMember> list2 = new ArrayList<ERefsetCidStrMember>();
        
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

    
    private ERefsetCidStrMember makeTestComponent1() {
        
        ERefsetCidStrMember member = new ERefsetCidStrMember();
        member.componentUuid = new UUID(64386, 75497);
        member.uuid1 = new UUID(111, 222);
        member.string1 = "FIRST VALUE"; 
        member.refsetUuid = new UUID(14386, 65497);
        member.pathUuid = new UUID(4350, 5469);
        member.statusUuid = new UUID(5386, 4497);
        member.time = this.myTime;
        member.primordialUuid = new UUID(320, 230);
        // Add additionalIdComponents 
        member.additionalIds = new ArrayList<TkIdentifier>();
        EIdentifierUuid ac = new EIdentifierUuid();
        ac.authorityUuid = new UUID(4350, 5469);
        ac.denotation = new UUID(4350, 5469);
        ac.pathUuid = new UUID(4350, 5469);
        ac.statusUuid = new UUID(5386, 4497);
        ac.time = this.myTime;
        member.additionalIds.add(ac);       
        // Add extraVersions 
        member.revisions = new ArrayList<TkRefexUuidStringRevision>();
        ERefsetCidStrRevision rsv = new ERefsetCidStrRevision();
        rsv.uuid1 = new UUID(4350, 5469);
        rsv.string1 = "VALUE 1"; 
        rsv.pathUuid = new UUID(4350, 5469);
        rsv.statusUuid = new UUID(5386, 4497);
        rsv.time = this.myTime;
        member.revisions.add(rsv);
        
        return member;
    }

    private ERefsetCidStrMember makeTestComponent2() {
        
        ERefsetCidStrMember member = new ERefsetCidStrMember();
        member.componentUuid = new UUID(111, 75497);
        member.uuid1 = new UUID(222, 222);
        member.string1 = "SECOND VALUE"; 
        member.refsetUuid = new UUID(333, 65497);
        member.pathUuid = new UUID(444, 5469);
        member.statusUuid = new UUID(555, 4497);
        member.time = this.myTime;
        member.primordialUuid = new UUID(666, 230);
        // Add additionalIdComponents 
        member.additionalIds = new ArrayList<TkIdentifier>();
        EIdentifierUuid ac = new EIdentifierUuid();
        ac.authorityUuid = new UUID(888, 5469);
        ac.denotation = new UUID(999, 5469);
        ac.pathUuid = new UUID(1111, 5469);
        ac.statusUuid = new UUID(2222, 4497);
        ac.time = this.myTime;
        member.additionalIds.add(ac);       
        // Add extraVersions 
        member.revisions = new ArrayList<TkRefexUuidStringRevision>();
        ERefsetCidStrRevision rsv = new ERefsetCidStrRevision();
        rsv.uuid1 = new UUID(3333, 5469);
        rsv.string1 = "VALUE 2"; 
        rsv.pathUuid = new UUID(4444, 5469);
        rsv.statusUuid = new UUID(5555, 4497);
        rsv.time = this.myTime;
        member.revisions.add(rsv);
        
        return member;

    }


}



