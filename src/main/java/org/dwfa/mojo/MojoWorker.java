/*
 * Created on Apr 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.mojo;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import javax.security.auth.login.LoginException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.gui.I_ManageUserTransactions;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoSuchWorkspaceException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.process.WorkspaceActiveException;
import org.dwfa.bpa.worker.Worker;


/**
 * @author kec
 *
 */
public class MojoWorker extends Worker implements /*I_GetWorkFromQueue,*/ Runnable {

    private I_QueueProcesses queue;
    private Thread workerThread;
    private boolean sleeping;
    private long sleepTime = 1000 * 60 * 1;
//    private I_SelectProcesses selector;

//    private I_DefineTask task;
    private HashMap<String, I_DefineTask> tasks = new HashMap<String, I_DefineTask>();
    private I_EncodeBusinessProcess process; 
    

    /**
     * @param config
     * @param id
     * @param desc
     * @throws ConfigurationException
     * @throws LoginException 
     * @throws IOException 
     */
    public MojoWorker(Configuration config, UUID id, String desc, String destination/*, I_SelectProcesses selector*/) 
    throws ConfigurationException, LoginException, IOException, PrivilegedActionException {
        super(config, id, desc);
        
        process = new BusinessProcess(){
	    	public Object readProperty(String propertyLabel)
	        throws IntrospectionException, IllegalAccessException,
	        InvocationTargetException {
	    			return readAttachement(propertyLabel);
	    	}
        };
        process.setDestination(destination);
        
        //        this.selector = selector;
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#isWorkspaceActive(net.jini.id.UUID)
     */
    public boolean isWorkspaceActive(UUID workspaceId) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#createWorkspace(net.jini.id.UUID, java.lang.String, org.dwfa.bpa.gui.TerminologyConfiguration)
     */
    public I_Workspace createWorkspace(UUID workspaceId, String name, File menuDir) throws WorkspaceActiveException,
            HeadlessException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkspace(net.jini.id.UUID)
     */
    public I_Workspace getWorkspace(UUID workspaceId)
            throws NoSuchWorkspaceException {
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
     * @see org.dwfa.bpa.process.I_Work#selectFromList(java.lang.Object[], java.lang.String, java.lang.String)
     */
    public Object selectFromList(Object[] list, String title,
            String instructions) {
        throw new UnsupportedOperationException();
    }

//    /**
//     * @see org.dwfa.queue.I_GetWorkFromQueue#queueContentsChanged()
//     */
//    public void queueContentsChanged() {
//        if (this.sleeping) {
//            this.workerThread.interrupt();
//        }
//        
//    }
//
//    /**
//     * @see org.dwfa.queue.I_GetWorkFromQueue#start(org.dwfa.bpa.process.I_QueueProcesses)
//     */
//    public void start(I_QueueProcesses queue) {
//        this.queue = queue;
//        this.workerThread = new Thread(this, "Worker " + this.getWorkerDesc());
//        this.workerThread.start();
//        
//    }
//    
//    private void  sleep() {
//        this.sleeping = true;
//        try {
//            Thread.sleep(sleepTime);
//        } catch (InterruptedException e) {
//            
//        }
//        
//        this.sleeping = false;
//    }

    public Object getProperty(String propName) throws TaskFailedException{
    	try{
//    		return process.readProperty( propName );
    		return process.readAttachement( propName );
    	}catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } /*catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        }*/
    }
    
    public void setProcessProperty(String propName, Object propValue) throws TaskFailedException {
    	try{
//    	  process.setProperty(propName, propValue);
    	  process.writeAttachment(propName, propValue);
    	} catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } /*catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        }*/
    }
    
