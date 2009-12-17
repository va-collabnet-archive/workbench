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
/**
 * 
 */
package org.dwfa.bpa.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.util.LogWithAlerts;

public class ProcessMenuActionListener implements ActionListener {
    private class MenuProcessThread implements Runnable {

        private String action;

        /**
         * @param action
         */
        public MenuProcessThread(String action) {
            super();
            // TODO Auto-generated constructor stub
            this.action = action;
        }

        public void run() {
            I_Work worker = workspaceFrame.getWorker();
            try {
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(processFile)));
                I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();
                ois.close();
                if (worker.isExecuting()) {
                    worker = worker.getTransactionIndependentClone();
                }
                process.execute(worker);
                worker.commitTransactionIfActive();
            } catch (Exception ex) {

                worker.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "<html>Exception processing action: "
                    + action + "<p><p>" + ex.getMessage() + "<p><p>See log for details.");
            }
        }
    };

    private File processFile;
    private WorkspaceFrame workspaceFrame;

    public ProcessMenuActionListener(File processFile, WorkspaceFrame workspaceFrame) {
        super();
        this.processFile = processFile;
        this.workspaceFrame = workspaceFrame;
    }

    public void actionPerformed(ActionEvent e) {
        new Thread(new MenuProcessThread(e.getActionCommand()), "Menu Process Execution").start();
    }
}
