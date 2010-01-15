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

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;

/**
 * A class that implements this method is a plugin which identifies the usage for a refset.  The getApplications
 * method will return a list of "RefsetApplications", which is the masterfile and item name/number, as well as 
 * any regions, that contains data for external systems.  This is used to map concepts to records 
 * for an external system.
 * 
 * @author Steven Neiner
 *
 */
public interface I_RefsetUsageInterpreter {

	public List<I_RefsetApplication> getApplications(String refsetName);
	
	public I_GetConceptData getRefsetForItem(String masterfile, String item) throws TerminologyException;
	
	/**
	 * Class used to describe a usage for a refset.  This is a masterfile, item name or number, and an optional
	 * region or locality.
	 * 
	 * @author Steven Neiner
	 *
	 */
	public interface I_RefsetApplication {
		public String getMasterfile();
		
		public void setMasterfile(String masterfile);
		
		public String getItemNumber();
		
		public void setItemNumber(String itemNumber);
		
		public void setRegion(String region);
		
		public String getRegion();
		
		public boolean itemIsTermName();

	}
}
