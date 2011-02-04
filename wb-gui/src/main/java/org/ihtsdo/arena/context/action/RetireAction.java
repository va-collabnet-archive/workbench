package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.drools.facts.ComponentFact;

public class RetireAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	ComponentVersionBI component;
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
				for (PathBI ep: config.getEditingPathSet()) {
					componentVersion.makeAnalog(
							ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
							config.getDbConfig().getUserConcept().getNid(),
							ep.getConceptNid(),
							Long.MAX_VALUE);
				}
				I_GetConceptData concept = Terms.get().getConceptForNid(componentVersion.getNid());
				Terms.get().addUncommitted(concept);
			}
		} catch (TerminologyException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}

	}

}
