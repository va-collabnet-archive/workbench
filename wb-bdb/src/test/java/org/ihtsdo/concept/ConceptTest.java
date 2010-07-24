package org.ihtsdo.concept;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.util.io.FileIO;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EConceptAttributesRevision;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.EDescriptionRevision;
import org.ihtsdo.etypes.EImage;
import org.ihtsdo.etypes.EImageRevision;
import org.ihtsdo.etypes.ERefsetCidIntMember;
import org.ihtsdo.etypes.ERefsetCidIntRevision;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.etypes.ERelationshipRevision;
import org.ihtsdo.tk.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.concept.component.description.TkDescription;
import org.ihtsdo.tk.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.concept.component.media.TkMedia;
import org.ihtsdo.tk.concept.component.media.TkMediaRevision;
import org.ihtsdo.tk.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.concept.component.refset.cidint.TkRefsetCidIntRevision;
import org.ihtsdo.tk.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.concept.component.relationship.TkRelationshipRevision;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
public class ConceptTest {

    private Concept testObj1;
    private Concept testObj2;
    private Concept testObj3;
    
    EConcept testConcept;
    EConcept test2Concept;
    String dbTarget;

    // myTime is used to make sure all time properties use 
    // the same time value
    protected long myTime = Long.MIN_VALUE;
    
