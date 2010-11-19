package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.drools.facts.ComponentFact;
import org.ihtsdo.tk.drools.facts.RelFact;

public class CloneRelAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	ComponentVersionBI component;
	public CloneRelAction(String actionName, RelFact fact) {
		super(actionName);
		this.component = fact.getComponent();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			I_GetConceptData concept = Terms.get().getConceptForNid(component.getNid());
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
			if (ConAttrVersionBI.class.isAssignableFrom(component.getClass())) {
				throw new UnsupportedOperationException();
			}
			if (ConceptVersionBI.class.isAssignableFrom(component.getClass())) {
				throw new UnsupportedOperationException();
			}
			if (DescriptionVersionBI.class.isAssignableFrom(component.getClass())) {
				DescriptionVersionBI desc = (DescriptionVersionBI) component;
				Terms.get().newDescription(UUID.randomUUID(), concept, 
						desc.getLang(), desc.getText(), Terms.get().getConcept(desc.getTypeNid()), 
						config, Terms.get().getConcept(desc.getStatusNid()), Long.MAX_VALUE);
			}
			if (RelationshipVersionBI.class.isAssignableFrom(component.getClass())) {
				RelationshipVersionBI rel = (RelationshipVersionBI) component;
				I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), concept, 
						rel.getTypeNid(), 
						rel.getDestinationNid(), 
						rel.getCharacteristicNid(), 
						rel.getRefinabilityNid(), 
						rel.getGroup(), 
						rel.getStatusNid(), 
						config.getDbConfig().getUserConcept().getNid(), 
						pathItr.next().getConceptNid(), 
						Long.MAX_VALUE);
				
				while (pathItr.hasNext()) {
					newRel.makeAnalog(newRel.getStatusNid(), newRel.getAuthorNid(), 
							pathItr.next().getConceptNid(), Long.MAX_VALUE);
				}
			}
			Terms.get().addUncommitted(concept);
			
			
		} catch (TerminologyException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}

	}

}
