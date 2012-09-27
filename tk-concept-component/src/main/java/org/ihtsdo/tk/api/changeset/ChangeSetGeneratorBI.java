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
package org.ihtsdo.tk.api.changeset;

import java.io.IOException;

import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

// TODO: Auto-generated Javadoc
/**
 * The Interface ChangeSetGeneratorBI.
 */
public interface ChangeSetGeneratorBI {

    /**
     * Open.
     *
     * @param commitStampNids the commit stamp nids
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void open(NidSetBI commitStampNids) throws IOException;

    /**
     * Write changes.
     *
     * @param conceptChronicle the concept chronicle
     * @param time the time
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void writeChanges(ConceptChronicleBI conceptChronicle, long time) throws IOException;
    
    /**
     * Sets the policy.
     *
     * @param changeSetGenerationPolicy the new policy
     */
    public void setPolicy(ChangeSetGenerationPolicy changeSetGenerationPolicy);

    /**
     * Commit.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void commit() throws IOException;

}
