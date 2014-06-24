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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import javax.naming.ConfigurationException;
import javax.security.auth.login.LoginException;

import javax.swing.JFrame;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.bpa.util.SelectObjectDialog;
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

    /**
     * @param config
     * @param id
     * @param desc
     * @throws ConfigurationException
     * @throws LoginException
     * @throws IOException
     */
    public MasterWorker(UUID id, String desc) throws IOException {
        super(id, desc);
        init(id, desc);
    }

    private void init(UUID id, String desc) {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "Created new worker: {0} id: {1}", new Object[]{desc, id});
        }
        executeStartupProcesses();
    }

    /**
     * @throws TaskFailedException
     * @see org.dwfa.bpa.process.I_Work#selectService(net.jini.core.lookup.ServiceItem[])
     */
    @Override
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
            logger.log(Level.FINER, "Prompting user to select from list: {0}", labelText);
        }
        return SelectObjectDialog.showDialog(activeFrame, activeFrame, labelText, title, list, null, null);
        
    }

    @Override
    public Object getObjFromFilesystem(Frame parent, String title, String startDir, FilenameFilter fileFilter)
            throws IOException, ClassNotFoundException {
        return FileIO.getObjFromFilesystem(parent, title, startDir, fileFilter).getObj();
    }

    @Override
    public void writeObjToFilesystem(Frame parent, String title, String startDir, String defaultFile, Object obj)
            throws IOException {
        FileIO.writeObjToFilesystem(parent, title, startDir, defaultFile, obj);
    }

    @Override
    public Condition execute(I_EncodeBusinessProcess process) throws TaskFailedException {
        Condition condition = null;
        if (isExecuting()) {
            throw new TaskFailedException("This Worker is executing another process. " + this.getWorkerDescWithId());
        } else {
            condition = super.execute(process);
        }
        return condition;
    }

    @Override
    public I_Work getTransactionIndependentClone() throws IOException {
        
        return new MasterWorker(UUID.randomUUID(), this.getWorkerDesc() + " transaction independent clone.");
        
    }

}
