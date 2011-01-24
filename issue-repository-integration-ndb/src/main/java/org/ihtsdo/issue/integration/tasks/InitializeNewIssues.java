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

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.JFileChooser;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.BusinessProcess;
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
import org.ihtsdo.issue.IssueSearchCriteria;
import org.ihtsdo.issue.integration.util.IssueAssignmentsUtil;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * The Class InitializeNewIssues.
 */
@BeanList(specs = 
	  { @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class InitializeNewIssues extends AbstractTask {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

    /** The Constant dataVersion. */
    private static final int dataVersion = 1;
    
    /** The issue repo prop. */
    private TermEntry issueRepoProp;
    
    /** The file name. */
    private String fileName;

    /** The message. */
    private String message = "Please select a file";

    /**
     * Gets the message.
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     * 
     * @param message the new message
     */
    public void setMessage(String message) {
        this.message = message;
    }

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
    	
    	JFileChooser fileChooser = new JFileChooser();
    	File bpFile = null;
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle(message);
        int returnValue = fileChooser
                .showDialog(new Frame(), "Choose file");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            bpFile = fileChooser.getSelectedFile();
            fileName = bpFile.getPath();
            System.out.println(fileName);
            if (worker.getLogger().isLoggable(Level.INFO)) {
                worker.getLogger().info(("Selected file: " + fileName));
            }
        } else {
            throw new TaskFailedException("User failed to select a file.");
        }
        
        BusinessProcess businessProcess = null;
        
		try {
			InputStream file = new FileInputStream(bpFile);
			InputStream buffer = new BufferedInputStream(file);
	        ObjectInput input = new ObjectInputStream (buffer);
	        businessProcess = (BusinessProcess) input.readObject();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
        
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
		
		businessProcess.setDestination("author.inbox");
		
    	IssueSearchCriteria criteria = new IssueSearchCriteria(null, "New Issue", null,null,null,null, null);
    	IssueAssignmentsUtil.initializeIssueForAssignments(repository, criteria, businessProcess, "Ready to download");
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

	/**
	 * Gets the file name.
	 * 
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name.
	 * 
	 * @param fileName the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
