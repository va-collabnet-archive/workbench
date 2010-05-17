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
package org.dwfa.bpa;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.UUID;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_RecordExecution;

/**
 * @author kec
 * 
 */
public class ExecutionRecord implements I_RecordExecution {
    /**
     * 
     */
    private Date date;
    private int taskId;
    private UUID workerId;
    private String workerDescription;
    private Condition condition;

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(date);
        out.writeInt(taskId);
        out.writeObject(workerId);
        out.writeObject(workerDescription);
        out.writeObject(condition);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.date = (Date) in.readObject();
            this.taskId = in.readInt();
            this.workerId = (UUID) in.readObject();
            this.workerDescription = (String) in.readObject();
            this.condition = (Condition) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @param date
     * @param taskId
     * @param workerId
     * @param workerDescription
     * @param condition
     */
    public ExecutionRecord(Date date, int taskId, UUID workerId, String workerDescription, Condition condition) {
        super();
        if (workerId == null) {
            throw new IllegalArgumentException("workerID cannot be null");
        }
        this.date = date;
        this.taskId = taskId;
        this.workerId = workerId;
        this.workerDescription = workerDescription;
        this.condition = condition;
    }

    public ExecutionRecord() {
        super();
    }

    /**
     * @see org.dwfa.bpa.process.I_RecordExecution#getDate()
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * @see org.dwfa.bpa.process.I_RecordExecution#getTaskId()
     */
    public int getTaskId() {
        return this.taskId;
    }

    /**
     * @see org.dwfa.bpa.process.I_RecordExecution#getWorkerId()
     */
    public String getWorkerId() {
        if (this.workerId == null) {
            return null;
        }
        return this.workerId.toString();
    }

    /**
     * @see org.dwfa.bpa.process.I_RecordExecution#getWorkerDesc()
     */
    public String getWorkerDesc() {
        return this.workerDescription;
    }

    /**
     * @see org.dwfa.bpa.process.I_RecordExecution#getCondition()
     */
    public Condition getCondition() {
        return this.condition;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(I_RecordExecution o) {
        ExecutionRecord another = (ExecutionRecord) o;
        int comp = this.date.compareTo(another.getDate());
        if (comp == 0) {
            comp = this.taskId - another.taskId;
        }
        if (comp == 0) {
            comp = this.workerDescription.compareTo(another.workerDescription);
        }

        return comp;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return compareTo((I_RecordExecution) obj) == 0;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.date.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return date + " taskId: " + taskId + ": " + condition + "; " + workerDescription + " (" + workerId + ")";
    }

    /**
     * @return Returns the workerDescription.
     */
    public String getWorkerDescription() {
        return workerDescription;
    }

    /**
     * @param workerDescription The workerDescription to set.
     */
    public void setWorkerDescription(String workerDescription) {
        this.workerDescription = workerDescription;
    }

    /**
     * @param condition The condition to set.
     */
    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    /**
     * @param date The date to set.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @param taskId The taskId to set.
     */
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    /**
     * @param workerId The workerId to set.
     */
    public void setWorkerId(String workerId) {
        this.workerId = UUID.fromString(workerId);
    }
}
