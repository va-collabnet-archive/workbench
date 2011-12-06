package org.ihtsdo.project.workflow.api;

import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.project.workflow.model.WfInstance;

public class WorkflowSearcher {

	public WorkflowSearcher() {
		super();
	}

	public List<WfInstance> searchWfInstances(List<WorkflowSearchFilterBI> filters) {

		List<WfInstance> candidates = new ArrayList<WfInstance>();
		List<WfInstance> results = new ArrayList<WfInstance>();

		boolean thereIsAUserFilter = false;

		for (WorkflowSearchFilterBI loopFilter : filters) {
			if (loopFilter instanceof DestinationFilter) {
				thereIsAUserFilter = true;
			}
		}

		if (thereIsAUserFilter) {
			//TODO: filter bu user, worklist tiene una propiedad que son los WorkflowMemebrs, 
			//esos tienen un user y eso permite saltear el worklist sin pasar por todos
			//los miembros el worklist
		}

		// Hay que armar candidates con todos los candidatos, recorriendo lo menos posible
		// usando el metodo worklistmember.getInstance();

		//Filtrar
		for (WfInstance loopInstance : candidates) {
			boolean accepted = true;
			for (WorkflowSearchFilterBI loopFilter : filters) {
				if (!loopFilter.filter(loopInstance)) {
					accepted = false;
					break;
				}
			}
			if (accepted) {
				results.add(loopInstance);
			}
		}

		return results;

	}


}
