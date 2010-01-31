package org.ihtsdo.db.bdb.concept.component.relationship;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EConceptAttributesVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RelationshipTest {

    private Relationship testObj1;
    private Relationship testObj2;
    private Relationship testObj3;
    
    EConcept testConcept;
    EConcept test2Concept;
    String dbTarget;
  
    @Before
    public void setUp() throws Exception {
        dbTarget = "target/" + UUID.randomUUID();
        Bdb.setup(dbTarget);
        testConcept = new EConcept();
        EConceptAttributes eca1 = new EConceptAttributes();
        eca1.primordialComponentUuid = UUID.randomUUID();
        eca1.setStatusUuid(UUID.randomUUID());
        eca1.setPathUuid(UUID.randomUUID());
        eca1.setTime(System.currentTimeMillis());
        eca1.setDefined(true);
        
        EConceptAttributesVersion ecav = new EConceptAttributesVersion();
        ecav.setDefined(false);
        ecav.setPathUuid(eca1.getPathUuid());
        ecav.setStatusUuid(eca1.getStatusUuid());
        ecav.setTime(eca1.getTime() + 10);
        eca1.extraVersions = new ArrayList<EConceptAttributesVersion>(1);
        eca1.extraVersions.add(ecav);
        
        testConcept.setConceptAttributes(eca1);
        test2Concept = new EConcept();
        EConceptAttributes eca2 = new EConceptAttributes();
        eca2.primordialComponentUuid = UUID.randomUUID();
        eca2.setStatusUuid(UUID.randomUUID());
        eca2.setPathUuid(UUID.randomUUID());
        eca2.setDefined(false);
        eca2.setTime(eca1.getTime());
        test2Concept.setConceptAttributes(eca2);
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
        Set<Relationship> coll = new java.util.HashSet<Relationship>();
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

    private Relationship makeTestObject1() {
        
        // Create an object to test... 
        Relationship obj = new Relationship();
        obj.setC2Id(1);
        obj.setCharacteristicId(1);
        obj.setGroup(1);
        obj.setRefinabilityId(1);
        obj.setTypeId(1);
        obj.setStatusAtPositionNid(1);
        try {
            Concept c;
            c = Concept.get(testConcept);
            obj.enclosingConcept = c; 
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        obj.nid = 1;
        obj.primordialSapNid = 1; 
        obj.primordialUNid = 1;
        obj.revisions = new ArrayList<RelationshipRevision>(1);

        RelationshipRevision rev = new RelationshipRevision(); 
        rev.primordialComponent = obj; 
        rev.setStatusAtPositionNid(1);
        rev.setCharacteristicId(1);
        rev.setGroup(1);
        rev.setRefinabilityId(1);
        rev.setTypeId(1);
        obj.revisions.add(rev); 
        
        return obj; 
    }

    private Relationship makeTestObject2() {
        
        // Create an object to test... 
        Relationship obj = new Relationship();
        obj.setC2Id(2);
        obj.setCharacteristicId(2);
        obj.setGroup(2);
        obj.setRefinabilityId(2);
        obj.setTypeId(2);
        obj.setStatusAtPositionNid(2);
        try {
            Concept c;
            c = Concept.get(test2Concept);
            obj.enclosingConcept = c; 
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        obj.nid = 2;
        obj.primordialSapNid = 2; 
        obj.primordialUNid = 2;
        obj.revisions = new ArrayList<RelationshipRevision>(1);

        RelationshipRevision rev = new RelationshipRevision(); 
        rev.primordialComponent = obj; 
        rev.setStatusAtPositionNid(2);
        rev.setCharacteristicId(2);
        rev.setGroup(2);
        rev.setRefinabilityId(2);
        rev.setTypeId(2);
        obj.revisions.add(rev); 
        
        return obj; 
    }
}


