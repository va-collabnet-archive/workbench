package org.dwfa.ace.api.cs;

import java.io.IOException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;

/**
 * Validates a component - it must match exactly with a component in the database.
 * @author Christine Hill
 *
 */
public class ComponentValidator extends SimpleValidator {

	private final RelationshipValidator relationshipValidator = new RelationshipValidator();
	private final DescriptionValidator descriptionValidator = new DescriptionValidator();
	private final ConceptAttributeValidator conceptAttributeValidator = new ConceptAttributeValidator();
	private String failureReport;
	private boolean timeLenient;
	private boolean strictMode;

	@Override
	protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf)
			throws IOException, TerminologyException {

		failureReport = "";
		
		conceptAttributeValidator.setTimeLenient(timeLenient);
		boolean validConceptAttributes = conceptAttributeValidator.validateAceBean(bean, tf);
		if (!validConceptAttributes) {
			String message = "Invalid concept attributes: " + conceptAttributeValidator.getFailureReport();
			failureReport += message;
			if (strictMode || bean.getUncommittedConceptAttributes() != null) {
				AceLog.getEditLog().severe(message);
				return false;
			}
			AceLog.getEditLog().warning(message);
		}
		
		descriptionValidator.setTimeLenient(timeLenient);
		boolean validDescription = descriptionValidator.validateAceBean(bean, tf);
		if (!validDescription) {
			String message = "Invalid description: " + descriptionValidator.getFailureReport();
			failureReport += message;
			if (strictMode || bean.getUncommittedDescriptions().size() > 0) {
				AceLog.getEditLog().severe(message);
				return false;
			}
			AceLog.getEditLog().warning(message);
		}

		relationshipValidator.setTimeLenient(timeLenient);
		boolean validRelationship = relationshipValidator.validateAceBean(bean,	tf);
		if (!validRelationship) {
			String message = "Invalid relationship: " + relationshipValidator.getFailureReport();
			failureReport += message;
			if (strictMode || bean.getUncommittedSourceRels().size() > 0) {
				AceLog.getEditLog().severe(message);
				return false;
			}
			AceLog.getEditLog().warning(message);
		}

		return true;

	}

	@Override
	public String getFailureReport() {
		return failureReport;
	}

	public boolean isTimeLenient() {
		return timeLenient;
	}

	public void setTimeLenient(boolean timeLenient) {
		this.timeLenient = timeLenient;
	}

	public boolean isStrictMode() {
		return strictMode;
	}

	public void setStrictMode(boolean strictMode) {
		this.strictMode = strictMode;
	}

}
