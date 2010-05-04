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
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.config.NoSuchEntryException;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.transaction.CannotJoinException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.CrashCountException;
import net.jini.core.transaction.server.ServerTransaction;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupDiscovery;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.lookup.JoinManager;
import net.jini.lookup.entry.Name;

import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_SelectObjects;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.jini.JiniManager;
import org.dwfa.jini.LookupJiniAndLocal;
import org.dwfa.jini.TransactionParticipantAggregator;

import com.sun.jini.start.LifeCycle;

public abstract class ObjectServerCore<T extends I_DescribeObject> implements ActionListener {

    private static Set<ObjectServerCore<I_DescribeObject>> openServers = new HashSet<ObjectServerCore<I_DescribeObject>>();

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
        public boolean accept(File f) {
            return f.getName().startsWith(this.objectID.toString());
        }

    }

    public static final String VERSION_STRING = "1.0";

    /**
     * Creates a new service ID.
     * 
     * @throws IOException
     */
    protected static ServiceID createServiceID(File directory) throws IOException {
        UUID uuid = UUID.randomUUID();
        ServiceID sid = new ServiceID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
        File serviceIdFile = new File(directory, "ServiceID.oos");
        FileOutputStream fos = new FileOutputStream(serviceIdFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(sid);
        oos.flush();
        oos.close();
        return sid;

    }

    /**
     * Cache of our <code>LifeCycle</code> object TODO implement the lifeCycle
     * destroy methods. See TxnManagerImpl for an example.
     */
    protected LifeCycle lifeCycle = null;

    protected JoinManager joinManager;

    protected final Configuration config;

    protected ServiceID serviceId;

    protected File directory;

    protected boolean readInsteadOfTake;

    protected String nodeInboxAddress;

    private Set<T> uncommittedTakes = Collections.synchronizedSet(new HashSet<T>());

    private SortedSet<T> objectInfoSortedSet;

    private Comparator<T> nativeComparator;

    private I_GetObjectInputStream oisGetter = new DefaultObjectInputStreamCreator();

    private File logDir;

    /**
     * @param files
     * @param i
     */
    public File undoTake(File file) {
        String currentName = file.getName();
        String newName = currentName.substring(0, currentName.lastIndexOf(getFileSuffixTakePending())).concat(
            getFileSuffix());
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
        String newName = currentName.substring(0, currentName.lastIndexOf(getFileSuffixWritePending())).concat(
            getFileSuffix());
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
        String newName = currentName.substring(0, currentName.lastIndexOf(getFileSuffix())).concat(
            getFileSuffixTakePending());
        File newFile = new File(file.getParentFile(), newName);
        file.renameTo(newFile);
        return newFile;
    }

    @SuppressWarnings("unchecked")
    public ObjectServerCore(String[] args, LifeCycle lc) throws Exception {
        super();
        openServers.add((ObjectServerCore<I_DescribeObject>) this);
        getLogger().info(
            "\n*******************\n\n" + "Starting " + this.getClass().getSimpleName() + " with config file: "
                + Arrays.asList(args) + "\n\n******************\n");
        this.config = ConfigurationProvider.getInstance(args, getClass().getClassLoader());
        this.lifeCycle = lc;
        oisGetter = (I_GetObjectInputStream) this.config.getEntry(this.getClass().getName(), "objectGetter",
            I_GetObjectInputStream.class);
        Boolean readInsteadOfTakeBool = (Boolean) this.config.getEntry(this.getClass().getName(), "readInsteadOfTake",
            Boolean.class, new Boolean(false));
        this.readInsteadOfTake = readInsteadOfTakeBool.booleanValue();
        this.nodeInboxAddress = (String) this.config.getEntry(this.getClass().getName(), "nodeInboxAddress",
            String.class);
        this.directory = (File) this.config.getEntry(this.getClass().getName(), "directory", File.class, new File(
            args[0]).getParentFile());
        this.directory.mkdirs();
        this.logDir = new File(this.directory, ".llog");
        this.logDir.mkdirs();

        this.nativeComparator = (Comparator<T>) this.config.getEntry(this.getClass().getName(), "nativeComparator",
            Comparator.class);
        initFromDirectory();
        this.init();
    }

    public String getNodeInboxAddress() throws RemoteException {
        return this.nodeInboxAddress;
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
     * Initializes the server, including exporting it and storing its proxy in
     * the registry.
     * 
     * @throws Exception
     *             if a problem occurs
     */
    @SuppressWarnings("unchecked")
    protected void init() throws Exception {
        if (JiniManager.isLocalOnly()) {
            List<Entry> entryList = new ArrayList<Entry>();
            Entry[] entries = (Entry[]) this.config.getEntry(this.getClass().getName(), "entries", Entry[].class,
                new Entry[] {});
            entryList.addAll(Arrays.asList(entries));

            Entry[] moreEntries = getFixedServiceEntries();
            entryList.addAll(Arrays.asList(moreEntries));

            ServiceItem serviceItem = new ServiceItem(this.getServiceID(), this,
                (Entry[]) entryList.toArray(new Entry[entryList.size()]));
            LookupJiniAndLocal.addToLocalServices(serviceItem);

        } else {
            LoginContext loginContext = (LoginContext) config.getEntry(this.getClass().getName(), "loginContext",
                LoginContext.class, null);
            if (loginContext == null) {
                getLogger().info(
                    "initAsSubject: " + this.getClass().getName() + " " + this.getServiceID()
                        + " with no login context.");
                initAsSubject();
            } else {
                loginContext.login();
                StringBuffer message = new StringBuffer();
                message.append("initAsSubject: " + this.getClass().getName() + " " + this.getServiceID()
                    + " with subject");
                if (getLogger().isLoggable(Level.FINE)) {
                    message.append(loginContext.getSubject());
                }
                message.append(".");

                getLogger().info(message.toString());
                Subject.doAsPrivileged(loginContext.getSubject(), new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        initAsSubject();
                        return null;
                    }
                }, null);
            }
        }
    }

    /**
     * Returns the exporter for exporting the server.
     * 
     * @throws ConfigurationException
     *             if a problem occurs getting the exporter from the
     *             configuration
     * @throws RemoteException
     *             if a remote communication problem occurs
     */
    protected Exporter getExporter() throws ConfigurationException, RemoteException {
        return (Exporter) config.getEntry(this.getClass().getName(), "exporter", Exporter.class, new BasicJeriExporter(
            TcpServerEndpoint.getInstance(0), new BasicILFactory()));
    }

    protected abstract Object export(Exporter exporter) throws ExportException;

    protected abstract Entry[] getFixedServiceEntries();

    /**
     * Returns the service ID for this server.
     * 
     * @throws IOException
     */
    protected ServiceID getServiceID() throws IOException {
        if (this.serviceId == null) {
            this.serviceId = createServiceID(this.directory);
        }
        return this.serviceId;
    }

    /**
     * @return Returns the file suffix for a pending write.
     */
    public abstract String getFileSuffixWritePending();

    protected EntryID newEntryID() {
        return new EntryID(UUID.randomUUID());
    }

    protected void initFromDirectory() throws IOException, ClassNotFoundException {
        File serviceIdFile = new File(this.directory, "ServiceID.oos");
        if (serviceIdFile.exists()) {
            FileInputStream fis = new FileInputStream(serviceIdFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = oisGetter.getObjectInputStream(bis);
            this.serviceId = (ServiceID) ois.readObject();
        } else {
            this.getServiceID();
        }

        rollbackUncommittedChanges();
        initEntryMetaInfo();
    }

    void rollbackUncommittedChanges() throws IOException {
        File[] files = this.directory.listFiles(new FileFilter() {

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
                    String newName = currentName.substring(0, currentName.lastIndexOf(getFileSuffixWritePending()))
                        .concat(getFileSuffix());
                    files[i].renameTo(new File(files[i].getParentFile(), newName));

                }
            }
        }
        files = this.directory.listFiles(new FileFilter() {
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
        ObjectInputStream ois = oisGetter.getObjectInputStream(bis);
        Object obj = (I_EncodeBusinessProcess) ois.readObject();
        ois.close();
        return obj;
    }

    /**
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoMatchingEntryException
     * @see org.dwfa.bpa.process.I_QueueProcesses#read(net.jini.id.Uuid,
     *      net.jini.core.transaction.Transaction)
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
        ObjectInputStream ois = oisGetter.getObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }

    /**
     * @see org.dwfa.bpa.process.I_QueueProcesses#getProcessMetaData(org.dwfa.bpa.process.I_SelectProcesses)
     */
    public Collection<T> getMetaData(I_SelectObjects selector) throws RemoteException {
        if (selector == null) {
            return new ArrayList<T>(this.objectInfoSortedSet);
        }
        Collection<T> returnValues = new ArrayList<T>();
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
     * @see org.dwfa.bpa.process.I_QueueProcesses#take(net.jini.id.Uuid,
     *      net.jini.core.transaction.Transaction)
     */
    public Object take(EntryID entryID, Transaction t) throws TransactionException, IOException,
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

            File processFile = files[0];
            processFile = startTake(processFile);
            FileInputStream fis = new FileInputStream(processFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = oisGetter.getObjectInputStream(bis);
            Object obj = ois.readObject();
            ois.close();
            T objDesc = getObjectDescription(obj, entryID);

            this.uncommittedTakes.add(objDesc);
            this.objectInfoSortedSet.remove(objDesc);
            addTakeToTransaction(objDesc, this.objectInfoSortedSet, this.uncommittedTakes, processFile,
                (ServerTransaction) t, this);
            return obj;
        } catch (FileNotFoundException ex) {
            throw new NoMatchingEntryException(ex.toString());
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_QueueProcesses#write(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.EntryID, net.jini.core.transaction.Transaction)
     */
    public void write(T object, EntryID entryID, Transaction t) throws RemoteException, IOException,
            TransactionException {
        if (t == null) {
            write(object, entryID);
        } else {
            File objectFile = new File(this.directory, object.getObjectID() + "." + entryID
                + getFileSuffixWritePending());
            FileOutputStream fos = new FileOutputStream(objectFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.close();
            this.addWriteToTransaction(getObjectDescription(object, entryID), this.objectInfoSortedSet, objectFile,
                (ServerTransaction) t, this);
        }
    }

    public void write(T object, EntryID entryID) throws RemoteException, IOException, TransactionException {
        File objectFile = new File(this.directory, object.getObjectID() + "." + entryID + getFileSuffix());
        FileOutputStream fos = new FileOutputStream(objectFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.close();
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
     * @see org.dwfa.bpa.process.I_QueueProcesses#writeThenTake(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      net.jini.core.transaction.Transaction,
     *      net.jini.core.transaction.Transaction)
     */
    public EntryID writeThenTake(T object, Transaction writeTran, Transaction takeTran) throws RemoteException,
            IOException, TransactionException {
        EntryID entryID = newEntryID();
        File processFile = new File(this.directory, object.getObjectID() + "." + entryID + getFileSuffixTakePending());
        FileOutputStream fos = new FileOutputStream(processFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.close();
        writeLogEntry(processFile);
        T objectDesc = getObjectDescription(object, entryID);
        this.addWriteThenTakeToTransaction(objectDesc, this.objectInfoSortedSet, processFile,
            (ServerTransaction) writeTran, takeTran);
        this.uncommittedTakes.add(objectDesc);
        this.addTakeToTransaction(objectDesc, this.objectInfoSortedSet, this.uncommittedTakes, processFile,
            (ServerTransaction) takeTran, this);
        return entryID;
    }

    /**
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws TransactionException
     * @see org.dwfa.bpa.process.I_QueueProcesses#take(org.dwfa.bpa.process.I_SelectProcesses,
     *      net.jini.core.transaction.Transaction)
     */
    public Object take(I_SelectObjects selector, Transaction t) throws TransactionException, IOException,
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

    public Object read(I_SelectObjects selector, Transaction t) throws TransactionException, IOException,
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

    protected abstract T getObjectDescription(Object obj, EntryID entryID);

    /**
     * @throws IOException
     * @throws ClassNotFoundException
     * @see org.dwfa.bpa.process.I_QueueProcesses#take(net.jini.id.Uuid,
     *      net.jini.core.transaction.Transaction)
     */
    public Object take(UUID objectID, Transaction t) throws TransactionException, IOException, ClassNotFoundException,
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

                File objectFile = files[0];

                EntryID entryID = getEntryID(objectFile);
                objectFile = startTake(objectFile);
                FileInputStream fis = new FileInputStream(objectFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = oisGetter.getObjectInputStream(bis);
                Object obj = ois.readObject();
                ois.close();
                T desc = this.getObjectDescription(obj, entryID);
                this.uncommittedTakes.add(desc);
                this.objectInfoSortedSet.remove(desc);
                this.addTakeToTransaction(desc, this.objectInfoSortedSet, this.uncommittedTakes, objectFile,
                    (ServerTransaction) t, this);
                return obj;
            }
        } catch (FileNotFoundException ex) {
            throw new NoMatchingEntryException(ex.toString());
        }
    }

    private Object take(UUID objectID) throws TransactionException, IOException, ClassNotFoundException,
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
            ObjectInputStream ois = oisGetter.getObjectInputStream(bis);
            Object obj = ois.readObject();
            ois.close();
            T desc = this.getObjectDescription(obj, entryID);
            this.objectInfoSortedSet.remove(desc);
            objectFile.delete();
            return obj;
        } catch (FileNotFoundException ex) {
            throw new NoMatchingEntryException(ex.toString());
        }
    }

    public void hide(EntryID entryID, Transaction t) throws RemoteException, IOException, ClassNotFoundException,
            TransactionException, NoMatchingEntryException {
        try {

            File[] files = this.directory.listFiles(new MatchEntryID(entryID));
            if (files == null) {
                throw new NoMatchingEntryException("No matching files for entryID: " + entryID);
            }
            if (files.length != 1) {
                throw new NoMatchingEntryException("Found " + files.length + " matching files for entryID: " + entryID);
            }

            File objFile = files[0];
            objFile = startTake(objFile);
            FileInputStream fis = new FileInputStream(objFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = oisGetter.getObjectInputStream(bis);
            Object obj = ois.readObject();
            ois.close();
            T objDesc = this.getObjectDescription(obj, entryID);
            this.uncommittedTakes.add(objDesc);
            this.objectInfoSortedSet.remove(objDesc);
            addHideToTransaction(objDesc, this.objectInfoSortedSet, this.uncommittedTakes, objFile,
                (ServerTransaction) t, this);
        } catch (FileNotFoundException ex) {
            throw new NoMatchingEntryException(ex.toString());
        }
    }

    private void addTakeToTransaction(T objDesc, SortedSet<T> name, Set<T> name2, File processFile,
            ServerTransaction st, ObjectServerCore core) throws TransactionException, UnknownTransactionException,
            CannotJoinException, CrashCountException, RemoteException {
        if (st == null) {
            return;
        }
        TakeTransactionPart part = new TakeTransactionPart<T>(objDesc, objectInfoSortedSet, uncommittedTakes,
            processFile, this, this);
        TransactionParticipantAggregator.addTransactionPart(st, part);
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().fine("addTakeToTransaction");
        }

    }

    private void addHideToTransaction(T objDesc, SortedSet<T> name, Set<T> name2, File processFile,
            ServerTransaction st, ObjectServerCore core) throws TransactionException, UnknownTransactionException,
            CannotJoinException, CrashCountException, RemoteException {
        if (st == null) {
            return;
        }
        HideTransactionPart part = new HideTransactionPart<T>(objDesc, objectInfoSortedSet, uncommittedTakes,
            processFile, this, this);
        TransactionParticipantAggregator.addTransactionPart(st, part);
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().fine("addHideTransactionPart");
        }

    }

    private void addWriteThenTakeToTransaction(T objectDesc, SortedSet<T> name, File processFile,
            ServerTransaction writeTransaction, Transaction takeTran) throws TransactionException,
            UnknownTransactionException, CannotJoinException, CrashCountException, RemoteException {
        if (writeTransaction == null) {
            return;
        }
        WriteThenTakeTransactionPart part = new WriteThenTakeTransactionPart<T>(objectDesc, objectInfoSortedSet,
            processFile, takeTran, this);
        TransactionParticipantAggregator.addTransactionPart(writeTransaction, part);
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().fine("addWriteThenTakeToTransaction");
        }

    }

    private void addWriteToTransaction(T object, SortedSet<T> name, File processFile, ServerTransaction st,
            ObjectServerCore core) throws TransactionException, UnknownTransactionException, CannotJoinException,
            CrashCountException, RemoteException {
        if (st == null) {
            return;
        }
        WriteTransactionPart part = new WriteTransactionPart<T>(object, objectInfoSortedSet, processFile, this, this);
        TransactionParticipantAggregator.addTransactionPart(st, part);
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().fine("addQueueWriteToTransaction");
        }

    }

    /**
     * 
     */
    protected synchronized void initEntryMetaInfo() {
        this.objectInfoSortedSet = Collections.synchronizedSortedSet(new TreeSet<T>(this.nativeComparator));
        File[] files = this.directory.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.getName().endsWith(getFileSuffix());
            }
        });
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                try {
                    FileInputStream fis = new FileInputStream(files[i]);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = oisGetter.getObjectInputStream(bis);
                    Object obj = ois.readObject();
                    ois.close();
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
                } catch (Exception ex) {
                    getLogger().log(Level.SEVERE,
                        "Exception processing file: " + files[i] + " Message: " + ex.getMessage(), ex);
                }
            }
        }
    }

    public abstract void actionPerformed(ActionEvent evt);

    /**
     * Initializes the server, assuming that the appropriate subject is in
     * effect.
     */
    protected void initAsSubject() throws Exception {
        /* Export the server */
        Exporter exporter = getExporter();

        /* Create the smart proxy */
        Object smartProxy = export(exporter);

        /* Get the discovery manager, for discovering lookup services */
        DiscoveryManagement discoveryManager;
        try {
            discoveryManager = (DiscoveryManagement) config.getEntry(this.getClass().getName(), "discoveryManager",
                DiscoveryManagement.class);
        } catch (NoSuchEntryException e) {
            getLogger().warning("No entry for discoveryManager in config file. " + e.toString());
            String[] groups = (String[]) config.getEntry(this.getClass().getName(), "groups", String[].class);
            discoveryManager = new LookupDiscovery(groups, config);
        }

        Entry[] entries = (Entry[]) this.config.getEntry(this.getClass().getName(), "entries", Entry[].class,
            new Entry[] {});

        /* Get the join manager, for joining lookup services */
        joinManager = new JoinManager(smartProxy, entries, getServiceID(), discoveryManager, null /* leaseMgr */,
            config);

        Entry[] moreEntries = getFixedServiceEntries();
        joinManager.addAttributes(moreEntries);

        /*
         * Boolean publishLocalProxy = (Boolean) this.config.getEntry(this
         * .getClass().getName(), "publishLocalProxy", Boolean.class,
         * new Boolean(false));
         * if (publishLocalProxy.booleanValue()) {
         */
        ArrayList<Entry> entryList = new ArrayList<Entry>(Arrays.asList(entries));
        entryList.addAll(Arrays.asList(moreEntries));
        ListIterator<Entry> itr = entryList.listIterator();
        while (itr.hasNext()) {
            Entry entry = itr.next();
            if (Name.class.isAssignableFrom(entry.getClass())) {
                Name oldName = (Name) entry;
                Name newName = new Name(oldName.name + " (local)");
                itr.remove();
                itr.add(newName);
                break;
            }

        }

        ServiceItem serviceItem = new ServiceItem(this.getServiceID(), smartProxy,
            (Entry[]) entryList.toArray(new Entry[entryList.size()]));
        LookupJiniAndLocal.addToLocalServices(serviceItem);
        /*
         * }
         */
    }

    /**
     * @return Returns the logger.
     */
    protected abstract Logger getLogger();

    /**
     * @see org.dwfa.bpa.process.I_QueueProcesses#write(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      net.jini.core.transaction.Transaction)
     */
    public EntryID write(T obj, Transaction t) throws RemoteException, IOException, TransactionException {
        EntryID entryID = newEntryID();
        this.write(obj, entryID, t);
        return entryID;
    }
}
