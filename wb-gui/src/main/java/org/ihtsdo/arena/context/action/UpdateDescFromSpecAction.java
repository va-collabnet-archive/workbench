package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.Collection;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.ContradictionException;//THIS
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.DescSpecFact;
import org.ihtsdo.tk.drools.facts.SpecFact;
import org.ihtsdo.tk.drools.facts.RelSpecFact;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelationshipSpec;

public class UpdateDescFromSpecAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ConceptVersionBI component;
    SpecFact<?> spec;
    I_ConfigAceFrame config;

    public UpdateDescFromSpecAction(String actionName,
            ConceptFact fact, SpecFact<?> spec, I_ConfigAceFrame config) throws IOException {
        super(actionName);
        this.component = fact.getComponent();
        this.spec = spec;
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (DescSpecFact.class.isAssignableFrom(spec.getClass())) {
                updateDesc();
            } else {
                throw new Exception("Can't handle type: " + spec);
            }
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void updateDesc() throws TerminologyException, IOException, ContradictionException {
        I_GetConceptData concept = Terms.get().getConceptForNid(component.getNid());

        Collection descriptions = component.getDescriptionsActive();

        for (Object descObject : descriptions) {
            DescriptionVersionBI desc = (DescriptionVersionBI) descObject;
            DescriptionSpec descSpec = ((DescSpecFact) spec).getDescSpec();

            if (desc.getTypeNid() == descSpec.getDescriptionTypeSpec().getLenient().getNid()
                    && !(desc.getText().equals(descSpec.getDescriptionText()))) { //if desc type is equal and text has changed, retire and make new

                //description
                if (DescSpecFact.class.isAssignableFrom(spec.getClass())) {

                    Terms.get().newDescription(UUID.randomUUID(), Terms.get().getConcept(concept.getNid()),
                            descSpec.getLangText(),
                            descSpec.getDescriptionText(),
                            Terms.get().getConcept(descSpec.getDescriptionTypeSpec().getLenient().getNid()),
                            config, SnomedMetadataRfx.getSTATUS_CURRENT_NID());
                    Terms.get().addUncommitted(Terms.get().getConcept(concept.getNid()));
                }

                //concept
                if (RelSpecFact.class.isAssignableFrom(spec.getClass())) {
                    RelationshipSpec relSpec = ((RelSpecFact) spec).getRelSpec();
                    Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
                    I_GetConceptData originConcept = Terms.get().getConcept(concept.getNid());
                    I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(),
                            originConcept,
                            relSpec.getRelationshipTypeSpec().getLenient().getNid(),
                            relSpec.getTargetSpec().getLenient().getNid(),
                            SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID(),
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
                }



                if (I_AmPart.class.isAssignableFrom(desc.getClass())) {
                    I_AmPart componentVersion = (I_AmPart) desc;
                    for (PathBI ep : config.getEditingPathSet()) {
                        componentVersion.makeAnalog(
                                SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                                Long.MAX_VALUE,
                                config.getEditCoordinate().getAuthorNid(),
                                config.getEditCoordinate().getModuleNid(), 
                                ep.getConceptNid());
                    }
                    I_GetConceptData retireConcept = Terms.get().getConceptForNid(componentVersion.getNid());
                    Terms.get().addUncommitted(retireConcept);
                }
            } else { //other: make analog and update
                I_DescriptionVersioned<?> description = Terms.get().getDescription(Terms.get().uuidToNative(descSpec.getUuids()));
                I_DescriptionPart descPart = description.getTuples(config.getConflictResolutionStrategy()).iterator().next().getMutablePart();
                I_DescriptionPart newPart = (I_DescriptionPart) descPart.makeAnalog(
                        SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                        Long.MAX_VALUE,
                        config.getEditCoordinate().getAuthorNid(),
                        config.getEditCoordinate().getModuleNid(), 
                        config.getEditingPathSet().iterator().next().getConceptNid());
                try {
                    newPart.setText(descSpec.getDescriptionText());
                } catch (PropertyVetoException ex) {
                    throw new IOException(ex);
                }
                description.addVersion(newPart);

            }
            Terms.get().addUncommitted(Terms.get().getConcept(concept.getNid()));
        }
    }
}