/**
 * 
 */
package org.ihtsdo.concept.component.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponentBinder;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EConceptAttributesRevision;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sleepycat.je.DatabaseEntry;

/**
 * @author kec
 *
 */
public class ConceptAttributesVersionTest {
	EConcept testConcept;
	EConcept test2Concept;
	String dbTarget;
	/**
	 * @throws java.lang.Exception
	 */
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

	/**
	 * Test method for {@link org.ihtsdo.concept.component.attributes.ConceptAttributesRevision#ConceptAttributesVersion(com.sleepycat.bind.tuple.TupleInput, org.ihtsdo.concept.component.attributes.ConceptAttributes)}.
	 */
	@Test
	@Ignore
	public void testConceptAttributesVersionTupleInputConceptAttributes() {
		try {
			Concept c = Concept.getTempConcept(testConcept);
			assertNotNull(c.getConceptAttributes());
			ConceptComponentBinder<ConceptAttributesRevision, ConceptAttributes> cab = 
				new ConceptAttributesBinder();
			DatabaseEntry entry = new DatabaseEntry();
			ArrayList<ConceptAttributes> origList = c.getConceptAttributesList();
			cab.objectToEntry(origList, entry);
			cab.setupBinder(c);
			Collection<ConceptAttributes> newList = cab.entryToObject(entry);
			assertEquals(origList, newList);

			Concept c2 = Concept.getTempConcept(test2Concept);
			ArrayList<ConceptAttributes> c2List = c2.getConceptAttributesList();
			cab.objectToEntry(c2List, entry);
			Collection<ConceptAttributes> c2NewList = cab.entryToObject(entry);
			assertEquals(c2List, c2NewList);
			if (c2List.equals(newList)) {
				fail("lists should not be equal");
			}
			ConceptAttributes ca1 = origList.get(0);
			testCa1(ca1);
			
			Concept c3 = Concept.getTempConcept(testConcept);
			Bdb.getConceptDb().writeConcept(c3);
			Concept c4 = Bdb.getConceptDb().getConcept(c3.getNid());
			testCa1(c4.getConceptAttributes());
		} catch (IOException e) {
			fail(e.toString());
		}
		
	}

	private void testCa1(ConceptAttributes ca1) {
		assertNotNull(ca1.revisions);
		assertEquals(ca1.revisions.size(), 1);
		ConceptAttributesRevision cav = ca1.revisions.get(0);
		assertEquals(ca1.getStatusId(), cav.getStatusId());
		assertEquals(ca1.getPathId(), cav.getPathId());
		if (ca1.primordialSapNid == cav.sapNid) {
			fail("statusAtPositionNid should not be equal. ");
		}

		if (ca1.getTime() == cav.getTime()) {
			fail("getTime should not be equal. ");
		}
		
		if (ca1.isDefined() == cav.isDefined()) {
			fail("isDefined should not be equal. ");
		}
	}

}
