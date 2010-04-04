package org.ihtsdo.concept.component.image;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EConceptAttributesRevision;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ImageTest {

    private Image testObj1;
    private Image testObj2;
    private Image testObj3;
    protected long myTime = 1263758387001L;
    
    EConcept testConcept;
    EConcept test2Concept;
    String dbTarget;

    @Before
    public void setUp() throws Exception {
        dbTarget = "target/" + UUID.randomUUID();
        Bdb.setup(dbTarget);
        testConcept = new EConcept();
        EConceptAttributes eca1 = new EConceptAttributes();
        eca1.primordialUuid = UUID.randomUUID();
        eca1.setStatusUuid(UUID.randomUUID());
        eca1.setPathUuid(UUID.randomUUID());
        eca1.setTime(System.currentTimeMillis());
        eca1.setDefined(true);
        
        EConceptAttributesRevision ecav = new EConceptAttributesRevision();
        ecav.setDefined(false);
        ecav.setPathUuid(eca1.getPathUuid());
        ecav.setStatusUuid(eca1.getStatusUuid());
        ecav.setTime(eca1.getTime() + 10);
        eca1.revisions = new ArrayList<EConceptAttributesRevision>(1);
        eca1.revisions.add(ecav);
        
        testConcept.setConceptAttributes(eca1);
        test2Concept = new EConcept();
        EConceptAttributes eca2 = new EConceptAttributes();
        eca2.primordialUuid = UUID.randomUUID();
        eca2.setStatusUuid(UUID.randomUUID());
        eca2.setPathUuid(UUID.randomUUID());
        eca2.setDefined(false);
        eca2.setTime(eca1.getTime());
        test2Concept.setConceptAttributes(eca2);
    }

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		Bdb.close();
		FileIO.recursiveDelete(new File(dbTarget));
	}

    @Test
    public void testEqualsObject() throws IOException {
        // The contract of the equals method in Object 
        // specifies that equals must implement an equivalence 
        // relation on non-null objects:
        
        // Make all the components be the same 
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
    public void testEqualsInACollection() throws IOException {
        // Make both the objects be the same 
        testObj1 = makeTestObject1();
        testObj2 = makeTestObject1();
        
        // Put testComponent1 in a collection 
        Set<Image> coll = new java.util.HashSet<Image>();
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

    private Image makeTestObject1() throws IOException {
        
        // Create an object to test... 
        Image obj = new Image();
        obj.revisions = new CopyOnWriteArrayList<ImageRevision>();
        ImageRevision ir = new ImageRevision(); 
        ir.primordialComponent = obj; 
        ir.sapNid = 1;
        obj.revisions.add(ir); 
        
        Concept c = Concept.getTempConcept(testConcept);
        obj.enclosingConceptNid = c.getNid();

        obj.nid = 1;
        obj.primordialSapNid = 1; 
        obj.primordialUNid = 1;
        obj.versions = null; 
        
        return obj; 
    }

    private Image makeTestObject2() throws IOException {
        
        // Create an object to test... 
        Image obj = new Image();
        obj.revisions = new CopyOnWriteArrayList<ImageRevision>();
        ImageRevision ir = new ImageRevision(); 
        ir.primordialComponent = obj; 
        ir.sapNid = 2;
        obj.revisions.add(ir); 
        
        Concept c = Concept.getTempConcept(test2Concept);
        obj.enclosingConceptNid = c.getNid();

        obj.nid = 2;
        obj.primordialSapNid = 2; 
        obj.primordialUNid = 2;
        obj.versions = null; 
        
        return obj; 
    }
}

