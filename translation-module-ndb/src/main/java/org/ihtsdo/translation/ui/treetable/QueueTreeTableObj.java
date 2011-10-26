/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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

package org.ihtsdo.translation.ui.treetable;

import org.dwfa.bpa.process.EntryID;

/**
 * The Class QueueTreeTableObj.
 */
public class QueueTreeTableObj {
    

	private String status;

	private EntryID entryId;

	private String sourceFSN;

	private String sourcePref;

	private String targetFSN;

	private String targetPref;

	/**
     * Instantiates a new tree obj.
     */
    public QueueTreeTableObj(String sObjType,String sourceFSN,String sourcePref,String sStatus,String targetFSN, String targetPref, EntryID entryID) {
        this.objType=sObjType;
        this.sourceFSN=sourceFSN;
        this.sourcePref=sourcePref;
        this.status=sStatus;
        this.targetFSN=targetFSN;
        this.targetPref=targetPref;
        this.entryId=entryID;
        
    }
    
    /** The _obj type. */
    private String objType;
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
        return sourceFSN ;
    }
    
    /**
     * Gets the obj type.
     * 
     * @return the obj type
     */
    public String getObjType(){
        return objType;
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	public EntryID getEntryId() {
		return entryId;
	}

	public String getSourceFSN() {
		return sourceFSN;
	}

	public void setSourceFSN(String sourceFSN) {
		this.sourceFSN = sourceFSN;
	}

	public String getSourcePref() {
		return sourcePref;
	}

	public void setSourcePref(String sourcePref) {
		this.sourcePref = sourcePref;
	}

	public String getTargetFSN() {
		return targetFSN;
	}

	public void setTargetFSN(String targetFSN) {
		this.targetFSN = targetFSN;
	}

	public String getTargetPref() {
		return targetPref;
	}

	public void setTargetPref(String targetPref) {
		this.targetPref = targetPref;
	}

}
