package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.DescFact;

public class SetICSignificantAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	DescriptionVersionBI desc;
	I_ConfigAceFrame config;

	public SetICSignificantAction(String actionName, DescFact fact, I_ConfigAceFrame config) {
		super(actionName);
		this.desc = fact.getComponent();
		this.config = config;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
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
			boolean initialSignificance = desc.isInitialCaseSignificant();
			boolean changePerformed = false;
			for (DescriptionVersionBI descVersion : descConcept.getDescriptionsActive()) {
				String otherText = descVersion.getText();
				//if same word then add to description list
				String otherInitialWord = null;
				if (otherText.indexOf(" ") != -1) {
					otherInitialWord = otherText.substring(0, otherText.indexOf(" "));
				} else {
					otherInitialWord = otherText;
				}
				if (initialWord.equals(otherInitialWord) &&
						initialSignificance == descVersion.isInitialCaseSignificant()) {
					DescriptionAnalogBI analog = null;
					if (desc.isInitialCaseSignificant()) {
						if (descVersion.isUncommitted()) {
							DescriptionAnalogBI da = (DescriptionAnalogBI) descVersion;
							da.setInitialCaseSignificant(false);
							changePerformed = true;
						} else {
							for (PathBI ep : config.getEditingPathSet()) {
								analog = (DescriptionAnalogBI) descVersion.makeAnalog(
										SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
										Long.MAX_VALUE,
                                                                                config.getEditCoordinate().getAuthorNid(),
                                                                                config.getEditCoordinate().getModuleNid(), 
                                                                                ep.getConceptNid());
								analog.setInitialCaseSignificant(false);
							}
							changePerformed = true;
						}
					} else {
						if (descVersion.isUncommitted()) {
							DescriptionAnalogBI da = (DescriptionAnalogBI) descVersion;
							da.setInitialCaseSignificant(true);
							changePerformed = true;
						} else {
							for (PathBI ep : config.getEditingPathSet()) {
								analog = (DescriptionAnalogBI) descVersion.makeAnalog(
										SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
										Long.MAX_VALUE,
                                                                                config.getEditCoordinate().getAuthorNid(),
                                                                                config.getEditCoordinate().getModuleNid(), 
                                                                                ep.getConceptNid());
								analog.setInitialCaseSignificant(true);
							}
							changePerformed = true;
						}
					}
				}
			}
			// End of loop
			if (changePerformed) {
				I_GetConceptData concept = Terms.get().getConceptForNid(descConcept.getNid());
				Terms.get().addUncommitted(concept);
			}
		} catch (IOException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		} catch (PropertyVetoException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		} catch(ContradictionException ex){
			AceLog.getAppLog().alertAndLogException(ex);
		}


	}
}
