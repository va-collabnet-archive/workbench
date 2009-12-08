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
 * Created on Apr 1, 2006
 */
package org.dwfa.bpa.process;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.UUID;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;

public interface I_StoreIdentifiedObjects {
    /**
     * @param obj
     *            The process to write to the queue
     * @param t
     *            transaction that governs this operation
     * @throws RemoteException
     * @throws TransactionException
     */
    public EntryID write(Object obj, Transaction t) throws RemoteException, IOException, TransactionException;

    public void write(Object obj, EntryID entryID, Transaction t) throws RemoteException, IOException,
            TransactionException;

    /**
     * @param entryID
     * @param t
     *            transaction that governs this operation
     * @return The specified process
     * @throws RemoteException
     * @throws TransactionException
     */
    public Object take(EntryID entryID, Transaction t) throws RemoteException, IOException, ClassNotFoundException,
            TransactionException, NoMatchingEntryException;

    /**
     * @param processId
     * @param t
     *            transaction that governs this operation
     * @return The specified object
     * @throws RemoteException
     * @throws TransactionException
     */
    public Object take(UUID objectID, Transaction t) throws RemoteException, IOException, ClassNotFoundException,
            TransactionException, NoMatchingEntryException;

    /**
     * Takes the first object (as ordered by the Stores's native ordering) that
     * matches the selection criterion.
     * 
     * @param selector
     * @param t
     *            transaction that governs this operation
     * @return The specified object
     * @throws RemoteException
     * @throws TransactionException
     * @throws NoMatchingEntryException
     */
    public Object take(I_SelectObjects selector, Transaction t) throws RemoteException, IOException,
            ClassNotFoundException, TransactionException, NoMatchingEntryException;

    /**
     * Hides the process with the given entryID for the duration of the
     * transaction.
     * 
     * @param entryID Identifier for the entry to be hidden.
     * @param t transaction that governs this operation
     * @throws RemoteException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws TransactionException
     * @throws NoMatchingEntryException
     */
    public void hide(EntryID entryID, Transaction t) throws RemoteException, IOException, ClassNotFoundException,
            TransactionException, NoMatchingEntryException;

    /**
     * @param processId
     * @param t
     *            transaction that governs this operation
     * @return The specified object
     * @throws RemoteException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object read(EntryID entryID, Transaction t) throws RemoteException, IOException, ClassNotFoundException;

    /**
     * @param selector
     *            Provides criterion to select the returned
     *            I_DescribeBusinessProcess objects.
     * @return A Collection of I_DescribeBusinessProcess objects that meet the
     *         selector's criterion.
     * @throws RemoteException
     */
    public Collection<I_DescribeObject> getProcessMetaData(I_SelectObjects selector) throws RemoteException,
            IOException;

    /**
     * Allow a process to be be written (allowing a transactional boundry),
     * without actually giving up control of the process. This function will
     * allow tasks to "checkpoint" a process while continuing execution.
     * 
     * @param process
     *            The process to write.
     * @param writeTran
     *            The transaction that governs writing this process.
     * @param takeTran
     *            The transaction under which the process is marked as taken. If
     *            the writeTran fails to commit, the takeTran will also fail.
     *            Once the writeTran commits, the takeTran is independent of the
     *            writeTran.
     * @throws TransactionException
     */
    public EntryID writeThenTake(Object process, Transaction writeTran, Transaction takeTran) throws RemoteException,
            IOException, ClassNotFoundException, TransactionException;

    /**
     * @return An address that specifies the inbox from which this queue can be
     *         accessed.
     * @throws RemoteException
     */
    public String getNodeInboxAddress() throws RemoteException;

}
