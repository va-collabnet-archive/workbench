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
 * Created on May 31, 2005
 */
package org.dwfa.queue.bpa.worker;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.Properties;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.login.LoginException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.gui.I_ManageUserTransactions;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.NoSuchWorkspaceException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.process.WorkspaceActiveException;
import org.dwfa.bpa.util.Base64;
import org.dwfa.bpa.worker.Worker;
import org.dwfa.bpa.worker.task.I_GetWorkFromQueue;
import org.dwfa.jini.ElectronicAddress;

/**
 * @author kec
 * 
 */
public class OnDemandOutboxQueueWorker extends Worker implements I_GetWorkFromQueue, Runnable {

	public static final String PROCESS_ATTACHMENT_TYPE = "application/x-java-serialized-object-base64";

	private I_QueueProcesses queue;

	private Thread workerThread;

	private boolean sleeping;

	private long sleepTime = 1000 * 60 * 1;

	private I_SelectProcesses selector;

	private Properties props;

	private boolean noSmtp = true;

	/**
	 * @param config
	 * @param id
	 * @param desc
	 * @throws ConfigurationException
	 * @throws LoginException
	 * @throws IOException
	 */
	public OnDemandOutboxQueueWorker(Configuration config, UUID id, String desc, I_SelectProcesses selector)
	throws ConfigurationException, LoginException, IOException, PrivilegedActionException {
		super(config, id, desc);
		this.selector = selector;
		props = new Properties();
		String mailHost;
		try {
			mailHost = (String) this.config.getEntry(this.getClass().getName(), "mailHost", String.class);
			props.put("mail.host", mailHost);
			this.noSmtp = false;
		} catch (ConfigurationException e) {
			this.getLogger().info("No mailhost specified. SMTP sending will be turned off. ");
		}
		// add handlers for main MIME types
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		mc.addMailcap(PROCESS_ATTACHMENT_TYPE + ";; x-java-content-handler=com.sun.mail.handlers.text_plain");
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
		// Don't send anything automatically
	}
	public void send() {
		Transaction t;
		HashSet<I_EncodeBusinessProcess> processSet=new HashSet<I_EncodeBusinessProcess>();
		try {
			BusinessProcess.validateAddress(this.queue.getNodeInboxAddress(), null);
			while (true) {
				System.out.println(this.getWorkerDesc() + " starting outbox run");
				t = this.getActiveTransaction();
				I_EncodeBusinessProcess process = this.queue.take(selector, t);
				System.out.println(this.getWorkerDesc() + " found process: " + process);
				try {
					BusinessProcess.validateAddress(process.getOriginator(), process.getProcessID());
				} catch (TaskFailedException ex) {
					System.out.println(this.getWorkerDesc() + " found missing or malformed origin for process: " + process
							+ " setting origin to queue's node inbox address");
					process.setOriginator(this.queue.getNodeInboxAddress());

				}

				try {
					if (jiniDelivery(process, t)) {
						this.commitTransactionIfActive();
					} else if (smtpDelivery(process, t)) {
						this.commitTransactionIfActive();
					} else {
						processSet.add(process);
						this.commitTransactionIfActive();
						System.out.println("Worker: " + this.getWorkerDesc() + " (" + this.getId()
								+ ") cannot deliver process to: " + process.getDestination());
					}
				} catch (TaskFailedException ex) {
					System.out.println(this.getWorkerDesc() + " cannot deliver process " + process.getId() + " to: "
							+ process.getDestination());
					processSet.add(process);
					this.commitTransactionIfActive();
				}
			}
		} catch (NoMatchingEntryException ex) {
			try {
				for (I_EncodeBusinessProcess process: processSet){
					this.queue.write(process,  this.getActiveTransaction());
					this.commitTransactionIfActive();
				}
				//                    this.abortActiveTransaction();
			} catch (Exception e) {
				logger.log (Level.SEVERE, "Worker: " + this.getWorkerDesc() + " (" + this.getId() + ") "
						+ e.getMessage(), e);
			}
			System.out.println("No Matching Exception");
		} catch (Throwable ex) {
			this.discardActiveTransaction();
			System.out.println("Throwable Exception");
			logger.log(Level.SEVERE, this.getWorkerDesc(), ex);
		}
	}

