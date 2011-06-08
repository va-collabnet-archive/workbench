package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.drools.facts.DescFact;

public class SetICSignificantAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    DescriptionVersionBI desc;

    public SetICSignificantAction(String actionName, DescFact fact) {
        super(actionName);
        this.desc = fact.getComponent();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        I_ConfigAceFrame config;
        I_DescriptionVersioned dv = null;
        try {
            config = Terms.get().getActiveAceFrameConfig();
            I_DescriptionVersioned descVersion = Terms.get().getDescription(desc.getNid());
            DescriptionAnalogBI analog = null;
            if (desc.isInitialCaseSignificant()) {
                for (PathBI ep : config.getEditingPathSet()) {
                    analog = (DescriptionAnalogBI) descVersion.makeAnalog(
                            ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
                            config.getDbConfig().getUserConcept().getNid(),
                            ep.getConceptNid(),
                            Long.MAX_VALUE);
                    analog.setInitialCaseSignificant(false);
                }
            } else {
                for (PathBI ep : config.getEditingPathSet()) {
                    analog = (DescriptionAnalogBI) descVersion.makeAnalog(
                            ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
                            config.getDbConfig().getUserConcept().getNid(),
                            ep.getConceptNid(),
                            Long.MAX_VALUE);
                    analog.setInitialCaseSignificant(true);
                }
            }
            I_GetConceptData concept = Terms.get().getConceptForNid(analog.getNid());
            Terms.get().addUncommitted(concept);
        } catch (TerminologyException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }


    }
}
