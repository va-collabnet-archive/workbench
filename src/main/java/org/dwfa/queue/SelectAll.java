/*
 * Created on Apr 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue;

import java.io.Serializable;

import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.bpa.process.I_SelectProcesses;

/**
 * @author kec
 *
 */
public class SelectAll implements I_SelectProcesses, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see org.dwfa.bpa.process.I_SelectProcesses#select(org.dwfa.bpa.process.I_DescribeBusinessProcess)
     */
    public boolean select(I_DescribeBusinessProcess process) {
        return true;
    }

    public boolean select(I_DescribeObject obj) {
        return true;
    }

}
