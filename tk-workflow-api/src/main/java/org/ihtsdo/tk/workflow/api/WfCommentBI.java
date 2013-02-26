package org.ihtsdo.tk.workflow.api;

public interface WfCommentBI extends Comparable<WfCommentBI>{

	String getAuthor();
	
	Long getDate();
	
	String getComment();
	
	String getRole();
}
