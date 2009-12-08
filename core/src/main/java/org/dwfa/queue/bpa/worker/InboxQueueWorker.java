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
 * Created on Jun 7, 2005
 */
package org.dwfa.queue.bpa.worker;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.login.LoginException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.transaction.Transaction;

import org.apache.geronimo.javamail.store.pop3.POP3Folder;
import org.dwfa.bpa.gui.I_ManageUserTransactions;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoSuchWorkspaceException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.process.WorkspaceActiveException;
import org.dwfa.bpa.util.Base64;
import org.dwfa.bpa.util.FrameWithOpenFramesListener;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.bpa.worker.Worker;
import org.dwfa.bpa.worker.task.I_GetWorkFromQueue;

/**
 * @author kec
 * 
 */
public class InboxQueueWorker extends Worker implements I_GetWorkFromQueue, Runnable {
    private I_QueueProcesses queue;

    private Thread workerThread;

    private boolean sleeping;

    private long sleepTime = 1000 * 60 * 1;

    private Properties props;
    private Authenticator a = null;
    private String username;
    private String mailHost;

    public class MailAuthenticator extends Authenticator {

        private FrameWithOpenFramesListener passwordDialog;
        private JLabel mainLabel = new JLabel("Please enter the password for this inbox POP account: ");
        private JLabel userLabel = new JLabel("User name: ");
        private JLabel passwordLabel = new JLabel("Password: ");
        private JTextField usernameField = new JTextField(20);
        private JPasswordField passwordField = new JPasswordField(20);
        private JButton okButton = new JButton("OK");
        private boolean done = false;

        public MailAuthenticator(String username) throws Exception {
            /*
             * List frameList = OpenFrames.getFrames();
             * JFrame frontFrame = null;
             * for (Iterator frameItr = frameList.iterator();
             * frameItr.hasNext();) {
             * JFrame frame = (JFrame) frameItr.next();
             * if (frame.isActive()) {
             * frontFrame = frame;
             * }
             * }
             */
            JPanel contentPanel = new JPanel();
            passwordDialog = new FrameWithOpenFramesListener("Mail Authentication", "Authenticate", contentPanel);
            passwordDialog.setContentPane(contentPanel);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            contentPanel.setLayout(new GridLayout(4, 1));
            contentPanel.add(mainLabel);
            JPanel p2 = new JPanel();
            p2.add(userLabel);
            p2.add(usernameField);
            usernameField.setText(username);
            usernameField.setEditable(false);
            contentPanel.add(p2);
            JPanel p3 = new JPanel();
            p3.add(passwordLabel);
            p3.add(passwordField);
            contentPanel.add(p3);
            JPanel p4 = new JPanel();
            p4.add(okButton);
            contentPanel.add(p4);
            passwordDialog.pack();

            ActionListener al = new HideDialog();
            okButton.addActionListener(al);
            usernameField.addActionListener(al);
            passwordField.addActionListener(al);

        }

