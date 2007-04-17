/*
 * Created on Mar 27, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.ws;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

public abstract class WaitForCancelOrComplete extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private transient Exception ex;

    protected transient Condition exitCondition;

    protected transient ActionListener cancelActionListener;

    protected transient ActionListener completeActionListener;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }

    class CancelActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            exitCondition = Condition.ITEM_CANCELED;
            synchronized(WaitForCancelOrComplete.this) {
                WaitForCancelOrComplete.this.notifyAll();
            }

        }

    }

    class CompleteActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            exitCondition = Condition.ITEM_COMPLETE;
            synchronized(WaitForCancelOrComplete.this) {
                WaitForCancelOrComplete.this.notifyAll();
            }

        }

    }
    
    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public synchronized Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
    	worker.getCurrentWorkspace().setStatusMessage("<html><font color='red'>Waiting for user input.");
        evaluateStart(process, worker);
        I_Workspace ws = worker.getCurrentWorkspace();
        ws.setWorkspaceVisible(true);
        ex = null;
        this.exitCondition = null;
        this.cancelActionListener = new CancelActionListener();
        this.completeActionListener = new CompleteActionListener();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
    
                public void run() {
                    try {
                        addListeners(worker);
                    } catch (Exception e) {
                        ex = e;
                    }
                }
    
    
            });
            if (ex != null) {
                throw new TaskFailedException(ex);
            }
            this.waitTillDone(worker.getLogger());
            SwingUtilities.invokeAndWait(new Runnable() {
    
                public void run() {
                    try {
                        removeListeners(worker);
                    } catch (Exception e) {
                        ex = e;
                    }
                }
    
            });
            if (ex != null) {
                throw new TaskFailedException(ex);
            }
    
        } catch (InterruptedException ex2) {
            worker.getLogger().log(Level.SEVERE, ex2.getMessage(), ex2);
        } catch (InvocationTargetException e1) {
            throw new TaskFailedException(e1);
        }
    	worker.getCurrentWorkspace().setStatusMessage("");
        evaluateAfterAction(process, worker, exitCondition);
        return this.exitCondition;
    }

    public abstract void evaluateStart(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException;
    public abstract void evaluateAfterAction(I_EncodeBusinessProcess process, I_Work worker, Condition exitCondition)  throws TaskFailedException;

    protected abstract void addListeners(I_Work worker) throws TaskFailedException;
    protected abstract void removeListeners(I_Work worker) throws TaskFailedException;

    private void waitTillDone(Logger l) {
        while (!this.isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                l.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    
    }

    public boolean isDone() {
        return this.exitCondition != null;
    }


    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[0];
    }

    public WaitForCancelOrComplete() {
        super();
    }

}
