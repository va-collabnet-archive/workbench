/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.helper.io;

import java.beans.PersistenceDelegate;

// TODO: Auto-generated Javadoc
/**
 * The Class PersistenceDelegateSpec.
 */
public class PersistenceDelegateSpec {
    
    /** The type. */
    private Class<?> type;
    
    /** The persistence delegate. */
    private PersistenceDelegate persistenceDelegate;

    /**
     * Instantiates a new persistence delegate spec.
     *
     * @param type the type
     * @param persistenceDelegate the persistence delegate
     */
    public PersistenceDelegateSpec(Class<?> type, PersistenceDelegate persistenceDelegate) {
        super();
        this.type = type;
        this.persistenceDelegate = persistenceDelegate;
    }

    /**
     * Gets the persistence delegate.
     *
     * @return the persistence delegate
     */
    public PersistenceDelegate getPersistenceDelegate() {
        return persistenceDelegate;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }
}