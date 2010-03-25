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
 * Created on Jan 9, 2006
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
        return new Expression(oldInstance, oldInstance.getClass(), "getFromString", new Object[] { c.toString() });
    }

}
