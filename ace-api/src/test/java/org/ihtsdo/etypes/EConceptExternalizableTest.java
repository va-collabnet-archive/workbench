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

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadWriteExternal() throws IOException, ClassNotFoundException {
		
			EConcept testConcept = makeTestConcept();
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			testConcept.writeExternal(dos);
			dos.close();
			
			
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			DataInputStream dis = new DataInputStream(bais);
			EConcept testConcept2 = new EConcept(dis);
			assertTrue(testConcept.conceptAttributes.primordialComponentUuid.equals(testConcept2.conceptAttributes.primordialComponentUuid));
			assertTrue(testConcept.conceptAttributes.defined == testConcept2.conceptAttributes.defined);
			assertTrue(testConcept.conceptAttributes.pathUuid.equals(testConcept2.conceptAttributes.pathUuid));
			assertTrue(testConcept.conceptAttributes.statusUuid.equals(testConcept2.conceptAttributes.statusUuid));
			assertTrue(testConcept.conceptAttributes.time == testConcept2.conceptAttributes.time);
	}

	private EConcept makeTestConcept() {
		EConcept testConcept = new EConcept();
		testConcept.conceptAttributes = new EConceptAttributes();
		testConcept.conceptAttributes.primordialComponentUuid = new UUID(2, 3);
		testConcept.conceptAttributes.defined = false;
		testConcept.conceptAttributes.extraVersions = null;
		testConcept.conceptAttributes.idComponents = null;
		testConcept.conceptAttributes.pathUuid = new UUID(4, 5);
		testConcept.conceptAttributes.statusUuid = new UUID(8, 9);
		testConcept.conceptAttributes.time = System.currentTimeMillis();
		
		testConcept.descriptions = new ArrayList<EDescription>(1);
		EDescription desc = new EDescription();
		desc.conceptUuid  = new UUID(11, 12);
		desc.initialCaseSignificant = false;
		desc.lang = "en";
		desc.text = "hello world";			
		desc.typeUuid = new UUID(13, 14);
		desc.pathUuid = new UUID(4, 5);
		desc.statusUuid = new UUID(8, 9);
		desc.time = testConcept.conceptAttributes.time;
		desc.primordialComponentUuid = new UUID(20, 30);
		desc.extraVersions = new ArrayList<EDescriptionVersion>();
		EDescriptionVersion edv = new EDescriptionVersion();
		edv.initialCaseSignificant = true;
		edv.lang = "en-uk";
		edv.text = "hello world 2";
		edv.typeUuid  = new UUID(13, 14);
		edv.pathUuid = new UUID(4, 5);
		edv.statusUuid = new UUID(8, 9);
		edv.time = testConcept.conceptAttributes.time;
		desc.extraVersions.add(edv);
		testConcept.descriptions.add(desc);
		
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
		rel.time = testConcept.conceptAttributes.time;
		rel.primordialComponentUuid = new UUID(20, 30);
		testConcept.relationships.add(rel);
		rel.extraVersions = new ArrayList<ERelationshipVersion>();
		ERelationshipVersion erv = new ERelationshipVersion();
		erv.characteristicUuid  = new UUID(861, 947);
		erv.refinabilityUuid  = new UUID(586, 937);
		erv.group = 3; 
		erv.typeUuid  = new UUID(846, 957);
		erv.pathUuid = new UUID(425, 526);
		erv.statusUuid = new UUID(846, 967);
		erv.time = testConcept.conceptAttributes.time;
		rel.extraVersions.add(erv);
		
		
		testConcept.images = new ArrayList<EImage>(1);
		EImage img = new EImage();
		img.conceptUuid = new UUID(120, 130);
		img.format = "jpg";
		img.image = new byte[] {0, 2, 3, 4, 5, 6, 7, 8, 9 };
		img.textDescription = "interesting image";
		img.typeUuid = new UUID(121, 132);
		img.pathUuid = new UUID(450, 569);
		img.statusUuid = new UUID(868, 977);
		img.time = testConcept.conceptAttributes.time;
		img.primordialComponentUuid = new UUID(206, 305);
		testConcept.images.add(img);
		img.extraVersions = new ArrayList<EImageVersion>();
		EImageVersion iv = new EImageVersion();
		iv.textDescription = "interesting image e";
		iv.typeUuid = new UUID(1231, 1532);
		iv.pathUuid = new UUID(24450, 5469);
		iv.statusUuid = new UUID(8668, 9757);
		iv.time = testConcept.conceptAttributes.time;
		img.extraVersions.add(iv);
		
		testConcept.refsetMembers = new ArrayList<ERefset>();
		ERefsetCidIntMember cidIntMember = new ERefsetCidIntMember();
		cidIntMember.c1Uuid = new UUID(4386, 5497);
		cidIntMember.intValue = 33;
		cidIntMember.refsetUuid = new UUID(14386, 65497);
		cidIntMember.componentUuid = new UUID(64386, 75497);
		cidIntMember.pathUuid = new UUID(4350, 5469);
		cidIntMember.statusUuid = new UUID(5386, 4497);
		cidIntMember.time = testConcept.conceptAttributes.time;
		cidIntMember.primordialComponentUuid = new UUID(320, 230);
		testConcept.refsetMembers.add(cidIntMember);
		cidIntMember.extraVersions = new ArrayList<ERefsetCidIntVersion>();
		ERefsetCidIntVersion rciv = new ERefsetCidIntVersion();
		rciv.c1Uuid = new UUID(114386, 656497);
		rciv.intValue = 99;
		rciv.pathUuid = new UUID(4350, 5469);
		rciv.statusUuid = new UUID(5386, 4497);
		rciv.time = testConcept.conceptAttributes.time;
		cidIntMember.extraVersions.add(rciv);
		return testConcept;
	}

}
