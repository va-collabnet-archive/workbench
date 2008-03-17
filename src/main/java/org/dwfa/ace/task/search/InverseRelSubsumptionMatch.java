package org.dwfa.ace.task.search;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Trivial extension that inverts the parent criteria - returns true if the specified component
 * does not have a relationship of the specified type and target.
 * 
 * @author Dion McMurtrie
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/search", type = BeanType.TASK_BEAN),
        @Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class InverseRelSubsumptionMatch extends RelSubsumptionMatch {

	private static final long serialVersionUID = 1L;

	@Override
    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
    	return ! super.test(component, frameConfig);
    }
}
