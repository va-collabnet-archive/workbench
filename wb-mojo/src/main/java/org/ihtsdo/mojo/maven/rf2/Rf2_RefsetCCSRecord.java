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

public class Rf2_RefsetCCSRecord extends Rf2_RefsetBaseRecord {

	 // DATA COLUMNS    
    protected static final int COMPONENT_1 = 6;
    protected static final int COMPONENT_2 = 7;
    protected static final int STRING = 8;
    
    protected final long componentId1;
    protected final long componentId2;
    protected final String stringValue;

    public Rf2_RefsetCCSRecord(String id, String dateStr, boolean active, String moduleUuidStr,
            long refsetIdL, long referencedComponentIdL, long componentId1, long componentId2, String stringValue)
            throws ParseException {
        super(id, dateStr, active, moduleUuidStr, refsetIdL, referencedComponentIdL);
        this.componentId1 = componentId1;
        this.componentId2 = componentId2;
        this.stringValue = stringValue;
    }       

    protected List<? extends Rf2_RefsetBaseRecord> getEmptyRecordList() {
		return new ArrayList<Rf2_RefsetCCSRecord>();
	}
    
	protected void addRecord(List<? extends Rf2_RefsetBaseRecord> records, String[] line) throws NumberFormatException, ParseException, IOException {
    	// This method just parses the default stuff (up to referenced componentid).
    	// It really should be overridden in the implementing class
    	((List<Rf2_RefsetCCSRecord>) records).add(new Rf2_RefsetCCSRecord(line[ID],
	            Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]),
	            Rf2x.convertStringToBoolean(line[ACTIVE]),
	            Rf2x.convertSctIdToUuidStr(line[MODULE_ID]),
	            Long.parseLong(line[REFSET_ID]),
	            Long.parseLong(line[REFERENCED_COMPONENT_ID]),
	            Long.parseLong(line[COMPONENT_1]),
	            Long.parseLong(line[COMPONENT_2]),
	            line[STRING]));
    }

	protected void writeExtensionValues(BufferedWriter writer) throws IOException {
		// Component 1 Extension Value UUID
        writer.append(Rf2x.convertSctIdToUuidStr(componentId1) + TAB_CHARACTER);
        
        // Component 2 Extension Value UUID
        writer.append(Rf2x.convertSctIdToUuidStr(componentId2) + TAB_CHARACTER);

        // Component Extension Integer Value
        writer.append(stringValue + TAB_CHARACTER);		
	}
}