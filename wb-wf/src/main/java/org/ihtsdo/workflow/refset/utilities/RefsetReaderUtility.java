package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.RefsetFields;
import org.ihtsdo.workflow.refset.RefsetUtility;



/* 
* @author Jesse Efron
* 
*/
public class RefsetReaderUtility extends RefsetUtility {
	protected RefsetFields fields = null;
	private Set <String> contents = null;
	
	protected RefsetReaderUtility() throws TerminologyException, IOException {
		super();
	}

	public String printContents(int refsetId) {
		if (contents == null)
			getContents(refsetId);
		
		String retVal = new String();
		
		for (String s : contents)
		{
			retVal = retVal + s + "\n";
		}
		
		return retVal;
	}
	
	public  Set<String> getContents(int refsetId) 
	{
		contents = new HashSet<String>();

		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();

	        Collection<? extends I_ExtendByRef> extVersions = Terms.get().getRefsetExtensionMembers(refsetId);
	        
	        for (I_ExtendByRef thinExtByRefVersioned : extVersions) {
	
	            List<? extends I_ExtendByRefVersion> extensions = thinExtByRefVersioned.getTuples(config.getAllowedStatus(),
	                config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
	
	            for (I_ExtendByRefVersion thinExtByRefTuple : extensions) {
	                if (thinExtByRefTuple.getRefsetId() == refsetId) {
	
	                	int refCompId = thinExtByRefTuple.getComponentId();
	                	I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) thinExtByRefTuple.getMutablePart();
	
	                    contents.add(refCompId + part.getStringValue());
	                }
	            }
	        }
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return contents;
	}
}
