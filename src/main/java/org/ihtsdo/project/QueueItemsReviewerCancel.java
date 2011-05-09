package org.ihtsdo.project;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;

import net.jini.core.entry.Entry;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;

import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeQueueEntry;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.queue.ObjectServerCore;
import org.dwfa.queue.SelectAll;
import org.dwfa.swing.SwingWorker;

public class QueueItemsReviewerCancel {

	private String CUSTOM_NODE_KEY = "CUSTOM_NODE_KEY";;
	private I_QueueProcesses queue;
	private I_SelectProcesses selector;
	private I_Work worker;
	private HashSet<UUID> contract;
	private String queueName;
	private I_Work outboxWorker;
	private I_QueueProcesses outboxQueue;
	private UUID contractUuid;

	public QueueItemsReviewerCancel(I_Work worker ,String queueName, I_SelectProcesses selector,HashSet<UUID> contract,UUID contractUuid){
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
		if (contract!=null ){
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
					boolean modified=false;
					I_DescribeQueueEntry qEntry = (I_DescribeQueueEntry) descProcess;
					process = this.queue.read(qEntry.getEntryID(), null);
					UUID contractUuidTarget=(UUID) process.readAttachement(ProcessAttachmentKeys.QUEUE_UTIL_CONTRACT_UUID.getAttachmentKey());
					if (contractUuidTarget.toString().equals(contractUuid.toString())){
						HashMap<UUID,String>contractTarget=(HashMap<UUID, String>) process.readAttachement(ProcessAttachmentKeys.QUEUE_UTIL_CONTRACT.getAttachmentKey());

						if ( contractTarget!=null ){
							for (UUID uuid:contract){
								if (contractTarget.containsKey(uuid)){
									contractTarget.remove(uuid);
									modified=true;
								}
							}
						}
						if(modified){

							Transaction transaction=worker.getActiveTransaction();
							process = this.queue.take(qEntry.getEntryID(),transaction);

							process.writeAttachment(ProcessAttachmentKeys.QUEUE_UTIL_CONTRACT.getAttachmentKey(), contractTarget);

							this.queue.write(process, worker.getActiveTransaction());
							worker.commitTransactionIfActive();
						}
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (TransactionException e) {
				e.printStackTrace();
			} catch (NoMatchingEntryException e) {
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
