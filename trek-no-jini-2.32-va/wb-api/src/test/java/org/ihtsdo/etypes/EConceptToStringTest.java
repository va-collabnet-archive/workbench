package org.ihtsdo.etypes;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.media.TkMediaRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EConceptToStringTest {

    protected long myTime = 1263758387001L;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore
    public void testToString() throws IOException, ClassNotFoundException {
        EConcept testConcept = makeTestConcept();
        String actualOutput = testConcept.toString();
        System.out.println("ACTUAL OUTPUT:\n" + actualOutput);
        String expectedOutput = 
            "EConcept: " +
            "\n   Descriptions: \n\t" +
            "[EDescription:  pathUuid:00000000-0000-0004-0000-000000000005 statusUuid:00000000-0000-0008-0000-000000000009 Time:(Sun Jan 17 13:59:47 CST 2010) primordialComponentUuid:00000000-0000-0014-0000-00000000001e additionalIdComponents:null extraVersions:[EDescriptionVersion:  pathUuid:00000000-0000-0004-0000-000000000005 statusUuid:00000000-0000-0008-0000-000000000009 Time:(Sun Jan 17 13:59:47 CST 2010) initialCaseSignificant:true lang:en-uk text:hello world 2 typeUuid:00000000-0000-000d-0000-00000000000e; ] conceptUuid:00000000-0000-000b-0000-00000000000c initialCaseSignificant:false lang:en text:hello world typeUuid:00000000-0000-000d-0000-00000000000e; ]" +
            "\n   Relationships: \n\t" +
            "[ERelationship:  pathUuid:00000000-0000-002d-0000-000000000038 statusUuid:00000000-0000-0056-0000-000000000061 Time:(Sun Jan 17 13:59:47 CST 2010) primordialComponentUuid:00000000-0000-0014-0000-00000000001e additionalIdComponents:null extraVersions:[ERelationshipVersion:  pathUuid:00000000-0000-01a9-0000-00000000020e statusUuid:00000000-0000-034e-0000-0000000003c7 Time:(Sun Jan 17 13:59:47 CST 2010) characteristicUuid:00000000-0000-035d-0000-0000000003b3 refinabilityUuid:00000000-0000-024a-0000-0000000003a9 group:3 typeUuid:00000000-0000-034e-0000-0000000003bd; ] c1Uuid:00000000-0000-0028-0000-000000000032 c2Uuid:00000000-0000-0029-0000-000000000034 characteristicUuid:00000000-0000-002a-0000-000000000035 refinabilityUuid:00000000-0000-002b-0000-000000000036 relGroup:22 typeUuid:00000000-0000-002c-0000-000000000037; ]" +
            "\n   RefsetMembers: \n\t" +
            "[ERefsetCidIntMember:  pathUuid:00000000-0000-10fe-0000-00000000155d statusUuid:00000000-0000-150a-0000-000000001191 Time:(Sun Jan 17 13:59:47 CST 2010) primordialComponentUuid:00000000-0000-0140-0000-0000000000e6 additionalIdComponents:null extraVersions:[ERefsetCidIntVersion:  pathUuid:00000000-0000-10fe-0000-00000000155d statusUuid:00000000-0000-150a-0000-000000001191 Time:(Sun Jan 17 13:59:47 CST 2010) c1Uuid:00000000-0001-bed2-0000-0000000a0471 intValue:99; ] refsetUuid:00000000-0000-3832-0000-00000000ffd9 componentUuid:00000000-0000-fb82-0000-0000000126e9;  c1Uuid:00000000-0000-1122-0000-000000001579 intValue:33; ]" +
            "\n   ConceptAttributes: \n\t" +
            "EConceptAttributes:  pathUuid:00000000-0000-0004-0000-000000000005 statusUuid:00000000-0000-0008-0000-000000000009 Time:(Sun Jan 17 13:59:47 CST 2010) primordialComponentUuid:00000000-0000-0002-0000-000000000003 additionalIdComponents:null extraVersions:null defined:false; " +
            "\n   Images: \n\t" +
            "[EImage:  pathUuid:00000000-0000-01c2-0000-000000000239 statusUuid:00000000-0000-0364-0000-0000000003d1 Time:(Sun Jan 17 13:59:47 CST 2010) primordialComponentUuid:00000000-0000-00ce-0000-000000000131 additionalIdComponents:null extraVersions:[EImageVersion:  pathUuid:00000000-0000-5f82-0000-00000000155d statusUuid:00000000-0000-21dc-0000-00000000261d Time:(Sun Jan 17 13:59:47 CST 2010) textDescription:interesting image e typeUuid:00000000-0000-04cf-0000-0000000005fc; ] conceptUuid:00000000-0000-0078-0000-000000000082 format:jpg image:\000\002\003\004\005\006\007\008   textDescription:interesting image typeUuid:00000000-0000-0079-0000-000000000084; ]" +
            "\n   destRelUuidTypeUuids: \n\t" +
            "null" +
            "\n   refsetUuidMemberUuidForConcept: \n\t" +
            "null" +
            "\n   refsetUuidMemberUuidForDescriptions: \n\t" +
            "null" +
            "\n   refsetUuidMemberUuidForRels: \n\t" +
            "null"; 

        System.out.println("EXPECTED OUTPUT:\n" + expectedOutput);
        assertTrue(actualOutput.equals(expectedOutput));

        
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        DataOutputStream dos = new DataOutputStream(baos);
//        testConcept.writeExternal(dos);
//        dos.close();
//        
//        
//        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//        DataInputStream dis = new DataInputStream(bais);
//        EConcept testConcept2 = new EConcept(dis);
//        assertTrue(testConcept.conceptAttributes.primordialComponentUuid.equals(testConcept2.conceptAttributes.primordialComponentUuid));
//        assertTrue(testConcept.conceptAttributes.defined == testConcept2.conceptAttributes.defined);
//        assertTrue(testConcept.conceptAttributes.pathUuid.equals(testConcept2.conceptAttributes.pathUuid));
//        assertTrue(testConcept.conceptAttributes.statusUuid.equals(testConcept2.conceptAttributes.statusUuid));
//        assertTrue(testConcept.conceptAttributes.time == testConcept2.conceptAttributes.time);


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
        testConcept.conceptAttributes.time = this.myTime;
        
        testConcept.descriptions = new ArrayList<TkDescription>(1);
        EDescription desc = new EDescription();
        desc.conceptUuid  = new UUID(11, 12);
        desc.initialCaseSignificant = false;
        desc.lang = "en";
        desc.text = "hello world";          
        desc.typeUuid = new UUID(13, 14);
        desc.pathUuid = new UUID(4, 5);
        desc.statusUuid = new UUID(8, 9);
        desc.time = testConcept.conceptAttributes.time;
        desc.primordialUuid = new UUID(20, 30);
        desc.revisions = new ArrayList<TkDescriptionRevision>();
        EDescriptionRevision edv = new EDescriptionRevision();
        edv.initialCaseSignificant = true;
        edv.lang = "en-uk";
        edv.text = "hello world 2";
        edv.typeUuid  = new UUID(13, 14);
        edv.pathUuid = new UUID(4, 5);
        edv.statusUuid = new UUID(8, 9);
        edv.time = this.myTime;
        desc.revisions.add(edv);
        testConcept.descriptions.add(desc);
        
        testConcept.relationships = new ArrayList<TkRelationship>(1);
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
        testConcept.relationships.add(rel);
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
        
        
        testConcept.media = new ArrayList<TkMedia>(1);
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
        testConcept.media.add(img);
        img.revisions = new ArrayList<TkMediaRevision>();
        EImageRevision iv = new EImageRevision();
        iv.textDescription = "interesting image e";
        iv.typeUuid = new UUID(1231, 1532);
        iv.pathUuid = new UUID(24450, 5469);
        iv.statusUuid = new UUID(8668, 9757);
        iv.time = this.myTime;
        img.revisions.add(iv);
        
        testConcept.refsetMembers = new ArrayList<TkRefexAbstractMember<?>>();
        ERefsetCidIntMember cidIntMember = new ERefsetCidIntMember();
        cidIntMember.uuid1 = new UUID(4386, 5497);
        cidIntMember.int1 = 33;
        cidIntMember.refsetUuid = new UUID(14386, 65497);
        cidIntMember.componentUuid = new UUID(64386, 75497);
        cidIntMember.pathUuid = new UUID(4350, 5469);
        cidIntMember.statusUuid = new UUID(5386, 4497);
        cidIntMember.time = this.myTime;
        cidIntMember.primordialUuid = new UUID(320, 230);
        testConcept.refsetMembers.add(cidIntMember);
        cidIntMember.revisions = new ArrayList<TkRefexUuidIntRevision>();
        ERefsetCidIntRevision rciv = new ERefsetCidIntRevision();
        rciv.uuid1 = new UUID(114386, 656497);
        rciv.int1 = 99;
        rciv.pathUuid = new UUID(4350, 5469);
        rciv.statusUuid = new UUID(5386, 4497);
        rciv.time = this.myTime;
        cidIntMember.revisions.add(rciv);
        return testConcept;
    }

}
