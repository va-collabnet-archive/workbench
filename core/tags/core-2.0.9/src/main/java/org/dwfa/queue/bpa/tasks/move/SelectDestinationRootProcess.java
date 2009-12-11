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
/*
 * Created on Jan 18, 2006
 */
package org.dwfa.queue.bpa.tasks.move;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.ElectronicAddress;

/**
 * Presents a dialog box to the user to select a destination queue. Sets the 
 * destination address of the <em>root</em> process to the selected destination. 
 * <p>
 * @author kec
 *  
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/queue", type = BeanType.TASK_BEAN)})
public class SelectDestinationRootProcess extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            ;
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            ElectronicAddress eAddress = new ElectronicAddress();
            ServiceTemplate tmpl = new ServiceTemplate(null,
                    new Class[] { I_QueueProcesses.class },
                    new Entry[] { eAddress });
            ServiceItemFilter filter = null;
            ServiceItem[] serviceItems = worker.lookup(
                    tmpl, 1, 200, filter, 1000 * 15);
            ServiceItem item = (ServiceItem) worker.selectFromList(
                    serviceItems, "Select destination",
                    "Select the destination for this process");
            eAddress = null;
            for (int i = 0; i < item.attributeSets.length; i++) {
                if (ElectronicAddress.class
                        .isAssignableFrom(item.attributeSets[i].getClass())) {
                    eAddress = (ElectronicAddress) item.attributeSets[i];
                    break;
                }
            }
            if (eAddress == null) {
                throw new TaskFailedException("No electronic address selected.");
            }
            I_EncodeBusinessProcess rootProcess = worker.getProcessStack().get(0);
            rootProcess.setDestination(eAddress.address);
            rootProcess.validateDestination();
            return Condition.CONTINUE;
        } catch (Exception ex) {
            throw new TaskFailedException(ex);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Noting to do.

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

}

