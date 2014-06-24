package org.ihtsdo.project.workflow.api.wf2.implementation;

public class CancelSearch {
	public boolean keepSearching = true;
	
	public void cancel(boolean cancel){
		keepSearching = cancel;
	}
	
	public boolean isCanceled(){
		return !keepSearching;
	}
	
}
