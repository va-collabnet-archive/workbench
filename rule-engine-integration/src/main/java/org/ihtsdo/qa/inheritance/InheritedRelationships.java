package org.ihtsdo.qa.inheritance;

import java.util.List;

import org.dwfa.ace.api.I_RelTuple;

public class InheritedRelationships {
	List<I_RelTuple[]> roleGroups;
	List<I_RelTuple> singleRoles;
	public InheritedRelationships(List<I_RelTuple[]> roleGroups,
			List<I_RelTuple> singleRoles) {
		super();
		this.roleGroups = roleGroups;
		this.singleRoles = singleRoles;
	}
	public List<I_RelTuple[]> getRoleGroups() {
		return roleGroups;
	}
	public List<I_RelTuple> getSingleRoles() {
		return singleRoles;
	}

}
