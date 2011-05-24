/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.arena.contradiction;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import javax.swing.SwingWorker;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.contradiction.ContradictionConceptProcessor;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 *
 * @author kec
 */
public class ContradictionFinderSwingWorker 
extends SwingWorker<Set<Integer>, Integer> {
    
    private TerminologyListModel conflicts;
    private final I_ShowActivity actvityPanel;
	private ViewCoordinate viewCoord;

    public ContradictionFinderSwingWorker(ViewCoordinate vc, 
            TerminologyListModel conflicts) {
        this.conflicts = conflicts;
        this.viewCoord = vc;
        this.actvityPanel = Terms.get().newActivityPanel(true, null, "Identifying conflicts", true);
    }

    @Override
    protected Set<Integer> doInBackground() throws Exception {
        ContradictionConceptProcessor ccp 
                = new ContradictionConceptProcessor(viewCoord, 
                Ts.get().getAllConceptNids(),
                actvityPanel);
        
        Ts.get().iterateConceptDataInParallel(ccp);
        
        Set<Integer> returnSet = new HashSet<Integer>();
        returnSet.addAll(ccp.getResults().getConflictingNids());
        returnSet.addAll(ccp.getResults().getDuplicateNewNids());
        returnSet.addAll(ccp.getResults().getDuplicateEditNids());
        
        return returnSet;
    }

    @Override
    protected void process(List<Integer> list) {
        for (Integer nid: list) {
            try {
                conflicts.addElement((I_GetConceptData) 
                        Ts.get().getConcept(nid));
            } catch (IOException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    @Override
    protected void done() {
        try {
            Set<Integer> conflictingNids = get();
            conflictingNids.removeAll(conflicts.getNidsInList());
            for (Integer cnid: conflictingNids) {
                conflicts.addElement((I_GetConceptData) 
                        Ts.get().getConcept(cnid));
            }
            actvityPanel.complete();
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }
}
