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
package org.ihtsdo.mojo.mojo.epicexport.kp;

import java.io.IOException;
import java.util.ArrayList;
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
import org.ihtsdo.mojo.mojo.epicexport.ExportToEpicLoadFilesMojo;
import org.ihtsdo.mojo.mojo.epicexport.ExternalTermPublisher;
import org.ihtsdo.mojo.mojo.epicexport.ExternalTermRecord;
import org.ihtsdo.mojo.mojo.epicexport.I_ExportValueConverter;
import org.ihtsdo.mojo.mojo.epicexport.I_RefsetUsageInterpreter.I_RefsetApplication;
import org.dwfa.tapi.TerminologyException;

/**
 * A value converter is a class that mines a refset, it's parent concept, or it's description concept, for values
 * used to populate an external terminology system.
 * 
 *  @author Steven Neiner
 */

public class ExportValueConverter implements I_ExportValueConverter{
	public static enum DISPLAYTYPE  {FULLY_SPECIFIED_NAME , PREFERED_TERM, SYNONYM, OTHER};
	public static final String DISPLAY_TYPE_UUID_FSN = "5e1fe940-8faf-11db-b606-0800200c9a66";
	public static final String DISPLAY_TYPE_UUID_PREFERED_TERM = "d8e3b37d-7c11-33ef-b1d0-8769e2264d44";
	public static final String DISPLAY_TYPE_UUID_SYNONYM = "d6fad981-7df6-3388-94d8-238cc0465a79";
	
	public static final String SOFT_DELETE_FLAG = "*SD";
	public static final String ID_UUID_UUID = "2faa9262-8fb2-11db-b606-0800200c9a66";
	public static final String ID_UUID_SNOMED = "0418a591-f75b-39ad-be2c-3ab849326da9";
	public static final String ID_UUID_ICD9 = "a8160cc4-c49c-3a56-aa82-ea51e6c538ba";
	public static final String ID_UUID_ICD10 = "9228d285-e625-33f9-bf46-9cfba3beee6d";
	
