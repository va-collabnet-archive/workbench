package org.dwfa.ace.api.cs;

import java.io.IOException;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.tapi.TerminologyException;

/**
 * Validator for concept attributes.
 * 
 * @author Dion McMurtrie
 */
public class ConceptAttributeValidator extends SimpleValidator {

	private boolean timeLenient = false;
	private StringBuffer failureReport;
	
	@Override
	protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf)
			throws IOException, TerminologyException {
		
		termFactory = tf;
		failureReport = new StringBuffer();
		
		UniversalAceConceptAttributes conceptAttributes = bean.getConceptAttributes();
		if (conceptAttributes == null && bean.getUncommittedConceptAttributes() != null) {
			if (bean.getUncommittedConceptAttributes() != null) {
				return true;
			} 
			AceLog.getEditLog().warning("UniversalAceBean has no conceptAttributes and no uncommitted concept attributes:\n" + bean);
			return false;
		}
		
		int startParts = 0;
		I_ConceptAttributeVersioned thinConAttr = tf.getConcept(bean.getConceptAttributes().getConId()).getConceptAttributes();
		for (UniversalAceConceptAttributesPart part : conceptAttributes.getVersions()) {

			if (part.getTime() != Long.MAX_VALUE) {
				startParts++;
				boolean match = false;
				for (I_ConceptAttributePart conceptAttributePart : thinConAttr.getVersions()) {
					if (conceptAttributePart.getConceptStatus() == getNativeId(part.getConceptStatus())
							&& conceptAttributePart.getPathId() == getNativeId(part.getPathId())
							&& (timeLenient || conceptAttributePart.getVersion() == tf.convertToThinVersion(part.getTime()))) {
						match = true;
						break;
					}
				}
					
				if (!match) {
					failureReport.append("\nConcept does not contain a concept attribute part match.");
					I_ConceptAttributePart newConceptAttributePart = tf.newConceptAttributePart();
					newConceptAttributePart.setConceptStatus(getNativeId(part.getConceptStatus()));
					newConceptAttributePart.setDefined(part.isDefined());
					newConceptAttributePart.setPathId(getNativeId(part.getPathId()));
					newConceptAttributePart.setVersion(tf.convertToThinVersion(part.getTime()));
					failureReport.append("\n   newPart is " + part );
					failureReport.append(    "\n   new native part: " + newConceptAttributePart );
					
					for (I_ConceptAttributePart conceptAttributePart: thinConAttr.getVersions()) {
						failureReport.append("\n     existing part: " + conceptAttributePart);
					}
					
					return false; // test 2
				}	
			}
			
		}
		if (startParts != thinConAttr.getVersions().size()) {
			failureReport.append("number of concept attribute parts is different for " + bean + " and " + thinConAttr.getVersions());
			return false; // test 3
		}

		// passed all tests for all attributes
		return true;
	}

	@Override
	public String getFailureReport() {
		return failureReport.toString();
	}
	
	public boolean isTimeLenient() {
		return timeLenient;
	}

	public void setTimeLenient(boolean timeLenient) {
		this.timeLenient = timeLenient;
	}

}
