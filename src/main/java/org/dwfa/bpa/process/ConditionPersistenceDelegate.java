/*
 * Created on Jan 9, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;

public class ConditionPersistenceDelegate extends PersistenceDelegate {

    public ConditionPersistenceDelegate() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Expression instantiate(Object oldInstance, Encoder out) {
        Condition c = (Condition) oldInstance;
        return new Expression(oldInstance,
                oldInstance.getClass(),
                "getFromString",
                new Object[] {c.toString()}
        );
    }
    

}
