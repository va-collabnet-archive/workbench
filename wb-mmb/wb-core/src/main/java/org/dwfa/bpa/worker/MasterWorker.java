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
 * Created on Mar 21, 2005
 */
package org.dwfa.bpa.worker;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.security.auth.login.LoginException;
import javax.swing.JFrame;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.transaction.TransactionException;

import org.dwfa.bpa.gui.GridBagPanel;
import org.dwfa.bpa.gui.I_ManageUserTransactions;
import org.dwfa.bpa.gui.WorkspaceFrame;
import org.dwfa.bpa.gui.WorkspacePanel;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoSuchWorkspaceException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.process.WorkspaceActiveException;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.bpa.util.SelectObjectDialog;
import org.dwfa.jini.SelectServiceDialog;
import org.dwfa.util.io.FileIO;

/**
 * @author kec TODO allow support for multiple master workers sharing the same
 *         workspace, but have independent transactions. Use case based on
 *         process based menu workers that may be invoked while another
 *         transaction is in process. Perhaps a master worker can have a
 *         subordinate worker to run such processes. This issue is now
 *         partially/completely(?) addressed by adding the ability for a worker
 *         to clone itself, and overriding the execute method to defer to a
 *         clone if this worker is already executing. Also the workspaces are
 *         now static, so all the clones share the same workspace.
 */
public class MasterWorker extends Worker {

    private static Map<UUID, I_Workspace> workspaces = new HashMap<UUID, I_Workspace>();

    private I_Workspace currentWorkspace;

    private boolean clonable = true;

    /**
     * @return Returns the currentWorkspace.
     */
    public I_Workspace getCurrentWorkspace() {
        return currentWorkspace;
    }

    /**
     * @param currentWorkspace
     *            The currentWorkspace to set.
     */
    public void setCurrentWorkspace(I_Workspace currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }

    /**
     * @param config
     * @param id
     * @param desc
     * @throws ConfigurationException
     * @throws LoginException
     * @throws IOException
     */
    public MasterWorker(Configuration config, UUID id, String desc) throws ConfigurationException, LoginException,
            IOException, PrivilegedActionException {
        super(config, id, desc);
        init(config, id, desc);
    }

    public MasterWorker(Configuration config) throws ConfigurationException, LoginException, IOException,
            PrivilegedActionException {
        super(config, MasterWorker.class);
        init(config, id, desc);
    }

    private void init(Configuration config, UUID id, String desc) throws ConfigurationException {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Created new worker: " + desc + " id: " + id);
        }
        clonable = (Boolean) config.getEntry(this.getClass().getName(), "clonable", Boolean.class, Boolean.TRUE);

        executeStartupProcesses();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#isWorkspaceActive(UUID)
     */
    public boolean isWorkspaceActive(UUID workspaceId) {
        return workspaces.containsKey(workspaceId);
    }

    /**
     * @throws WorkspaceActiveException
     * @throws QueryException
     * @throws HeadlessException
     * @see org.dwfa.bpa.process.I_Work#createWorkspace(UUID, java.lang.String,
     *      org.dwfa.bpa.gui.TerminologyConfiguration)
     */
    public I_Workspace createWorkspace(UUID workspaceId, String title, File menuDir) throws Exception {
        return this.createWorkspace(workspaceId, title, null, menuDir);
    }

    public I_Workspace createWorkspace(UUID workspaceId, String title, I_ManageUserTransactions transactionInterface,
            File menuDir) throws Exception {
        if (workspaces.containsKey(workspaceId)) {
            throw new WorkspaceActiveException();
        }
        WorkspaceFrame frame = new WorkspaceFrame(title, transactionInterface, menuDir, this);
        frame.getWorkspace().setName(title);
        frame.getWorkspace().setId(workspaceId);
        workspaces.put(workspaceId, frame.getWorkspace());
        this.setCurrentWorkspace(frame.getWorkspace());
        return frame.getWorkspace();
    }

    public I_Workspace createHeadlessWorkspace(UUID workspaceId) throws WorkspaceActiveException, HeadlessException,
            TransactionException {
        if (workspaces.containsKey(workspaceId)) {
            throw new WorkspaceActiveException();
        }
        WorkspacePanel workspace = new WorkspacePanel(new ArrayList<GridBagPanel>(), null);
        workspaces.put(workspaceId, workspace);
        this.setCurrentWorkspace(workspace);
        return workspace;
    }

    /**
     * @throws TaskFailedException
     * @see org.dwfa.bpa.process.I_Work#selectService(net.jini.core.lookup.ServiceItem[])
     */
    public Object selectFromList(Object[] list, String title, String labelText) throws TaskFailedException {
        if (list == null || list.length == 0) {
            throw new TaskFailedException("item list is null or zero length: " + list);
        }
        Iterator<JFrame> frameItr = OpenFrames.getFrames().iterator();
        JFrame activeFrame = null;
        while (frameItr.hasNext()) {
            JFrame aFrame = frameItr.next();
            if (aFrame.isActive()) {
                activeFrame = aFrame;
                break;
            }
        }
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Prompting user to select from list: " + labelText);
        }
        if (ServiceItem[].class.isAssignableFrom(list.getClass())) {
            return SelectServiceDialog.showDialog(activeFrame, activeFrame, labelText, title, (ServiceItem[]) list,
                null, null);
        } else {
            return SelectObjectDialog.showDialog(activeFrame, activeFrame, labelText, title, list, null, null);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkspace(UUID)
     */
    public I_Workspace getWorkspace(UUID workspaceId) throws NoSuchWorkspaceException {
        this.setCurrentWorkspace(workspaces.get(workspaceId));
        return workspaces.get(workspaceId);
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkspaces()
     */
    public Collection<I_Workspace> getWorkspaces() {
        return Collections.unmodifiableCollection(workspaces.values());
    }

    public Object getObjFromFilesystem(Frame parent, String title, String startDir, FilenameFilter fileFilter)
            throws IOException, ClassNotFoundException {
        return FileIO.getObjFromFilesystem(parent, title, startDir, fileFilter).getObj();
    }

    public void writeObjToFilesystem(Frame parent, String title, String startDir, String defaultFile, Object obj)
            throws IOException {
        FileIO.writeObjToFilesystem(parent, title, startDir, defaultFile, obj);
    }

    public Condition execute(I_EncodeBusinessProcess process) throws TaskFailedException {
        Condition condition = null;
        if (isExecuting()) {
            throw new TaskFailedException("This Worker is executing another process. " + this.getWorkerDescWithId());
        } else {
            condition = super.execute(process);
        }
        return condition;
    }

    public I_Work getTransactionIndependentClone() throws LoginException, ConfigurationException, IOException,
            PrivilegedActionException {
        if (clonable) {
            MasterWorker newClone = new MasterWorker(config);
            newClone.setWorkerDesc(newClone.getWorkerDesc() + " transaction independent clone.");
            newClone.setCurrentWorkspace(this.getCurrentWorkspace());
            return newClone;
        }
        throw new ConfigurationException("clone not supported for this worker: " + this.getWorkerDescWithId());

    }

}
