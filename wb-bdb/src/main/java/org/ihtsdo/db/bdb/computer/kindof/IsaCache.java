package org.ihtsdo.db.bdb.computer.kindof;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PositionBI;
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
			ConceptFetcherBI fcfc) throws Exception {
		if (isCancelled() == false) {
			IntSet parentSet = new IntSet();
			Concept c = (Concept) fcfc.fetch();
			parentSet = getParentSet(c);
			typeMap.put(cNid, parentSet.getSetValues());
		}
	}

	public IntSet getParentSet(Concept concept) throws Exception {
		IntSet parentSet = new IntSet();
		for (RelationshipChronicleBI relv : concept.getRelsOutgoing()) {
			for (RelationshipVersionBI rv : relv.getVersions()) {
				if (types.contains(rv.getTypeNid())) {
					if (relv.getVersions(coordinate).size() > 0) {
						parentSet.add(rv.getDestinationNid());
						break;
					}
				}
			}
		}
		return parentSet;
	}

	public void updateConcept(int cNid) throws Exception {
		Concept c = (Concept) Ts.get().getConcept(cNid);
		IntSet parentSet = new IntSet();
		parentSet = getParentSet(c);
		typeMap.put(cNid, parentSet.getSetValues());
	}

	@Override
	public NidBitSetBI getNidSet() throws IOException {
		return nidSet;
	}

}
