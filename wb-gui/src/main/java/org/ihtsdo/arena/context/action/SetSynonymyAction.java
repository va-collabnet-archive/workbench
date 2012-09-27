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
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.SynonymyType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidAnalogBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.spec.ConceptSpec;

public class SetSynonymyAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    DescriptionVersionBI desc;
    String dialect;
    ConceptSpec synonymy;
    boolean currentSynonymy;
    I_ConfigAceFrame config;

    public SetSynonymyAction(String actionName, DescFact fact, ConceptSpec synonymy, boolean currentSynonymy, I_ConfigAceFrame config) {
        super(actionName);
        this.desc = fact.getComponent();
        this.synonymy = synonymy;
        this.currentSynonymy = currentSynonymy;
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            I_AmPart componentVersion;
            config = Terms.get().getActiveAceFrameConfig();
            TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            ViewCoordinate vc = config.getViewCoordinate();
            int dosNid = SnomedMetadataRfx.getSYNONYMY_REFEX_NID();

            if (currentSynonymy) {
                Collection<? extends RefexChronicleBI> refexes = desc.getRefexesActive(vc);
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
                        if (refex.getRefexNid() == dosNid) {
                            //make analog
                            componentVersion = (I_AmPart) refex;
                            AnalogBI analog = null;
                            for (PathBI ep : config.getEditingPathSet()) {
                                analog = componentVersion.makeAnalog(
                                        SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                                        Long.MAX_VALUE,
                                        config.getEditCoordinate().getAuthorNid(),
                                        config.getEditCoordinate().getModuleNid(), 
                                        ep.getConceptNid());

                                RefexVersionBI<?> newRefex = (RefexVersionBI<?>) analog;
                                //test member type
                                if (RefexNidVersionBI.class.isAssignableFrom(newRefex.getClass())) {
                                    RefexNidVersionBI rcv = (RefexNidVersionBI) newRefex;
                                    RefexNidAnalogBI rca = (RefexNidAnalogBI) rcv;

                                    rca.setNid1(synonymyTypeNid);

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
                            TK_REFEX_TYPE.CID,
                            desc.getNid(),
                            dosNid);
                    syn.put(RefexProperty.CNID1, Ts.get().getNidForUuids(SynonymyType.SYNONYM.getLenient().getPrimUuid()));
                    RefexChronicleBI<?> newRefex = tc.construct(syn);
                    I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
                    Ts.get().addUncommitted(refex);
                } else if (synonymy.equals(SynonymyType.NEAR_SYNONYMOUS)) {
                    RefexCAB nearSyn = new RefexCAB(
                            TK_REFEX_TYPE.CID,
                            desc.getNid(),
                            dosNid);
                    nearSyn.put(RefexProperty.CNID1, Ts.get().getNidForUuids(SynonymyType.NEAR_SYNONYMOUS.getLenient().getPrimUuid()));
                    RefexChronicleBI<?> newRefex = tc.construct(nearSyn);
                    I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
                    Ts.get().addUncommitted(refex);
                } else if (synonymy.equals(SynonymyType.NON_SYNONYMOUS)) {
                    RefexCAB notSyn = new RefexCAB(
                            TK_REFEX_TYPE.CID,
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
        } catch (ContradictionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }


    }
}