	/**
	 * @param process
	 * @param t
	 * @return
	 */
	private boolean smtpDelivery(I_EncodeBusinessProcess process, Transaction t) {
		if (noSmtp) {
			return false;
		}
		try {
			logger.info(this.getWorkerDesc() + " trying SMTP delivery. ");
			process.validateAddresses();
			Session mailConnection = Session.getInstance(props, null);
			Message msg = new MimeMessage(mailConnection);
			Address from = new InternetAddress(process.getOriginator());
			msg.setFrom(from);
			// TODO make this more robust by only looking for splits
			// outside of quotes.
			String[] destinations = process.getDestination().split("[,]");
			Address[] toAddresses = new Address[destinations.length];
			for (int i = 0; i < destinations.length; i++) {
				toAddresses[i] = new InternetAddress(destinations[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, toAddresses);

			msg.setSubject(process.getProcessID().toString());
			msg.addHeader("X-Priority", process.getPriority().getXPriorityValue());
			MimeBodyPart bodyPart = new MimeBodyPart();
			StringBuffer msgBody = new StringBuffer();
			msgBody.append("process id: " + process.getProcessID().toString());
			msgBody.append("\nCurrent task: " + process.getCurrentTaskId());
			msgBody.append("\nDeadline: " + process.getDeadline());
			msgBody.append("\nPriority: " + process.getPriority());
			msgBody.append("\nSubject: " + process.getSubject());
			bodyPart.setText(msgBody.toString());

			MimeBodyPart processPart = new MimeBodyPart();
			processPart.setContent(Base64.encodeObject(process), PROCESS_ATTACHMENT_TYPE);
			MimeMultipart multipart = new MimeMultipart();
			multipart.addBodyPart(bodyPart);
			multipart.addBodyPart(processPart);
			msg.setContent(multipart);

			Transport.send(msg);

			logger.info(this.getWorkerDesc() + " process: " + process.getProcessID() + " delivered to: "
					+ process.getDestination() + " via SMTP. ");
			return true;
		} catch (Exception e) {
			logger.log(Level.WARNING, this.getWorkerDesc() + " cannot deliver message", e);
		}
		return false;
	}

	/**
	 * @param t
	 * @param process
	 * @throws TransactionException
	 * @throws IOException
	 * @throws IOException
	 * @throws TransactionException
	 * @throws TaskFailedException
	 */
	private boolean jiniDelivery(I_EncodeBusinessProcess process, Transaction t) throws IOException,
	TransactionException, TaskFailedException {

		ElectronicAddress eAddress = new ElectronicAddress(process.getDestination());
		logger.info(this.getWorkerDesc() + " trying jini delivery. ");
		ServiceTemplate tmpl = new ServiceTemplate(null, new Class[] { I_QueueProcesses.class },
				new Entry[] { eAddress });
		ServiceItemFilter filter = null;
		try {
			process.validateDestination();
			ServiceItem item = jinilookup(tmpl, filter);
			if (item == null) {
				return false;
			}
			I_QueueProcesses queue = (I_QueueProcesses) item.service;
			queue.write(process, t);
			logger.info(this.getWorkerDesc() + " process: " + process.getProcessID() + " delivered to: "
					+ process.getDestination() + " via Jini discovered queue service. ");
			return true;
		} catch (InterruptedException e) {
			logger.info(this.getWorkerDesc() + " Jini delivery failed: " + e.toString());
		} catch (IOException e) {
			logger.info(this.getWorkerDesc() + " Jini delivery failed: " + e.toString());
		} catch (PrivilegedActionException e) {
			logger.info(this.getWorkerDesc() + " Jini delivery failed: " + e.toString());
		} catch (ConfigurationException e) {
			logger.info(this.getWorkerDesc() + " Jini delivery failed: " + e.toString());
		}
		return false;

	}

	/**
	 * @param tmpl
	 * @param filter
	 * @return
	 * @throws InterruptedException
	 * @throws RemoteException
	 * @throws IOException
	 * @throws ConfigurationException
	 * @throws PrivilegedActionException
	 */
	protected ServiceItem jinilookup(ServiceTemplate tmpl, ServiceItemFilter filter) throws InterruptedException,
	RemoteException, IOException, PrivilegedActionException, ConfigurationException {
		ServiceItem item = lookup(tmpl, filter, 1000 * 10);
		return item;
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
