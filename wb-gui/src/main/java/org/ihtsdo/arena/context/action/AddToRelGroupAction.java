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
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.DescSpecFact;
import org.ihtsdo.tk.drools.facts.RelSpecFact;
import org.ihtsdo.tk.drools.facts.SpecFact;
import org.ihtsdo.tk.drools.facts.RelGroupFact;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelationshipSpec;

public class AddToRelGroupAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    SpecFact<?> spec;
    I_ConfigAceFrame config;

    public AddToRelGroupAction(String actionName,
            RelGroupFact relGroup, SpecFact<?> spec, I_ConfigAceFrame config) throws IOException {
        super(actionName);
        this.component = relGroup.getRelGroup();
        this.spec = spec;
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (RelSpecFact.class.isAssignableFrom(spec.getClass())) {
                addRel();
            } else if (DescSpecFact.class.isAssignableFrom(spec.getClass())) {
                addDesc();
            } else {
                throw new Exception("Can't handle type: " + spec);
            }
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void addDesc() throws TerminologyException, IOException {
        DescriptionSpec descSpec = ((DescSpecFact) spec).getDescSpec();
        Terms.get().newDescription(UUID.randomUUID(), Terms.get().getConcept(component.getConceptNid()),
                descSpec.getLangText(),
                descSpec.getDescriptionText(),
                Terms.get().getConcept(descSpec.getDescriptionTypeSpec().getLenient().getNid()),
                config, SnomedMetadataRfx.getSTATUS_CURRENT_NID());
        Terms.get().addUncommitted(Terms.get().getConcept(component.getConceptNid()));
    }

    private void addRel() {
        RelationshipSpec relSpec = ((RelSpecFact) spec).getRelSpec();
        try {
            RelationshipGroupChronicleBI group = (RelationshipGroupChronicleBI) component;
            Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
            I_GetConceptData originConcept = Terms.get().getConcept(component.getConceptNid());
            I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(),
                    originConcept,
                    relSpec.getRelationshipTypeSpec().getLenient().getNid(),
                    relSpec.getTargetSpec().getLenient().getNid(),
                    SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID(),
                    SnomedMetadataRfx.getREL_OPTIONAL_REFINABILITY_NID(),
                    group.getRelationshipGroupNumber(), //set to relGroup
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
}