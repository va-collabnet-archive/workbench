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
package org.dwfa.bpa.worker.task;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/worker", type = BeanType.TASK_BEAN) })
public class NewMasterWorker extends AbstractTask {
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private String workerName = "Master Worker";
    private String startupDirectory = "none";
    private String workerPropName = ProcessAttachmentKeysForWorkerTasks.WORKER.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(workerName);
        out.writeObject(startupDirectory);
        out.writeObject(workerPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            workerName = (String) in.readObject();
            startupDirectory = (String) in.readObject();
            workerPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        JiniConfigWrapper jcw = new JiniConfigWrapper(worker.getJiniConfig());
        jcw.addObject(MasterWorker.class.getName(), "workerUuid", UUID.randomUUID());
        jcw.addObject(MasterWorker.class.getName(), "name", workerName);
        jcw.addObject(MasterWorker.class.getName(), "startupDirectory", new File(startupDirectory));
        try {
            MasterWorker masterWorker = new MasterWorker(jcw);
            process.setProperty(workerPropName, masterWorker);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // nothing to do...

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

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        Object old = this.workerName;
        this.workerName = workerName;
        this.firePropertyChange("workerName", old, workerName);
    }

    public String getStartupDirectory() {
        return startupDirectory;
    }

    public void setStartupDirectory(String startupDirectory) {
        Object old = this.startupDirectory;
        this.startupDirectory = startupDirectory;
        this.firePropertyChange("startupDirectory", old, startupDirectory);
    }

    public String getWorkerPropName() {
        return workerPropName;
    }

    public void setWorkerPropName(String workerPropName) {
        Object old = this.workerPropName;
        this.workerPropName = workerPropName;
        this.firePropertyChange("workerPropName", old, workerPropName);
    }

}