package org.ihtsdo.etypes;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EConceptExternalizableTest {

    private EConcept testConcept; 
    private long myTime = Long.MIN_VALUE;
    
    @Before
    public void setUp() throws Exception {
        this.testConcept = makeTestConcept();
        this.myTime = System.currentTimeMillis(); 
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testReadWriteExternal() throws IOException, ClassNotFoundException {
        // Set myTime 
        this.myTime = System.currentTimeMillis(); 
        
        // Write the test Concept out to a Data Output Stream 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        this.testConcept.writeExternal(dos);
        dos.close();
    
        // Re-constitute the testConcept by reading it in from a Data Input Stream 
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        EConcept testConcept2 = new EConcept(dis);
        
        // Determine if we have an equivalent object  
        assertTrue(this.testConcept.equals(testConcept2));

    }

    private EConcept makeTestConcept() {
        EConcept testConcept = new EConcept();
        testConcept.conceptAttributes = new EConceptAttributes();
        testConcept.conceptAttributes.primordialUuid = new UUID(2, 3);
        testConcept.conceptAttributes.defined = false;
        testConcept.conceptAttributes.revisions = null;
        testConcept.conceptAttributes.additionalIds = null;
        testConcept.conceptAttributes.pathUuid = new UUID(4, 5);
        testConcept.conceptAttributes.statusUuid = new UUID(8, 9);
        testConcept.conceptAttributes.authorUuid = UUID.randomUUID();
        testConcept.conceptAttributes.time = System.currentTimeMillis(); 
        
        // Add a Description 
        testConcept.descriptions = new ArrayList<EDescription>(1);
        EDescription desc = new EDescription();
        desc.conceptUuid  = new UUID(11, 12);
        desc.initialCaseSignificant = false;
        desc.lang = "en";
        desc.text = "hello world";          
        desc.typeUuid = new UUID(13, 14);
        desc.pathUuid = new UUID(4, 5);
        desc.statusUuid = new UUID(8, 9);
        desc.authorUuid = UUID.randomUUID();
        desc.time = System.currentTimeMillis(); 
        desc.primordialUuid = new UUID(20, 30);
        desc.revisions = new ArrayList<EDescriptionRevision>();
        // add a EDescriptionVersion
        EDescriptionRevision edv = new EDescriptionRevision();
        edv.initialCaseSignificant = true;
        edv.lang = "en-uk";
        edv.text = "hello world 2";
        edv.typeUuid  = new UUID(13, 14);
        edv.pathUuid = new UUID(4, 5);
        edv.statusUuid = new UUID(8, 9);
        edv.authorUuid = UUID.randomUUID();
        edv.time = System.currentTimeMillis(); 
        desc.revisions.add(edv);   
        // add another EDescriptionVersion
        edv = new EDescriptionRevision();
        edv.initialCaseSignificant = false;
        edv.lang = "en-uk";
        edv.text = "hello world 3";
        edv.typeUuid  = new UUID(23, 24);
        edv.pathUuid = new UUID(24, 25);
        edv.statusUuid = new UUID(28, 29);
        edv.authorUuid = UUID.randomUUID();
        edv.time = System.currentTimeMillis(); 
        desc.revisions.add(edv);
        
        testConcept.descriptions.add(desc);
        
        
        
        // Add Relationships  
        testConcept.relationships = new ArrayList<ERelationship>(1);
        ERelationship rel = new ERelationship();
        rel.c1Uuid = new UUID(40, 50);
        rel.c2Uuid = new UUID(41, 52);
        rel.characteristicUuid = new UUID(42, 53);
        rel.refinabilityUuid = new UUID(43, 54);
        rel.relGroup = 22; 
        rel.typeUuid = new UUID(44, 55);
        rel.pathUuid = new UUID(45, 56);
        rel.statusUuid = new UUID(86, 97);
        rel.authorUuid = UUID.randomUUID();
        rel.time = System.currentTimeMillis(); 
        rel.primordialUuid = new UUID(20, 30);
        testConcept.relationships.add(rel);
        rel.revisions = new ArrayList<ERelationshipRevision>();
        // Add relationship versions 
        ERelationshipRevision erv = new ERelationshipRevision();
        erv.characteristicUuid  = new UUID(861, 947);
        erv.refinabilityUuid  = new UUID(586, 937);
        erv.group = 3; 
        erv.typeUuid  = new UUID(846, 957);
        erv.pathUuid = new UUID(425, 526);
        erv.statusUuid = new UUID(846, 967);
        erv.authorUuid = UUID.randomUUID();
        erv.time = System.currentTimeMillis(); 
        rel.revisions.add(erv);
        // add another relationship version 
        erv.characteristicUuid  = new UUID(661, 647);
        erv.refinabilityUuid  = new UUID(686, 637);
        erv.group = 3; 
        erv.typeUuid  = new UUID(646, 657);
        erv.pathUuid = new UUID(625, 626);
        erv.statusUuid = new UUID(646, 667);
        erv.authorUuid = UUID.randomUUID();
        erv.time = System.currentTimeMillis(); 
        rel.revisions.add(erv);
        
        
        // Add Images  
        testConcept.images = new ArrayList<EImage>(1);
        EImage img = new EImage();
        img.conceptUuid = new UUID(120, 130);
        img.format = "jpg";
        img.image = new byte[] {0, 2, 3, 4, 5, 6, 7, 8, 9 };
        img.textDescription = "interesting image";
        img.typeUuid = new UUID(121, 132);
        img.pathUuid = new UUID(450, 569);
        img.statusUuid = new UUID(868, 977);
        img.authorUuid = UUID.randomUUID();
        img.time = System.currentTimeMillis(); 
        img.primordialUuid = new UUID(206, 305);
        testConcept.images.add(img);
        img.revisions = new ArrayList<EImageRevision>();
        // Image Versions 
        EImageRevision iv = new EImageRevision();
        iv.textDescription = "interesting image e";
        iv.typeUuid = new UUID(1231, 1532);
        iv.pathUuid = new UUID(24450, 5469);
        iv.statusUuid = new UUID(8668, 9757);
        iv.authorUuid = UUID.randomUUID();
        iv.time = System.currentTimeMillis(); 
        img.revisions.add(iv);
        // Add another Image Version 
        iv = new EImageRevision();
        iv.textDescription = "boring image e";
        iv.typeUuid = new UUID(2231, 2532);
        iv.pathUuid = new UUID(34450, 3469);
        iv.statusUuid = new UUID(4668, 4757);
        iv.authorUuid = UUID.randomUUID();
        iv.time = System.currentTimeMillis(); 
        img.revisions.add(iv);
        
        
        // Add Refset Members  
        testConcept.refsetMembers = new ArrayList<ERefsetMember<?>>();
        ERefsetCidIntMember cidIntMember = new ERefsetCidIntMember();
        cidIntMember.c1Uuid = new UUID(4386, 5497);
        cidIntMember.intValue = 33;
        cidIntMember.refsetUuid = new UUID(14386, 65497);
        cidIntMember.componentUuid = new UUID(64386, 75497);
        cidIntMember.pathUuid = new UUID(4350, 5469);
        cidIntMember.statusUuid = new UUID(5386, 4497);
        cidIntMember.authorUuid = UUID.randomUUID();
        cidIntMember.time = System.currentTimeMillis(); 
        cidIntMember.primordialUuid = new UUID(320, 230);
        testConcept.refsetMembers.add(cidIntMember);
        cidIntMember.revisions = new ArrayList<ERefsetCidIntRevision>();
        // Add extra Refset Members Versions 
        ERefsetCidIntRevision rciv = new ERefsetCidIntRevision();
        rciv.c1Uuid = new UUID(114386, 656497);
        rciv.intValue = 99;
        rciv.pathUuid = new UUID(4350, 5469);
        rciv.statusUuid = new UUID(5386, 4497);
        rciv.authorUuid = UUID.randomUUID();
        rciv.time = System.currentTimeMillis(); 
        cidIntMember.revisions.add(rciv);
        // add another Refset Members version 
        rciv = new ERefsetCidIntRevision();
        rciv.c1Uuid = new UUID(44386, 46497);
        rciv.intValue = 99;
        rciv.pathUuid = new UUID(4350, 4469);
        rciv.statusUuid = new UUID(4386, 4497);
        rciv.authorUuid = UUID.randomUUID();
        rciv.time = System.currentTimeMillis(); 
        cidIntMember.revisions.add(rciv);
        
        return testConcept;
    }

}
