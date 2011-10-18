/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.issue.integration.tasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.IssueDAO;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * The Class CreateIssue.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class CreateIssue extends AbstractTask {

	/** The issue repo prop. */
	private TermEntry issueRepoProp;

	/** The external id. */
	private String externalId;
	
	/** The title. */
	private String title;
	
	/** The component id. */
	private String componentId;
	
	/** The description. */
	private String description;

	/** The priority. */
	private String priority;
	
	/** The user. */
	private String user;
	
	/** The status. */
	private String status;
	
	/** The field map. */
	private HashMap<String,Object> fieldMap;

	/** The Issue prop name. */
	private String IssuePropName="issueKey";

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;
	
	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(issueRepoProp);
		out.writeObject(externalId);
		out.writeObject(title);
		out.writeObject(componentId);
		out.writeObject(description);
		out.writeObject(priority);
		out.writeObject(user);
		out.writeObject(status);
		out.writeObject(fieldMap);
		out.writeObject(IssuePropName);

	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			issueRepoProp=(TermEntry)in.readObject();
			externalId = (String) in.readObject();
			title = (String) in.readObject();
			componentId = (String) in.readObject();
			description = (String) in.readObject();
			priority = (String) in.readObject();
			user = (String) in.readObject();
			status = (String) in.readObject();
			fieldMap = (HashMap<String,Object>) in.readObject();
			IssuePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		
			try{
				I_GetConceptData issueRepoConcept = null;
				IssueRepository repository =null;
				if(issueRepoProp== null){
					// get from attachment
					repository = (IssueRepository) process.readAttachement("issueRepositoryKey");
				} else {
					issueRepoConcept = Terms.get().getConcept(issueRepoProp.ids);
				}

				if (title == null) {
					// get from attachment
				}

				if (componentId == null) {
					// get from attachment
				}
				if (description == null) {
					// get from attachment
				}
				if (priority == null) {
					// get from attachment
				}

				if (user == null) {
					// get from attachment
				}

				if (status == null) {
					status="Open";
				}

				if (fieldMap == null) {
					fieldMap=new HashMap<String,Object>();
				}

				if (IssuePropName == null) {
					// get from attachment
				}
				Issue iss= new Issue();

				iss.setCategory("");
				iss.setComponent("");
				iss.setComponentId(componentId);
				iss.setExternalUser("");
				iss.setDescription(description);
				iss.setPriority(priority);
				iss.setProjectId("");
				iss.setWorkflowStatus(status);
				iss.setTitle(title);
				iss.setUser(user);
				iss.setFieldMap(fieldMap);
				iss.setDownloadStatus("Ready to download");
				
				if (repository==null)
					repository = IssueRepositoryDAO.getIssueRepository(issueRepoConcept);
				
				IssueDAO jwto=new IssueDAO();
				
				iss=jwto.createIssue(repository, iss);
				
				process.writeAttachment(IssuePropName, iss);
				
	         } catch (Exception e) {
	            throw new TaskFailedException(e);
	         }
			return Condition.CONTINUE;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {  };
	}

	/**
	 * Gets the issue repo prop.
	 * 
	 * @return the issue repo prop
	 */
	public TermEntry getIssueRepoProp() {
		return issueRepoProp;
	}

	/**
	 * Sets the issue repo prop.
	 * 
	 * @param issueRepoProp the new issue repo prop
	 */
	public void setIssueRepoProp(TermEntry issueRepoProp) {
		this.issueRepoProp = issueRepoProp;
	}

	/**
	 * Gets the external id.
	 * 
	 * @return the external id
	 */
	public String getExternalId() {
		return externalId;
	}

	/**
	 * Sets the external id.
	 * 
	 * @param externalId the new external id
	 */
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	/**
	 * Gets the title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 * 
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets the component id.
	 * 
	 * @return the component id
	 */
	public String getComponentId() {
		return componentId;
	}

	/**
	 * Sets the component id.
	 * 
	 * @param componentId the new component id
	 */
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the priority.
	 * 
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}

	/**
	 * Sets the priority.
	 * 
	 * @param priority the new priority
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}

	/**
	 * Gets the user.
	 * 
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the user.
	 * 
	 * @param user the new user
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 * 
	 * @param status the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Gets the field map.
	 * 
	 * @return the field map
	 */
	public HashMap<String, Object> getFieldMap() {
		return fieldMap;
	}

	/**
	 * Sets the field map.
	 * 
	 * @param fieldMap the field map
	 */
	public void setFieldMap(HashMap<String, Object> fieldMap) {
		this.fieldMap = fieldMap;
	}

	
}