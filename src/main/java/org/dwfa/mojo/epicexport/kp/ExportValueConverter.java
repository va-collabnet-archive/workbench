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
package org.dwfa.mojo.epicexport.kp;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.log.AceLog;
import org.dwfa.mojo.epicexport.ExportToEpicLoadFilesMojo;
import org.dwfa.mojo.epicexport.ExternalTermRecord;
import org.dwfa.mojo.epicexport.I_ExportValueConverter;
import org.dwfa.mojo.epicexport.I_RefsetUsageInterpreter.I_RefsetApplication;

/**
 * A value converter is a class that mines a refset, it's parent concept, or it's description concept, for values
 * used to populate an external terminology system.
 * 
 *  @author Steven Neiner
 */

public class ExportValueConverter implements I_ExportValueConverter{
	private int startingVersion;
	private String itemValue;
	private String previousItemValue;
	private String region;
	
	public ExportValueConverter(int startingVersion) {
		this.startingVersion = startingVersion;
		
	}
	
	public void populateValues(I_RefsetApplication refsetUsage, I_GetConceptData conceptForDescription, 
			I_DescriptionVersioned description, I_ThinExtByRefTuple extensionTuple, 
			I_ThinExtByRefPart previousPart) throws Exception {
		
		I_ThinExtByRefPart extensionTuplePart = extensionTuple.getMutablePart();
		this.itemValue = null;
		this.previousItemValue = null;
		this.region = null;
		
		if (refsetUsage.getMasterfile().equals(EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_BILLING)) {
			if (refsetUsage.getItemNumber().equals("2")) {
	    		itemValue = getDisplayName(conceptForDescription); 
	    		previousItemValue = getPreviousDisplayName(conceptForDescription); 
			}
			else {
				itemValue = getValueAsString(extensionTuplePart);
				previousItemValue = getValueAsString(previousPart);
			}
		}
		else if (refsetUsage.getMasterfile().equals(EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_CLINICAL)) {
			if (refsetUsage.getItemNumber().equals("2")) {
    			itemValue = description.getLastTuple().getMutablePart().getText();
    			previousItemValue = getPreviousDisplayName(description);
    			region = refsetUsage.getRegion();
			}
			else if (refsetUsage.getItemNumber().equals("50")){
			
	    		I_ThinExtByRefPartBoolean doAdd = (I_ThinExtByRefPartBoolean) extensionTuplePart;
	    		if (doAdd.getBooleanValue() && description != null) {
	    			itemValue = description.getLastTuple().getMutablePart().getText();
	    			previousItemValue = getPreviousDisplayName(description);
	    		}
			}
			else {
				itemValue = getValueAsString(extensionTuplePart);
				previousItemValue = getValueAsString(previousPart);
			}
		}
		else if (refsetUsage.getMasterfile().equals(ExportToEpicLoadFilesMojo.EPIC_MASTERFILE_NAME_WILDCARD)) {
			itemValue = getValueAsString(extensionTuplePart);
			previousItemValue = getValueAsString(previousPart);
		}

	}
	
    public int getStartingVersion() {
		return startingVersion;
	}

	public void setStartingVersion(int startingVersion) {
		this.startingVersion = startingVersion;
	}

	public String getItemValue() {
		return itemValue;
	}

	public String getPreviousItemValue() {
		return previousItemValue;
	}

	public String getRegion() {
		return region;
	}

	public String getValueAsString(I_ThinExtByRefPart thinExtByRefPart) {
    	String value = null;
    	if (thinExtByRefPart != null) {
	    	if (I_ThinExtByRefPartString.class.isAssignableFrom(thinExtByRefPart.getClass())) {
	    		I_ThinExtByRefPartString str = (I_ThinExtByRefPartString) thinExtByRefPart;
	    		value = str.getStringValue();
	    	}
	    	else if (I_ThinExtByRefPartInteger.class.isAssignableFrom(thinExtByRefPart.getClass())) {
	    		I_ThinExtByRefPartInteger str = (I_ThinExtByRefPartInteger) thinExtByRefPart;
	    		value = new Integer(str.getIntValue()).toString();
	    	}
	    	else if (I_ThinExtByRefPartBoolean.class.isAssignableFrom(thinExtByRefPart.getClass())) {
	    		I_ThinExtByRefPartBoolean str = (I_ThinExtByRefPartBoolean) thinExtByRefPart;
	    		value = (str.getBooleanValue()) ? "1" : "0";
	    	}
	    	else
	    		AceLog.getAppLog().warning("Unhandled refset data type for I_ThinExtByRefPart: " + thinExtByRefPart);
    	}
    	return value;

    }