    public void addTask(I_DefineTask task, int position) throws TaskFailedException{
//    	String key = new Integer(position).toString() ;
//    	tasks.put( key , task);
    	try{
    		process.addTask( task );
    	} catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
    	} catch (PropertyVetoException e){
    		throw new TaskFailedException(e);
        
        }
    }
    
    public void addBranch(I_DefineTask task1, I_DefineTask task2, Condition condition){
    	process.addBranch(task1, task2, condition);
    }
    
    public void run() {
//        I_EncodeBusinessProcess process = new BusinessProcess();
    	
    	
        for(int i = 0; i<process.getTasks().size(); i++ ){
        	System.out.println("[Mojo Worker]>> inside task execute loop");
        	process.getTask(i);//  tasks.get( key );
        	try{
        		System.out.println("[Mojo Worker]>> start process " + process.getTask(i).getName());
        		this.execute( process );
        		System.out.println("[Mojo Worker]>> task executed");
        	 } catch (TaskFailedException ex) {
                 this.discardActiveTransaction();
                 logger.log(Level.WARNING, "Worker: " + this.getWorkerDesc() + " ("+ this.getId() + ") " + ex.getMessage(), ex);
             }
        }//End loop
        
    	
//        Transaction t;
//        while (true) {
//            try {
//                 while (true) {
//                    try {
//                        t = this.getActiveTransaction();
//                        I_EncodeBusinessProcess process = new BusinessProcess();
//                        if (logger.isLoggable(Level.INFO)) {
//                            logger.info(this.getWorkerDesc() + " TAKE: " + process.getName() + " (" + process.getProcessID() + ") " + ": " + process.getCurrentTaskId() + " " + 
//                                    process.getTask(process.getCurrentTaskId()).getName() + " deadline: " + dateFormat.format(process.getDeadline()));
//                        }
//                        
//                        this.execute(process);
//                        this.commitTransactionIfActive();
//                    } catch (TaskFailedException ex) {
//                        this.discardActiveTransaction();
//                        logger.log(Level.WARNING, "Worker: " + this.getWorkerDesc() + " ("+ this.getId() + ") " + ex.getMessage(), ex);
//                    }
//                }
//
//            } catch (NoMatchingEntryException ex) {
//                try {
//                    this.abortActiveTransaction();
//                } catch (Exception e) {
//                    logger.log(Level.SEVERE, "Worker: " + this.getWorkerDesc() + " ("+ this.getId() + ") " + e.getMessage(), e);
//                } 
//                if (logger.isLoggable(Level.FINE)) {
//                    logger.fine(this.getWorkerDesc() + " ("+ this.getId() + ") started sleep.");
//                }
////                this.sleep();
//                if (logger.isLoggable(Level.FINE)) {
//                    logger.fine(this.getWorkerDesc() + " ("+ this.getId() + ") awake.");
//                }
//            } catch (Throwable ex) {
//                this.discardActiveTransaction();
//                logger.log(Level.SEVERE, this.getWorkerDesc(), ex);
//            }
//        }
        
    }
    /**
     * @see org.dwfa.bpa.process.I_Work#createHeadlessWorkspace(net.jini.id.UUID, org.dwfa.bpa.gui.TerminologyConfiguration)
     */
    public I_Workspace createHeadlessWorkspace(UUID workspace_id) throws WorkspaceActiveException, HeadlessException {
        throw new UnsupportedOperationException();
    }
    public I_Workspace createWorkspace(UUID arg0, String arg1, I_ManageUserTransactions arg2, File menuDir) throws WorkspaceActiveException, Exception {
        throw new UnsupportedOperationException();
    }

	public Object getObjFromFilesystem(Frame arg0, String arg1, String arg2, FilenameFilter arg3) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException();
	}

	public void writeObjToFilesystem(Frame arg0, String arg1, String arg2, String arg3, Object arg4) throws IOException {
        throw new UnsupportedOperationException();
	}

	public I_Work getTransactionIndependentClone() throws LoginException, ConfigurationException, IOException, PrivilegedActionException {
		throw new UnsupportedOperationException();
	}

}//End class MojoWorker
