package org.ihtsdo.project;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.jini.core.entry.Entry;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.select.Selector;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.queue.SelectAll;
import org.ihtsdo.project.model.WorkListMember;

public class QueueConsolidate {

		private I_QueueProcesses queue;
		private I_SelectProcesses selector;
		private I_Work worker;
		private Set<I_GetConceptData> concepts;
		private String queueName;

		public QueueConsolidate(I_Work worker ,String queueName, I_SelectProcesses selector,Set<I_GetConceptData> concepts){
			this.queueName=queueName;
			if (selector==null)
				this.selector=new SelectAll();
			else
				this.selector=selector;
			this.worker=worker;
			
			this.concepts=concepts;
		}
		
		public void consolidate() throws TaskFailedException, LeaseDeniedException, RemoteException, InterruptedException, IOException, PrivilegedActionException{
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
            I_GetConceptData concept=null;
            Transaction transaction=worker.getActiveTransaction();
            while (true) {
                I_EncodeBusinessProcess process = null;
                try {
                	process = this.queue.take(selector, transaction);
                    BusinessProcess.validateAddress(process.getOriginator(), process.getProcessID());
                    WorkListMember member=(WorkListMember)process.readAttachement("A:WORKLIST_MEMBER");
                    concept= (I_GetConceptData)member.getConcept();
                } catch (TaskFailedException ex) {
                    System.out.println("Found missing or malformed origin for process: " + process
                        + " setting origin to queue's node inbox address");
                    process.setOriginator(this.queue.getNodeInboxAddress());

                } catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransactionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoMatchingEntryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}

                for(I_GetConceptData con:concepts ){
                	if (con.equals(concept)){
		                try {
		                    //if (jiniDelivery(process, transaction)) {
		                        worker.commitTransactionIfActive();
//		                    }else {
//		                        worker.discardActiveTransaction();
//		                        System.out.println(" cannot deliver process to: " + process.getDestination());
//		                    }
		                } catch (UnknownTransactionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (CannotCommitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (TransactionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                	}
                }
            }
		}
		
}
