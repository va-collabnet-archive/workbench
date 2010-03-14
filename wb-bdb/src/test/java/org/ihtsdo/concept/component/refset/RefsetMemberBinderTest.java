package org.ihtsdo.concept.component.refset;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.refsetmember.cid.CidMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sleepycat.je.DatabaseEntry;

public class RefsetMemberBinderTest {

    private RefsetMemberBinder binder;
    String dbTarget;

    @Before
    public void setUp() throws Exception {
        dbTarget = "target/" + UUID.randomUUID();
        Bdb.setup(dbTarget);
        binder = new RefsetMemberBinder();
    }

    @After
    public void tearDown() throws Exception {
        Bdb.close();
        FileIO.recursiveDelete(new File(dbTarget));
    }

    @Test
    public void testRoundTrip() throws IOException {
        Collection<RefsetMember<?, ?>> list = new ArrayList<RefsetMember<?,?>>();
        long time = System.currentTimeMillis();
        
        UUID primordialConceptUuid = UUID.randomUUID();
        int cNid = Bdb.uuidToNid(primordialConceptUuid);
        assert cNid != 0;
        Concept c = Concept.get(cNid);
        ConceptAttributes ca = new ConceptAttributes();
        ca.enclosingConceptNid = cNid;
        ca.nid = cNid;
        ca.primordialSapNid = Bdb.getSapNid(ReferenceConcepts.CURRENT.getNid(), 
            ReferenceConcepts.TERM_AUXILIARY_PATH.getNid(), 
            time);
        ca.primordialUNid = Bdb.getUuidsToNidMap().getUNid(primordialConceptUuid);
        ca.setDefined(true);
        c.setConceptAttributes(ca);
        
        CidMember member = new CidMember();
        UUID primordialComponentUuid = UUID.randomUUID();
        int memberNid = Bdb.uuidToNid(primordialComponentUuid);
        member.nid = memberNid;
        member.referencedComponentNid = cNid;
        member.setC1Nid(cNid);
        member.primordialUNid = Bdb.getUuidsToNidMap().getUNid(primordialComponentUuid);
        member.primordialSapNid = Bdb.getSapNid(ReferenceConcepts.CURRENT.getNid(), 
            ReferenceConcepts.TERM_AUXILIARY_PATH.getNid(), 
            time);
        member.makeAnalog(ReferenceConcepts.RETIRED.getNid(), 
            ReferenceConcepts.TERM_AUXILIARY_PATH.getNid(), 
            time + 1);
        list.add(member);
        DatabaseEntry entry = new DatabaseEntry();
        binder.setupBinder(c);
        binder.objectToEntry(list, entry);
        Collection<RefsetMember<?, ?>> newList = binder.entryToObject(entry);
        assertEquals(list, newList);
    }

}
