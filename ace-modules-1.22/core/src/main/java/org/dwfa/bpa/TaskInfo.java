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
 * Created on Jan 9, 2006
 */
package org.dwfa.bpa;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_DefineTask;

public class TaskInfo implements Serializable {

    I_DefineTask task;

    List<Branch> branches;

    List<ExecutionRecord> executionRecords;

    Rectangle bounds;

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.task);
        out.writeObject(this.branches);
        out.writeObject(this.executionRecords);
        out.writeObject(this.bounds);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if ((objDataVersion > 0) && (objDataVersion <= dataVersion)) {
            try{
                this.task = (I_DefineTask) in.readObject();
                this.branches = (List<Branch>) in.readObject();
                this.executionRecords = (List<ExecutionRecord>) in.readObject();
                this.bounds = (Rectangle) in.readObject();
            } catch (StreamCorruptedException ignor){
                System.out.println("Cannot load object" + ignor);
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(task);
        buff.append("\nbranches: " + branches);
        buff.append("\nexecution records:");
        if (this.executionRecords != null) {
            Iterator<ExecutionRecord> recordItr = this.executionRecords.iterator();
            while (recordItr.hasNext()) {
                Object record = recordItr.next();
                buff.append("\n" + record.toString());
            }
        }
        // buff.append(" bounds: " + bounds);
        return buff.toString();
    }

    public TaskInfo() {
        super();
    }

    /**
     * @param task
     * @param branches
     * @param executionRecords
     * @param bounds
     * @param process TODO
     */
    public TaskInfo(I_DefineTask task, List<Branch> branches, List<ExecutionRecord> executionRecords, Rectangle bounds) {
        super();
        this.task = task;
        this.branches = branches;
        this.executionRecords = executionRecords;
        this.bounds = bounds;
    }

    public int getTaskIdForCondition(Condition condition) throws NoBranchForConditionException {
        if (this.branches != null) {
            Iterator<Branch> branchItr = this.branches.iterator();
            while (branchItr.hasNext()) {
                I_Branch branch = branchItr.next();
                if (branch.getCondition().equals(condition)) {
                    int destId = branch.getDestinationId();
                    return destId;
                }
            }
        }
        throw new NoBranchForConditionException(" Condition: " + condition + " taskId: " + getTask().getId() + " "
            + getTask().getName());
    }

    /**
     * @return Returns the bounds.
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * @param bounds
     *            The bounds to set.
     */
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    /**
     * @return Returns the branches.
     */
    public List<Branch> getBranches() {
        if (this.branches == null) {
            this.branches = new ArrayList<Branch>(1);
        }
        return Collections.unmodifiableList(branches);
    }

    public void addBranch(I_DefineTask destination, Condition condition) {
        this.addBranch(new Branch(condition, destination.getId()));

    }

    public void addBranch(Branch b) {
        if (this.branches == null) {
            this.branches = new ArrayList<Branch>(1);
        }
        this.branches.add(b);

    }

    /**
     * @return Returns the executionRecords.
     */
    public List<ExecutionRecord> getExecutionRecords() {
        if (this.executionRecords == null) {
            this.executionRecords = new ArrayList<ExecutionRecord>(1);
        }
        return Collections.unmodifiableList(executionRecords);
    }

    public void clearExecutionRecords() {
        this.executionRecords = null;
    }

    /**
     * @param executionRecords
     *            The executionRecords to set.
     */
    public void addExecutionRecord(ExecutionRecord record) {
        if (this.executionRecords == null) {
            this.executionRecords = new ArrayList<ExecutionRecord>(1);
        }
        this.executionRecords.add(record);
    }

    /**
     * @return Returns the task.
     */
    public I_DefineTask getTask() {
        return task;
    }

    /**
     * @param task
     *            The task to set.
     */
    public void setTask(I_DefineTask task) {
        this.task = task;
    }

    /**
     * @param destination
     * @param condition
     */
    public void removeBranch(I_DefineTask destination, Condition condition) {
        this.branches.remove(new Branch(condition, destination.getId()));
    }
}
