package org.ihtsdo.translation.ui;

import java.util.Set;

import org.dwfa.bpa.process.EntryID;

public class QueueTableObj {

	private String status;

	private EntryID entryId;

	private String sourceFSN;

	private String sourcePref;

	private String targetFSN;

	private String targetPref;

	private Set<String> tagsArray;
	
	private String worklistName;

	private Long statusTime;

	/**
     * Instantiates a new tree obj.
	 * @param tagsArray 
	 * @param statusTime 
     */
    public QueueTableObj(String sObjType,String sourceFSN,String sourcePref,String sStatus,String targetFSN, String targetPref, EntryID entryID, Set<String> tagsArray,String worklistName, Long statusTime) {
        this.objType=sObjType;
        this.sourceFSN=sourceFSN;
        this.sourcePref=sourcePref;
        this.status=sStatus;
        this.targetFSN=targetFSN;
        this.targetPref=targetPref;
        this.entryId=entryID;
        this.tagsArray=tagsArray;
        this.worklistName=worklistName;
        this.statusTime=statusTime;
        
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

	public Set<String> getTagsArray() {
		return tagsArray;
	}

	public void setTagsArray(Set<String> tagsArray) {
		this.tagsArray = tagsArray;
	}

	public String getWorklistName() {
		return worklistName;
	}

	public void setWorklistName(String worklistName) {
		this.worklistName = worklistName;
	}

	public Long getStatusTime() {
		return statusTime;
	}

	public void setStatusTime(Long statusTime) {
		this.statusTime = statusTime;
	}
}
