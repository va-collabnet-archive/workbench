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

import java.util.ArrayList;
import java.util.List;

import org.dwfa.mojo.epicexport.I_RefsetInterpreter;

public class RefsetApplicationInterpreter implements I_RefsetInterpreter{
	public static final String EPIC_MASTERFILE_NAME_EDG_BILLING = "edgbilling";
	public static final String EPIC_MASTERFILE_NAME_EDG_CLINICAL = "edgclinical";
	public static final String EPIC_MASTERFILE_NAME_WILDCARD = "*";

	public List<I_RefsetApplication> getApplications(String refsetName) {
		List<I_RefsetApplication> applications = new ArrayList<I_RefsetApplication>();


		if(refsetName.equals("EDG Billing Item 2")) {
    		applications.add(new RefsetApplication(
    				RefsetApplicationInterpreter.EPIC_MASTERFILE_NAME_EDG_BILLING, "2"));
    	}
    	else if(refsetName.startsWith("EDG Billing Item ")) {
    		String item = refsetName.substring(17);
    		applications.add(new RefsetApplication(
    				RefsetApplicationInterpreter.EPIC_MASTERFILE_NAME_EDG_BILLING, item));
    	}
    	else if(refsetName.equals("EDG Billing Contact Date")) {
    		applications.add(new RefsetApplication(RefsetApplicationInterpreter.EPIC_MASTERFILE_NAME_EDG_BILLING, "20"));
    	}

    	
    	/**
    	 *  EDG Clinical refsets
    	 */
    	else if (refsetName.startsWith("EDG Clinical Item 2 "))
    	{
    		String region = refsetName.substring(20);
    		RefsetApplication app = new RefsetApplication(RefsetApplicationInterpreter.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "2");
    		applications.add(app);
    		app.setRegion(region);
    	}
    	else if(refsetName.startsWith("EDG Clinical Item ")) {
    		String item = refsetName.substring(18);
    		applications.add(new RefsetApplication(
    				RefsetApplicationInterpreter.EPIC_MASTERFILE_NAME_EDG_CLINICAL, item));
    	}
    	else if(refsetName.equals("Reason for Soft Delete")) {
    		applications.add(new RefsetApplication(
    				RefsetApplicationInterpreter.EPIC_MASTERFILE_NAME_WILDCARD, "5"));
    	}
    	else if (refsetName.equals("ICD10-CM Code Mapping Status") ||
    			refsetName.equals("Path reference set") || 
    			refsetName.equals("Path origin reference set")) {
    		// Ignore
    	}
    		
    	else 
    		System.out.println("Unhandled refset name: " + refsetName);

		return applications;
	}

	public class RefsetApplication implements I_RefsetInterpreter.I_RefsetApplication {
	
		String masterfile;
		String itemNumber;
		String value;
		String previousValue;
		String region;
		
		public RefsetApplication (String masterfile, String itemNumber) {
			setMasterfile(masterfile);
			setItemNumber(itemNumber);
		}
		
		public String getMasterfile() {
			return masterfile;
		}
		
		public void setMasterfile(String masterfile) {
			this.masterfile = masterfile;
		}
		
		public String getItemNumber() {
			return itemNumber;
		}
		
		public void setItemNumber(String itemNumber) {
			this.itemNumber = itemNumber;
		}
		
		public String getRegion() {
			return region;
		}
		
		public void setRegion(String region) {
			this.region = region;
		}
		
	}
}
