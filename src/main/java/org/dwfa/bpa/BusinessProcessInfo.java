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
 * Created on Apr 20, 2005
 */
package org.dwfa.bpa;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeQueueEntry;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.ProcessID;
import org.dwfa.bpa.process.TaskFailedException;

/**
 * @author kec
 * 
 */
public class BusinessProcessInfo implements I_DescribeQueueEntry, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private ProcessID processID;
    private Date deadline;
    private Priority priority;
    private String originator;
    private String destination;
    private String subject;
    private String name;
    private EntryID entryID;

    /**
     * @param processID
     * @param deadline
     * @param priority
     * @param originator
     * @param destination
     * @param subject
     */
    public BusinessProcessInfo(ProcessID processID, Date deadline, Priority priority, String originator,
            String destination, String subject, String name, EntryID entryID) {
        super();
        this.processID = processID;
        this.deadline = deadline;
        this.priority = priority;
        this.originator = originator;
        this.destination = destination;
        this.subject = subject;
        this.name = name;
        this.entryID = entryID;
    }

    public BusinessProcessInfo(I_DescribeQueueEntry info) {
        this(info.getProcessID(), info.getDeadline(), info.getPriority(), info.getOriginator(), info.getDestination(),
            info.getSubject(), info.getName(), info.getEntryID());
    }

    public BusinessProcessInfo(I_DescribeBusinessProcess info, EntryID entryID) {
        this(info.getProcessID(), info.getDeadline(), info.getPriority(), info.getOriginator(), info.getDestination(),
            info.getSubject(), info.getName(), entryID);
    }

    /**
     * @see org.dwfa.bpa.process.I_DescribeBusinessProcess#getProcessID()
     */
    public ProcessID getProcessID() {
        return this.processID;
    }

    /**
     * @see org.dwfa.bpa.process.I_DescribeBusinessProcess#getDeadline()
     */
    public Date getDeadline() {
        return this.deadline;
    }

    /**
     * @see org.dwfa.bpa.process.I_DescribeBusinessProcess#getPriority()
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * @see org.dwfa.bpa.process.I_DescribeBusinessProcess#getOriginator()
     */
    public String getOriginator() {
        return this.originator;
    }

    /**
     * @see org.dwfa.bpa.process.I_DescribeBusinessProcess#getDestination()
     */
    public String getDestination() {
        return this.destination;
    }

    /**
     * @see org.dwfa.bpa.process.I_DescribeBusinessProcess#getSubject()
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * @see org.dwfa.bpa.process.I_DescribeBusinessProcess#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see org.dwfa.bpa.process.I_DescribeBusinessProcess#validateAddresses()
     */
    public void validateAddresses() throws TaskFailedException {
        BusinessProcess.validateAddresses(this);

    }

    /**
     * @see org.dwfa.bpa.process.I_DescribeQueueEntry#getEntryID()
     */
    public EntryID getEntryID() {
        return this.entryID;
    }

    /**
     * @see org.dwfa.bpa.process.I_DescribeBusinessProcess#validateDestination()
     */
    public void validateDestination() throws TaskFailedException {
        BusinessProcess.validateAddress(this.destination, this.processID);

    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append(" processID: " + processID);
        b.append(" deadline: " + deadline);
        b.append(" priority: " + priority);
        b.append(" originator: " + originator);
        b.append(" destination: " + destination);
        b.append(" subject: " + subject);
        b.append(" name: " + name);
        b.append(" entryID: " + entryID);

        return b.toString();
    }

    public UUID getObjectID() {
        return this.getProcessID().getUuid();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (I_DescribeQueueEntry.class.isAssignableFrom(obj.getClass())) {
            I_DescribeQueueEntry another = (I_DescribeQueueEntry) obj;
            return toString().equals(another.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return entryID.hashCode();
    }

}
