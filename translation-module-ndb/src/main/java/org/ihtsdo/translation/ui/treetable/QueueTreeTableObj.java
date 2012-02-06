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
    

	/** The status. */
	private String status;

	/** The entry id. */
	private EntryID entryId;

	/** The source fsn. */
	private String sourceFSN;

	/** The source pref. */
	private String sourcePref;

	/** The target fsn. */
	private String targetFSN;

	/** The target pref. */
	private String targetPref;

	/**
	 * Instantiates a new tree obj.
	 *
	 * @param sObjType the s obj type
	 * @param sourceFSN the source fsn
	 * @param sourcePref the source pref
	 * @param sStatus the s status
	 * @param targetFSN the target fsn
	 * @param targetPref the target pref
	 * @param entryID the entry id
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

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}


	/**
	 * Gets the entry id.
	 *
	 * @return the entry id
	 */
	public EntryID getEntryId() {
		return entryId;
	}

	/**
	 * Gets the source fsn.
	 *
	 * @return the source fsn
	 */
	public String getSourceFSN() {
		return sourceFSN;
	}

	/**
	 * Sets the source fsn.
	 *
	 * @param sourceFSN the new source fsn
	 */
	public void setSourceFSN(String sourceFSN) {
		this.sourceFSN = sourceFSN;
	}

	/**
	 * Gets the source pref.
	 *
	 * @return the source pref
	 */
	public String getSourcePref() {
		return sourcePref;
	}

	/**
	 * Sets the source pref.
	 *
	 * @param sourcePref the new source pref
	 */
	public void setSourcePref(String sourcePref) {
		this.sourcePref = sourcePref;
	}

	/**
	 * Gets the target fsn.
	 *
	 * @return the target fsn
	 */
	public String getTargetFSN() {
		return targetFSN;
	}

	/**
	 * Sets the target fsn.
	 *
	 * @param targetFSN the new target fsn
	 */
	public void setTargetFSN(String targetFSN) {
		this.targetFSN = targetFSN;
	}

	/**
	 * Gets the target pref.
	 *
	 * @return the target pref
	 */
	public String getTargetPref() {
		return targetPref;
	}

	/**
	 * Sets the target pref.
	 *
	 * @param targetPref the new target pref
	 */
	public void setTargetPref(String targetPref) {
		this.targetPref = targetPref;
	}

}
