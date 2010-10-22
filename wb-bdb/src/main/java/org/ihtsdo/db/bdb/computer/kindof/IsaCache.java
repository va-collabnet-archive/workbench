package org.ihtsdo.db.bdb.computer.kindof;

import java.io.IOException;

import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_FetchConceptFromCursor;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class IsaCache extends TypeCache {

	private NidBitSetBI nidSet;
	
	public IsaCache(NidBitSetBI nidSet) {
		super();
		this.nidSet = nidSet;
	}

	@Override
	public void processUnfetchedConceptData(int cNid,
			I_FetchConceptFromCursor fcfc) throws Exception {
		if (isCancelled() == false) {
			IntSet parentSet = new IntSet();
			Concept c = fcfc.fetch();
			for (RelationshipChronicleBI relv : c.getRelsOutgoing()) {
				for (RelationshipVersionBI rv : relv.getVersions()) {
					if (types.contains(rv.getTypeNid())) {
						if (relv.getVersions(coordinate).size() > 0) {
							parentSet.add(rv.getDestinationNid());
							break;
						}
					}
				}
			}
			typeMap.put(cNid, parentSet.getSetValues());
		}
	}

	@Override
	public NidBitSetBI getNidSet() throws IOException {
		return nidSet;
	}

}
