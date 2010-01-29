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

public class RelationshipRevisionTest {

    private RelationshipRevision testObj1;
    private RelationshipRevision testObj2;
    private RelationshipRevision testObj3;
    
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
        try {
            testObj1 = makeTestObject1();
            testObj2 = makeTestObject1();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Put testComponent1 in a collection 
        Set<RelationshipRevision> coll = new java.util.HashSet<RelationshipRevision>();
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

    private RelationshipRevision makeTestObject1() {
        
        // Create an object to test... 
        RelationshipRevision obj = new RelationshipRevision();
        obj.setCharacteristicId(1);
        obj.setGroup(1);
        obj.setRefinabilityId(1);
        obj.setTypeId(1);
        obj.setStatusAtPositionNid(1);
        
        Relationship member = new Relationship();
        member.setC2Id(1);
        member.setCharacteristicId(1);
        member.setGroup(1);
        member.setRefinabilityId(1);
        member.setTypeId(1);
        member.setStatusAtPositionNid(1);
        try {
            Concept c;
            c = Concept.get(testConcept);
            member.enclosingConcept = c; 
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        member.nid = 1;
        member.primordialSapNid = 1; 
        member.primordialUNid = 1;
        member.additionalVersions = null;
        member.versions = null;
        obj.primordialComponent = member; 

        return obj; 
    }

    private RelationshipRevision makeTestObject2() {
        
        // Create an object to test... 
        RelationshipRevision obj = new RelationshipRevision();
        obj.setCharacteristicId(2);
        obj.setGroup(2);
        obj.setRefinabilityId(2);
        obj.setTypeId(2);
        obj.setStatusAtPositionNid(2);
        
        Relationship member = new Relationship();
        member.setC2Id(2);
        member.setCharacteristicId(2);
        member.setGroup(2);
        member.setRefinabilityId(2);
        member.setTypeId(2);
        member.setStatusAtPositionNid(2);
        try {
            Concept c;
            c = Concept.get(test2Concept);
            member.enclosingConcept = c; 
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        member.nid = 2;
        member.primordialSapNid = 2; 
        member.primordialUNid = 2;
        member.additionalVersions = null;
        member.versions = null;
        obj.primordialComponent = member; 

        return obj; 
    }
}

