/*
 * Created on Apr 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue;

import java.io.Serializable;
import java.util.Date;

import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.bpa.process.I_SelectProcesses;

/**
 * @author kec
 *
 */
public class SelectIfDeadlineOverdue implements I_SelectProcesses, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see org.dwfa.bpa.process.I_SelectProcesses#select(org.dwfa.bpa.process.I_DescribeBusinessProcess)
     */
    public boolean select(I_DescribeBusinessProcess process) {
        Date now = new Date();
        return now.after(process.getDeadline());
    }

    public boolean select(I_DescribeObject obj) {
        return select((I_DescribeBusinessProcess) obj);
    }



}
