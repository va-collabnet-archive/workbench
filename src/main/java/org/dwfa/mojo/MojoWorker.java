/*
 * Created on Apr 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.mojo;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

import org.dwfa.bpa.gui.I_ManageUserTransactions;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoSuchWorkspaceException;
import org.dwfa.bpa.process.WorkspaceActiveException;
import org.dwfa.bpa.worker.Worker;


public class MojoWorker extends Worker {

    /**
     * @param config
     * @param id
     * @param desc
     * @throws ConfigurationException
     * @throws LoginException 
     * @throws IOException 
     */
    public MojoWorker(Configuration config, UUID id, String desc) 
    throws ConfigurationException, LoginException, IOException, PrivilegedActionException {
        super(config, id, desc);
        
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#isWorkspaceActive(net.jini.id.UUID)
     */
    public boolean isWorkspaceActive(UUID workspaceId) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#createWorkspace(net.jini.id.UUID, java.lang.String, org.dwfa.bpa.gui.TerminologyConfiguration)
     */
    public I_Workspace createWorkspace(UUID workspaceId, String name, File menuDir) throws WorkspaceActiveException,
            HeadlessException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkspace(net.jini.id.UUID)
     */
    public I_Workspace getWorkspace(UUID workspaceId)
            throws NoSuchWorkspaceException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getCurrentWorkspace()
     */
    public I_Workspace getCurrentWorkspace() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#setCurrentWorkspace(org.dwfa.bpa.process.I_Workspace)
     */
    public void setCurrentWorkspace(I_Workspace workspace) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkspaces()
     */
    public Collection<I_Workspace> getWorkspaces() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#selectFromList(java.lang.Object[], java.lang.String, java.lang.String)
     */
    public Object selectFromList(Object[] list, String title,
            String instructions) {
        throw new UnsupportedOperationException();
    }
    

    /**
     * @see org.dwfa.bpa.process.I_Work#createHeadlessWorkspace(net.jini.id.UUID, org.dwfa.bpa.gui.TerminologyConfiguration)
     */
    public I_Workspace createHeadlessWorkspace(UUID workspace_id) throws WorkspaceActiveException, HeadlessException {
        throw new UnsupportedOperationException();
    }
    public I_Workspace createWorkspace(UUID arg0, String arg1, I_ManageUserTransactions arg2, File menuDir) throws WorkspaceActiveException, Exception {
        throw new UnsupportedOperationException();
    }

	public Object getObjFromFilesystem(Frame arg0, String arg1, String arg2, FilenameFilter arg3) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException();
	}

	public void writeObjToFilesystem(Frame arg0, String arg1, String arg2, String arg3, Object arg4) throws IOException {
        throw new UnsupportedOperationException();
	}

	public I_Work getTransactionIndependentClone() throws LoginException, ConfigurationException, IOException, PrivilegedActionException {
		throw new UnsupportedOperationException();
	}

}//End class MojoWorker
