package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow2.ProjectBI;
import org.ihtsdo.project.workflow2.WfActivityBI;
import org.ihtsdo.project.workflow2.WfProcessDefinitionBI;
import org.ihtsdo.project.workflow2.WfFilterBI;
import org.ihtsdo.project.workflow2.WfRoleBI;
import org.ihtsdo.project.workflow2.WfStateBI;
import org.ihtsdo.project.workflow2.WfProcessInstanceBI;
import org.ihtsdo.project.workflow2.WfUserBI;
import org.ihtsdo.project.workflow2.WorkflowBI;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

public class Workflow implements WorkflowBI {
	
	TerminologyStoreDI ts;
	ConceptChronicleBI worklistsRoot;

	public Workflow() {
		ts = Ts.get();
		try {
			worklistsRoot = ts.getConcept(UUID.fromString("2facb3a8-6829-314a-9798-ed006930ca18"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Collection<WfProcessInstanceBI> getProcessInstances(ConceptVersionBI concept) throws Exception {
		Collection<WfProcessInstanceBI> instances = new ArrayList<WfProcessInstanceBI>();
		Collection<? extends RefexVersionBI<?>> annotations = concept.getAnnotationsActive(concept.getViewCoordinate());
		for (RefexVersionBI loopAnnot : annotations) {
			ConceptChronicleBI refset = ts.getConcept(loopAnnot.getRefexNid());
			if (ts.isKindOf(refset.getConceptNid(), worklistsRoot.getConceptNid(), 
					Terms.get().getActiveAceFrameConfig().getViewCoordinate())) {
				WorkList wlist = TerminologyProjectDAO.getWorkList((I_GetConceptData) refset, Terms.get().getActiveAceFrameConfig());
				if (wlist != null) {
					instances.add(TerminologyProjectDAO.getWorkListMember(
							(I_GetConceptData) concept, wlist, Terms.get().getActiveAceFrameConfig()).getWfInstance());
				}
				
			}
		}
		return instances;
	}

	@Override
	public Collection<WfProcessInstanceBI> searchWorkflow(Collection<WfFilterBI> filters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfUserBI> getAllUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfStateBI> getAllStates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfActivityBI> getAllActivities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfRoleBI> getAllRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfProcessDefinitionBI> getAllProcessDefinitions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ProjectBI> getAllProjects() throws Exception {
		List<ProjectBI> projects = new ArrayList<ProjectBI>();
		for (I_TerminologyProject loopProject : TerminologyProjectDAO.getAllProjects(Terms.get().getActiveAceFrameConfig())) {
			projects.add(new Project(loopProject));
		}
		return projects;
	}

}
