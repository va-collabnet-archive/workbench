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

/**
 * A value converter is a class that mines a refset, it's parent concept, or it's description concept, for values
 * used to populate an external terminology system.
 * 
 *  @author Steven Neiner
 */

import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.mojo.epicexport.I_RefsetUsageInterpreter.I_RefsetApplication;

public interface I_ExportValueConverter {

	/**
	 * Method to read a concept, its descriptions, or extensions for data used to populate a terminology
	 * record.  This method is called in ExternalTermPublisher when it encounters a refset.  The 
	 * reslting value are returned in getItemvalue(), getPreviousItemvalue(), getRegion().
	 * 
	 * @param refsetUsage - the masterfile / item combination that we are populating values
	 * @param conceptForDescription - The concept that the description lives under
	 * @param description - The description where the term resides
	 * @param extensionTuple - The extension (or refset) 
	 * @param previousPart - The extension as it was at the starting version
	 * @throws Exception
	 */
	public void populateValues(I_RefsetApplication refsetUsage, I_GetConceptData conceptForDescription, 
			I_DescriptionVersioned description, I_ExtendByRefVersion extensionTuple, 
			I_ExtendByRefPart previousPart) throws Exception;
	
    /**
     * The starting version number
     * 
     * @return int 
     */
	public int getStartingVersion();

	/**
	 * Sets the starting version, for reading previous values and comparing for changes.
	 * 
	 * @param startingVersion
	 */
	public void setStartingVersion(int startingVersion);

	/**
	 * The resulting item value, after calling populateValues.
	 * 
	 * @return String
	 */
	public String getItemValue();

	/**
	 * The resulting value as it was as of setStartingVersion()
	 * 
	 * @return String
	 */
	public String getPreviousItemValue();

	/**
	 * A region or locality the refest applies to, if applicable
	 * 
	 * @return String
	 */
	public String getRegion();
	
	/**
	 * This method is called after ExternalTermRecord has finished reading the extensions and has 
	 * constructed a term record. 
	 * 
	 * @param extensionTuple - The extension used to define the display name of the term record
	 * @param rootConcept - The concept where the descriptions and extensions reside under
	 * @param record - the record so far, after populating from the extensions
	 * @throws Exception
	 */
	public void addRecordIds(I_ExtendByRefVersion extensionTuple, I_GetConceptData rootConcept,
			ExternalTermRecord record) throws Exception;
	
	/**
	 * Used to indicate the record is an independent term record, usually should mean the record has a
	 * display name.
	 * 
	 * @param record
	 * @return boolean, true if the record is an independent term record.
	 */
	public boolean recordIsStandAloneTerm(ExternalTermRecord record);
	
	/**
	 * Obtains the ID for a concept, given the UUID of the ID type
	 * 
	 * @param concept - the concept to get the ID for.
	 * @param idTypeUUID - The UUID (String) of the ID type we want
	 * @return String ID of the concept
	 * @throws Exception
	 */
	public String getIdForConcept(I_GetConceptData concept, String idTypeUUID) throws Exception;
	
	/**
	 * Given the status id, which is the numeric concept id for the status concept, returns an 
	 * enumerated status.
	 * 
	 * @param statusId
	 * @return The enumerated status
	 * @throws Exception
	 */
	public ExternalTermRecord.status getInterpretedStatus(int statusId) throws Exception;
}
