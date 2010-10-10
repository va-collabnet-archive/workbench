package org.dwfa.queue;

import java.io.IOException;
import java.io.Serializable;

import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.bpa.process.I_SelectProcesses;

public class SelectAllWithSatisfiedDbConstraints implements I_SelectProcesses, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @throws IOException 
     * @see org.dwfa.bpa.process.I_SelectProcesses#select(org.dwfa.bpa.process.I_DescribeBusinessProcess)
     */
    public boolean select(I_DescribeBusinessProcess process) throws IOException {
        return process.dbDependenciesAreSatisfied();
    }

    public boolean select(I_DescribeObject obj) throws IOException {
    	if (I_DescribeBusinessProcess.class.isAssignableFrom(obj.getClass())) {
    		I_DescribeBusinessProcess p = (I_DescribeBusinessProcess) obj;
    		return p.dbDependenciesAreSatisfied();
    	}
        return false;
    }

}
