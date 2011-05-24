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
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefset;
import org.ihtsdo.workflow.refset.WorkflowRefsetFields;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowRefsetReader extends WorkflowRefset {
	protected WorkflowRefsetFields fields = null;
	private Set <String> contents = null;
	private int currentNid = 0;
	private WorkflowRefset refset = null;

	
	protected WorkflowRefsetReader() throws TerminologyException, IOException {
		super();
	}

	protected WorkflowRefsetReader(WorkflowRefset rs) throws TerminologyException, IOException {
		refsetId = refset.getRefsetId();
		refsetName = refset.getRefsetName();
		refset = rs;
	}

	protected WorkflowRefsetReader(int nid, String name) throws TerminologyException, IOException {
		super(nid, name, true);
	}

	public Collection<UUID> getRefsetUids() throws TerminologyException, IOException
	{
		return refset.getRefsetUids();
	}
}
