package org.ihtsdo.tk.workflow.api;

public interface WfCommentBI extends Comparable<WfCommentBI>{

	WfUserBI getAuthor();
	
	Long getDate();
	
	String getComment();
	 
}