    @Before
    public void setUp() throws Exception {
        
        this.myTime = System.currentTimeMillis(); 

        dbTarget = "target/" + UUID.randomUUID();
        Bdb.setup(dbTarget);
        testConcept = new EConcept();
        EConceptAttributes eca1 = new EConceptAttributes();
        eca1.primordialUuid = UUID.randomUUID();
        eca1.setStatusUuid(UUID.randomUUID());
        eca1.setPathUuid(UUID.randomUUID());
        eca1.setTime(this.myTime);
        eca1.setDefined(true);
        
        EConceptAttributesRevision ecav = new EConceptAttributesRevision();
        ecav.setDefined(false);
        ecav.setPathUuid(eca1.getPathUuid());
        ecav.setStatusUuid(eca1.getStatusUuid());
        ecav.setTime(eca1.getTime() + 10);
        eca1.revisions = new ArrayList<TkConceptAttributesRevision>(1);
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
    @Ignore
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


    private Concept makeTestObject1() {
        
        // Create a test object... 
        Concept obj = null;
        EConcept ec = makeConcept1();  
        try {
            obj = Concept.getTempConcept(ec);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return obj;        
    }
    
    private EConcept makeConcept1() {
        EConcept testConcept = new EConcept();

        // Create Concept Attributes
        EConceptAttributes ca = new EConceptAttributes();
        ca.additionalIds = null;
        ca.primordialUuid = new UUID(2, 3);
        ca.setDefined(false);
        ca.revisions = null;
        ca.additionalIds = null;
        ca.setPathUuid(new UUID(4, 5));
        ca.setStatusUuid(new UUID(8, 9));
        ca.setTime(this.myTime);

        testConcept.setConceptAttributes(ca);

        // Add a Description
        List<TkDescription> descriptionList = new ArrayList<TkDescription>(1);
        EDescription desc = new EDescription();
        desc.additionalIds = null;
        desc.primordialUuid = new UUID(20, 30);
        desc.setConceptUuid(new UUID(11, 12));
        desc.setInitialCaseSignificant(false);
        desc.setLang("en");
        desc.setPathUuid(new UUID(4, 5));
        desc.setStatusUuid(new UUID(8, 9));
        desc.setTypeUuid(new UUID(4, 7));
        desc.setText("hello world");
        desc.setTime(this.myTime);
        desc.revisions = new ArrayList<TkDescriptionRevision>(2);
        // add an EDescriptionVersion version
        EDescriptionRevision edv = new EDescriptionRevision();
        edv.setInitialCaseSignificant(true);
        edv.setLang("en-uk");
        edv.setPathUuid(new UUID(4, 5));
        edv.setStatusUuid(new UUID(8, 9));
        edv.setText("hello world 2");
        edv.setTime(this.myTime);
        edv.setTypeUuid(new UUID(13, 14));
        desc.revisions.add(edv);
        // add another EDescriptionVersion
        edv = new EDescriptionRevision();
        edv.setInitialCaseSignificant(true);
        edv.setLang("en-uk");
        edv.setPathUuid(new UUID(24, 25));
        edv.setStatusUuid(new UUID(28, 29));
        edv.setText("hello world 3");
        edv.setTime(this.myTime);
        edv.setTypeUuid(new UUID(23, 24));
        desc.revisions.add(edv);
        descriptionList.add(desc);

        // Add another Description
        desc = new EDescription();
        desc.additionalIds = null;
        desc.primordialUuid = new UUID(200, 300);
        desc.setConceptUuid(new UUID(110, 120));
        desc.setInitialCaseSignificant(false);
        desc.setLang("en");
        desc.setPathUuid(new UUID(40, 50));
        desc.setStatusUuid(new UUID(80, 90));
        desc.setTypeUuid(new UUID(40, 70));
        desc.setText("Aloha World");
        desc.setTime(this.myTime);
        desc.revisions = null;
        descriptionList.add(desc);

        // Add another Description
        desc = new EDescription();
        desc.additionalIds = null;
        desc.primordialUuid = new UUID(201, 301);
        desc.setConceptUuid(new UUID(112, 122));
        desc.setInitialCaseSignificant(false);
        desc.setLang("gr");
        desc.setPathUuid(new UUID(43, 53));
        desc.setStatusUuid(new UUID(84, 94));
        desc.setTypeUuid(new UUID(45, 75));
        desc.setText("Danke shane");
        desc.setTime(this.myTime);
        desc.revisions = null;
        descriptionList.add(desc);

        testConcept.setDescriptions(descriptionList);

        
        // Add Relationships
        List<TkRelationship> relList =  new ArrayList<TkRelationship>(1);

        ERelationship rel = new ERelationship();
        rel.additionalIds = null;
        rel.setAdditionalIdComponents(null);
        rel.setC1Uuid(new UUID(40, 50));
        rel.setC2Uuid(new UUID(41, 52));
        rel.setCharacteristicUuid(new UUID(42, 53));
        rel.setPathUuid(new UUID(45, 56)); 
        rel.setPrimordialComponentUuid(new UUID(20, 30));
        rel.setRefinabilityUuid(new UUID(43, 54));
        rel.setRelGroup(22);
        rel.setStatusUuid(new UUID(86, 97));
        rel.setTime(this.myTime);
        rel.setTypeUuid(new UUID(44, 55));
        rel.revisions = new ArrayList<TkRelationshipRevision>(2);
        // Add relationship versions
        ERelationshipRevision erv = new ERelationshipRevision();
        erv.setCharacteristicUuid(new UUID(861, 947));
        erv.setPathUuid(new UUID(425, 526));
        erv.setRefinabilityUuid(new UUID(586, 937));
        erv.setRelGroup(3);
        erv.setStatusUuid(new UUID(846, 967));
        erv.setTime(this.myTime);
        erv.setTypeUuid(new UUID(846, 957));
        rel.revisions.add(erv);
        // add another relationship version
        erv = new ERelationshipRevision();
        erv.setCharacteristicUuid(new UUID(661, 647));
        erv.setPathUuid(new UUID(625, 626));
        erv.setRefinabilityUuid(new UUID(686, 637));
        erv.setRelGroup(3);
        erv.setStatusUuid(new UUID(646, 667));
        erv.setTime(this.myTime);
        erv.setTypeUuid(new UUID(646, 657));
        rel.revisions.add(erv);
        relList.add(rel);
        
        // Add another Relationship
        rel = new ERelationship();
        rel.additionalIds = null;
        rel.setAdditionalIdComponents(null);
        rel.setC1Uuid(new UUID(45, 55));
        rel.setC2Uuid(new UUID(49, 59));
        rel.setCharacteristicUuid(new UUID(46, 56));
        rel.setPathUuid(new UUID(41, 51)); 
        rel.setPrimordialComponentUuid(new UUID(21, 31));
        rel.setRefinabilityUuid(new UUID(48, 58));
        rel.setRelGroup(16);
        rel.setStatusUuid(new UUID(88, 98));
        rel.setTime(this.myTime);
        rel.setTypeUuid(new UUID(49, 59));
        rel.revisions = null;
        relList.add(rel);
        testConcept.setRelationships(relList);

        // Add Images
        List<TkMedia> imageList = new ArrayList<TkMedia>(1);
        EImage img = new EImage();
        img.setAdditionalIdComponents(null);
        img.setConceptUuid(new UUID(120, 130)); 
        img.setFormat("jpg");
        img.setImage(new byte[] { 0, 2, 3, 4, 5, 6, 7, 8, 9 });
        img.setPathUuid(new UUID(450, 569));
        img.setPrimordialComponentUuid(new UUID(206, 305));
        img.setStatusUuid(new UUID(868, 977));
        img.setTextDescription("interesting image");
        img.setTime(this.myTime);
        img.setTypeUuid(new UUID(121, 132));
        img.revisions = new ArrayList<TkMediaRevision>(2);
        // Image Versions
        EImageRevision iv = new EImageRevision();
        iv.setPathUuid(new UUID(24450, 5469));
        iv.setStatusUuid(new UUID(8668, 9757));
        iv.setTextDescription("interesting image e");
        iv.setTime(this.myTime);
        iv.setTypeUuid(new UUID(1231, 1532));
        img.revisions.add(iv);
        // Add another Image Version
        iv = new EImageRevision();
        iv.setPathUuid(new UUID(34450, 3469));
        iv.setStatusUuid(new UUID(4668, 4757));
        iv.setTextDescription("boring image e");
        iv.setTime(this.myTime);
        iv.setTypeUuid(new UUID(2231, 2532));
        img.revisions.add(iv);
        imageList.add(img);
        testConcept.setImages(imageList);
        
        // Add Refset Members  
        List<TkRefsetAbstractMember<?>> refsetList =  new ArrayList<TkRefsetAbstractMember<?>>();

        ERefsetCidIntMember cidIntMember = new ERefsetCidIntMember();
        cidIntMember.additionalIds = null; 
        cidIntMember.setAdditionalIdComponents(null);
        cidIntMember.setC1Uuid(new UUID(4386, 5497));
        cidIntMember.setComponentUuid(new UUID(64386, 75497));
        cidIntMember.setIntValue(33);
        cidIntMember.setPathUuid(new UUID(4350, 5469));
        cidIntMember.setPrimordialComponentUuid(new UUID(320, 230));
        cidIntMember.setRefsetUuid(new UUID(14386, 65497));
        cidIntMember.setStatusUuid(new UUID(5386, 4497));
        cidIntMember.setTime(this.myTime);
        cidIntMember.revisions = new ArrayList<TkRefsetCidIntRevision>(2);
        // Add extra Refset Members Versions 
        ERefsetCidIntRevision rciv = new ERefsetCidIntRevision();
        rciv.setC1Uuid(new UUID(114386, 656497));
        rciv.setIntValue(99);
        rciv.setPathUuid(new UUID(4350, 5469));
        rciv.setStatusUuid(new UUID(5386, 4497));
        rciv.setTime(this.myTime); 
        cidIntMember.revisions.add(rciv);
        // add another Refset Members version 
        rciv = new ERefsetCidIntRevision();
        rciv.setC1Uuid(new UUID(44386, 46497));
        rciv.setIntValue(54);
        rciv.setPathUuid(new UUID(4350, 4469));
        rciv.setStatusUuid(new UUID(4386, 4497));
        rciv.setTime(this.myTime); 
        cidIntMember.revisions.add(rciv);
        refsetList.add(cidIntMember);
        testConcept.setRefsetMembers(refsetList);
        
        return testConcept;
    }

    private EConcept makeConcept2() {
        EConcept testConcept = new EConcept();

        // Create Concept Attributes
        EConceptAttributes ca = new EConceptAttributes();
        ca.additionalIds = null;
        ca.primordialUuid = new UUID(1, 1);
        ca.setDefined(true);
        ca.revisions = null;
        ca.additionalIds = null;
        ca.setPathUuid(new UUID(2, 2));
        ca.setStatusUuid(new UUID(3, 3));
        ca.setTime(this.myTime);

        testConcept.setConceptAttributes(ca);

        // Add a Description
        List<TkDescription> descriptionList = new ArrayList<TkDescription>(1);
        EDescription desc = new EDescription();
        desc.additionalIds = null;
        desc.primordialUuid = new UUID(21, 21);
        desc.setConceptUuid(new UUID(22, 22));
        desc.setInitialCaseSignificant(false);
        desc.setLang("en");
        desc.setPathUuid(new UUID(23, 23));
        desc.setStatusUuid(new UUID(24, 24));
        desc.setTypeUuid(new UUID(25, 25));
        desc.setText("good morning");
        desc.setTime(this.myTime);
        desc.revisions = new ArrayList<TkDescriptionRevision>(2);
        // add an EDescriptionVersion version
        EDescriptionRevision edv = new EDescriptionRevision();
        edv.setInitialCaseSignificant(true);
        edv.setLang("en-uk");
        edv.setPathUuid(new UUID(26, 26));
        edv.setStatusUuid(new UUID(27, 27));
        edv.setText("adios");
        edv.setTime(this.myTime);
        edv.setTypeUuid(new UUID(28, 28));
        desc.revisions.add(edv);
        // add another EDescriptionVersion
        edv = new EDescriptionRevision();
        edv.setInitialCaseSignificant(true);
        edv.setLang("en-uk");
        edv.setPathUuid(new UUID(29, 29));
        edv.setStatusUuid(new UUID(30, 30));
        edv.setText("extreme hello");
        edv.setTime(this.myTime);
        edv.setTypeUuid(new UUID(31, 31));
        desc.revisions.add(edv);
        descriptionList.add(desc);
        testConcept.setDescriptions(descriptionList);

        // Add Relationships
        List<TkRelationship> relList =  new ArrayList<TkRelationship>(1);

        ERelationship rel = new ERelationship();
        rel.additionalIds = null;
        rel.setAdditionalIdComponents(null);
        rel.setC1Uuid(new UUID(51, 51));
        rel.setC2Uuid(new UUID(52, 52));
        rel.setCharacteristicUuid(new UUID(53, 53));
        rel.setPathUuid(new UUID(54, 54)); 
        rel.setPrimordialComponentUuid(new UUID(55, 55));
        rel.setRefinabilityUuid(new UUID(56, 56));
        rel.setRelGroup(16);
        rel.setStatusUuid(new UUID(57, 57));
        rel.setTime(this.myTime);
        rel.setTypeUuid(new UUID(58, 58));
        rel.revisions = new ArrayList<TkRelationshipRevision>(2);
        // Add relationship versions
        ERelationshipRevision erv = new ERelationshipRevision();
        erv.setCharacteristicUuid(new UUID(59, 59));
        erv.setPathUuid(new UUID(60, 60));
        erv.setRefinabilityUuid(new UUID(61, 61));
        erv.setRelGroup(10);
        erv.setStatusUuid(new UUID(62, 62));
        erv.setTime(this.myTime);
        erv.setTypeUuid(new UUID(63, 63));
        rel.revisions.add(erv);
        // add another relationship version
        erv = new ERelationshipRevision();
        erv.setCharacteristicUuid(new UUID(64, 64));
        erv.setPathUuid(new UUID(65, 65));
        erv.setRefinabilityUuid(new UUID(66, 66));
        erv.setRelGroup(59);
        erv.setStatusUuid(new UUID(67, 67));
        erv.setTime(this.myTime);
        erv.setTypeUuid(new UUID(68, 68));
        rel.revisions.add(erv);
        relList.add(rel);
        testConcept.setRelationships(relList);

        // Add Images
        List<TkMedia> imageList = new ArrayList<TkMedia>(1);
        EImage img = new EImage();
        img.setAdditionalIdComponents(null);
        img.setConceptUuid(new UUID(70, 70)); 
        img.setFormat("png");
        img.setImage(new byte[] { 0, 2, 3, 4, 5, 6, 7, 8, 9 });
        img.setPathUuid(new UUID(71, 71));
        img.setPrimordialComponentUuid(new UUID(72, 72));
        img.setStatusUuid(new UUID(73, 73));
        img.setTextDescription("an amazing image!");
        img.setTime(this.myTime);
        img.setTypeUuid(new UUID(74, 74));
        img.revisions = new ArrayList<TkMediaRevision>(2);
        // Image Versions
        EImageRevision iv = new EImageRevision();
        iv.setPathUuid(new UUID(75, 75));
        iv.setStatusUuid(new UUID(75, 75));
        iv.setTextDescription("ugly duck");
        iv.setTime(this.myTime);
        iv.setTypeUuid(new UUID(77, 77));
        img.revisions.add(iv);
        // Add another Image Version
        iv = new EImageRevision();
        iv.setPathUuid(new UUID(78, 78));
        iv.setStatusUuid(new UUID(79, 79));
        iv.setTextDescription("wow!");
        iv.setTime(this.myTime);
        iv.setTypeUuid(new UUID(80, 80));
        img.revisions.add(iv);
        imageList.add(img);
        testConcept.setImages(imageList);
        
        // Add Refset Members  
        List<TkRefsetAbstractMember<?>> refsetList =  new ArrayList<TkRefsetAbstractMember<?>>();

        ERefsetCidIntMember cidIntMember = new ERefsetCidIntMember();
        cidIntMember.additionalIds = null; 
        cidIntMember.setAdditionalIdComponents(null);
        cidIntMember.setC1Uuid(new UUID(90, 90));
        cidIntMember.setComponentUuid(new UUID(91, 91));
        cidIntMember.setIntValue(01);
        cidIntMember.setPathUuid(new UUID(92, 92));
        cidIntMember.setPrimordialComponentUuid(new UUID(93, 93));
        cidIntMember.setRefsetUuid(new UUID(94, 94));
        cidIntMember.setStatusUuid(new UUID(95, 95));
        cidIntMember.setTime(this.myTime);
        cidIntMember.revisions = new ArrayList<TkRefsetCidIntRevision>(2);
        // Add extra Refset Members Versions 
        ERefsetCidIntRevision rciv = new ERefsetCidIntRevision();
        rciv.setC1Uuid(new UUID(96, 96));
        rciv.setIntValue(21);
        rciv.setPathUuid(new UUID(97, 97));
        rciv.setStatusUuid(new UUID(98, 98));
        rciv.setTime(this.myTime); 
        cidIntMember.revisions.add(rciv);
        // add another Refset Members version 
        rciv = new ERefsetCidIntRevision();
        rciv.setC1Uuid(new UUID(99, 99));
        rciv.setIntValue(61);
        rciv.setPathUuid(new UUID(100, 100));
        rciv.setStatusUuid(new UUID(101, 101));
        rciv.setTime(this.myTime); 
        cidIntMember.revisions.add(rciv);
        refsetList.add(cidIntMember);
        testConcept.setRefsetMembers(refsetList);
        
        return testConcept;
    }

 }