    public String getDisplayName(I_GetConceptData conceptData) throws Exception {
    	String ret = null;

    	List<? extends I_DescriptionVersioned> descs = conceptData.getDescriptions();
    	for (Iterator<? extends I_DescriptionVersioned> i = descs.iterator(); i.hasNext();) {
    		I_DescriptionVersioned d = i.next();
    		I_DescriptionTuple dt = d.getLastTuple();
    		I_DescriptionPart part = dt.getMutablePart();
    		ret = part.getText();
    	}
    	
    	return ret;
    }

    public String getPreviousDisplayName(I_GetConceptData conceptData) throws Exception {
    	String ret = null;

    	List<? extends I_DescriptionVersioned> descs = conceptData.getDescriptions();
    	I_DescriptionTuple newestOldTuple = null;
    	for (Iterator<? extends I_DescriptionVersioned> i = descs.iterator(); i.hasNext();) {
    		I_DescriptionVersioned d = i.next();
    		for (I_DescriptionTuple dt : d.getTuples()) {
    			if (dt.getVersion() < this.startingVersion)
    				if (newestOldTuple == null) {
    					newestOldTuple = dt;
    				}
    				else if (dt.getVersion() > newestOldTuple.getVersion()) {
    					newestOldTuple = dt;
    				}
    		}
    	}
    	if (newestOldTuple != null)
    		ret = newestOldTuple.getMutablePart().getText();
    	return ret;
    }
    

    public String getPreviousDisplayName(I_DescriptionVersioned d) throws Exception {
    	String ret = null;


    	I_DescriptionTuple newestOldTuple = null;
		for (I_DescriptionTuple dt : d.getTuples()) {
			if (dt.getVersion() < this.startingVersion)
				if (newestOldTuple == null) {
					newestOldTuple = dt;
				}
				else if (dt.getVersion() > newestOldTuple.getVersion()) {
					newestOldTuple = dt;
				}
		}
    	if (newestOldTuple != null)
    		ret = newestOldTuple.getMutablePart().getText();
    	return ret;
    }
    

    public void addRecordIds(I_ThinExtByRefTuple extensionTuple, I_GetConceptData rootConcept,
    		ExternalTermRecord record) 
    	throws Exception {
    	
    	String dot11 = null;
    	String dot1 = null;
    	String uuid = null;
    	String rootUuid = null;
    	String snomed = null;
    	String icd9 = null;
    	String icd10 = null;
    	
    	I_GetConceptData idConcept = Terms.get().getConcept(extensionTuple.getComponentId());
		uuid = getIdForConcept(idConcept, "2faa9262-8fb2-11db-b606-0800200c9a66");
		rootUuid = getIdForConcept(rootConcept, "2faa9262-8fb2-11db-b606-0800200c9a66");
		
		if(record.getMasterFileName().equals(EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_CLINICAL)) {
			dot11 = getIdForConcept(idConcept, "e3dadc2a-196d-5525-879a-3037af99607d");
			dot1 = getIdForConcept(idConcept, "e49a55a7-319d-5744-b8a9-9b7cc86fd1c6");
		}
		else if(record.getMasterFileName().equals(EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_BILLING)) {
			dot11 = getIdForConcept(idConcept, "bf3e7556-38cb-5395-970d-f11851c9f41e");
			dot1 = getIdForConcept(idConcept, "af8be384-dc60-5b56-9ad8-bc1e4b5dfbae");
		}
		//snomed = getIdForConcept(rootConcept, "0418a591-f75b-39ad-be2c-3ab849326da9");
		snomed = getIdForConcept(idConcept, "0418a591-f75b-39ad-be2c-3ab849326da9");
		icd9 = getIdForConcept(idConcept, "a8160cc4-c49c-3a56-aa82-ea51e6c538ba");
		icd10 = getIdForConcept(idConcept, "9228d285-e625-33f9-bf46-9cfba3beee6d");
		
		if (dot11 != null)
			record.addItem("11", dot11, dot11);
		if (dot1 != null)
			record.addItem("1", dot1, dot1);
		record.addItem("35", uuid, uuid);
		record.addItem("rootuuid", rootUuid, rootUuid);
   		if (snomed != null)
   			record.addItem("snomed", snomed, snomed);
   		if (icd9 != null)
   			record.addItem("icd9", icd9, icd9);
   		if (icd10 != null)
   			record.addItem("icd10", icd10, icd10);
    }

    public boolean recordIsStandAloneTerm(ExternalTermRecord record) {
    	return record.hasItem("2");
    }
    
    public String getIdForConcept(I_GetConceptData concept, String idTypeUUID) throws Exception {
    	String ret = null;
    	//getLog().info("Looking for id in: " + concept);
		I_GetConceptData idSourceConcept = Terms.get().getConcept(new UUID[] { UUID
				.fromString(idTypeUUID) }); 
		int idSourceNid = idSourceConcept.getConceptId();
		for (I_IdPart part : concept.getIdentifier().getMutableIdParts()) {
			if (part.getAuthorityNid() == idSourceNid) {
				ret = part.getDenotation().toString();
				break;
			}
		}
		return ret;
    }

}
