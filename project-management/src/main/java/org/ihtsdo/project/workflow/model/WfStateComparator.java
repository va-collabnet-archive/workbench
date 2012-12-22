package org.ihtsdo.project.workflow.model;

import java.util.Comparator;

public class WfStateComparator implements Comparator<WfState>{

	@Override
	public int compare(WfState arg0, WfState arg1) {
		return arg0.getName().compareTo(arg1.getName());
	}

}
