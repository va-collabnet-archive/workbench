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
import org.ihtsdo.arena.spec.AcceptabilityType;
import org.ihtsdo.arena.spec.Refsets;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.DescFact;

public class MakeNotAcceptableAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    DescriptionVersionBI desc;
    String dialect;
    I_ConfigAceFrame config;

    public MakeNotAcceptableAction(String actionName, DescFact fact, String dialect, I_ConfigAceFrame config) {
        super(actionName);
        this.desc = fact.getComponent();
        this.dialect = dialect;
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            I_AmPart componentVersion;
            TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            ViewCoordinate vc = config.getViewCoordinate();
            Collection<? extends RefexChronicleBI> refexes =
                    desc.getCurrentRefexes(vc);

            if (dialect.equals("en-us")) {
                int evalRefsetNid = Ts.get().getNidForUuids(Refsets.EN_US_LANG.getLenient().getPrimUuid());
                if (refexes != null) {
                    for (RefexChronicleBI refex : refexes) {
                        if (refex.getCollectionNid() == evalRefsetNid) {
                            if (refex.isUncommitted()) {
                                RefexCnidAnalogBI refexAnalog = (RefexCnidAnalogBI) refex;
                                refexAnalog.setCnid1(Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));
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

                                    rca.setCnid1(Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));

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
                int evalRefsetNid = Ts.get().getNidForUuids(Refsets.EN_GB_LANG.getLenient().getPrimUuid());
                if (refexes != null) {
                    for (RefexChronicleBI refex : refexes) {
                        if (refex.getCollectionNid() == evalRefsetNid) {
                            if (refex.isUncommitted()) {
                                RefexCnidAnalogBI refexAnalog = (RefexCnidAnalogBI) refex;
                                refexAnalog.setCnid1(Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));
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

                                    rca.setCnid1(Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));

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


        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }


    }
}
