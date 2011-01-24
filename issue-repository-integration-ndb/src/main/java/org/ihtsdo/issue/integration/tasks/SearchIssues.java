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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
import org.ihtsdo.issue.IssueSearchCriteria;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * The Class SearchIssues.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class SearchIssues extends AbstractTask {

	/** The issue repo prop. */
	private TermEntry issueRepoProp;

	/** The status string. */
	private String statusString;

	/** The priority string. */
	private String priorityString;

	/** The user string. */
	private String userString;

	/** The property map key. */
	private String propertyMapKey;

	/** The property map string value. */
	private String propertyMapStringValue;

	/** The property map concept value. */
	private TermEntry propertyMapConceptValue;

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
		out.writeObject(statusString);
		out.writeObject(priorityString);
		out.writeObject(userString);
		out.writeObject(propertyMapKey);
		out.writeObject(propertyMapStringValue);
		out.writeObject(propertyMapConceptValue);
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
			issueRepoProp = (TermEntry) in.readObject();
			statusString = (String) in.readObject();
			priorityString = (String) in.readObject();
			userString = (String) in.readObject();
			propertyMapKey = (String) in.readObject();
			propertyMapStringValue = (String) in.readObject();
			propertyMapConceptValue = (TermEntry) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	
	/**
	 * Instantiates a new search issues.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public SearchIssues() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		List<Issue> issueMatches = new ArrayList<Issue>();
		try {
			I_GetConceptData issueRepoConcept = null;
			IssueRepository repository =null;
			if(issueRepoProp== null){
				// get from attachment
				repository = (IssueRepository) process.readAttachement("issueRepositoryKey");
			} else {
				issueRepoConcept = Terms.get().getConcept(issueRepoProp.ids);
			}

			if (repository==null)
				repository = IssueRepositoryDAO.getIssueRepository(issueRepoConcept);

			HashMap<String, Object> map = null;
			if (propertyMapKey != null) {
				map = new HashMap<String, Object>();
				if (propertyMapStringValue != null && propertyMapConceptValue != null) {
					// throw error
					throw new TaskFailedException("Search only String or Concept value, can't process both with one key.");
				} else if (propertyMapStringValue != null) {
					map.put(propertyMapKey, propertyMapStringValue);
				} else if (propertyMapConceptValue != null) {
					map.put(propertyMapKey, propertyMapConceptValue);
				}
			}

			
			IssueDAO jwto=new IssueDAO();
			IssueSearchCriteria criteria = new IssueSearchCriteria(userString, statusString, priorityString,null,null,null, map);
			issueMatches = jwto.searchIssues(repository,criteria);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		process.writeAttachment("issueMatchesKey", issueMatches);

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
	 * Gets the status string.
	 * 
	 * @return the status string
	 */
	public String getStatusString() {
		return statusString;
	}

	/**
	 * Sets the status string.
	 * 
	 * @param statusString the new status string
	 */
	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	/**
	 * Gets the user string.
	 * 
	 * @return the user string
	 */
	public String getUserString() {
		return userString;
	}

	/**
	 * Sets the user string.
	 * 
	 * @param userString the new user string
	 */
	public void setUserString(String userString) {
		this.userString = userString;
	}

	/**
	 * Gets the property map key.
	 * 
	 * @return the property map key
	 */
	public String getPropertyMapKey() {
		return propertyMapKey;
	}

	/**
	 * Sets the property map key.
	 * 
	 * @param propertyMapKey the new property map key
	 */
	public void setPropertyMapKey(String propertyMapKey) {
		this.propertyMapKey = propertyMapKey;
	}

	/**
	 * Gets the property map string value.
	 * 
	 * @return the property map string value
	 */
	public String getPropertyMapStringValue() {
		return propertyMapStringValue;
	}

	/**
	 * Sets the property map string value.
	 * 
	 * @param propertyMapStringValue the new property map string value
	 */
	public void setPropertyMapStringValue(String propertyMapStringValue) {
		this.propertyMapStringValue = propertyMapStringValue;
	}

	/**
	 * Gets the property map concept value.
	 * 
	 * @return the property map concept value
	 */
	public TermEntry getPropertyMapConceptValue() {
		return propertyMapConceptValue;
	}

	/**
	 * Sets the property map concept value.
	 * 
	 * @param propertyMapConceptValue the new property map concept value
	 */
	public void setPropertyMapConceptValue(TermEntry propertyMapConceptValue) {
		this.propertyMapConceptValue = propertyMapConceptValue;
	}

	/**
	 * Gets the priority string.
	 * 
	 * @return the priority string
	 */
	public String getPriorityString() {
		return priorityString;
	}

	/**
	 * Sets the priority string.
	 * 
	 * @param priorityString the new priority string
	 */
	public void setPriorityString(String priorityString) {
		this.priorityString = priorityString;
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
	
}