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
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.Refsets;
import org.ihtsdo.arena.spec.AcceptabilityType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.DescFact;


public class MakeAcceptableAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    DescriptionVersionBI desc;
    String dialect;

    public MakeAcceptableAction(String actionName, DescFact fact, String dialect) {
        super(actionName);
        this.desc = fact.getComponent();
        this.dialect = dialect;
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
            Collection<? extends RefexChronicleBI> refexes =
                    desc.getCurrentRefexes(vc);

            if (dialect.equals("en-us")) {
                int evalRefsetNid = 0;
                if (Ts.get().hasUuid(SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getPrimUuid())) {
                    evalRefsetNid = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getNid();
                } else{
                    evalRefsetNid = SnomedMetadataRf1.US_LANGUAGE_REFSET_RF1.getLenient().getNid();
                }
                
                int acceptabilityNid = 0;
                if (Ts.get().hasUuid(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid())) {
                    acceptabilityNid = SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid();
                } else{
                    acceptabilityNid = SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1.getLenient().getNid();
                }
                
                if (refexes != null) {
                    for (RefexChronicleBI refex : refexes) {
                        if (refex.getCollectionNid() == evalRefsetNid) {
                            if (refex.isUncommitted()) {
                                RefexCnidAnalogBI refexAnalog = (RefexCnidAnalogBI) refex;
                                refexAnalog.setCnid1(acceptabilityNid);
                                I_GetConceptData concept = Terms.get().getConceptForNid(refex.getNid());
                                Terms.get().addUncommitted(concept);
                            } else {
                                //make analog
                                componentVersion = (I_AmPart) refex;
                                AnalogBI analog = null;
                                for (PathBI ep : config.getEditingPathSet()) {
                                    analog = componentVersion.makeAnalog(
                                            SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                                            config.getDbConfig().getUserConcept().getNid(),
                                            ep.getConceptNid(),
                                            Long.MAX_VALUE);
                                }
                                RefexVersionBI<?> newRefex = (RefexVersionBI<?>) analog;
                                //test member type
                                if (RefexCnidVersionBI.class.isAssignableFrom(newRefex.getClass())) {
                                    RefexCnidVersionBI rcv = (RefexCnidVersionBI) newRefex;
                                    RefexCnidAnalogBI rca = (RefexCnidAnalogBI) rcv;

                                    rca.setCnid1(acceptabilityNid);

                                    I_GetConceptData concept = Terms.get().getConceptForNid(newRefex.getNid());
                                    Terms.get().addUncommitted(concept);
                                } else {
                                    throw new UnsupportedOperationException("Can't convert: RefexCnidVersionBI");
                                }
                            }
                        }
                    }
                }
            } else if (dialect.equals("en-gb")) {
                int evalRefsetNid = 0;
                if (Ts.get().hasUuid(SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getPrimUuid())) {
                    evalRefsetNid = SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getNid();
                } else{
                    evalRefsetNid = SnomedMetadataRf1.GB_LANGUAGE_REFSET_RF1.getLenient().getNid();
                }
                
                int acceptabilityNid = 0;
                if (Ts.get().hasUuid(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid())) {
                    acceptabilityNid = SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid();
                } else{
                    acceptabilityNid = SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1.getLenient().getNid();
                }
                
                if (refexes != null) {
                    for (RefexChronicleBI refex : refexes) {
                        if (refex.getCollectionNid() == evalRefsetNid) {
                            if (refex.isUncommitted()) {
                                RefexCnidAnalogBI refexAnalog = (RefexCnidAnalogBI) refex;
                                refexAnalog.setCnid1(acceptabilityNid);
                                I_GetConceptData concept = Terms.get().getConceptForNid(refex.getNid());
                                Terms.get().addUncommitted(concept);
                            } else {
                                //make analog
                                componentVersion = (I_AmPart) refex;
                                AnalogBI analog = null;
                                for (PathBI ep : config.getEditingPathSet()) {
                                    analog = componentVersion.makeAnalog(
                                            SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                                            config.getDbConfig().getUserConcept().getNid(),
                                            ep.getConceptNid(),
                                            Long.MAX_VALUE);
                                }
                                RefexVersionBI<?> newRefex = (RefexVersionBI<?>) analog;
                                //test member type
                                if (RefexCnidVersionBI.class.isAssignableFrom(newRefex.getClass())) {
                                    RefexCnidVersionBI rcv = (RefexCnidVersionBI) newRefex;
                                    RefexCnidAnalogBI rca = (RefexCnidAnalogBI) rcv;

                                    rca.setCnid1(acceptabilityNid);

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
                throw new UnsupportedOperationException("Dialect not supported");
            }


        } catch (TerminologyException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }


    }
}
