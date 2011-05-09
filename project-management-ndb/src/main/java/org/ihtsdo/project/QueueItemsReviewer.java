package org.ihtsdo.project;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.security.auth.login.LoginException;
import javax.swing.SwingUtilities;

import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeQueueEntry;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.ProcessID;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.queue.ObjectServerCore;
import org.dwfa.queue.SelectAll;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.TranslationHelperPanel;

public class QueueItemsReviewer {

		private String CUSTOM_NODE_KEY = "CUSTOM_NODE_KEY";;
		private I_QueueProcesses queue;
		private I_SelectProcesses selector;
		private I_Work worker;
		private HashMap<UUID,String> contract;
		private String queueName;
		private I_Work outboxWorker;
		private I_QueueProcesses outboxQueue;
		private UUID contractUuid;

		public QueueItemsReviewer(I_Work worker ,String queueName, I_SelectProcesses selector,HashMap<UUID,String> contract,UUID contractUuid){
			this.queueName=queueName;
			if (selector==null)
				this.selector=new SelectAll();
			else
				this.selector=selector;
			this.worker=worker;
			
			this.contract=contract;
			this.contractUuid=contractUuid;
		}
		
		public void process() throws TaskFailedException, LeaseDeniedException, RemoteException, InterruptedException, IOException, PrivilegedActionException{
			try {
				ServiceID serviceID = null;
				Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
				Entry[] attrSetTemplates = new Entry[] { new Name(queueName) };
				ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
				ServiceItemFilter filter = null;
				ServiceItem service = worker.lookup(template, filter);
				if (service == null) {
					throw new TaskFailedException("No queue with the specified name could be found: "
							+ queueName);
				}
				queue = (I_QueueProcesses) service.service;


			} catch (Exception e) {
				throw new TaskFailedException(e);
			}
			I_EncodeBusinessProcess process = null;
			HashSet<UUID> uuidSet=new HashSet<UUID>();
			try {
				Collection<I_DescribeBusinessProcess> processes= this.queue.getProcessMetaData(selector);

				for(I_DescribeBusinessProcess descProcess:processes){
					I_DescribeQueueEntry qEntry = (I_DescribeQueueEntry) descProcess;
					process = this.queue.read(qEntry.getEntryID(), null);

					WorkListMember member=(WorkListMember)process.readAttachement(ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey());
					if (member!=null){
						for (UUID uuid:member.getUids()){
							if (contract.containsKey(uuid)){
								String destination=contract.get(uuid);
								if (destination==null){
									Transaction transaction=worker.getActiveTransaction();
									process = this.queue.take(qEntry.getEntryID(),transaction);
									worker.commitTransactionIfActive();
								}else if  (!destination.equalsIgnoreCase(queueName)){
									Transaction transaction=worker.getActiveTransaction();
									process = this.queue.take(qEntry.getEntryID(),transaction);
									process.setDestination(destination);

									if (outboxQueue==null){
										ServiceItem service = getOutboxQueue();

										outboxQueue = (I_QueueProcesses) service.service;
									}

									process.writeAttachment(CUSTOM_NODE_KEY, null);

									outboxQueue.write(process, outboxWorker.getActiveTransaction());
									outboxWorker.commitTransactionIfActive();
									worker.commitTransactionIfActive();
								}
								uuidSet.add(uuid);
								break;
							}
						}
					}
				}
				if (uuidSet.size()>0){
					sendNotifyToUsers(uuidSet);
				}
			} catch (TaskFailedException ex) {
				System.out.println("Found missing or malformed origin for process: " + process
						+ " setting origin to queue's node inbox address");
				process.setOriginator(this.queue.getNodeInboxAddress());

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (TransactionException e) {
				e.printStackTrace();
			} catch (NoMatchingEntryException e) {
				e.printStackTrace();
			} catch (LoginException e) {
				
				e.printStackTrace();
			} catch (ConfigurationException e) {
				
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setupExecuteEnd();
				}

				private void setupExecuteEnd()  {
					RefreshServer ros = new RefreshServer();
					ros.start();

				}

			});
		}
		private void sendNotifyToUsers(HashSet<UUID> uuidSet) throws LoginException, TaskFailedException, ConfigurationException, IOException, PrivilegedActionException, TerminologyException, InterruptedException, TransactionException, LeaseDeniedException {

			if (outboxQueue==null){
				ServiceItem service;
				service = getOutboxQueue();
				outboxQueue = (I_QueueProcesses) service.service;
			}
			BusinessProcess selectedProcess = null;
			File file =  new File("sampleProcesses/WrlstMemberCancelReassign.bp"); 
			selectedProcess = TerminologyProjectDAO.getBusinessProcess(file);
			
			selectedProcess.writeAttachment(ProcessAttachmentKeys.QUEUE_UTIL_CONTRACT_UUID.getAttachmentKey(), contractUuid);
			selectedProcess.writeAttachment(ProcessAttachmentKeys.QUEUE_UTIL_CONTRACT.getAttachmentKey(), uuidSet);
			
			selectedProcess.setSubject(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW_CANCEL);
	
			I_ConfigAceFrame config;
			config = Terms.get().getActiveAceFrameConfig();

			for (String address : config.getAddressesList()) {
				if (address.trim().endsWith(".inbox") && !address.equals(queue.getNodeInboxAddress())) {
					
					selectedProcess.setDestination(address) ;
					selectedProcess.setProcessID(new ProcessID(UUID.randomUUID()));
					
					System.out.println(
		    				"Moving process " + selectedProcess.getProcessID() + " to outbox: " + outboxQueue.toString());
		    		
					outboxQueue.write(selectedProcess, outboxWorker.getActiveTransaction());
					outboxWorker.commitTransactionIfActive();
		    		System.out.println("Moved process " + selectedProcess.getProcessID() + " to queue: " + outboxQueue.toString());

				}
			}
		}

		private ServiceItem getOutboxQueue() throws LoginException, ConfigurationException, IOException, PrivilegedActionException, TerminologyException, TaskFailedException, InterruptedException {
			I_ConfigAceFrame config;
			config = Terms.get().getActiveAceFrameConfig();

			String outboxQueueName = config.getUsername() + ".outbox";
			ServiceID serviceID = null;
			Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
			Entry[] attrSetTemplates = new Entry[] { new Name(outboxQueueName) };
			ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
			ServiceItemFilter filter = null;
			if (outboxWorker==null)
				outboxWorker=worker.getTransactionIndependentClone();

			ServiceItem service = outboxWorker.lookup(template, filter);
			if (service == null) {
				throw new TaskFailedException("No queue with the specified name could be found: "
						+ outboxQueueName);
			}
			return service;
			
		}
		public class RefreshServer extends SwingWorker<Boolean> {

			@Override
			protected Boolean construct() throws Exception {
				ObjectServerCore.refreshServers();
				return true;
			}

			@Override
			protected void finished() {
				try {
					get();
				} catch (InterruptedException e1) {

					e1.printStackTrace();
				} catch (ExecutionException e1) {

					e1.printStackTrace();
				}
			}

		}
		
}
