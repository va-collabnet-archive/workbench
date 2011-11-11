package org.ihtsdo.db.bdb.computer.kindof;

import java.io.IOException;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.attributes.ConceptAttributes.Version;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.ValidationException;

public class IsaCache extends TypeCache {

	private NidBitSetBI nidSet;
	private int activeValueNid;

	public IsaCache(NidBitSetBI nidSet) {
		super();
		this.nidSet = nidSet;
		try {
			this.activeValueNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
		} catch (ValidationException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	@Override
	public void processUnfetchedConceptData(int cNid,
			ConceptFetcherBI fcfc) throws Exception {
		if (isCancelled() == false) {
			Concept c = (Concept) fcfc.fetch();
			NidSet parentSet = getParentSet(c);
			typeMap.put(cNid, parentSet.getSetValues());
		}
	}

	public NidSet getParentSet(Concept concept) throws Exception {
		NidSet parentSet = new NidSet();
		// Retired concept should be setup in the isa cache by stated parents
		boolean isActive = true;
		for (Version loopAttrVersion : concept.getConceptAttributes().getVersions(coordinate)) {
			if (loopAttrVersion.getStatusNid() != activeValueNid) {
				isActive = false;
			}
		}

		if (isActive) {
			for (RelationshipChronicleBI relv : concept.getRelsOutgoing()) {
				for (RelationshipVersionBI rv : relv.getVersions(inferredViewCoordinate)) {
					if (types.contains(rv.getTypeNid()) && rv.getStatusNid() == activeValueNid) {
						parentSet.add(rv.getDestinationNid());
						break;
					}
				}
			}
			
			if (parentSet.size() == 0) {
				for (RelationshipChronicleBI relv : concept.getRelsOutgoing()) {
					for (RelationshipVersionBI rv : relv.getVersions(statedViewCoordinate)) {
						if (types.contains(rv.getTypeNid()) && rv.getStatusNid() == activeValueNid) {
							parentSet.add(rv.getDestinationNid());
							break;
						}
					}
				}
			}
			
		} else {
			for (RelationshipChronicleBI relv : concept.getRelsOutgoing()) {
				for (RelationshipVersionBI rv : relv.getVersions(statedViewCoordinate)) {
					if (types.contains(rv.getTypeNid()) && rv.getStatusNid() == activeValueNid) {
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
