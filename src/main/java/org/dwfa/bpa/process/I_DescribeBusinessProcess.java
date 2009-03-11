/*
 * Created on Apr 20, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;

import java.util.Date;


/**
 * Metadata interface for business processes. Implemented by both 
 * business processes and by object that serve as proxies for business
 * processes in lists and other structures. 
 * @author kec
 *
 */
public interface I_DescribeBusinessProcess extends I_DescribeObject {
    /**
     * @return An identifier that uniquely identifies the process. 
     */
    public ProcessID getProcessID();
    /**
     * @return Deadline by which the process should complete. 
     */
    public Date getDeadline();

    /**
     * @return Priority of executing the process. 
     */
    public Priority getPriority();

    /**
     * @return The address of the originator of this process. 
     */
    public String getOriginator();
    /**
     * @return The address where the process should be delivered for execution. 
     */
    public String getDestination();
    
    /**
     * @return A descriptive subject for the process. 
     */
    public String getSubject();
    
    /**
     * @return Name of this business process. 
     */
    public String getName();
    
    /**
     * @throws TaskFailedException If originator or destination address are missing, or malformed. 
     */
    public void validateAddresses() throws TaskFailedException;
    
    /**
     * @throws TaskFailedException If originator or destination address are missing, or malformed. 
     */
    public void validateDestination() throws TaskFailedException;
    
}