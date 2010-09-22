package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.DescSpecFact;
import org.ihtsdo.tk.drools.facts.RelSpecFact;
import org.ihtsdo.tk.drools.facts.SpecFact;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;

public class UpdateDescFromSpecAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	ConceptVersionBI concept;
	SpecFact<?> spec;

	public UpdateDescFromSpecAction(String actionName, 
			ConceptFact concept, SpecFact<?> spec) throws IOException {
		super(actionName);
		this.concept = concept.getConcept();
		this.spec = spec;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (DescSpecFact.class.isAssignableFrom(spec.getClass())) {
				updateDesc();
			} else {
				throw new Exception("Can't handle type: " + spec);
			}
		} catch (Exception ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
	}

	private void updateDesc() throws TerminologyException, IOException {
		DescriptionSpec descSpec = ((DescSpecFact) spec).getDescSpec();
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_DescriptionVersioned description = Terms.get().getDescription(Terms.get().uuidToNative(descSpec.getUuids()));
		I_DescriptionPart descPart = description.getTuples(config.getConflictResolutionStrategy()).iterator().next().getMutablePart();
		I_DescriptionPart newPart = (I_DescriptionPart) descPart.makeAnalog(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), 
				config.getEditingPathSet().iterator().next().getConceptNid(), Long.MAX_VALUE);
		newPart.setText(descSpec.getDescText());
		description.addVersion(newPart);
		Terms.get().addUncommitted(Terms.get().getConcept(concept.getNid()));
	}

}