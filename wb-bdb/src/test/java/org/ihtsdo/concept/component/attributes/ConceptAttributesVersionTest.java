/**
 * 
 */
package org.ihtsdo.concept.component.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.attributes.ConceptAttributes.Version;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EConceptAttributesRevision;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author kec
 *
 */
public class ConceptAttributesVersionTest {
	EConcept testConcept;
	String dbTarget;
	I_ConfigAceFrame config;
    private PathBI p0;
    private PathBI p1_1;
    private PathBI p1_2;
    private PathBI p2;
    private long t0;
    private long t1;
    private long t2;
    private long t3;
    private long t4;

	public void setUp() throws Exception {
		dbTarget = "target/" + UUID.randomUUID();
		Bdb.setup(dbTarget);
		
        FileInputStream fis = new FileInputStream(new File("src/test/resources/wb-aux.jbin"));
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream in = new DataInputStream(bis);
        try {
            while (true) {
                EConcept eConcept = new EConcept(in);
                Bdb.getConceptDb().writeConcept(Concept.get(eConcept));
            }
        } catch (EOFException e) {
            in.close();
        }

		config = NewDefaultProfile.newProfile("", "", "", "", "");
		for (PositionBI pos: config.getViewPositionSet()) {
	        config.getEditingPathSet().add(pos.getPath());
		}

		Concept p0Concept = (Concept) Terms.get().newConcept(UUID.randomUUID(), false, config);
		addDescription("P-0", p0Concept);

		p0 = Terms.get().newPath(config.getViewPositionSet(), p0Concept, config);
		PositionBI firstOrigin = Terms.get().newPosition(p0, Integer.MAX_VALUE);
        Terms.get().commit();
		Set<PositionBI> firstOriginSet = new HashSet<PositionBI>();
		firstOriginSet.add(firstOrigin);
		
		Concept p1_1_concept = (Concept) Terms.get().newConcept(UUID.randomUUID(), false, config);
        addDescription("P-1.1", p1_1_concept);
        Concept p1_2_concept = (Concept) Terms.get().newConcept(UUID.randomUUID(), false, config);
        addDescription("P-1.2", p1_2_concept);

        p1_1 = Terms.get().newPath(firstOriginSet, p1_1_concept, config);
        p1_2 = Terms.get().newPath(firstOriginSet, p1_2_concept, config);

        Concept p2_concept = (Concept) Terms.get().newConcept(UUID.randomUUID(), false, config);
        addDescription("P-2", p2_concept);
        Set<PositionBI> secondOriginSet = new HashSet<PositionBI>();
        secondOriginSet.add(Terms.get().newPosition(p1_1, Integer.MAX_VALUE));
        secondOriginSet.add(Terms.get().newPosition(p1_2, Integer.MAX_VALUE));

        p2 = Terms.get().newPath(secondOriginSet, p2_concept, config);

		Terms.get().commit();

		t0 = System.currentTimeMillis();
		testConcept = new EConcept();
		EConceptAttributes eca1 = new EConceptAttributes();
		eca1.primordialUuid = UUID.randomUUID();
		eca1.setStatusUuid(UUID.randomUUID());
		eca1.setPathUuid(p0Concept.getPrimUuid());
		eca1.setTime(t0);
		eca1.setDefined(true);
        eca1.revisions = new ArrayList<TkConceptAttributesRevision>(1);
		
         t1 = t0 + 10000;
 		 addAttribute(t1, eca1, p0Concept.getPrimUuid());

         t2 = t1 + 10000;
         addAttribute(t2, eca1, p1_1_concept.getPrimUuid());

         t3 = t2 + 10000;
         addAttribute(t3, eca1, p1_2_concept.getPrimUuid());

         t4 = t3 + 10000;
         addAttribute(t4, eca1, p2_concept.getPrimUuid());

		
		
		testConcept.setConceptAttributes(eca1);
	}

    private void addDescription(String text, Concept c) throws TerminologyException, IOException {
        Terms.get().newDescription(UUID.randomUUID(), c, "en", text, 
		    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
		Terms.get().addUncommittedNoChecks(c);
    }

    private void addAttribute(long time, EConceptAttributes eca1, UUID pathUuid) {
        EConceptAttributesRevision ecav = new EConceptAttributesRevision();
		ecav.setDefined(false);
		ecav.setPathUuid(pathUuid);
		ecav.setStatusUuid(eca1.getStatusUuid());
		ecav.setTime(time);
		eca1.revisions.add(ecav);
    }

    @Test
    @Ignore
    public void testVersionComputer() {
        try {
            
            setUp();
            
            Concept c = Concept.get(testConcept);
            
            IntSet allowedStatus = null;
            List<Version> tuples = c.getConceptAttributes().getTuples(allowedStatus, new PositionSetReadOnly(Terms.get().newPosition(p1_1, Integer.MAX_VALUE)), Precedence.PATH, config.getConflictResolutionStrategy());
            assertEquals(1, tuples.size());
            assertTrue(tuples.get(0).getTime() == t2);
            
            tuples = c.getConceptAttributes().getTuples(allowedStatus, new PositionSetReadOnly(Terms.get().newPosition(p1_1, Integer.MAX_VALUE)), Precedence.TIME, config.getConflictResolutionStrategy());
            assertEquals(1, tuples.size());
            assertTrue(tuples.get(0).getTime() == t2);
            
            tearDown();
            
        } catch (Exception e) {
            fail(e.toString());
        }
        
    }

	/**
	 * @throws java.lang.Exception
	 */
	public void tearDown() throws Exception {
	    Bdb.close();
		FileIO.recursiveDelete(new File(dbTarget));
	}
}
