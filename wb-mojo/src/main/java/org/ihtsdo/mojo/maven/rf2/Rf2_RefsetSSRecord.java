/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.maven.rf2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Rf2_RefsetSSRecord extends Rf2_RefsetBaseRecord {

    // DATA COLUMNS    
    protected static final int STRING_VAL_1 = 6;
    protected static final int STRING_VAL_2 = 7;    
    
    protected final String stringVal1;
    protected final String stringVal2;    

    public Rf2_RefsetSSRecord(String id, String dateStr, boolean active, String moduleUuidStr,
            long refsetIdL, long referencedComponentIdL, String stringVal1, String stringVal2)
            throws ParseException {
        super(id, dateStr, active, moduleUuidStr, refsetIdL, referencedComponentIdL);
        this.stringVal1 = stringVal1;
        this.stringVal2 = stringVal2;        
    }       

    public List<? extends Rf2_RefsetBaseRecord> getEmptyRecordList() {
		return new ArrayList<Rf2_RefsetSSRecord>();
	}
    
	public void addRecord(List<? extends Rf2_RefsetBaseRecord> records, String[] line) throws NumberFormatException, ParseException, IOException {
    	// This method just parses the default stuff (up to referenced componentid).
    	// It really should be overridden in the implementing class
    	((List<Rf2_RefsetSSRecord>) records).add(new Rf2_RefsetSSRecord(line[ID],
	            Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]),
	            Rf2x.convertStringToBoolean(line[ACTIVE]),
	            Rf2x.convertSctIdToUuidStr(line[MODULE_ID]),
	            Long.parseLong(line[REFSET_ID]),
	            Long.parseLong(line[REFERENCED_COMPONENT_ID]),
	            line[STRING_VAL_1],
	            line[STRING_VAL_2]));
    }

	public void writeExtensionValues(BufferedWriter writer) throws IOException {
		// String 1 Extension Value
        writer.append(stringVal1 + TAB_CHARACTER);
        
        // String 2 Extension Value
        writer.append(stringVal2 + TAB_CHARACTER);        
	}
}

