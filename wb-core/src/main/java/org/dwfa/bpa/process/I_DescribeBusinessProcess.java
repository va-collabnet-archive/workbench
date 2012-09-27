/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Created on Apr 20, 2005
 */
package org.dwfa.bpa.process;

import java.io.IOException;
import java.util.Date;

/**
 * Metadata interface for business processes. Implemented by both
 * business processes and by object that serve as proxies for business
 * processes in lists and other structures.
 * 
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
     * @throws TaskFailedException If originator or destination address are
     *             missing, or malformed.
     */
    public void validateAddresses() throws TaskFailedException;

    /**
     * @throws TaskFailedException If originator or destination address are
     *             missing, or malformed.
     */
    public void validateDestination() throws TaskFailedException;
    
    /**
     * 
     * @return <code>true</code> if the database dependencies required by this process are satisfied. 
     * @throws IOException 
     */
    public boolean dbDependenciesAreSatisfied() throws IOException;

}
