package org.ihtsdo.workflow.refset;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowRefset {

	protected String getProp(String key, String props) {
		String fullKey = "<key>" + key + "</key>";
		
		int idx = props.indexOf(fullKey);
		String s = props.substring(idx);
		
		int startIndex = s.indexOf("<value>");
		int endIndex = s.indexOf("</value>");
		
		return s.substring(startIndex + "<value>".length(), endIndex);
	}
}
