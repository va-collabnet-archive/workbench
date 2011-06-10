package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;

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
        try {
            config = Terms.get().getActiveAceFrameConfig();
            //get concept
            I_GetConceptData descConcept = Terms.get().getConceptForNid(desc.getNid());
            //get descriptions for concept
            Collection<? extends I_DescriptionVersioned> descriptions = descConcept.getDescriptions();
            //get inital word of selected description
            String descText = desc.getText();
            String initialWord = null;
            if (descText.indexOf(" ") != -1) {
                initialWord = descText.substring(0, descText.indexOf(" "));
            } else {
                initialWord = descText;
            }
            //compare to initial word of returned descriptions
            for (I_DescriptionVersioned descVersion : descriptions) {
                String otherText = descVersion.getText();
                //if same word then add to description list
                String otherInitialWord = null;
                if (otherText.indexOf(" ") != -1) {
                    otherInitialWord = otherText.substring(0, otherText.indexOf(" "));
                } else {
                    otherInitialWord = otherText;
                }
                if (initialWord.equals(otherInitialWord)
                        && descVersion.getStatusNid() == ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()) {
                    DescriptionAnalogBI analog = null;
                    if (desc.isInitialCaseSignificant()) {
                        if (descVersion.isUncommitted()) {
                            descVersion.setInitialCaseSignificant(false);
                            I_GetConceptData concept = Terms.get().getConceptForNid(descVersion.getNid());
                            Terms.get().addUncommitted(concept);
                        } else {
                            for (PathBI ep : config.getEditingPathSet()) {
                                analog = (DescriptionAnalogBI) descVersion.makeAnalog(
                                        ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
                                        config.getDbConfig().getUserConcept().getNid(),
                                        ep.getConceptNid(),
                                        Long.MAX_VALUE);
                                analog.setInitialCaseSignificant(false);
                            }
                            I_GetConceptData concept = Terms.get().getConceptForNid(analog.getNid());
                            Terms.get().addUncommitted(concept);
                        }
                    } else {
                        if (descVersion.isUncommitted()) {
                            descVersion.setInitialCaseSignificant(true);
                            I_GetConceptData concept = Terms.get().getConceptForNid(descVersion.getNid());
                            Terms.get().addUncommitted(concept);
                        } else {
                            for (PathBI ep : config.getEditingPathSet()) {
                                analog = (DescriptionAnalogBI) descVersion.makeAnalog(
                                        ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
                                        config.getDbConfig().getUserConcept().getNid(),
                                        ep.getConceptNid(),
                                        Long.MAX_VALUE);
                                analog.setInitialCaseSignificant(true);
                            }
                            I_GetConceptData concept = Terms.get().getConceptForNid(analog.getNid());
                            Terms.get().addUncommitted(concept);
                        }
                    }
                }
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
