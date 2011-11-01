package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ComponentFact;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;

public class CloneAndRetireAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    I_ConfigAceFrame config;

    public CloneAndRetireAction(String actionName, ComponentFact<ComponentVersionBI> fact, I_ConfigAceFrame config) {
        super(actionName);
        this.component = fact.getComponent();
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            I_GetConceptData concept = Terms.get().getConceptForNid(component.getNid());
            Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
            if (ConAttrVersionBI.class.isAssignableFrom(component.getClass())) {
                throw new UnsupportedOperationException();
            }
            if (ConceptVersionBI.class.isAssignableFrom(component.getClass())) {
                throw new UnsupportedOperationException();
            }
            if (DescriptionVersionBI.class.isAssignableFrom(component.getClass())) {
                DescriptionVersionBI desc = (DescriptionVersionBI) component;
                Collection<? extends RefexVersionBI<?>> oldRefexes = desc.getCurrentRefexes(config.getViewCoordinate());
                I_DescriptionVersioned newDesc = Terms.get().newDescription(UUID.randomUUID(), concept,
                        desc.getLang(), desc.getText(), Terms.get().getConcept(desc.getTypeNid()),
                        config, Terms.get().getConcept(desc.getStatusNid()), Long.MAX_VALUE);
                newDesc.setInitialCaseSignificant(desc.isInitialCaseSignificant());
                TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                        config.getViewCoordinate());
                int dosNid =SnomedMetadataRfx.getSYNONYMY_REFEX_NID();
                for (RefexVersionBI refex : oldRefexes) {
                    if (refex.getCollectionNid() != dosNid) { //not cloning degree of synonymy refeset membership
                        RefexCAB newSpec = new RefexCAB(
                                TK_REFSET_TYPE.CID,
                                newDesc.getNid(),
                                refex.getCollectionNid());
                        RefexCnidVersionBI cv =
                                (RefexCnidVersionBI) refex.getVersion(config.getViewCoordinate());
                        int typeNid = cv.getCnid1();
                        newSpec.put(RefexProperty.CNID1, typeNid);
                        tc.construct(newSpec);
                        ConceptChronicleBI refexConcept = Ts.get().getConcept(refex.getConceptNid());
                        if (!refexConcept.isAnnotationStyleRefex()) {
                            Ts.get().addUncommitted(refexConcept);
                        }
                    }
                }
                retireFromRefexes(component);
            }
            if (RelationshipVersionBI.class.isAssignableFrom(component.getClass())) {
                RelationshipVersionBI rel = (RelationshipVersionBI) component;
                I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), concept,
                        rel.getTypeNid(),
                        rel.getDestinationNid(),
                        rel.getCharacteristicNid(),
                        rel.getRefinabilityNid(),
                        rel.getGroup(),
                        rel.getStatusNid(),
                        config.getDbConfig().getUserConcept().getNid(),
                        pathItr.next().getConceptNid(),
                        Long.MAX_VALUE);

                while (pathItr.hasNext()) {
                    newRel.makeAnalog(newRel.getStatusNid(), newRel.getAuthorNid(),
                            pathItr.next().getConceptNid(), Long.MAX_VALUE);
                }
            }

            if (I_AmPart.class.isAssignableFrom(component.getClass())) {
                I_AmPart componentVersion = (I_AmPart) component;
                ComponentVersionBI analog = null;
                for (PathBI ep : config.getEditingPathSet()) {
                    analog = (ComponentVersionBI) componentVersion.makeAnalog(
                            SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                            config.getDbConfig().getUserConcept().getNid(),
                            ep.getConceptNid(),
                            Long.MAX_VALUE);
                }
                if (DescriptionVersionBI.class.isAssignableFrom(component.getClass())) {
                    retireFromRefexes(analog);
                }
            }

            Terms.get().addUncommitted(concept);


        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (PropertyVetoException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InvalidCAB e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (ContraditionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }
    
    private void retireFromRefexes(ComponentVersionBI component) {
        DescriptionVersionBI desc = (DescriptionVersionBI) component;
        try {
            I_AmPart componentVersion;
            ViewCoordinate vc = config.getViewCoordinate();
            Collection<? extends RefexChronicleBI> refexes = desc.getCurrentRefexes(vc);
            int usNid = SnomedMetadataRfx.getUS_DIALECT_REFEX_NID();
            int gbNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
            int dosNid =SnomedMetadataRfx.getSYNONYMY_REFEX_NID();
            for (RefexChronicleBI refex : refexes) {
                int refexNid = refex.getCollectionNid();
                if (refexNid == gbNid || refexNid == usNid || refexNid == dosNid) {
                    componentVersion = (I_AmPart) refex;
                    for (PathBI ep : config.getEditingPathSet()) {
                        componentVersion.makeAnalog(
                                SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                                config.getDbConfig().getUserConcept().getNid(),
                                ep.getConceptNid(),
                                Long.MAX_VALUE);
                    }
                    I_GetConceptData concept = Terms.get().getConceptForNid(component.getNid());
                    Terms.get().addUncommitted(concept);
                } else {
                    throw new UnsupportedOperationException("Can't convert: RefexCnidVersionBI");
                }
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } 
    }
}
