package org.ihtsdo.project.workflow.filters;

import java.util.List;
import java.util.UUID;

import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;

public class FilterFactory{
	private static FilterFactory instance;
	private FilterFactory(){}
	
	public WfSearchFilterBI createFilterFromObject(Object obj){
		WfSearchFilterBI filter = null;
		if(obj instanceof WfState){
			filter = new WfStateFilter((WfState)obj);
		}else if(obj instanceof WorkList){
			WorkList wl = (WorkList)obj;
			filter = new WfWorklistFilter(wl.getUids());
		}
		return filter;
	}
	
	public WfComponentFilter createComponentFilter(String wfInstanceTextFilter){
		return new WfComponentFilter(wfInstanceTextFilter);
	}
	
	public WfDestinationFilter createDestinationFilter(String username, UUID id){
		WfUser destination = new WfUser(username, id);
		return new WfDestinationFilter(destination);
	}
	
	public WfStateFilter createWfStateFilter(String name, UUID id){
		WfState state = new WfState(name, id);
		return new WfStateFilter(state);
	}
	
	public WfWorklistFilter createWorklistFilter(List<UUID> worklistUUID){
		return new WfWorklistFilter(worklistUUID);
	}

	public static FilterFactory getInstance() {
		if(instance != null){
			return instance;
		}else{
			instance = new FilterFactory();
			return instance;
		}
	}
}
