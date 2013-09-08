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
package org.dwfa.queue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_SelectObjects;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.ihtsdo.ttk.lookup.InstanceWrapper;
import org.ihtsdo.ttk.lookup.LookupService;
import org.ihtsdo.ttk.queue.QueuePreferences;

public abstract class ObjectServerCore<T extends I_DescribeObject> implements ActionListener {

    private static Set<ObjectServerCore<I_DescribeObject>> openServers =
            new ConcurrentSkipListSet<>();

    public static void refreshServers() {
        for (ObjectServerCore<I_DescribeObject> server : openServers) {
            server.initEntryMetaInfo();
        }
    }

    public static class MatchEntryID implements FileFilter {
        EntryID entryID;

        /**
         * @param entryID
         */
        public MatchEntryID(EntryID entryID) {
            super();
            this.entryID = entryID;
        }

        /**
         * @see java.io.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept(File f) {
            String[] idParts = f.getName().split("[.]");
            if (idParts.length == 3) {
                return idParts[1].equals(entryID.toString());
            }
            return false;
        }

    }

    private class MatchObjectID implements FileFilter {
        UUID objectID;

        /**
         * @param processID
         */
        public MatchObjectID(UUID objectID) {
            super();
            this.objectID = objectID;
        }

        /**
         * @see java.io.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept(File f) {
            return f.getName().startsWith(this.objectID.toString());
        }

    }

    protected UUID serviceId;

    protected File directory;

    protected boolean readInsteadOfTake;

    private Set<T> uncommittedTakes = Collections.synchronizedSet(new HashSet<T>());

    private SortedSet<T> objectInfoSortedSet;

    private Comparator<T> nativeComparator;

    private I_GetObjectInputStream oisGetter = new DefaultObjectInputStreamCreator();

    private File logDir;
    
    private InstanceWrapper<ObjectServerCore<T>> instance;

    /**
     * @param files
     * @param i
     */
    public File undoTake(File file) {
        String currentName = file.getName();
        String newName =
                currentName.substring(0, currentName.lastIndexOf(getFileSuffixTakePending())).concat(getFileSuffix());
        File newFile = new File(file.getParentFile(), newName);
        file.renameTo(newFile);
        return newFile;
    }

