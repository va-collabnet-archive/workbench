package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.Refsets;
import org.ihtsdo.arena.spec.SynonymyType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.spec.ConceptSpec;

public class SetSynonymyAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    DescriptionVersionBI desc;
    String dialect;
    ConceptSpec synonymy;
    boolean currentSynonymy;

    public SetSynonymyAction(String actionName, DescFact fact, ConceptSpec synonymy, boolean currentSynonymy) {
        super(actionName);
        this.desc = fact.getComponent();
        this.dialect = dialect;
        this.synonymy = synonymy;
        this.currentSynonymy = currentSynonymy;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        I_ConfigAceFrame config;
        try {
            I_AmPart componentVersion;
            config = Terms.get().getActiveAceFrameConfig();
            TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                    config.getViewCoordinate());
            ViewCoordinate vc = config.getViewCoordinate();
            int dosNid = 0;
            if (Ts.get().hasUuid(SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getLenient().getPrimUuid())) {
                dosNid = SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getLenient().getNid();
            } else {
                dosNid = Refsets.DEGREE_OF_SYNONYMY.getLenient().getNid();
            }

            if (currentSynonymy) {
                Collection<? extends RefexChronicleBI> refexes = desc.getCurrentRefexes(vc);
                int synonymyTypeNid;

                if (synonymy.equals(SynonymyType.NEAR_SYNONYMOUS)) {
                    synonymyTypeNid = Ts.get().getNidForUuids(SynonymyType.NEAR_SYNONYMOUS.getLenient().getPrimUuid());
                } else if (synonymy.equals(SynonymyType.NON_SYNONYMOUS)) {
                    synonymyTypeNid = Ts.get().getNidForUuids(SynonymyType.NON_SYNONYMOUS.getLenient().getPrimUuid());
                } else {
                    synonymyTypeNid = Ts.get().getNidForUuids(SynonymyType.SYNONYM.getLenient().getPrimUuid());
                }

                if (refexes != null) {
                    for (RefexChronicleBI refex : refexes) {
                        if (refex.getCollectionNid() == dosNid) {
                            //make analog
                            componentVersion = (I_AmPart) refex;
                            AnalogBI analog = null;
                            for (PathBI ep : config.getEditingPathSet()) {
                                analog = componentVersion.makeAnalog(
                                        SnomedMetadataRfx.getCURRENT_NID(),
                                        config.getDbConfig().getUserConcept().getNid(),
                                        ep.getConceptNid(),
                                        Long.MAX_VALUE);

                                RefexVersionBI<?> newRefex = (RefexVersionBI<?>) analog;
                                //test member type
                                if (RefexCnidVersionBI.class.isAssignableFrom(newRefex.getClass())) {
                                    RefexCnidVersionBI rcv = (RefexCnidVersionBI) newRefex;
                                    RefexCnidAnalogBI rca = (RefexCnidAnalogBI) rcv;

                                    rca.setCnid1(synonymyTypeNid);

                                    I_GetConceptData concept = Terms.get().getConceptForNid(newRefex.getNid());
                                    Terms.get().addUncommitted(concept);
                                } else {
                                    throw new UnsupportedOperationException("Can't convert: RefexCnidVersionBI");
                                }
                            }
                        }
                    }
                }
            } else {
                if (synonymy.equals(SynonymyType.SYNONYM)) {
                    RefexCAB syn = new RefexCAB(
                            TK_REFSET_TYPE.CID,
                            desc.getNid(),
                            dosNid);
                    syn.put(RefexProperty.CNID1, Ts.get().getNidForUuids(SynonymyType.SYNONYM.getLenient().getPrimUuid()));
                    RefexChronicleBI<?> newRefex = tc.construct(syn);
                    I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
                    Ts.get().addUncommitted(refex);
                } else if (synonymy.equals(SynonymyType.NEAR_SYNONYMOUS)) {
                    RefexCAB nearSyn = new RefexCAB(
                            TK_REFSET_TYPE.CID,
                            desc.getNid(),
                            dosNid);
                    nearSyn.put(RefexProperty.CNID1, Ts.get().getNidForUuids(SynonymyType.NEAR_SYNONYMOUS.getLenient().getPrimUuid()));
                    RefexChronicleBI<?> newRefex = tc.construct(nearSyn);
                    I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
                    Ts.get().addUncommitted(refex);
                } else if (synonymy.equals(SynonymyType.NON_SYNONYMOUS)) {
                    RefexCAB notSyn = new RefexCAB(
                            TK_REFSET_TYPE.CID,
                            desc.getNid(),
                            dosNid);
                    notSyn.put(RefexProperty.CNID1, Ts.get().getNidForUuids(SynonymyType.NON_SYNONYMOUS.getLenient().getPrimUuid()));
                    RefexChronicleBI<?> newRefex = tc.construct(notSyn);
                    I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
                    Ts.get().addUncommitted(refex);
                } else {
                    throw new UnsupportedOperationException("Synonymy not supported");
                }
            }


        } catch (TerminologyException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }


    }
}
