package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
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
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ComponentFact;
import org.ihtsdo.tk.example.binding.SnomedMetadataRf1;
import org.ihtsdo.tk.example.binding.SnomedMetadataRf2;

public class RetireAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    ComponentVersionBI analog;

    public RetireAction(String actionName, ComponentFact<ComponentVersionBI> fact) {
        super(actionName);
        this.component = fact.getComponent();
        /*
        putValue(LARGE_ICON_KEY,
        new ImageIcon(BatchMonitor.class.getResource("/24x24/plain/delete2.png")));
         */
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (I_AmPart.class.isAssignableFrom(component.getClass())) {
                I_AmPart componentVersion = (I_AmPart) component;
                I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
                for (PathBI ep : config.getEditingPathSet()) {
                    analog = (ComponentVersionBI) componentVersion.makeAnalog(
                            SnomedMetadataRfx.getRETIRED_NID(),
                            config.getDbConfig().getUserConcept().getNid(),
                            ep.getConceptNid(),
                            Long.MAX_VALUE);
                }
                
                if(DescriptionVersionBI.class.isAssignableFrom(component.getClass())){
                	retireFromRefexes(component);
                }
                I_GetConceptData concept = Terms.get().getConceptForNid(analog.getNid());
                Terms.get().addUncommitted(concept);
            }
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
    
    private void retireFromRefexes(ComponentVersionBI component) {
        DescriptionVersionBI desc = (DescriptionVersionBI) component;
        try {
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            I_AmPart componentVersion;
            ViewCoordinate vc = config.getViewCoordinate();
            Collection<? extends RefexChronicleBI> refexes = desc.getCurrentRefexes(vc);
            int usNid = 0;
            int gbNid = 0;
            int dosNid = 0;
            if (Ts.get().hasUuid(SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getPrimUuid())) {
                usNid = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getNid();
            } else {
                usNid = SnomedMetadataRf1.US_LANGUAGE_REFSET_RF1.getLenient().getNid();
            }
            if (Ts.get().hasUuid(SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getPrimUuid())) {
                gbNid = SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getNid();
            } else {
                gbNid = SnomedMetadataRf1.GB_LANGUAGE_REFSET_RF1.getLenient().getNid();
            }
            if (Ts.get().hasUuid(SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getLenient().getPrimUuid())) {
                dosNid = SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getLenient().getNid();
            } else {
                dosNid = SnomedMetadataRf1.DEGREE_OF_SYNONYMY_REFSET_RF1.getLenient().getNid();
            }
            for (RefexChronicleBI refex : refexes) {
                int refexNid = refex.getCollectionNid();
                if (refexNid == gbNid || refexNid == usNid || refexNid == dosNid) {
                    componentVersion = (I_AmPart) refex;
                    for (PathBI ep : config.getEditingPathSet()) {
                        componentVersion.makeAnalog(
                                SnomedMetadataRfx.getRETIRED_NID(),
                                config.getDbConfig().getUserConcept().getNid(),
                                ep.getConceptNid(),
                                Long.MAX_VALUE);
                    }
                    I_GetConceptData concept = Terms.get().getConceptForNid(analog.getNid());
                    Terms.get().addUncommitted(concept);
                } else {
                    throw new UnsupportedOperationException("Can't convert: RefexCnidVersionBI");
                }
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (TerminologyException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }
}
