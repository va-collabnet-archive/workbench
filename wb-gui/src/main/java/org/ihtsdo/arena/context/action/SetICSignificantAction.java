package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
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
            ConceptVersionBI descConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), desc.getConceptNid());
            //get inital word of selected description
            String descText = desc.getText();
            String initialWord = null;
            if (descText.indexOf(" ") != -1) {
                initialWord = descText.substring(0, descText.indexOf(" "));
            } else {
                initialWord = descText;
            }
            //compare to initial word of returned descriptions
            for (DescriptionVersionBI descVersion : descConcept.getDescsActive()) {
                String otherText = descVersion.getText();
                //if same word then add to description list
                String otherInitialWord = null;
                if (otherText.indexOf(" ") != -1) {
                    otherInitialWord = otherText.substring(0, otherText.indexOf(" "));
                } else {
                    otherInitialWord = otherText;
                }
                if (initialWord.equals(otherInitialWord)) {
                    DescriptionAnalogBI analog = null;
                    if (desc.isInitialCaseSignificant()) {
                        if (descVersion.isUncommitted()) {
                            DescriptionAnalogBI da = (DescriptionAnalogBI) descVersion;
                            da.setInitialCaseSignificant(false);
                            I_GetConceptData concept = Terms.get().getConceptForNid(descVersion.getNid());
                            Terms.get().addUncommitted(concept);
                        } else {
                            for (PathBI ep : config.getEditingPathSet()) {
                                analog = (DescriptionAnalogBI) descVersion.makeAnalog(
                                        SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
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
                            DescriptionAnalogBI da = (DescriptionAnalogBI) descVersion;
                            da.setInitialCaseSignificant(true);
                            I_GetConceptData concept = Terms.get().getConceptForNid(descVersion.getNid());
                            Terms.get().addUncommitted(concept);
                        } else {
                            for (PathBI ep : config.getEditingPathSet()) {
                                analog = (DescriptionAnalogBI) descVersion.makeAnalog(
                                        SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
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
        } catch(ContraditionException ex){
            AceLog.getAppLog().alertAndLogException(ex);
        }


    }
}
