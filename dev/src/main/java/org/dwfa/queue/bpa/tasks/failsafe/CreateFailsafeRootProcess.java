/*
 * Created on Jun 14, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.failsafe;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;

/**
 * Creates a failsafe of the <em>root</em> process rather than of the process that directly 
 * contains this task. Otherwise it behaves the same as the parent <code>CreateFailsafe</code>
 * class. 
 * <p>
 * @see DestroyFailsafe
 * @see CreateFailsafeParentProcess
 * @see CreateFailsafeRootProcess
 * <p>
 * @author kec
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/queue tasks/failsafe", type = BeanType.TASK_BEAN)})
public class CreateFailsafeRootProcess extends CreateFailsafe {

    /**
     * 
     */
    private static final long serialVersionUID = -1005859353717389656L;

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

        try {
            I_EncodeBusinessProcess rootProcess = worker.getProcessStack().get(0);
            I_ContainData failsafeIdContainer = process.getDataContainer(this.failsafeDataId);
            createFailsafe(rootProcess, process, failsafeIdContainer, worker);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

    }
}
