package com.sun.jini.mahalo;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.core.transaction.server.TransactionParticipant;

import com.sun.jini.thread.RetryTask;
import com.sun.jini.thread.TaskManager;
import com.sun.jini.thread.WakeupManager;


/**
 * A <code>ParticipantTask</code> is a general task which
 * interacts with a participant.
 *
 * @author Sun Microsystems, Inc.
 *
 * @see TransactionParticipant
 * @see TaskManager
 */
public class LocalParticipantTask extends RetryTask {
    LocalParticipantHandle handle;
    LocalJob myjob;
    private static final Logger operationsLogger = 
        TxnManagerImpl.operationsLogger;
	
    /**
     * Constructs a <code>ParticipantTask</code>.
     *
     * @param manager <code>TaskManager</code> providing the threads
     *                of execution.
     *
     * @param myjob <code>Job</code> to which this task belongs.
     *
     * @param handle <code>ParticipantHandle</code> representing the
     *               <code>TransactionParticipant</code> with which
     *               this task interacts.
     */
    public LocalParticipantTask(TaskManager manager, WakeupManager wm,
    		LocalJob myjob, LocalParticipantHandle handle) {
	super(manager, wm);
	this.myjob = myjob;
	this.handle = handle;
    }

    /**
     * Inherit doc comment from supertype.
     *
     * @see com.sun.jini.thread.RetryTask
     */

    public boolean runAfter(List list, int max) {
        return false;
    }

    public boolean tryOnce() {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(ParticipantTask.class.getName(), 
	        "tryOnce");
	}

	boolean result = false;
	try {
	    result = myjob.performWork(this, handle);
	} catch (UnknownTaskException ute) {
	    //If task doesn't belong to the
	    //Job, then stop doing work.
	    result = true;
	} catch (JobException je) {
	    je.printStackTrace();
	}
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(ParticipantTask.class.getName(), 
	        "tryOnce", Boolean.valueOf(result));
	}

	return result;
    }
}
