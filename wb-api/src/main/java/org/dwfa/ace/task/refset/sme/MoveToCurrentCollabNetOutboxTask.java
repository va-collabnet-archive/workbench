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
package org.dwfa.ace.task.refset.sme;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivilegedActionException;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.Transaction;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.cement.QueueType;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;

/**
 * This task finds a matching CollabNet outbox queue based on the current
 * workbench user.
 * The outbox name must be named using the convention (outbox name set in the
 * queue config file):
 * workbenchUserName-collabnet.outbox
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf/sme", type = BeanType.TASK_BEAN) })
public class MoveToCurrentCollabNetOutboxTask extends AbstractTask {

    // Serialization Properties
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    // Other Properties
    private I_TermFactory termFactory;
    private String message = "";

    private transient I_QueueProcesses q;

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(message);

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            message = (String) in.readObject();
        }
    }

    /**
     * Performs the primary action of the task, which in this case is to gather
     * and validate data that has been entered by the user on the Workflow
     * Details
     * Sheet.
     * 
     * @return The exit condition of the task
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            termFactory = Terms.get();
            I_ConfigAceFrame config;
            config = termFactory.getActiveAceFrameConfig();
            String userName = config.getUsername();

            Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
            Entry[] attrSetTemplates = new Entry[] { new TermEntry(QueueType.Concept.OUTBOX_QUEUE.getUids()) };
            ServiceTemplate template = new ServiceTemplate(null, serviceTypes, attrSetTemplates);
            ServiceItemFilter filter = null;
            ServiceItem[] matches = worker.lookup(template, 1, 100, filter, 30000, true);
            boolean foundMatch = false;
            for (ServiceItem match : matches) {
                q = (I_QueueProcesses) match.service;
                if (q.getNodeInboxAddress().equals(userName + "-collabnet.outbox")) {
                    if (message != null && !message.equals("")) {
                        JFrame parentFrame = null;
                        for (JFrame frame : OpenFrames.getFrames()) {
                            if (frame.isActive()) {
                                parentFrame = frame;
                                break;
                            }
                        }

                        JOptionPane.showMessageDialog(parentFrame, message);
                    }
                    //q.write(process, worker.getActiveTransaction());
                    try {
                        process.setDbDependencies(Ts.get().getLatestChangeSetDependencies());
                    } catch (IOException e) {
                        throw new TaskFailedException(e);
                    }
                    foundMatch = true;
                    return Condition.STOP;
                }
            }

            if (matches.length == 0 || !foundMatch) {
                throw new TaskFailedException("No CollabNet outbox available.");
            }

        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PrivilegedActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Condition.STOP;
    }

    /**
     * This method overrides: getDataContainerIds() in AbstractTask
     * 
     * @return The data container identifiers used by this task.
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * This method implements the interface method specified by: getConditions()
     * in I_DefineTask
     * 
     * @return The possible evaluation conditions for this task.
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.STOP_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            getLogger().info("Starting complete, getting transaction.");
            Transaction t = worker.getActiveTransaction();
            getLogger().info("Got transaction: " + t);
            q.write(process, t);
            getLogger().info("Written to queue.");
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}