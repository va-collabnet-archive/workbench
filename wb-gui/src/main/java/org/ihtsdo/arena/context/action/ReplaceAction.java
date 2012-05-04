package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.RelFact;
import org.ihtsdo.tk.drools.facts.RelSpecFact;
import org.ihtsdo.tk.drools.facts.SpecFact;
import org.ihtsdo.tk.spec.RelSpec;

public class ReplaceAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    SpecFact<?> spec;
    ConceptVersionBI concept;
    I_ConfigAceFrame config;

    public ReplaceAction(String actionName, RelFact fact,
            SpecFact<?> spec, I_ConfigAceFrame config) {
        super(actionName);
        this.component = fact.getComponent();
        this.spec = spec;
        this.config = config;


        try {
            UUID uuid = Terms.get().nidToUuid(component.getConceptNid());
            ConceptVersionBI cv = Ts.get().getConceptVersion(
                    config.getViewCoordinate(),
                    uuid);
            this.concept = cv;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (RelSpecFact.class.isAssignableFrom(spec.getClass())) {
                addRel();
                if (I_AmPart.class.isAssignableFrom(component.getClass())) {
                    retireRel();
                }
            } else {
                throw new Exception("Can't handle type: " + spec);
            }
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void addRel() {
        RelSpec relSpec = ((RelSpecFact) spec).getRelSpec();
        try {
            Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
            I_GetConceptData originConcept = Terms.get().getConcept(concept.getNid());
            I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(),
                    originConcept,
                    relSpec.getRelTypeSpec().getLenient().getNid(),
                    relSpec.getDestinationSpec().getLenient().getNid(),
                    SnomedMetadataRfx.getREL_CH_DEFINING_CHARACTERISTIC_NID(),
                    SnomedMetadataRfx.getREL_OPTIONAL_REFINABILITY_NID(),
                    0,
                    SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                    config.getDbConfig().getUserConcept().getNid(),
                    pathItr.next().getConceptNid(),
                    Long.MAX_VALUE);

            while (pathItr.hasNext()) {
                newRel.makeAnalog(newRel.getStatusNid(),
                        Long.MAX_VALUE,
                        config.getEditCoordinate().getAuthorNid(),
                        config.getEditCoordinate().getModuleNid(), 
                        pathItr.next().getConceptNid());
            }
            Terms.get().addUncommitted(originConcept);

        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    private void retireRel() {
        try {
            I_AmPart componentVersion = (I_AmPart) component;
            for (PathBI ep : config.getEditingPathSet()) {
                componentVersion.makeAnalog(
                        SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                        Long.MAX_VALUE,
                        config.getEditCoordinate().getAuthorNid(),
                        config.getEditCoordinate().getModuleNid(), 
                        ep.getConceptNid());
            }
            I_GetConceptData concept = Terms.get().getConceptForNid(componentVersion.getNid());
            Terms.get().addUncommitted(concept);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }
}
