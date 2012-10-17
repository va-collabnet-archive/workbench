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
package org.ihtsdo.tk.api;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.search.ScoredComponentReference;


/**
 * The Interface TerminologyDI provides methods for working with terminology
 * which are general to both chronicles and versions.
 */
public interface TerminologyDI {

    /**
     * Perform a search of description text based on given
     * <code>query</code>.
     *
     * @param query the string to search for
     * @return the matching results in the form
     * of <code>ScoredComponentReferences</code>, which represent the
     * concept/component nid of the result and a score of how well the result
     * matches the search criteria
     * @throws IOException signals that an I/O exception has occurred
     * @throws ParseException the parse exception
     */
    Collection<ScoredComponentReference> doTextSearch(String query) throws IOException, ParseException;

    /**
     * Adds a new or changed
     * <code>conceptChronicle</code> as uncommitted. Performs datachecks in the
     * plugins/precommit folder of the install directory.
     *
     * @param conceptChronicle the concept with changes
     * @throws IOException signals that an I/O exception has occurred
     */
    void addUncommitted(ConceptChronicleBI conceptChronicle) throws IOException;

    /**
     * Adds a new or changed
     * <code>conceptVersion</code> as uncommitted. Performs datachecks in the
     * plugins/precommit folder of the install directory.
     *
     * @param conceptVersion the concept version with changes
     * @throws IOException signals that an I/O exception has occurred
     */
    void addUncommitted(ConceptVersionBI conceptVersion) throws IOException;

    /**
     * Adds a new or changed
     * <code>conceptChronicle</code> as uncommitted. No datachecks are
     * performed.
     *
     * @param conceptChronicle the concept with changes
     * @throws IOException signals that an I/O exception has occurred
     */
    void addUncommittedNoChecks(ConceptChronicleBI conceptChronicle) throws IOException;

    /**
     * Adds a new
     * <code>conceptVersion</code> as uncommitted. No datachecks are performed.
     *
     * @param conceptVersion the new concept version
     * @throws IOException signals that an I/O exception has occurred
     */
    void addUncommittedNoChecks(ConceptVersionBI conceptVersion) throws IOException;

    /**
     * Write direct. TODO-javadoc: doesn't go through a transaction, such as loading an econcepts file
     *
     * @param conceptChronicle the concept chronicle
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated not in TK3
     */
    @Deprecated
    void writeDirect(ConceptChronicleBI conceptChronicle) throws IOException;

    /**
     * Commits all uncommitted changes. Performs datachecks in the
     * plugins/commit folder of the install directory.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    void commit() throws IOException;

    /**
     * Cancels all uncommitted changes.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    void cancel() throws IOException;

    /**
     * Commits the specified
     * <code>conceptChronicle</code>. Only the specified concept will be
     * committed. Performs datachecks in the plugins/commit folder of the
     * install directory.
     *
     * @param conceptChronicle the concept to commit
     * @throws IOException signals that an I/O exception has occurred
     */
    void commit(ConceptChronicleBI conceptChronicle) throws IOException;

    /**
     * Commits the specified
     * <code>conceptChronicle</code> with changesets written according to the
     * <code>changeSetPolicy</code>.
     *
     * @param conceptChronicle the concept to commit
     * @param changeSetPolicy the change set policy to use for writing changeset
     * generated on commit
     * @throws IOException signals that an I/O exception has occurred
     */
    void commit(ConceptChronicleBI conceptChronicle, ChangeSetPolicy changeSetPolicy) throws IOException;

    /**
     * Cancels any uncommitted changes on the specified
     * <code>conceptChronicle</code>.
     *
     * @param conceptChronicle the uncommitted concept
     * @throws IOException signals that an I/O exception has occurred
     */
    void cancel(ConceptChronicleBI conceptChronicle) throws IOException;

    /**
     * Commits an uncommitted
     * <code>conceptVersion</code>.
     *
     * @param conceptVersion the uncommitted concept version
     * @throws IOException signals that an I/O exception has occurred
     */
    void commit(ConceptVersionBI conceptVersion) throws IOException;

    /**
     * Cancels an uncommitted
     * <code>conceptVersion</code>.
     *
     * @param conceptVersion the uncommitted concept version to cancel
     * @throws IOException signals that an I/O exception has occurred
     */
    void cancel(ConceptVersionBI conceptVersion) throws IOException;

    /**
     * Adds a change set generator which is needed in order to write changesets
     * upon commit. This is only necessary to add when needing to write
     * changesets for edits that do not occur in a running envrionment. For
     * example, an edit generated at build time will need to use a changeset
     * generator in order to generate a changeset. A changeset generator is
     * already in place for user-initiated edits in the workbench, and will
     * generate changesets according to the user's preferences.
     *
     * @param key the string identifying this <code>changesetGenerator</code>
     * @param changeSetGenerator the change set generator to use for writing
     * changests
     */
    void addChangeSetGenerator(String key, ChangeSetGeneratorBI changeSetGenerator);

    /**
     * Removes the change set generator. If generating changesets at build time,
     * the changeset generator should be removed after the changeset is
     * generated.
     *
     * @param key the string identifying the changeset generator to be removed
     */
    void removeChangeSetGenerator(String key);

    /**
     * Creates the dto change set generator. TODO-javadoc: specifically what writes out dto/econcepts changesets
     *
     * @param changeSetFileName the change set file name
     * @param changeSetTempFileName the change set temp file name
     * @param changeSetGenerationPolicy the change set generation policy
     * @return the change set generator bi
     */
    ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName,
            File changeSetTempFileName,
            ChangeSetGenerationPolicy changeSetGenerationPolicy);

    /**
     * Gets the positions associated with the given
     * <code>stampNids</code>.
     *
     * @param stampNids the stamp nids representing the desired positions
     * @return the positions found from the specified stamp nids
     * @throws IOException signals that an I/O exception has occurred
     * @see StampBI
     */
    Set<PositionBI> getPositionSet(Set<Integer> stampNids) throws IOException;

    /**
     * Gets the paths associated with the given
     * <code>stampNids</code>.
     *
     * @param stampNids the stamp nids representing the desired paths
     * @return the paths found from the specified stamp nids
     * @throws IOException signals that an I/O exception has occurred
     */
    Set<PathBI> getPathSetFromStampSet(Set<Integer> stampNids) throws IOException;

    /**
     * Gets the paths used in the given
     * <code>positions</code>.
     *
     * @param positions the positions
     * @return the paths found in the specified positions
     * @throws IOException signals that an I/O exception has occurred
     */
    Set<PathBI> getPathSetFromPositionSet(Set<PositionBI> positions) throws IOException;

    /**
     * Gets the
     * <code>PathBI</code> object for the path associated with the given
     * <code>pathNid</code>.
     *
     * @param pathNid the path nid
     * @return the path associated with the specified path nid
     * @throws IOException signals that an I/O exception has occurred
     */
    PathBI getPath(int pathNid) throws IOException;

    /**
     * Gets the nid associated with the component spcified by the <code>alternateId</code>.
     *
     * @param authorityUuid the uuid representing the authority associated with the alternate id
     * @param alternateId a string representation of the alternate id
     * @return the nid associated with the specified component
     * @throws IOException signals that an I/O exception has occurred
     */
    int getNidFromAlternateId(UUID authorityUuid, String alternateId) throws IOException;
}
