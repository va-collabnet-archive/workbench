package org.ihtsdo.project.workflow.model;

import java.util.Comparator;

public class WfUserComparator implements Comparator<WfUser> {
	
	@Override
	public int compare(WfUser arg0, WfUser arg1) {
		return arg0.getUsername().compareTo(arg1.getUsername());
	}
	
}