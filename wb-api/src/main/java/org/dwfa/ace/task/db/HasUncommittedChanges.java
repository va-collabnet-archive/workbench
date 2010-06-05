/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.task.db;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task determines whether the database has uncommitted changes.
 * @author Chrissy Hill
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/db", type = BeanType.TASK_BEAN) })
public class HasUncommittedChanges extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
        	if (Terms.get().getUncommitted().size() > 0) {
        		if (SwingUtilities.isEventDispatchThread()) {
        			askToCommit();
        		} else {
        			SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
		        			askToCommit();
						}
					});
        		}
                
            }
            if (Terms.get().getUncommitted().size() > 0) {
                return Condition.TRUE;
            } else {
                return Condition.FALSE;
            }

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

	private void askToCommit() {
		Object[] options = {"Commit Changes",
                "Cancel Changes",
                "Do Nothing"};
		int n = JOptionPane.showOptionDialog(
			    OpenFrames.getActiveFrame(),
			    "How would you like to proceed?",
			    "Uncommitted Changes",
			    JOptionPane.YES_NO_CANCEL_OPTION,
			    JOptionPane.QUESTION_MESSAGE,
			    null,
			    options,
			    options[2]);     
		if (n == JOptionPane.YES_OPTION) {
			try {
				Terms.get().commit();
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		} else if (n == JOptionPane.NO_OPTION) {
			try {
				Terms.get().cancel();
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		} else {
            try {
				if (Terms.get().getActiveAceFrameConfig() != null) {
				    I_ConfigAceFrame activeFrame = Terms.get().getActiveAceFrameConfig();
				    for (I_ConfigAceFrame frameConfig : activeFrame.getDbConfig()
				            .getAceFrames()) {
				        frameConfig.setCommitEnabled(true);
				    }
				}
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
	}

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

    }

    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
