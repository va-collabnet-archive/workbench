/*
 * Created on Apr 1, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;

import java.io.Serializable;
import java.util.UUID;

public interface I_DescribeObject extends Serializable {
    public UUID getObjectID();
}
