package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.drools.facts.ComponentFact;
import org.ihtsdo.tk.drools.facts.RelFact;
import org.ihtsdo.tk.drools.facts.ConceptFact;

import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import java.beans.PropertyVetoException;
import org.dwfa.ace.api.I_RelPart;


import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.drools.facts.ComponentFact;


public class UngroupRelAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		RelationshipVersionBI component;
		public UngroupRelAction(String actionName, RelFact fact) {
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
					for (PathBI ep: config.getEditingPathSet()) {
								I_AmPart part = (I_AmPart) componentVersion.makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), 
								config.getDbConfig().getUserConcept().getNid(),
								ep.getConceptNid(), 
								Long.MAX_VALUE);
					
					
					I_RelPart rel = (I_RelPart) part;
					rel.setGroup(0);
					
					I_GetConceptData concept = Terms.get().getConceptForNid(rel.getNid());
					
					Terms.get().addUncommitted(concept);
					}
				}
				
			} catch (TerminologyException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			} catch (IOException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}
			}

	}
