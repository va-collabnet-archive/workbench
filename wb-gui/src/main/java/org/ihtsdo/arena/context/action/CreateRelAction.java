package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.spec.ConceptSpec;

public class CreateRelAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ConceptVersionBI origin;
    int relTypeNid;
    int destNid;
    I_ConfigAceFrame config;

    public CreateRelAction(String actionName,
            ConceptFact origin, ConceptSpec type, ConceptFact destination, I_ConfigAceFrame config) throws IOException {
        super(actionName);
        this.origin = origin.getConcept();
        relTypeNid = type.getLenient().getNid();
        destNid = destination.getConcept().getNid();
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
            I_GetConceptData originConcept = Terms.get().getConcept(origin.getNid());
            I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(),
                    originConcept,
                    relTypeNid,
                    destNid,
                    SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID(),
                    SnomedMetadataRfx.getREL_OPTIONAL_REFINABILITY_NID(),
                    0,
                    SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                    config.getDbConfig().getUserConcept().getNid(),
                    pathItr.next().getConceptNid(),
                    Long.MAX_VALUE);

            while (pathItr.hasNext()) {
                newRel.makeAnalog(newRel.getStatusNid(), config.getDbConfig().getUserConcept().getNid(), 
                        pathItr.next().getConceptNid(), Long.MAX_VALUE);
            }
            Terms.get().addUncommitted(originConcept);

        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }
}