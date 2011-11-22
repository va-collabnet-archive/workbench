package org.ihtsdo.db.bdb.computer.kindof;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.ConceptVersion;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;

public class IsaCache extends TypeCache {

	private NidBitSetBI nidSet;

	public IsaCache(NidBitSetBI nidSet) {
		super();
		this.nidSet = nidSet;
	}

	@Override
	public void processUnfetchedConceptData(int cNid,
			ConceptFetcherBI fcfc) throws Exception {
		if (isCancelled() == false) {
			Concept c = (Concept) fcfc.fetch();
                        if (c.getPrimUuid().equals(UUID.fromString("874ec6f1-5e6d-3478-9dbd-11ee47dac99d"))) {
                            System.out.println("Found: 874ec6f1-5e6d-3478-9dbd-11ee47dac99d");
                        }
			NidSet parentSet = getParentSet(c);
			typeMap.put(cNid, parentSet.getSetValues());
		}
	}

	public NidSet getParentSet(Concept concept) throws Exception {
		NidSet parentSet = new NidSet();
		ConceptVersion cv = new ConceptVersion((Concept) concept, inferredViewCoordinate);
		// Retired concept should be setup in the isa cache by stated parents
		

		if (!cv.isActive() || cv.getRelsOutgoingActiveIsa().isEmpty()) {
			cv = new ConceptVersion((Concept) concept, statedViewCoordinate);
		}

		for (RelationshipVersionBI rv : cv.getRelsOutgoingActive()) {
			if (types.contains(rv.getTypeNid())) {
				parentSet.add(rv.getDestinationNid());
			}
		}

		return parentSet;
	}

	public void updateConcept(int cNid) throws Exception {
		Concept c = (Concept) Ts.get().getConcept(cNid);
		NidSet parentSet = getParentSet(c);
		typeMap.put(cNid, parentSet.getSetValues());
	}

	public boolean isTested(int cNid) {
		return typeMap.containsKey(cNid);
	}

	@Override
	public NidBitSetBI getNidSet() throws IOException {
		return nidSet;
	}

	public void shutdown() {
		setCancelled(true);
	}
}
