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

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.issue.integration.util.IssueAssignmentsUtil;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * The Class ExecuteOutboxWorkerLogic.
 */
@BeanList(specs = 
	  { @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class ExecuteOutboxWorkerLogic extends AbstractTask {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

    /** The Constant dataVersion. */
    private static final int dataVersion = 1;
    
    /** The issue repo prop. */
    private TermEntry issueRepoProp;
    
    /**
     * Write object.
     * 
     * @param out the out
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
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
        	// 
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
    	
    	I_GetConceptData issueRepoConcept = null;
		IssueRepository repository =null;
		if(issueRepoProp== null){
			// get from attachment
			repository = (IssueRepository) process.readAttachement("issueRepositoryKey");
		} else {
			try {
				issueRepoConcept = Terms.get().getConcept(issueRepoProp.ids);
				repository = IssueRepositoryDAO.getIssueRepository(issueRepoConcept);
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
    	try {
			IssueAssignmentsUtil.readQueueAndUpdateIssues(repository, "author.todo", worker,"Ready to download");
		} catch (Exception e) {
			e.printStackTrace();
		}
        return Condition.CONTINUE;
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do. 

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
        return new int[] {};
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
