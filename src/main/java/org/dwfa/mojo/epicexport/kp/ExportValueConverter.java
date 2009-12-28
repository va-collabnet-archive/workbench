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

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.log.AceLog;
import org.dwfa.mojo.epicexport.ExportToEpicLoadFilesMojo;
import org.dwfa.mojo.epicexport.I_ExportValueConverter;
import org.dwfa.mojo.epicexport.I_RefsetUsageInterpreter.I_RefsetApplication;

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
		
		I_ThinExtByRefPart extensionTuplePart = extensionTuple.getMutableIdPart();
		this.itemValue = null;
		this.previousItemValue = null;
		this.region = null;
		
		if (refsetUsage.getMasterfile().equals(RefsetUsageInterpreter.EPIC_MASTERFILE_NAME_EDG_BILLING)) {
			if (refsetUsage.getItemNumber().equals("2")) {
	    		itemValue = getDisplayName(conceptForDescription); 
	    		previousItemValue = getPreviousDisplayName(conceptForDescription); 
			}
			else {
				itemValue = getValueAsString(extensionTuplePart);
				previousItemValue = getValueAsString(previousPart);
			}
		}
		else if (refsetUsage.getMasterfile().equals(RefsetUsageInterpreter.EPIC_MASTERFILE_NAME_EDG_CLINICAL)) {
			if (refsetUsage.getItemNumber().equals("2")) {
    			itemValue = description.getLastTuple().getMutableIdPart().getText();
    			previousItemValue = getPreviousDisplayName(description);
    			region = refsetUsage.getRegion();
			}
			else if (refsetUsage.getItemNumber().equals("50")){
	    		I_ThinExtByRefPartBoolean doAdd = (I_ThinExtByRefPartBoolean) extensionTuplePart;
	    		if (doAdd.getValue() && description != null) {
	    			itemValue = description.getLastTuple().getMutableIdPart().getText();
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
	    		value = (str.getValue()) ? "1" : "0";
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
    		I_DescriptionPart part = dt.getMutableIdPart();
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
    		ret = newestOldTuple.getMutableIdPart().getText();
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
    		ret = newestOldTuple.getMutableIdPart().getText();
    	return ret;
    }
}
