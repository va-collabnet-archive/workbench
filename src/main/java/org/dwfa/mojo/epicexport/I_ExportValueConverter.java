package org.dwfa.mojo.epicexport;

import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.mojo.epicexport.I_RefsetUsageInterpreter.I_RefsetApplication;

public interface I_ExportValueConverter {

	public void populateValues(I_RefsetApplication refsetUsage, I_GetConceptData conceptForDescription, 
			I_DescriptionVersioned description, I_ThinExtByRefTuple extensionTuple, 
			I_ThinExtByRefPart previousPart) throws Exception;
	
    public int getStartingVersion();

	public void setStartingVersion(int startingVersion);

	public String getItemValue();

	public String getPreviousItemValue();

	public String getRegion();
}
