/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.api.changeset;

import java.io.IOException;

import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

/**
 * The Interface ChangeSetGeneratorBI contains methods for writting
 * changesets.
 */
public interface ChangeSetGeneratorBI {

    /**
     * Opens the changeset writers for the given stamp nids associated with the
     * commit.
     *
     * @param commitStampNids the commit stamp nids
     * @throws IOException signals that an I/O exception has occurred
     */
    public void open(NidSetBI commitStampNids) throws IOException;

    /**
     * Writes changesets for the given
     * <code>conceptChronicle</code> and
     * <code>commitTime</code>.
     *
     * @param conceptChronicle the concept to write changes for
     * @param commitTime the commit time associated with the change
     * @throws IOException signals that an I/O exception has occurred
     */
    public void writeChanges(ConceptChronicleBI conceptChronicle, long commitTime) throws IOException;

    /**
     * Sets the changeset generation policy.
     *
     * @param changeSetGenerationPolicy the changeset generation policy to use when written changesets
     */
    public void setPolicy(ChangeSetGenerationPolicy changeSetGenerationPolicy);

    /**
     * Writes the changeset files and closes the changeset writers.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    public void commit() throws IOException;
}
