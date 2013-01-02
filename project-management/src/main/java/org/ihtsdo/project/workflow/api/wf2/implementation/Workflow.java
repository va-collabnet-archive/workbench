package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.workflow2.ProjectBI;
import org.ihtsdo.project.workflow2.WfActionBI;
import org.ihtsdo.project.workflow2.WfDefinitionBI;
import org.ihtsdo.project.workflow2.WfFilterBI;
import org.ihtsdo.project.workflow2.WfRoleBI;
import org.ihtsdo.project.workflow2.WfStateBI;
import org.ihtsdo.project.workflow2.WfTaskBI;
import org.ihtsdo.project.workflow2.WfUserBI;
import org.ihtsdo.project.workflow2.WorkflowBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public class Workflow implements WorkflowBI {

	public Workflow() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Collection<WfTaskBI> getTasks(ConceptVersionBI concept) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfTaskBI> searchWorkflow(Collection<WfFilterBI> filters) {
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
	public Collection<WfActionBI> getAllActions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfRoleBI> getAllRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfDefinitionBI> getAllDefinitions() {
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
