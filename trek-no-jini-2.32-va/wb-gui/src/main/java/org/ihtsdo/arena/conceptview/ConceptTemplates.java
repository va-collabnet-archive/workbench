/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.io.IOException;
import jsr166y.ConcurrentReferenceHashMap;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.drools.EditPanelKb;

/**
 *
 * @author akf
 */
public class ConceptTemplates {
    
    public static ConcurrentReferenceHashMap<Integer, Boolean> templates = new ConcurrentReferenceHashMap<Integer, Boolean>(
            ConcurrentReferenceHashMap.ReferenceType.STRONG,
                ConcurrentReferenceHashMap.ReferenceType.WEAK);
    public static ConcurrentReferenceHashMap<Integer, Boolean> dataChecks = new ConcurrentReferenceHashMap<Integer, Boolean>(
            ConcurrentReferenceHashMap.ReferenceType.STRONG,
                ConcurrentReferenceHashMap.ReferenceType.WEAK);
    
    public static EditPanelKb kb = null;
    public static EditPanelKb getKb(){
        if(kb != null){
            return kb;
        }
        I_ConfigAceFrame config = null;
        try {
            config = Terms.get().getActiveAceFrameConfig();
        } catch (TerminologyException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
        return new EditPanelKb(config);
    }
}
