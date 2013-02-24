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

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

import com.googlecode.sardine.model.Collection;

public abstract class Rf2_RefsetBaseRecord implements Comparable<Rf2_RefsetBaseRecord> {

	protected static final String LINE_TERMINATOR = "\r\n";
    protected static final String TAB_CHARACTER = "\t";
    
    // DATA COLUMNS
    protected static final int ID = 0;// id
    protected static final int EFFECTIVE_TIME = 1; // effectiveTime
    protected static final int ACTIVE = 2; // active
    protected static final int MODULE_ID = 3; // moduleId
    protected static final int REFSET_ID = 4; // refSetId
    protected static final int REFERENCED_COMPONENT_ID = 5; // referencedComponentId    
    
    // RECORD FIELDS
    protected final String id;
    protected final String effDateStr;
    protected final long timeL;
    protected final boolean isActive;
    protected final long refsetIdL;
    protected final long referencedComponentIdL;       

    String pathUuidStr; // SNOMED Core default
    // String authorUuidStr; // saved as user
    String moduleUuidStr;    
    
    public Rf2_RefsetBaseRecord(String id, String dateStr, boolean active, String moduleUuidStr,
            long refsetIdL, long referencedComponentIdL)
            throws ParseException {
        this.id = id;
        this.effDateStr = dateStr;
        this.timeL = Rf2x.convertDateToTime(dateStr);
        this.isActive = active;

        this.refsetIdL = refsetIdL;
        this.referencedComponentIdL = referencedComponentIdL;        

        // SNOMED Core :NYI: setup path as a POM parameter.
        this.pathUuidStr = Rf2Defaults.getPathSnomedCoreUuidStr();
        // this.authorUuidStr = Rf2Defaults.getAuthorUuidStr();
        this.moduleUuidStr = moduleUuidStr;
    }            

    protected abstract List<? extends Rf2_RefsetBaseRecord> getEmptyRecordList();

	protected abstract void addRecord(List<? extends Rf2_RefsetBaseRecord> records, String[] line) throws NumberFormatException, ParseException, IOException;
	
	public void writeArf(BufferedWriter writer) throws IOException, TerminologyException {

        // Refset UUID
        writer.append(Rf2x.convertSctIdToUuidStr(refsetIdL) + TAB_CHARACTER);

        // Member UUID
        if (id.length() == 36) {
            writer.append(id + TAB_CHARACTER);
        } else {
            writer.append(id.substring(0,8) + '-');
            writer.append(id.substring(8,12) + '-');
            writer.append(id.substring(12,16) + '-');
            writer.append(id.substring(16,20) + '-');
            writer.append(id.substring(20,32) + TAB_CHARACTER);
        }

        // Status UUID
        writer.append(Rf2x.convertActiveToStatusUuid(isActive) + TAB_CHARACTER);

        // Component UUID
        writer.append(Rf2x.convertSctIdToUuidStr(referencedComponentIdL) + TAB_CHARACTER);

        // Effective Date
        writer.append(effDateStr + TAB_CHARACTER);

        // Path UUID
        writer.append(pathUuidStr + TAB_CHARACTER);   
        
        // TODO: uncomment when we write the rest of the code to handle additional refset types
//        writeExtensionValues(writer);

        // Author UUID String --> user
        writer.append(Rf2Defaults.getAuthorUuidStr() + TAB_CHARACTER);

        // Module UUID String
        writer.append(this.moduleUuidStr + LINE_TERMINATOR);
    }

    protected abstract void writeExtensionValues(BufferedWriter writer) throws IOException;

	@Override
    public int compareTo(Rf2_RefsetBaseRecord t) {
        if (this.referencedComponentIdL < t.referencedComponentIdL) {
            return -1; // instance less than received
        } else if (this.referencedComponentIdL > t.referencedComponentIdL) {
            return 1; // instance greater than received
        } else {
            if (this.timeL < t.timeL) {
                return -1; // instance less than received
            } else if (this.timeL > t.timeL) {
                return 1; // instance greater than received
            }
        }
        return 0; // instance == received
    }
}
