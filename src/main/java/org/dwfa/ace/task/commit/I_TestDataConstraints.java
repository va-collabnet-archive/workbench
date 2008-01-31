package org.dwfa.ace.task.commit;

import org.dwfa.ace.api.I_Transact;
import org.dwfa.bpa.process.TaskFailedException;

/**
 * An interface for JavaBeans that can be used to validate data as a task in a business process, or wrapped in a MOJO, 
 * or accessed at commit time, or when changes are created. 
 * 
 * The intent is to provide a generic class for testing data constraints that can be applied:
 * <ol>
 * <li>When a change is made to a component, before a commit
 * <li>When a change to a component is committed
 * <li>When a business process is applied to a component
 * <li>When a change set is imported
 * <li>When validating an entire database
 * </ol>
 * @author kec
 *
 */
public interface I_TestDataConstraints {

    public boolean test(I_Transact component, I_AlertToDataConstraintFailure failureAlertObject) throws TaskFailedException;

}
