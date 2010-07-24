package org.ihtsdo.etypes;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.tk.concept.component.relationship.TkRelationshipRevision;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ERelationshipTest {

    private ERelationship testComponent1;
    private ERelationship testComponent2;
    private ERelationship testComponent3;
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
        Set<ERelationship> coll = new java.util.HashSet<ERelationship>();
        coll.add(testComponent1);

        // Test for the presence of testComponent1 by using the  
        // equivalent object testComponent2
        assertTrue(coll.contains(testComponent2));         

    }

    @Test
    public void testEqualsForTwoArrayLists() {
        // Make two ArrayLists with the same components in each 
        List<ERelationship> list1 = new ArrayList<ERelationship>();
        List<ERelationship> list2 = new ArrayList<ERelationship>();
        
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

    
    private ERelationship makeTestComponent1() {
        
        ERelationship rel = new ERelationship();
        rel.c1Uuid = new UUID(40, 50);
        rel.c2Uuid = new UUID(41, 52);
        rel.characteristicUuid = new UUID(42, 53);
        rel.refinabilityUuid = new UUID(43, 54);
        rel.relGroup = 22; 
        rel.typeUuid = new UUID(44, 55);
        rel.pathUuid = new UUID(45, 56);
        rel.statusUuid = new UUID(86, 97);
        rel.time = this.myTime;
        rel.primordialUuid = new UUID(20, 30);
        rel.revisions = new ArrayList<TkRelationshipRevision>();
        ERelationshipRevision erv = new ERelationshipRevision();
        erv.characteristicUuid  = new UUID(861, 947);
        erv.refinabilityUuid  = new UUID(586, 937);
        erv.group = 3; 
        erv.typeUuid  = new UUID(846, 957);
        erv.pathUuid = new UUID(425, 526);
        erv.statusUuid = new UUID(846, 967);
        erv.time = this.myTime;
        rel.revisions.add(erv);

        return rel;
    }

    private ERelationship makeTestComponent2() {
        ERelationship rel = new ERelationship();
        rel.c1Uuid = new UUID(60, 61);
        rel.c2Uuid = new UUID(62, 63);
        rel.characteristicUuid = new UUID(64, 65);
        rel.refinabilityUuid = new UUID(66, 67);
        rel.relGroup = 50; 
        rel.typeUuid = new UUID(68, 69);
        rel.pathUuid = new UUID(70, 71);
        rel.statusUuid = new UUID(72, 73);
        rel.time = this.myTime;
        rel.primordialUuid = new UUID(74, 75);
        rel.revisions = new ArrayList<TkRelationshipRevision>();
        ERelationshipRevision erv = new ERelationshipRevision();
        erv.characteristicUuid  = new UUID(860, 861);
        erv.refinabilityUuid  = new UUID(862, 863);
        erv.group = 5; 
        erv.typeUuid  = new UUID(864, 865);
        erv.pathUuid = new UUID(866, 867);
        erv.statusUuid = new UUID(868, 869);
        erv.time = this.myTime;
        rel.revisions.add(erv);

        return rel;
    }


}


