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
package org.dwfa.ace.task.queue;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.queue.QueueServer;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import com.sun.jini.start.LifeCycle;

@BeanList(specs = { @Spec(directory = "tasks/ide/queue", type = BeanType.TASK_BEAN) })
public class OpenQueuesInFolder extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String queueDir = "profiles/queues";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(queueDir);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            queueDir = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            File directory = new File(queueDir);

            if (directory.listFiles() != null) {
                for (File dir : directory.listFiles()) {
                    processFile(dir, null);
                }
            }

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private void processFile(File file, LifeCycle lc) throws Exception {
        if (file.isDirectory() == false) {
            if (file.getName().equalsIgnoreCase("queue.config")) {
                AceLog.getAppLog().info("Found queue: " + file.toURI().toURL().toExternalForm());
                if (QueueServer.started(file)) {
                    AceLog.getAppLog().info("Queue already started: " + file.toURI().toURL().toExternalForm());
                } else {
                    new QueueServer(new String[] { file.getCanonicalPath() }, lc);
                }
            }
        } else {
            for (File f : file.listFiles()) {
                processFile(f, lc);
            }
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getQueueDir() {
        return queueDir;
    }

    public void setQueueDir(String queueDir) {
        this.queueDir = queueDir;
    }

}