	public static final String ID_UUID_EDGCLINICAL_DOT1 = "e49a55a7-319d-5744-b8a9-9b7cc86fd1c6";
	public static final String ID_UUID_EDGCLINICAL_DOT11 = "e3dadc2a-196d-5525-879a-3037af99607d";
	public static final String ID_UUID_EDGCLINICAL_CSMID = "51220bb9-edff-55c3-b271-8cbffe54121d";
	public static final String ID_UUID_EDGBILLING_DOT1 = "af8be384-dc60-5b56-9ad8-bc1e4b5dfbae";
	public static final String ID_UUID_EDGBILLING_DOT11 = "bf3e7556-38cb-5395-970d-f11851c9f41e";
	public static final String ID_UUID_EDGBILLING_CSMID = "51220bb9-edff-55c3-b271-8cbffe54121d";
	
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
    		newestOldTuple = getNewestTupleAsOf(this.startingVersion, d);
    	}
    	if (newestOldTuple != null)
    		ret = newestOldTuple.getMutablePart().getText();
    	return ret;
    }
    
    public I_DescriptionTuple getNewestTupleAsOf(int version, I_DescriptionVersioned desc) {
    	I_DescriptionTuple newestOldTuple = null;
		for (I_DescriptionTuple dt : desc.getTuples()) {
			if (dt.getVersion() < version)
				if (newestOldTuple == null) {
					newestOldTuple = dt;
				}
				else if (dt.getVersion() > newestOldTuple.getVersion()) {
					newestOldTuple = dt;
				}
		}
		return newestOldTuple;
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

    	I_GetConceptData idConcept = Terms.get().getConcept(extensionTuple.getComponentId());
    	if (record.getMasterFileName().equals(EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_CLINICAL)) {
    		addIdToRecord("1", ID_UUID_EDGCLINICAL_DOT1, idConcept, record);
    		addIdToRecord("11", ID_UUID_EDGCLINICAL_DOT11, idConcept, record);
    		addIdToRecord("csm_id", ID_UUID_EDGCLINICAL_CSMID, idConcept, record);
    	}
    	else if (record.getMasterFileName().equals(EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_BILLING)) {
    		addIdToRecord("1", ID_UUID_EDGBILLING_DOT1, idConcept, record);
    		addIdToRecord("11", ID_UUID_EDGBILLING_DOT11, idConcept, record);
    		addIdToRecord("csm_id", ID_UUID_EDGBILLING_CSMID, idConcept, record);
    	}
    	addIdToRecord("35", ID_UUID_UUID, idConcept, record);
    	addIdToRecord("uuid", ID_UUID_UUID, idConcept, record);
    	addIdToRecord("parentuuid", ID_UUID_UUID, rootConcept, record);
    	addIdToRecord("snomed", ID_UUID_SNOMED, idConcept, record);
    	addIdToRecord("snomedparent", ID_UUID_SNOMED, rootConcept, record);
    	addIdToRecord("icd9", ID_UUID_ICD9, idConcept, record);
    	addIdToRecord("icd10", ID_UUID_ICD10, idConcept, record);
    	if (isSoftDeleted(record)) 
    		record.addItem("5", SOFT_DELETE_FLAG, 
    				wasSoftDeleted(record) ? SOFT_DELETE_FLAG : null);
    	
    }
    
    private boolean isSoftDeleted(ExternalTermRecord record) throws Exception {
    	//The term's description is retired
    	boolean ret = record.getTermStatus() == ExternalTermRecord.status.RETIRED;
    	// ... or the concept's fully specified name is retired.
    	ret = ret || getInterpretedStatus(getConceptStatus(record.getRootConcept(), Integer.MAX_VALUE)) 
    		== ExternalTermRecord.status.RETIRED;
    	ExternalTermRecord.Item i = record.getFirstItem("2");
    	// ... or the .2 refset is retired
    	ret = ret || getInterpretedStatus(i.getExtensionTuple().getStatusId()) == ExternalTermRecord.status.RETIRED;
    	return ret;
    }
    
    private boolean wasSoftDeleted(ExternalTermRecord record) throws Exception {
    	boolean ret = record.getPreviousStatus() == ExternalTermRecord.status.RETIRED;
    	// ... or the concept's fully specfied name is retired.
    	ret = ret || getInterpretedStatus(getConceptStatus(record.getRootConcept(), this.startingVersion)) 
    		== ExternalTermRecord.status.RETIRED;
    	ExternalTermRecord.Item i = record.getFirstItem("2");
    	I_ThinExtByRefPart prevExt = ExternalTermPublisher.getPreviousVersionOfExtension(
    			i.getExtensionTuple(), this.startingVersion);
    	if (prevExt != null)
    		ret = ret || getInterpretedStatus(prevExt.getStatusId()) == ExternalTermRecord.status.RETIRED;
    	return ret;
    }
    

    private int getConceptStatus(I_GetConceptData concept, int version) throws Exception {
    	int ret = 0;
    	List<? extends I_DescriptionVersioned> descs = concept.getDescriptions();
    	for (I_DescriptionVersioned d : descs) {
    		if (ExportValueConverter.getDescriptionType(d) == ExportValueConverter.DISPLAYTYPE.FULLY_SPECIFIED_NAME) {
				if (version == Integer.MAX_VALUE) {
					ret = d.getLastTuple().getStatusId();
				}
				else {
					I_DescriptionTuple dt = getNewestTupleAsOf(version, d);
					if (dt != null)
						ret = dt.getStatusId();
				}
			}
    	}
    	return ret;
    }
    
    public static ExportValueConverter.DISPLAYTYPE getDescriptionType(I_DescriptionVersioned description) 
    	throws IOException, TerminologyException {
    	ExportValueConverter.DISPLAYTYPE ret = ExportValueConverter.DISPLAYTYPE.OTHER;
		I_GetConceptData c = Terms.get().getConcept(description.getLastTuple().getTypeId());
		String uuidStr = c.getUids().get(0).toString();
		if (uuidStr.equals(DISPLAY_TYPE_UUID_FSN))
			ret = ExportValueConverter.DISPLAYTYPE.FULLY_SPECIFIED_NAME;
		else if(uuidStr.equals(DISPLAY_TYPE_UUID_PREFERED_TERM))
			ret = ExportValueConverter.DISPLAYTYPE.PREFERED_TERM;
		else if(uuidStr.equals(DISPLAY_TYPE_UUID_SYNONYM))
			ret = ExportValueConverter.DISPLAYTYPE.SYNONYM;
    	return ret;
    }
    
    public static String getConceptDescription(I_GetConceptData concept, 
    		ExportValueConverter.DISPLAYTYPE type) throws Exception {
    	String ret = null;
    	for (I_DescriptionVersioned d: concept.getDescriptions()) {
    		if (ExportValueConverter.getDescriptionType(d) == type)
    			ret = d.getLastTuple().getText();
    	}
    	return ret;
    }

    public static String getPreferedTerm(I_GetConceptData concept) throws Exception {
    	return ExportValueConverter.getConceptDescription(concept, 
    			ExportValueConverter.DISPLAYTYPE.PREFERED_TERM);
    }

    public static String getFullySpecifiedName(I_GetConceptData concept) throws Exception {
    	return ExportValueConverter.getConceptDescription(concept, 
    			ExportValueConverter.DISPLAYTYPE.FULLY_SPECIFIED_NAME);
    }
    
    public static List<String> getSynonyms(I_GetConceptData concept) throws Exception {
    	List<String> ret = new ArrayList<String>();
    	for (I_DescriptionVersioned d: concept.getDescriptions()) {
    		if (ExportValueConverter.getDescriptionType(d) == ExportValueConverter.DISPLAYTYPE.SYNONYM)
    			ret.add(d.getLastTuple().getText());
    	}
    	return ret;
    }

    private void addIdToRecord(String name, String UUID, I_GetConceptData concept, ExternalTermRecord record) 
    	throws Exception {
    	String id = getIdForConcept(concept, UUID);
    	if (id != null)
    		record.addItem(name, id, id);
    }
    
    public String getIdForConcept(I_GetConceptData concept, String idTypeUUID) throws Exception {
    	String ret = null;
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

    public boolean recordIsStandAloneTerm(ExternalTermRecord record) {
    	return record.hasItem("2");
    }
    
    public ExternalTermRecord.status getInterpretedStatus(int statusId) throws Exception {
    	ExternalTermRecord.status ret = null;
    	if (statusId != 0) {
	    	I_GetConceptData statusConcept = Terms.get().getConcept(statusId);
	    	String statusName = null;
	    	statusName = ExportValueConverter.getPreferedTerm(statusConcept);
	    	if (statusName != null) {
	    		if(statusName.equals("retired") ||
	    				statusName.equals("concept retired") ||
	    				statusName.equals("inappropriate")) {
	    			ret = ExternalTermRecord.status.RETIRED;
	    		}
	    		else if(statusName.equals("current")) {
	    			ret = ExternalTermRecord.status.CURRENT;
	    		}
	    		else if(statusName.equals("limited")) {
	    			ret = ExternalTermRecord.status.LIMITED;
	    		}
	    		else
	    			AceLog.getAppLog().warning("Unhandled status: " + statusName);
	    	}
    	}
    	return ret;
    }
}
