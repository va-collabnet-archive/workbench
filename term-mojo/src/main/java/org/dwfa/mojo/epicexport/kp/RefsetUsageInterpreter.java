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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.mojo.epicexport.I_RefsetUsageInterpreter;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * Class to determine what master file and items that a refset is used for.
 * 
 * @author Steven Neiner
 *
 */
public class RefsetUsageInterpreter implements I_RefsetUsageInterpreter{

	/**
	 * Given the name of a refset, determines what that refset is used for.  
	 * 
	 * @param refsetName - The name of the refset
	 * @return List of I_RefsetApplication, which is a masterfile name, and item number(or name) that
	 * the refset is used for.
	 */
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
		//TODO: Remove the startsWith("susan") when not needed
    	else if (refsetName.equals("ICD10-CM Code Mapping Status") ||
    			refsetName.equals("ICD9-CM Code Mapping") ||
    			refsetName.equals("Path reference set") || 
    			refsetName.equals("Refset Auxiliary Concept") ||
    			refsetName.equals("Path origin reference set") ||
    			refsetName.startsWith("susan")
    			) {
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

	/**
	 * Works the other way too!  Given a master file, item name/number combo, will return a refset if
	 * it can find one.
	 * 
	 * @param masterfile
	 * @param item
	 * @return
	 */
	public I_GetConceptData getRefsetForItem(String masterfile, String item) throws TerminologyException {
		I_GetConceptData ret = null;
		StringBuffer name = new StringBuffer("org.kp.refset.");
		name.append(masterfile.substring(0, 3).toUpperCase());
		if (masterfile.length() > 3) {
			name.append(' ');
			name.append(masterfile.substring(3, 4).toUpperCase());
			name.append(masterfile.substring(4).toLowerCase());
		}
		name.append(" Item ");
		name.append(item);
		
		UUID refsetId;
		try {
			refsetId = Type5UuidFactory.get(name.toString());
			ret = LocalVersionedTerminology.get().getConcept(refsetId);
		} catch (NoSuchAlgorithmException e) {
			throw new TerminologyException(e);
		} catch (UnsupportedEncodingException e) {
			throw new TerminologyException(e);
		} catch (IOException e) {
			throw new TerminologyException(e);
		}
		return ret;
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
