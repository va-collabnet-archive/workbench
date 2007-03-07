/*
 * Created on Apr 1, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;

import java.io.Serializable;

public interface I_SelectObjects extends Serializable {
    public boolean select(I_DescribeObject object);

}
