/*
 * Created on Mar 18, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa;

import java.io.Serializable;

import org.dwfa.bpa.process.Condition;


/**
 * @author kec
 *
 */
public interface I_Branch extends Serializable {

        /**
         * @return
         */
        public Condition getCondition();
        /**
         * @return
         */
        public int getDestinationId();
}
