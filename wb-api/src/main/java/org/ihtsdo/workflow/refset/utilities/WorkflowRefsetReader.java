package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefset;
import org.ihtsdo.workflow.refset.WorkflowRefsetFields;



/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefsetReader extends WorkflowRefset {
	public WorkflowRefsetReader(I_ConceptualizeUniversally refsetConcept) throws IOException, TerminologyException {
		super(refsetConcept);
	}
}
