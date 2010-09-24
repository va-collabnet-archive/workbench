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
package org.dwfa.queue.bpa.tasks.collabnet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

/**
 * Presents a dialog box to the user to select a destination queue. Sets the
 * destination address of the process that directly contains this task to the
 * selected destination.
 * 
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/queue tasks/collabnet", type = BeanType.TASK_BEAN) })
public class CollabnetSelectAssignee extends AbstractTask {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    
    private String assignees;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(assignees);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            assignees = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            
            String[] nameListRaw = this.assignees.split(":");
            HashMap<String, String> nameMap = new HashMap<String, String>();
            ArrayList<String> namesToDisplay = new ArrayList<String>();
            for (String raw : nameListRaw) {
                String[] s = raw.split("/");
                namesToDisplay.add(s[0]);
                nameMap.put(s[0], s[1]);
            }

            String selected = (String) worker.selectFromList(namesToDisplay.toArray(), "Select user",
            "Please select next user to recieve business process.");
            
            selected = nameMap.get(selected);
            process.setProperty("A: SEND_TO_USER", selected);
            
            return Condition.CONTINUE;
        } catch (Exception ex) {
            throw new TaskFailedException(ex);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do.
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }
    
    /**
     * @return Returns string of possible assignees.
     */
    public String getAssignees() {
        return assignees;
    }

    /**
     * Assignees list format "display_name1/collabnet_name1:display_name2/collabnet_name2: ..."
     * @param assignees 
     */
    public void setAssignees(String assignees) {
        this.assignees = assignees;
    }

}
