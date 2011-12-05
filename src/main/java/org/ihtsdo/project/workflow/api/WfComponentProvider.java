package org.ihtsdo.project.workflow.api;

import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;

public class WfComponentProvider {

	public List<WfUser> getUsers(){
		List<WfUser> wfUser=new ArrayList<WfUser>();
		
		return wfUser;
	}
	
	public List<WfRole> getRoles(){
		List<WfRole> wfRole=new ArrayList<WfRole>();
		
		return wfRole;
	}
	
	public List<WfPermission> getPermissions(){
		List<WfPermission> wfPermission=new ArrayList<WfPermission>();
		
		return wfPermission;
	}

	public List<WfState> getStates(){
		List<WfState> wfState=new ArrayList<WfState>();
		
		return wfState;
	}
}
