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
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.issue.Issue;

/**
 * The Class ShowDialogWithIssuesSearchResults.
 */
@BeanList(specs = 
	  { @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class ShowDialogWithIssuesSearchResults extends AbstractTask {
    
    /** The message. */
    private String message;
    
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
        out.writeObject(message);
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
            message = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
    	List<Issue> matches = (List<Issue>) process.readAttachement("issueMatchesKey");
    	message = "<html>" + message + "Results size: " + matches.size() + " <br>";
    	
    	for (Issue issue : matches) {
    		message = message + " Issue Id: " + issue.getExternalId() + " <br>";
    	}
    	
    	JOptionPane.showMessageDialog(new JFrame(), this.message);
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
}
