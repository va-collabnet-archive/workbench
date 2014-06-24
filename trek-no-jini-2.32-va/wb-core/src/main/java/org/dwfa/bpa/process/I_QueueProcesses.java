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
 * Created on Dec 20, 2004
 */
package org.dwfa.bpa.process;

import java.io.IOException;
import java.util.Collection;
import javax.transaction.Transaction;

/**
 * General purpose interface for queing processes, and making them available to
 * workers based on selection criterion supplied by the worker or other
 * interested objects.
 * 
 * @author kec TODO support blocking and time-out take operations.
 */
public interface I_QueueProcesses {

    /**
     * @param process
     *            The process to write to the queue
     * @param t
     *            transaction that governs this operation
     * @throws RemoteException
     * @throws TransactionException
     */
    public EntryID write(I_EncodeBusinessProcess process, Transaction t) throws IOException;

    public void write(I_EncodeBusinessProcess process, EntryID entryID, Transaction t) throws 
            IOException;

    /**
     * @param entryID
     * @param t
     *            transaction that governs this operation
     * @return The specified process
     * @throws RemoteException
     * @throws TransactionException
     */
    public I_EncodeBusinessProcess take(EntryID entryID, Transaction t) throws IOException,
            ClassNotFoundException, NoMatchingEntryException;

    /**
     * @param processId
     * @param t
     *            transaction that governs this operation
     * @return The specified process
     * @throws RemoteException
     * @throws TransactionException
     */
    public I_EncodeBusinessProcess take(ProcessID processID, Transaction t) throws IOException,
            ClassNotFoundException, NoMatchingEntryException;
    
    public void delete(EntryID entryID, Transaction t) throws NoMatchingEntryException;

    /**
     * Takes the first process (as ordered by the Queue's native ordering) that
     * matches the selection criterion.
     * 
     * @param selector
     * @param t
     *            transaction that governs this operation
     * @return The specified process
     * @throws RemoteException
     * @throws TransactionException
     * @throws NoMatchingEntryException
     */
    public I_EncodeBusinessProcess take(I_SelectProcesses selector, Transaction t) throws IOException,
            ClassNotFoundException, NoMatchingEntryException;

    /**
     * @param processId
     * @param t
     *            transaction that governs this operation
     * @return The specified process
     * @throws RemoteException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public I_EncodeBusinessProcess read(EntryID entryID, Transaction t) throws IOException,
            ClassNotFoundException, NoMatchingEntryException;

    /**
     * @param selector
     *            Provides criterion to select the returned
     *            I_DescribeBusinessProcess objects.
     * @return A Collection of I_DescribeBusinessProcess objects that meet the
     *         selector's criterion.
     * @throws RemoteException
     */
    public Collection<I_DescribeBusinessProcess> getProcessMetaData(I_SelectProcesses selector) throws IOException;

    /**
     * @return An address that specifies the inbox from which this queue can be
     *         accessed.
     * @throws RemoteException
     */
    public String getNodeInboxAddress();

}
