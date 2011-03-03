package org.ihtsdo.db.bdb.computer.kindof;

import java.io.IOException;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidSet;
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
            Concept c = (Concept) fcfc.fetch();
            NidSet parentSet = getParentSet(c);
            typeMap.put(cNid, parentSet.getSetValues());
        }
    }

    public NidSet getParentSet(Concept concept) throws Exception {
        NidSet parentSet = new NidSet();
        for (RelationshipChronicleBI relv : concept.getRelsOutgoing()) {
            for (RelationshipVersionBI rv : relv.getVersions(coordinate)) {
                if (types.contains(rv.getTypeNid())) {
                   parentSet.add(rv.getDestinationNid());
                   break;
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

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidSet;
    }
    
    public void shutdown() {
    	setCancelled(true);
    }
}
