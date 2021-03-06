package org.ihtsdo.etypes;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.media.TkMediaRevision;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EImageTest {

    private EImage testComponent1;
    private EImage testComponent2;
    private EImage testComponent3;
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
        Set<EImage> coll = new java.util.HashSet<EImage>();
        coll.add(testComponent1);

        // Test for the presence of testComponent1 by using the  
        // equivalent object testComponent2
        assertTrue(coll.contains(testComponent2));         

    }

    @Test
    public void testEqualsForTwoArrayLists() {
        // Make two ArrayLists with the same components in each 
        List<EImage> list1 = new ArrayList<EImage>();
        List<EImage> list2 = new ArrayList<EImage>();
        
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

    
    private EImage makeTestComponent1() {
        EImage img = new EImage();
        img.conceptUuid = new UUID(120, 130);
        img.format = "jpg";
        img.dataBytes = new byte[] {0, 2, 3, 4, 5, 6, 7, 8, 9 };
        img.textDescription = "interesting image";
        img.typeUuid = new UUID(121, 132);
        img.pathUuid = new UUID(450, 569);
        img.statusUuid = new UUID(868, 977);
        img.time = this.myTime; 
        img.primordialUuid = new UUID(206, 305);
        img.revisions = new ArrayList<TkMediaRevision>();
        EImageRevision iv = new EImageRevision();
        iv.textDescription = "interesting image e";
        iv.typeUuid = new UUID(1231, 1532);
        iv.pathUuid = new UUID(24450, 5469);
        iv.statusUuid = new UUID(8668, 9757);
        iv.time = this.myTime; 
        img.revisions.add(iv);

        return img;
    }

    private EImage makeTestComponent2() {
        EImage img = new EImage();
        img.conceptUuid = new UUID(112, 113);
        img.format = "png";
        img.dataBytes = new byte[] {0, 1, 2, 3, 5, 8, 13, 21, 34 };
        img.textDescription = "over-exposed image";
        img.typeUuid = new UUID(114, 115);
        img.pathUuid = new UUID(116, 117);
        img.statusUuid = new UUID(118, 119);
        img.time = this.myTime; 
        img.primordialUuid = new UUID(120, 121);
        img.revisions = new ArrayList<TkMediaRevision>();
        EImageRevision iv = new EImageRevision();
        iv.textDescription = "interesting image e";
        iv.typeUuid = new UUID(122, 123);
        iv.pathUuid = new UUID(124, 125);
        iv.statusUuid = new UUID(126, 126);
        iv.time = this.myTime; 
        img.revisions.add(iv);

        return img;
    }


}

