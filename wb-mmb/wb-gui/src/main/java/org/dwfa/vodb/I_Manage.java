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
package org.dwfa.vodb;

import java.io.IOException;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.tapi.TerminologyException;

/**
 * Generic template for persistent object management.
 * 
 * This is an alternative to interfaces which extend {@link I_StoreInBdb}
 * 
 * Defines methods for retrieving and persisting a specific type of object
 * without being logically bound to a specific database store.
 * 
 */
public interface I_Manage<T> {

    public T get(int nid) throws TerminologyException, IOException;

    public Set<T> getAll() throws TerminologyException;

    public boolean exists(int nid) throws TerminologyException, IOException;

    public void write(T object, I_ConfigAceFrame config) throws TerminologyException;

}
