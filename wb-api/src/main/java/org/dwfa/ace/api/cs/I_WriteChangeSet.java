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
package org.dwfa.ace.api.cs;

import java.io.IOException;
import java.io.Serializable;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;

public interface I_WriteChangeSet extends Serializable {

    public void open(I_IntSet commitSapNids) throws IOException;

    public void writeChanges(I_GetConceptData change, long time) throws IOException;
    
    public void setPolicy(ChangeSetPolicy policy);

    public void commit() throws IOException;

}
