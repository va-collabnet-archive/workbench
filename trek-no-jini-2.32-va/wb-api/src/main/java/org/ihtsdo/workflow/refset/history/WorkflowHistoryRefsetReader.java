package org.ihtsdo.workflow.refset.history;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetReader;



/*
* @author Jesse Efron
*
*/
public  class WorkflowHistoryRefsetReader extends WorkflowRefsetReader
{
	private static final String chiefTermSearchTerm = "Chief Terminologist";
	public static final String chiefTermReplaceTerm = "C. T.";

	public WorkflowHistoryRefsetReader() throws TerminologyException, IOException
	{
		super(workflowHistoryConcept);
	}

	// Workflow ID Only a UUID (No Concept)
	public UUID getWorkflowId(String props) {
		return getUUID("workflowId", props);
	}

	// Workflow ID Only a UUID (No Concept)
	public String getWorkflowIdAsString(String props) {
		return getProp("workflowId", props);
	}

	// I_GetConceptData values where appropriate
	public I_GetConceptData getState(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("state", props);
	}
	public I_GetConceptData getAction(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("action", props);
	}
	public I_GetConceptData getPath(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("path", props);
	}
	public I_GetConceptData getModeler(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("modeler", props);
	}
	public String getFSN(String props) throws NumberFormatException, TerminologyException, IOException {
		return getProp("fsn", props);
	}
	public Long getWorkflowTime(String props) throws NumberFormatException, TerminologyException, IOException {
		return Long.parseLong(getProp("workflowTime", props));
	}
	public Long getEffectiveTime(String props) throws NumberFormatException, TerminologyException, IOException {
		return Long.parseLong(getProp("effectiveTime", props));
	}
	public UUID getStateUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("state", props);
	}
	public UUID getActionUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("action", props);
	}
	public UUID getPathUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("path", props);
	}
	public UUID getModelerUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("modeler", props);
	}
	public boolean getAutoApproved(String props) throws NumberFormatException, TerminologyException, IOException {
		
		try {

			String key = "autoApproved";
			String prop = getProp(key, props);
		
			if (prop.equals("true"))
				return true;
			else 
				return false;

		} catch (IndexOutOfBoundsException ioob) {
			return false;
		}
	}
	
	public boolean getOverridden(String props) throws NumberFormatException, TerminologyException, IOException {
		
		try {

			String key = "overridden";
			String prop = getProp(key, props);
		
			if (prop.equals("true"))
				return true;
			else 
				return false;

		} catch (IndexOutOfBoundsException ioob) {
			return false;
		}
	}
	
	public String processMetaForDisplay(I_GetConceptData metaCon, ViewCoordinate vc) throws IOException {
		try {
			String term = WorkflowHelper.identifyPrefTerm(metaCon.getConceptNid(), vc);
			
			if (term.contains(" Workflow ")) {
				term = WorkflowHelper.shrinkTermForDisplay(term);
			}

			if (term.contains(chiefTermSearchTerm)) {
				StringBuffer retBuf = new StringBuffer();
				
				int searchTermBeginIdx = term.indexOf(chiefTermSearchTerm); 
				int searchTermEndIdx = term.indexOf(chiefTermSearchTerm) + chiefTermSearchTerm.length(); 
				
				retBuf.append(term.substring(0, searchTermBeginIdx)); 
				retBuf.append(chiefTermReplaceTerm); 
				retBuf.append(term.substring(searchTermEndIdx));
				
				term = retBuf.toString();
			}
			return term;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

/*	return "\nConcept (Referenced Component Id) = " + tf.getConcept(concept).getInitialText() +
	   "\nWorkflow Id = " + workflowId.toString() +
	   "\nPath = " + tf.getConcept(path).getInitialText() +
	   "\nModeler = " + tf.getConcept(modeler).getInitialText() + 
	   "\nAction = " + tf.getConcept(action).getInitialText() +
	   "\nState = " + tf.getConcept(state).getInitialText() +
	   "\nFSN = " + fsn +
	   "\nEffectiveTimestamp = " + formatter.format(new Date(effectiveTime)) +
	   "\nWorkflow Time = " + formatter.format(new Date(workflowTime)) +
	   "\nAutoApproved = " + autoApproved + 
	   "\nOverridden = " + overridden +
	   "\nRxMemberId = " + memberId;

*/
	
	@Override
	public boolean isIdenticalAutomatedAdjudication(TkRefexAbstractMember origMember, TkRefexAbstractMember testMember) {
		
		if (isIdenticalSap(origMember, testMember)) {
			return false;
		} else {
			String orig = ((TkRefsetStrMember)origMember).getString1();
			String test = ((TkRefsetStrMember)testMember).getString1();
			
			// For this Refset, IGNORE a) EffectiveTimestamp b) Modeler c) WrklfowTime d) RxMemberId
			try {
				if (origMember.getComponentUuid().equals(testMember.getComponentUuid()) &&
					this.getWorkflowId(orig).equals(this.getWorkflowId(test)) && 
					this.getPath(orig).equals(this.getPath(test)) &&
					this.getAction(orig).equals(this.getAction(test)) &&
					this.getState(orig).equals(this.getState(test)) &&
					this.getFSN(orig).equals(this.getFSN(test)) &&
					this.getAutoApproved(orig) == this.getAutoApproved(test) &&
					this.getOverridden(orig) == this.getOverridden(test)) {
					return true;
				}
			} catch (Exception e) {
				
			}
		}
		
		return false;
	}
}
/*
 * Example of multiple Adjudication before sync
 * 
 <properties>
<property><key>workflowId</key><value>8c4f25d2-b775-4b77-8322-13f6f20e462b</value></property>
<property><key>workflowId</key><value>8c4f25d2-b775-4b77-8322-13f6f20e462b</value></property>
<property><key>workflowId</key><value>8c4f25d2-b775-4b77-8322-13f6f20e462b</value></property>

<property><key>path</key><value>7dfa494a-abde-5bc0-b1e3-2563519130a2</value></property>
<property><key>path</key><value>7dfa494a-abde-5bc0-b1e3-2563519130a2</value></property>
<property><key>path</key><value>7dfa494a-abde-5bc0-b1e3-2563519130a2</value></property><


// *** DIFF
<property><key>modeler</key><value>9228e01a-6d92-3063-a75a-6db5651074cc</value></property>
<property><key>modeler</key><value>09422a35-01ed-3249-ba7a-0b7fe63472e3</value></property>
<property><key>modeler</key><value>85466416-7126-3cea-8bba-d74bf9c2b4e5</value></property>

<property><key>state</key><value>266cbccc-d2f2-3732-946b-07b150b50603</value></property>
<property><key>state</key><value>266cbccc-d2f2-3732-946b-07b150b50603</value></property>
<property><key>state</key><value>266cbccc-d2f2-3732-946b-07b150b50603</value></property>

<property><key>action</key><value>074566ac-cc6a-334f-a4fb-0e2b35cddcab</value></property>
<property><key>action</key><value>074566ac-cc6a-334f-a4fb-0e2b35cddcab</value></property>
<property><key>action</key><value>074566ac-cc6a-334f-a4fb-0e2b35cddcab</value></property>

// *** DIFF
<property><key>workflowTime</key><value>1320504122462</value></property>
<property><key>workflowTime</key><value>1320503376006</value></property><
<property><key>workflowTime</key><value>1320503374577</value></property>

<property><key>fsn</key><value>Primary malignant neoplasm of face (disorder)</value></property>
<property><key>fsn</key><value>Primary malignant neoplasm of face (disorder)</value></property>
<property><key>fsn</key><value>Primary malignant neoplasm of face (disorder)</value></property><

<property><key>autoApproved</key><value>false</value></property>
<property><key>autoApproved</key><value>false</value></property>
<property><key>autoApproved</key><value>false</value></property>

<property><key>overridden</key><value>false</value></property></properties>
<property><key>overridden</key><value>false</value></property></properties>
<property><key>overridden</key><value>false</value></property></properties>

// *** EFFECTIVE TIME & RxMemberId can be ignored as well
 * 
 */

