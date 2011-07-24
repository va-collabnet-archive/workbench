package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.RelGroupFact;








public class UngroupRelGroupAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		ComponentVersionBI sourceComponent;
		ComponentVersionBI targetComponent; //don't need
		
		public UngroupRelGroupAction(String actionName, RelGroupFact sourceFact, ConceptFact destFact) {
			super(actionName);
			this.sourceComponent = sourceFact.getComponent();
			this.targetComponent = destFact.getComponent();
		}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {	
			//get rels from sourceComponent 
			RelGroupVersionBI source = (RelGroupVersionBI) sourceComponent; 
			Collection<?> sourceRels = source.getCurrentRels(); 
			//loop through rels
			 for (Object relObject: sourceRels) {
				 if (I_AmPart.class.isAssignableFrom(relObject.getClass())) {
						I_AmPart componentVersion = (I_AmPart) relObject;
						I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
						for (PathBI ep: config.getEditingPathSet()) {
									I_AmPart part = (I_AmPart) componentVersion.makeAnalog(
									SnomedMetadataRfx.getSTATUS_CURRENT_NID(), 
									config.getDbConfig().getUserConcept().getNid(),
									ep.getConceptNid(), 
									Long.MAX_VALUE);
						
						
						I_RelPart rel = (I_RelPart) part;
						rel.setGroup(0);
						
						I_GetConceptData concept = Terms.get().getConceptForNid(componentVersion.getNid());
						
						Terms.get().addUncommitted(concept);
						}
					}
				 }
		} catch (TerminologyException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (ContraditionException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
	}
}
