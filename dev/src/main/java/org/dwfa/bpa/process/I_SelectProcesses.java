/*
 * Created on Apr 20, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;

/**
 * @author kec
 *
 */
public interface I_SelectProcesses extends I_SelectObjects {
    public boolean select(I_DescribeBusinessProcess process);

}