    /**
     * @param files
     * @param i
     * @throws IOException
     */
    public File finishWrite(File file) {
        String currentName = file.getName();
        String newName =
                currentName.substring(0, currentName.lastIndexOf(getFileSuffixWritePending())).concat(getFileSuffix());
        File newFile = new File(file.getParentFile(), newName);
        file.renameTo(newFile);
        try {
            writeLogEntry(newFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return newFile;
    }

    /**
     * @param files
     * @param i
     */
    public File startTake(File file) {
        String currentName = file.getName();
        String newName =
                currentName.substring(0, currentName.lastIndexOf(getFileSuffix())).concat(getFileSuffixTakePending());
        File newFile = new File(file.getParentFile(), newName);
        file.renameTo(newFile);
        return newFile;
    }

    @SuppressWarnings("unchecked")
    public ObjectServerCore(QueuePreferences qp) throws Exception {
        super();
        getLogger().log(
            Level.INFO,"\n*******************\n\n" + 
                "Starting {0} with preferences: {1}\n\n******************\n", 
            new Object[]{this.getClass().getSimpleName(), qp});
        this.readInsteadOfTake = qp.getReadInsteadOfTake();
        this.directory = qp.getQueueDirectory().getAbsoluteFile();
        this.directory.mkdirs();
        this.logDir = new File(this.directory, ".llog");
        this.logDir.mkdirs();

        this.nativeComparator = (Comparator<T>) new DefaultQueueComparator();
        initFromDirectory();
        instance    = 
                new InstanceWrapper(this, qp.getId(), 
                    qp.getDisplayName(), qp.getServiceItemProperties());
        LookupService.add(instance);

        // Add to openServers after fields are set.
        // Otherwise, concrete subclasses have nothing to use for comparisons.
        openServers.add((ObjectServerCore<I_DescribeObject>) this);
    }

    /**
     * @return Returns the file suffix.
     */
    public abstract String getFileSuffix();

    /**
     * @return Returns the file suffix for a pending take.
     */
    public abstract String getFileSuffixTakePending();

    /**
     * @return Returns the file suffix for a pending write.
     */
    public abstract String getFileSuffixWritePending();

    protected EntryID newEntryID() {
        return new EntryID(UUID.randomUUID());
    }

    protected final void initFromDirectory() throws IOException, ClassNotFoundException {
        rollbackUncommittedChanges();
        initEntryMetaInfo();
    }

    void rollbackUncommittedChanges() throws IOException {
        File[] files = this.directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return (pathname.getName().endsWith(getFileSuffixTakePending()) || pathname.getName().endsWith(
                    getFileSuffixWritePending()));
            }
        });
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith(getFileSuffixTakePending())) {
                    undoTake(files[i]);

                } else if (files[i].getName().endsWith(getFileSuffixWritePending())) {
                    String currentName = files[i].getName();
                    String newName =
                            currentName.substring(0, currentName.lastIndexOf(getFileSuffixWritePending())).concat(
                                getFileSuffix());
                    files[i].renameTo(new File(files[i].getParentFile(), newName));

                }
            }
        }
        files = this.directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(getFileSuffix());
            }
        });
        if (files != null) {
            for (File f : files) {
                writeLogEntry(f);
            }
        }

    }

    public Object read(EntryID entryID, Transaction t) throws IOException, ClassNotFoundException,
            NoMatchingEntryException {
        File[] files = this.directory.listFiles(new MatchEntryID(entryID));
        if (files == null) {
            throw new NoMatchingEntryException("No matching files for entryID: " + entryID);
        }
        if (files.length != 1) {
            throw new NoMatchingEntryException("Found " + files.length + " matching files for entryID: " + entryID);
        }
        FileInputStream fis = new FileInputStream(files[0]);
        BufferedInputStream bis = new BufferedInputStream(fis);
        Object obj;
        try (ObjectInputStream ois = oisGetter.getObjectInputStream(bis)) {
            obj = (I_EncodeBusinessProcess) ois.readObject();
        }
        return obj;
    }

    /**
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoMatchingEntryException
     * @see org.dwfa.bpa.process.I_QueueProcesses#read(net.jini.id.Uuid, net.jini.core.transaction.Transaction)
     */
    public Object read(UUID objectID, Transaction t) throws IOException, ClassNotFoundException,
            NoMatchingEntryException {
        File[] files = this.directory.listFiles(new MatchObjectID(objectID));
        if (files == null) {
            throw new NoMatchingEntryException("No matching files for objectID: " + objectID);
        }
        if (files.length != 1) {
            throw new NoMatchingEntryException("Found " + files.length + " matching files for objectID: " + objectID);
        }
        FileInputStream fis = new FileInputStream(files[0]);
        BufferedInputStream bis = new BufferedInputStream(fis);
        Object obj;
        try (ObjectInputStream ois = oisGetter.getObjectInputStream(bis)) {
            obj = ois.readObject();
        }
        return obj;
    }

    /**
     * @throws IOException 
     * @see org.dwfa.bpa.process.I_QueueProcesses#getProcessMetaData(org.dwfa.bpa.process.I_SelectProcesses)
     */
    public Collection<T> getMetaData(I_SelectObjects selector) throws IOException {
        if (selector == null) {
            return new ArrayList<>(this.objectInfoSortedSet);
        }
        Collection<T> returnValues = new ArrayList<>();
        synchronized (this.objectInfoSortedSet) {
            for (T desc : this.objectInfoSortedSet) {
                if (selector.select(desc)) {
                    returnValues.add(desc);
                }
            }
        }
        return returnValues;
    }

    /**
     * @throws IOException
     * @throws ClassNotFoundException
     * @see org.dwfa.bpa.process.I_QueueProcesses#take(net.jini.id.Uuid, net.jini.core.transaction.Transaction)
     */
    public Object take(EntryID entryID, Transaction t) throws IOException,
            ClassNotFoundException, NoMatchingEntryException {
        try {
            if (this.readInsteadOfTake) {
                return this.read(entryID, t);
            }
            File[] files = this.directory.listFiles(new MatchEntryID(entryID));
            if (files == null) {
                throw new NoMatchingEntryException("No matching files for entryID: " + entryID);
            }
            if (files.length != 1) {
                throw new NoMatchingEntryException("Found " + files.length + " matching files for entryID: " + entryID);
            }

            File originalProcessFile = files[0];
            File newProcessFile = startTake(originalProcessFile);
            FileInputStream fis = new FileInputStream(newProcessFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
                    Object obj;
                    try (ObjectInputStream ois = oisGetter.getObjectInputStream(bis)) {
                        obj = ois.readObject();
                    }
            T objDesc = getObjectDescription(obj, entryID);

            this.uncommittedTakes.add(objDesc);
            this.objectInfoSortedSet.remove(objDesc);
                    try {
                        addTakeToTransaction(objDesc, this.objectInfoSortedSet, this.uncommittedTakes, newProcessFile,
                            originalProcessFile, t, this);
                    } catch (            RollbackException | IllegalStateException | SystemException ex) {
                        throw new IOException(ex);
                    }
            return obj;
        } catch (FileNotFoundException ex) {
            throw new NoMatchingEntryException(ex.toString());
        }

    }
    
    public void delete(EntryID entryID, Transaction t) throws NoMatchingEntryException{
            File[] files = this.directory.listFiles(new MatchEntryID(entryID));
            if (files == null) {
                throw new NoMatchingEntryException("No matching files for entryID: " + entryID);
            }
            if (files.length != 1) {
                throw new NoMatchingEntryException("Found " + files.length + " matching files for entryID: " + entryID);
            }

            File originalProcessFile = files[0];
            originalProcessFile.delete();

    }

    /**
     * @see org.dwfa.bpa.process.I_QueueProcesses#write(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.EntryID, net.jini.core.transaction.Transaction)
     */
    public void write(T object, EntryID entryID, Transaction t) throws IOException {
        if (t == null) {
            write(object, entryID);
        } else {
            File objectFile =
                    new File(this.directory, object.getObjectID() + "." + entryID + getFileSuffixWritePending());
            FileOutputStream fos = new FileOutputStream(objectFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(object);
            }
            try {
                this.addWriteToTransaction(getObjectDescription(object, entryID), this.objectInfoSortedSet, objectFile, t, this);
            } catch (    RollbackException | IllegalStateException | SystemException ex) {
                throw new IOException(ex);
            }
        }
    }

    public void write(T object, EntryID entryID) throws IOException {
        File objectFile = new File(this.directory, object.getObjectID() + "." + entryID + getFileSuffix());
        FileOutputStream fos = new FileOutputStream(objectFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
        }
        writeLogEntry(objectFile);
        objectInfoSortedSet.add(getObjectDescription(object, entryID));
    }

    private void writeLogEntry(File entryFile) throws IOException {
        if (entryFile.exists()) {
            File logEntry = new File(logDir, entryFile.getName());
            if (logEntry.exists() == false) {
                logEntry.createNewFile();
            }
        }
    }

    /**
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws TransactionException
     * @see org.dwfa.bpa.process.I_QueueProcesses#take(org.dwfa.bpa.process.I_SelectProcesses,
     *      net.jini.core.transaction.Transaction)
     */
    public Object take(I_SelectObjects selector, Transaction t) throws IOException,
            ClassNotFoundException, NoMatchingEntryException {
        T selectedObjectInfo = null;
        synchronized (this.objectInfoSortedSet) {
            for (T desc : this.objectInfoSortedSet) {
                if (selector.select(desc)) {
                    selectedObjectInfo = desc;
                    break;
                }
            }
        }
        if (selectedObjectInfo != null) {
            return this.take(selectedObjectInfo.getObjectID(), t);
        }
        throw new NoMatchingEntryException();
    }

    public Object read(I_SelectObjects selector, Transaction t) throws IOException,
            ClassNotFoundException, NoMatchingEntryException {
        T selectedObjectInfo = null;
        synchronized (this.objectInfoSortedSet) {
            for (T desc : this.objectInfoSortedSet) {
                if (selector.select(desc)) {
                    selectedObjectInfo = desc;
                    break;
                }
            }
        }
        if (selectedObjectInfo != null) {
            return this.read(selectedObjectInfo.getObjectID(), t);
        }
        throw new NoMatchingEntryException(selector.toString());
    }

    public EntryID getEntryID(File f) {
        String[] idParts = f.getName().split("[.]");
        return new EntryID(UUID.fromString(idParts[1]));
    }

    protected abstract T getObjectDescription(Object obj, EntryID entryID) throws IOException;

    /**
     * @throws IOException
     * @throws ClassNotFoundException
     * @see org.dwfa.bpa.process.I_QueueProcesses#take(net.jini.id.Uuid, net.jini.core.transaction.Transaction)
     */
    public Object take(UUID objectID, Transaction t) throws IOException, ClassNotFoundException,
            NoMatchingEntryException {
        try {
            if (this.readInsteadOfTake) {
                return this.read(objectID, t);
            }
            if (t == null) {
                return this.take(objectID);
            } else {
                File[] files = this.directory.listFiles(new MatchObjectID(objectID));
                if (files == null) {
                    throw new NoMatchingEntryException("Found no matching files for objectID: " + objectID);
                }
                if (files.length != 1) {
                    throw new NoMatchingEntryException("Found " + files.length + " matching files for objectID: "
                        + objectID);
                }

                File originalObjectFile = files[0];

                EntryID entryID = getEntryID(originalObjectFile);
                File newObjectFile = startTake(originalObjectFile);
                FileInputStream fis = new FileInputStream(newObjectFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = oisGetter.getObjectInputStream(bis);
                Object obj = ois.readObject();
                ois.close();
                T desc = this.getObjectDescription(obj, entryID);
                this.uncommittedTakes.add(desc);
                this.objectInfoSortedSet.remove(desc);
                try {
                    this.addTakeToTransaction(desc, this.objectInfoSortedSet, this.uncommittedTakes, newObjectFile,
                        originalObjectFile, t, this);
                } catch (        RollbackException | IllegalStateException | SystemException ex) {
                    throw new IOException(ex);
                }
                return obj;
            }
        } catch (FileNotFoundException ex) {
            throw new NoMatchingEntryException(ex.toString());
        }
    }

    private Object take(UUID objectID) throws IOException, ClassNotFoundException,
            NoMatchingEntryException {
        try {
            File[] files = this.directory.listFiles(new MatchObjectID(objectID));
            if (files == null) {
                throw new NoMatchingEntryException("No matching files for objectID: " + objectID);
            }
            if (files.length != 1) {
                throw new NoMatchingEntryException("Found " + files.length + " matching files for objectID: "
                    + objectID);
            }

            File objectFile = files[0];

            EntryID entryID = getEntryID(objectFile);
            objectFile = startTake(objectFile);
            FileInputStream fis = new FileInputStream(objectFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
                    Object obj;
                    try (ObjectInputStream ois = oisGetter.getObjectInputStream(bis)) {
                        obj = ois.readObject();
                    }
            T desc = this.getObjectDescription(obj, entryID);
            this.objectInfoSortedSet.remove(desc);
            objectFile.delete();
            return obj;
        } catch (FileNotFoundException ex) {
            throw new NoMatchingEntryException(ex.toString());
        }
    }

    private void addTakeToTransaction(T objDesc, SortedSet<T> name, Set<T> name2, File newProcessFile,
            File originalProcessFile, Transaction st, ObjectServerCore core) 
            throws RollbackException, IllegalStateException, SystemException {
        if (st == null) {
            return;
        }
        TakeTransactionPart part =
                new TakeTransactionPart<>(objDesc, objectInfoSortedSet, uncommittedTakes, newProcessFile,
                    originalProcessFile, this, this);
        st.enlistResource(part);
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().fine("addTakeToTransaction");
        }
    }

    private void addWriteToTransaction(T object, SortedSet<T> name, File processFile, Transaction st,
            ObjectServerCore core) throws RollbackException, IllegalStateException, SystemException {
        if (st == null) {
            return;
        }
        WriteTransactionPart part = new WriteTransactionPart<>(object, objectInfoSortedSet, processFile, this, this);
        st.enlistResource(part);
    }

    /**
     * 
     */
    protected synchronized void initEntryMetaInfo() {
        this.objectInfoSortedSet = Collections.synchronizedSortedSet(new TreeSet<>(this.nativeComparator));
        File[] files = this.directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(getFileSuffix());
            }
        });
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                try {
                    FileInputStream fis = new FileInputStream(files[i]);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    Object obj;
                    try (ObjectInputStream ois = oisGetter.getObjectInputStream(bis)) {
                        obj = ois.readObject();
                    }
                    String[] idParts = files[i].getName().split("[.]");
                    T info;
                    if (idParts[1].length() != 36) {
                        EntryID entryID = new EntryID(UUID.randomUUID());
                        info = getObjectDescription(obj, entryID);
                        files[i].renameTo(new File(files[i].getParent(), info.getObjectID() + "." + entryID
                            + getFileSuffix()));
                    } else {
                        EntryID entryID = new EntryID(UUID.fromString(idParts[1]));
                        info = getObjectDescription(obj, entryID);
                    }
                    this.objectInfoSortedSet.add(info);
                } catch (IOException | ClassNotFoundException ex) {
                    getLogger().log(Level.SEVERE,
                        "Exception processing file: " + files[i] + " Message: " + ex.getMessage(), ex);
                }
            }
        }
    }

    @Override
    public abstract void actionPerformed(ActionEvent evt);

    /**
     * @return Returns the logger.
     */
    protected abstract Logger getLogger();

    /**
     * @see org.dwfa.bpa.process.I_QueueProcesses#write(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      net.jini.core.transaction.Transaction)
     */
    public EntryID write(T obj, Transaction t) throws IOException {
        EntryID entryID = newEntryID();
        this.write(obj, entryID, t);
        return entryID;
    }

    public Collection<Object> getInstanceProperties() {
        return instance.getInstanceProperties();
    }
    
    protected static final Set<ActionListener> commitListeners = new CopyOnWriteArraySet<>();
    public static void addCommitListener(ActionListener l) {
        commitListeners.add(l);
    }
    
    public static void removeCommitListener(ActionListener l) {
        commitListeners.remove(l);
    }
    
}
