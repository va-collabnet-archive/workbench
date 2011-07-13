package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.Refsets;
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
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.example.binding.SnomedMetadataRf2;

public class CopyDescAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI sourceComponent;
    ComponentVersionBI targetComponent;

    public CopyDescAction(String actionName, DescFact sourceFact, ConceptFact destFact) {
        super(actionName);
        this.sourceComponent = sourceFact.getComponent();
        this.targetComponent = destFact.getComponent();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            I_GetConceptData concept = Terms.get().getConceptForNid(targetComponent.getNid());
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
            if (ConAttrVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
                throw new UnsupportedOperationException();
            }
            if (ConceptVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
                throw new UnsupportedOperationException();
            }
            if (DescriptionVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
                DescriptionVersionBI desc = (DescriptionVersionBI) sourceComponent;
                Collection<? extends RefexVersionBI<?>> oldRefexes = desc.getCurrentRefexes(config.getViewCoordinate());
                I_DescriptionVersioned newDesc = Terms.get().newDescription(UUID.randomUUID(), concept,
                        desc.getLang(), desc.getText(), Terms.get().getConcept(desc.getTypeNid()),
                        config, Terms.get().getConcept(desc.getStatusNid()), Long.MAX_VALUE);
                newDesc.setInitialCaseSignificant(desc.isInitialCaseSignificant());
                TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                        config.getViewCoordinate());
                int dosNid = 0;
                if (Ts.get().hasUuid(SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getLenient().getPrimUuid())) {
                    dosNid = SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getLenient().getNid();
                } else {
                    dosNid = Refsets.DEGREE_OF_SYNONYMY.getLenient().getNid();
                }
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
            }
            if (RelationshipVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
                RelationshipVersionBI rel = (RelationshipVersionBI) sourceComponent;
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
}
