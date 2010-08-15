package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;

import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.workflow.refset.RefsetFields;
import org.ihtsdo.workflow.refset.RefsetUtility;



/* 
* @author Jesse Efron
* 
*/
public abstract class RefsetWriterUtility extends RefsetUtility {
	protected RefsetFields fields = null;
	public abstract String fieldsToRefsetString() throws IOException;

	protected RefsetWriterUtility() throws TerminologyException, IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public boolean addMember() {
		String refCompStr = null;
		boolean retVal = false;
		
		try {
			RefsetPropertyMap propMap = new RefsetPropertyMap();
			
			if (fields.valuesExist())
			{
		        refCompStr = fields.getReferencedComponentId().getInitialText();
				propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());
				helper.newRefsetExtension(refsetId, fields.getReferencedComponentId().getConceptNid(), REFSET_TYPES.STR, propMap, Terms.get().getActiveAceFrameConfig());
				retVal = true;
			}
		} catch (Exception io) {
			System.out.println("Failed adding member: " + refCompStr + " (" + fields.getReferencedComponentId().getConceptNid() + ")");
			System.out.println("to refset: " + refsetName);
			System.out.println("Attempting to add these properties: " + fields.toString());
			System.out.println(io.getMessage());
		}

		fields.cleanValues();
		return retVal;
	}

	public boolean retireMember(int refsetId, int memberId)  {
		String refCompStr = null;
		
		try {
			RefsetPropertyMap propMap = new RefsetPropertyMap();

			refCompStr = fields.getReferencedComponentId().getInitialText();
			propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());
			helper.retireRefsetExtension(refsetId, fields.getReferencedComponentId().getConceptNid(), propMap);
			return true;
		} catch (Exception io) {
			System.out.println("Failed removing member: " + refCompStr + " (" + fields.getReferencedComponentId().getConceptNid() + ")");
			System.out.println("to refset: " + refsetName);
			System.out.println("Attempting to add these properties: " + fields.toString());
			System.out.println(io.getMessage());
			return false;
		}
	}
}
