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
import org.dwfa.ace.log.AceLog;
import org.dwfa.mojo.epicexport.I_RefsetUsageInterpreter;

public class RefsetUsageInterpreter implements I_RefsetUsageInterpreter{

	public List<I_RefsetApplication> getApplications(String refsetName) {
		List<I_RefsetApplication> applications = new ArrayList<I_RefsetApplication>();
		List<String> nameComponents;

		if ((nameComponents = parseStringForWildcardContents(refsetName, "* Item 2 *")) != null) {
			RefsetApplication app = new RefsetApplication(
					formatMasterfileName(nameComponents.get(0)), "2");
			app.setRegion(nameComponents.get(1));
			applications.add(app);	
		}
		else if ((nameComponents = parseStringForWildcardContents(refsetName, "* Item *")) != null) {
			RefsetApplication app = new RefsetApplication(
					formatMasterfileName(nameComponents.get(0)), nameComponents.get(1));
			applications.add(app);	
		}
    	else if(refsetName.equals("EDG Billing Contact Date")) {
    		applications.add(new RefsetApplication(EpicLoadFileFactory.EPIC_MASTERFILE_NAME_EDG_BILLING, "20"));
    	}

    	else if(refsetName.equals("Reason for Soft Delete")) {
    		applications.add(new RefsetApplication(
    				EpicLoadFileFactory.EPIC_MASTERFILE_NAME_WILDCARD, "300002"));
    	}
    	else if (refsetName.equals("ICD10-CM Code Mapping Status") ||
    			refsetName.equals("ICD9-CM Code Mapping") ||
    			refsetName.equals("Path reference set") || 
    			refsetName.equals("Refset Auxiliary Concept") ||
    			refsetName.equals("Path origin reference set")) {
    		// Ignore
    	}
    		
    	else 
    		AceLog.getAppLog().warning("Unhandled refset name: " + refsetName);

		return applications;
	}
	
	public String formatMasterfileName(String name) {
		return name.replaceAll(" ", "").toLowerCase();
	}

	public List<String> parseStringForWildcardContents(String lookin, String pattern) {
		List<String> arguments = new ArrayList<String>();
		int lookforPointer = 0;
		int jumpto = 0;
		int found = 0;
		int wildcards = 0;
		String next;
		int lastPatternPointer = 0;
		int lastf = 0;
		
		for (int i = 0; i < pattern.length(); i++) {
			if (pattern.charAt(i) == '*') {
				wildcards++;
				jumpto = -1;
				int locationOfNextWildcard = pattern.indexOf("*", i + 1);
				if (locationOfNextWildcard == -1) {
					if (wildcards > found + 1)
						break;
					next = pattern.substring(i + 1);
				}
				else
					next = pattern.substring(i + 1, locationOfNextWildcard);
				if (next.length() > 0) {
					jumpto = lookin.indexOf(next, lookforPointer);
				}
				else
					jumpto = lookin.length();
				if (jumpto != -1) {
					String arg = lookin.substring(lookforPointer, jumpto);
					
					if (pattern.substring(lastPatternPointer, i).equals(lookin.substring(lastf, lookforPointer))) {
						arguments.add(arg);
						lookforPointer = jumpto - 1;
						found++;
						lastPatternPointer = i + 1;
						lastf = lookforPointer + 1;
					}
				}
			}
			lookforPointer++;
		}
		return (wildcards == found) ? arguments : null;
	}

	public class RefsetApplication implements I_RefsetUsageInterpreter.I_RefsetApplication {
	
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
		
		public boolean itemIsTermName() {
			return this.itemNumber.equals(EpicLoadFileFactory.DISPLAYNAME_ITEM);
		}
	}
}
