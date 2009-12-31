/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.mojo.epicexport;

import java.util.List;

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
	
	public void writeRecordIds(I_ThinExtByRefTuple extensionTuple, List<String> masterFilesImpacted,
    		EpicExportManager exportManager) throws Exception;

	public void addRecordIds(I_ThinExtByRefTuple extensionTuple, I_GetConceptData rootConcept,
			ExternalTermRecord record) throws Exception;
	
	public boolean recordIsStandAloneTerm(ExternalTermRecord record);
	
	public String getIdForConcept(I_GetConceptData concept, String idTypeUUID) throws Exception;
}
