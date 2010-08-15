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
 * Created on Apr 19, 2005
 */
package org.dwfa.bpa.tasks.pwdbreaker;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.TransactionException;
import net.jini.lookup.ServiceItemFilter;
import net.jini.space.JavaSpace05;

import org.dwfa.bpa.gui.FieldInputPanel;
import org.dwfa.bpa.gui.InstructionPanel;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.GenericTaskEntry;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/grid/pwdbreaker", type = BeanType.TASK_BEAN) })
public class BreakPassword extends AbstractTask {

    private transient JavaSpace05 space;

    private transient int lowmark;

    private transient int highmark;

    private transient int triesPerTask;

    private transient int waterlevel;

    private transient long startTime;

    private transient String salt;

    private transient String unencrypted;

    private transient UUID masterId;

    private transient Condition exitCondition;

    private transient String perfStr;

    private transient String waterMarkStr;

    private transient String answer;

    private transient String tries;

    private transient String encrypted;

    private transient InstructionPanel instruction;

    private transient DecimalFormat decimalFormatter;

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    private void informUser() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                StringBuffer buff = new StringBuffer();
                buff.append("<html>");
                buff.append(tries);
                buff.append("<br>");
                buff.append(perfStr);
                buff.append("<br>");
                buff.append(waterMarkStr);
                buff.append("<br>Encrypted String: ");
                buff.append(encrypted);
                buff.append("<br>Broken password: ");
                if (answer != null) {
                    buff.append("<font color='red'>");
                    buff.append(answer);
                } else {
                    buff.append("<font color='blue'>");
                    buff.append("pending");
                }
                instruction.setInstruction(buff.toString());
            }

        });

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public synchronized Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            this.exitCondition = null;
            this.waterlevel = 0;
            this.answer = null;
            I_Workspace workspace = worker.getCurrentWorkspace();
            workspace.setStatusMessage("<html><font color='red'>Breaking password...");
            FieldInputPanel fieldInputPanel = (FieldInputPanel) workspace.getPanel(ConfigureCryptBreakerWorkspace.INPUT);
            Map<String, String> fieldMap = fieldInputPanel.getFields();
            this.salt = fieldMap.get("Salt:");
            this.unencrypted = fieldMap.get("Word:");
            if (unencrypted.length() != 4) {
                throw new TaskFailedException("Password must be four characters");
            }

            // JCrypt expects 8 chars
            unencrypted = unencrypted + "!!!!";

            String triesPerTaskStr = fieldMap.get("Tries per task:");
            String highMarkStr = fieldMap.get("High mark:");
            String lowMarkStr = fieldMap.get("Low mark:");
            this.triesPerTask = Integer.parseInt(triesPerTaskStr);
            this.highmark = Integer.parseInt(highMarkStr);
            this.lowmark = Integer.parseInt(lowMarkStr);
            this.masterId = UUID.randomUUID();

            instruction = (InstructionPanel) workspace.getPanel(ConfigureCryptBreakerWorkspace.INSTRUCTION);

            ServiceTemplate tmpl = new ServiceTemplate(null, new Class[] { JavaSpace05.class }, null);
            ServiceItemFilter filter = null;
            long waitDur = 1000 * 60 * 3;
            ServiceItem service = worker.lookup(tmpl, filter, waitDur);
            this.decimalFormatter = new DecimalFormat("#,##0");
            space = (JavaSpace05) service.service;
            GenerateThread gThread = new GenerateThread();
            gThread.start();
            CollectThread cThread = new CollectThread();
            cThread.start();
            waitTillDone();
            informUser();
            workspace.setStatusMessage("<html><font color='blue'>Password broken...");
            return Condition.CONTINUE;
        } catch (TaskFailedException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new TaskFailedException(ex);
        }
    }

    private void waitTillDone() {
        while (!this.isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public boolean isDone() {
        return this.exitCondition != null;
    }

    protected void writeTask(GenericTaskEntry task) throws RemoteException, TransactionException {
        space.write(task, null, Lease.FOREVER);
    }

    protected Entry takeTask(Entry template) throws RemoteException, UnusableEntryException, TransactionException,
            InterruptedException {
        Entry result = (Entry) space.take(template, null, Long.MAX_VALUE);
        return result;
    }

    private class GenerateThread extends Thread {
        public void run() {
            try {
                generateTasks();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private class CollectThread extends Thread {
        public void run() {
            try {
                collectResults();
                exitCondition = Condition.CONTINUE;
                synchronized (BreakPassword.this) {
                    BreakPassword.this.notifyAll();
                }
            } catch (Exception e) {
                getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void generateTasks() throws RemoteException, TransactionException {

        startTime = System.currentTimeMillis();

        encrypted = JCrypt.crypt(salt, unencrypted);

        byte[] testWord = getFirstWord();

        for (;;) {
            waitForLowWaterMark(lowmark);

            while (waterlevel < highmark) {
                if (testWord[1] != salt.charAt(1)) {
                    return;
                }
                CryptTask task = new CryptTask(new Integer(triesPerTask), testWord, encrypted, masterId);
                if (getLogger().isLoggable(Level.FINE)) {
                    getLogger().fine("Writing task");
                }
                writeTask(task);
                changeWaterLevel(1);
                for (int i = 0; i < triesPerTask; i++) {
                    CryptTask.nextWord(testWord);
                }
            }
        }
    }

    private byte[] getFirstWord() {
        byte[] word = new byte[2 + 8];
        word[0] = (byte) salt.charAt(0);
        word[1] = (byte) salt.charAt(1);

        for (int i = 2; i < 6; i++) {
            // lowest printable char
            word[i] = (byte) ' ';
        }

        word[6] = (byte) '!';
        word[7] = (byte) '!';
        word[8] = (byte) '!';
        word[9] = (byte) '!';

        return word;
    }

    public void collectResults() throws RemoteException, UnusableEntryException, TransactionException,
            InterruptedException {
        int count = 0;
        Entry template;

        try {
            template = space.snapshot(new CryptResult(null, masterId));
        } catch (RemoteException e) {
            throw new RuntimeException("Can't create a snapshot");
        }

        for (;;) {
            CryptResult result = (CryptResult) takeTask(template);
            if (result != null) {
                count++;
                tries = "Tried " + this.decimalFormatter.format(count * triesPerTask) + " words";
                updatePerformance(count * triesPerTask);
                changeWaterLevel(-1);
                if (result.word != null) {
                    String word = CryptTask.getPrintableWord(result.word);
                    answer = "'" + word.substring(2, 6) + "'";
                    if (getLogger().isLoggable(Level.INFO)) {
                        getLogger().info("Found answer: " + answer + ". Adding poison.");
                    }
                    informUser();
                    addPoison();
                    return;
                }
            }
        }
    }

    private void addPoison() {
        PoisonPill poison = new PoisonPill(masterId);
        try {
            space.write(poison, null, 60 * 1000 * 5);
        } catch (Exception e) {
            // ignore, leases will eventually clear space
            if (getLogger().isLoggable(Level.WARNING)) {
                getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void updatePerformance(long wordsTried) {
        long now = System.currentTimeMillis();
        double elapsedTime = now - startTime;

        if (elapsedTime > 0) {
            double wordRate = wordsTried / (elapsedTime / 1000);
            this.perfStr = decimalFormatter.format(wordRate) + " words per second";
        } else {
            this.perfStr = wordsTried + " words in < 1 ms";
        }
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().fine(this.perfStr);
        }
        informUser();
    }

    private synchronized void changeWaterLevel(int delta) {
        waterlevel += delta;
        this.waterMarkStr = "Waterlevel is at " + waterlevel + " tasks";
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().fine(this.waterMarkStr);
        }
        informUser();
        notifyAll();
    }

    synchronized void waitForLowWaterMark(int level) {
        while (waterlevel > level) {
            try {
                wait();
            } catch (InterruptedException e) {
                ; // continue
            }
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