        class HideDialog implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                passwordDialog.setVisible(false);
                OpenFrames.removeFrame(passwordDialog);
                setDone(true);
            }

        }

        /**
         * @see org.dwfa.bpa.util.TaskWithProgress#isDone()
         */
        public boolean isDone() {
            return done;
        }

        private synchronized void setDone(boolean done) {
            this.done = done;
            this.notifyAll();
        }

        /**
         * @throws InterruptedException
         * @see org.dwfa.bpa.util.TaskWithProgress#waitTillDone()
         */
        public synchronized void waitTillDone() throws InterruptedException {
            while (!this.isDone())
                wait();

        }

        public synchronized PasswordAuthentication getPasswordAuthentication() {
            passwordField.requestFocusInWindow();
            passwordDialog.setVisible(true);
            try {
                this.waitTillDone();
            } catch (InterruptedException e) {
                // Nothing to do;
            }

            // getPassword( ) returns an array of chars for security reasons.
            // We need to convert that to a String for
            // the PasswordAuthentication( ) constructor.
            String password = new String(passwordField.getPassword());
            String username = usernameField.getText();
            // Erase the password in case this is used again.
            // The provider should cache the password if necessary.
            passwordField.setText("");
            return new PasswordAuthentication(username, password);

        }
    }

    /**
     * @param config
     * @param id
     * @param desc
     * @throws Exception
     */
    public InboxQueueWorker(Configuration config, UUID id, String desc, I_SelectProcesses selector) throws Exception {
        super(config, id, desc);
        props = new Properties();
        mailHost = (String) this.config.getEntry(this.getClass().getName(), "mailPop3Host", String.class);
        props.put("mail.pop3.host", mailHost);
        props.put("mail.debug", "false");
        username = (String) this.config.getEntry(this.getClass().getName(), "username", String.class);
        this.a = new MailAuthenticator(username);
        // add handlers for main MIME types
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        mc.addMailcap(OutboxQueueWorker.PROCESS_ATTACHMENT_TYPE
            + ";; x-java-content-handler=com.sun.mail.handlers.text_plain");
        CommandMap.setDefaultCommandMap(mc);
        this.setPluginForInterface(I_GetWorkFromQueue.class, this);
    }

    /**
     * @see org.dwfa.bpa.worker.task.I_GetWorkFromQueue#queueContentsChanged()
     */
    public void queueContentsChanged() {
        if (this.sleeping) {
            this.workerThread.interrupt();
        }
    }

    /**
     * @see org.dwfa.bpa.worker.task.I_GetWorkFromQueue#start(org.dwfa.bpa.process.I_QueueProcesses)
     */
    public void start(I_QueueProcesses queue) {
        this.queue = queue;
        this.workerThread = new Thread(this, "Worker " + this.getWorkerDesc());
        this.workerThread.start();

    }

    public void sleep() {
        this.sleeping = true;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {

        }

        this.sleeping = false;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        Transaction t;
        while (true) {
            try {
                logger.info(this.getWorkerDesc() + " starting inbox run");

                Session session = Session.getDefaultInstance(props, a);
                Store store = session.getStore("pop3");
                store.connect(this.mailHost, this.username, null);
                POP3Folder inbox = (POP3Folder) store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE);
                while (inbox.getMessageCount() > 0) {
                    Message msg = inbox.getMessage(1);
                    // process message
                    logger.info(this.getWorkerDesc() + " found message: " + msg.getSubject() + " from: "
                        + Arrays.asList(msg.getFrom()));
                    if (MimeMultipart.class.isAssignableFrom(msg.getContent().getClass())) {
                        MimeMultipart parts = (MimeMultipart) msg.getContent();
                        for (int i = 0; i < parts.getCount(); i++) {
                            MimeBodyPart part = (MimeBodyPart) parts.getBodyPart(i);
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine(this.getWorkerDesc() + " found part: " + part.getContentType());
                            }
                            if (part.getContentType().equals(OutboxQueueWorker.PROCESS_ATTACHMENT_TYPE)) {
                                t = this.getActiveTransaction();
                                DataHandler handler = part.getDataHandler();
                                I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) Base64.decodeToObject((String) handler.getContent());
                                this.queue.write(process, t);
                                this.commitTransactionIfActive();
                            }
                        }
                    } else {
                        logger.info(this.getWorkerDesc() + " found message: " + msg.getSubject() + " from: "
                            + Arrays.asList(msg.getFrom()) + "\nIt is of wrong type: "
                            + msg.getContent().getClass().getName() + "\nThe content is: " + msg.getContent());

                    }
                    msg.setFlag(Flags.Flag.DELETED, true);
                    inbox.close(true);
                    inbox.open(Folder.READ_WRITE);
                }

                inbox.close(true);
                store.close();

                logger.info(this.getWorkerDesc() + " finished inbox run");
            } catch (Throwable ex) {
                this.discardActiveTransaction();
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine(this.getWorkerDesc() + " (" + this.getId() + ") started sleep.");
            }
            this.sleep();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(this.getWorkerDesc() + " (" + this.getId() + ") awake.");
            }
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#execute(org.dwfa.bpa.process.I_EncodeBusinessProcess)
     */
    public synchronized Condition execute(I_EncodeBusinessProcess process) throws TaskFailedException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#isWorkspaceActive(net.jini.id.UUID)
     */
    public boolean isWorkspaceActive(UUID workspaceId) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#createWorkspace(net.jini.id.UUID,
     *      java.lang.String, org.dwfa.bpa.gui.TerminologyConfiguration)
     */
    public I_Workspace createWorkspace(UUID workspaceId, String name, File menuDir) throws WorkspaceActiveException,
            Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkspace(net.jini.id.UUID)
     */
    public I_Workspace getWorkspace(UUID workspaceId) throws NoSuchWorkspaceException {
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
     * @see org.dwfa.bpa.process.I_Work#selectFromList(java.lang.Object[],
     *      java.lang.String, java.lang.String)
     */
    public Object selectFromList(Object[] list, String title, String instructions) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#createHeadlessWorkspace(net.jini.id.UUID,
     *      org.dwfa.bpa.gui.TerminologyConfiguration)
     */
    public I_Workspace createHeadlessWorkspace(UUID workspace_id) throws WorkspaceActiveException, HeadlessException {
        throw new UnsupportedOperationException();
    }

    public I_Workspace createWorkspace(UUID arg0, String arg1, I_ManageUserTransactions arg2, File menuDir)
            throws WorkspaceActiveException, Exception {
        throw new UnsupportedOperationException();
    }

    public Object getObjFromFilesystem(Frame arg0, String arg1, String arg2, FilenameFilter arg3) throws IOException,
            ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    public void writeObjToFilesystem(Frame arg0, String arg1, String arg2, String arg3, Object arg4) throws IOException {
        throw new UnsupportedOperationException();
    }

    public I_Work getTransactionIndependentClone() throws LoginException, ConfigurationException, IOException,
            PrivilegedActionException {
        throw new UnsupportedOperationException();
    }

}
