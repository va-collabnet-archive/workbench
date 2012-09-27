
package org.ihtsdo.contradiction;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.contradiction.ContradictionIdentifierBI;
import org.ihtsdo.tk.contradiction.ContradictionResult;

public class ContradictionConceptProcessor implements ProcessUnfetchedConceptDataBI {

    NidBitSetBI cNids = new IdentifierSet();
    private ContradictionIdentifierBI detector = null;
    private ContradictionIdentificationResults results = null;
    private I_ShowActivity activityMonitor;
    private AtomicInteger count = new AtomicInteger();
    private AtomicInteger found = new AtomicInteger();

	public ContradictionConceptProcessor(ViewCoordinate viewCoord, NidBitSetBI nidBitSetBI, I_ShowActivity actvityPanel) {
        // Via Task

        results = new ContradictionIdentificationResults(viewCoord);
        detector = Ts.get().getConflictIdentifier(viewCoord, false);

        this.cNids = nidBitSetBI;

        actvityPanel.setMaximum(cNids.cardinality()); 
        actvityPanel.setValue(count.get());
        actvityPanel.setIndeterminate(false);
        
        this.activityMonitor = actvityPanel;
     }

	@Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fcfc) throws Exception {
        int currentCount = count.incrementAndGet();
        
        if (currentCount == 1) {
        	AceLog.getAppLog().log(Level.INFO, "Number of concepts being processed " + cNids.cardinality());
        } else if (currentCount%50000 == 0) { 
        	AceLog.getAppLog().log(Level.INFO, "Have processed " + currentCount + " concepts");
        }
        
        if (activityMonitor != null && activityMonitor.isCanceled()) {
            return;
        }
        
        if (cNids.isMember(cNid)) { 
            ConceptChronicleBI c = fcfc.fetch();
            ContradictionResult position = (detector.isConceptInConflict(c));

            if (position.equals(ContradictionResult.CONTRADICTION)) {
                results.addConflict(c.getConceptNid());
                found.incrementAndGet();
            } else if (position.equals(ContradictionResult.DUPLICATE_EDIT)) {
                results.addConflictingDuplicateEditConcepts(c.getConceptNid());
                found.incrementAndGet();
	        } else if (position.equals(ContradictionResult.DUPLICATE_NEW)) {
	            results.addConflictingDuplicateNewConcepts(c.getConceptNid());
                found.incrementAndGet();
	        } else if (position.equals(ContradictionResult.ERROR)) {
	        	AceLog.getAppLog().log(Level.WARNING, "Failure in detecting contradictions on concept: " + c.getPrimUuid());
	        }
            else if (position.equals(ContradictionResult.SINGLE_MODELER_CHANGE)) {
                results.addSingle(c.getConceptNid());
            } else {
                results.addNoneConflicting(c.getConceptNid());
            }
        }
    }
    
    public AtomicInteger getNumberContradictionsFound() {
    	return found;
    }
    
    public AtomicInteger getNumberConceptsProcessed() {
    	return count;
    }
    
    public ContradictionIdentificationResults getResults() {
        return results;
    }

    public void setNidSet(NidBitSetBI nids) {
        this.cNids = nids; 
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return cNids;
    }

    @Override
    public boolean continueWork() {
        if (activityMonitor != null) {
            return !activityMonitor.isCanceled();
        }
        return true;
    }
}
 
