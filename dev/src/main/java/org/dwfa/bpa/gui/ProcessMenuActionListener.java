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
            try {
                ObjectInputStream ois = new ObjectInputStream(
                        new BufferedInputStream(new FileInputStream(
                                processFile)));
                I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois
                        .readObject();
                ois.close();
                process.execute(worker);
                worker.commitTransactionIfActive();
            } catch (Exception ex) {

                worker.getLogger().log(Level.SEVERE, ex.getMessage(),
                        ex);
                JOptionPane.showMessageDialog(null, "<html>Exception processing action: " + 
                        action + "<p><p>" + 
                        ex.getMessage() + "<p><p>See log for details.");
            }
        }
    };
    private File processFile;
    private I_Work worker;

    public ProcessMenuActionListener(File processFile, I_Work worker) {
        super();
        this.processFile = processFile;
        this.worker = worker;
    }

    public void actionPerformed(ActionEvent e) {
        new Thread(new MenuProcessThread(e.getActionCommand()), "Menu Process Execution").start();
    }
}