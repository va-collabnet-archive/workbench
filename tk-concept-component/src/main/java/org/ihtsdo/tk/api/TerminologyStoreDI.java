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

//~--- non-JDK imports --------------------------------------------------------
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.helper.promote.TerminologyPromoterBI;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.contradiction.ContradictionIdentifierBI;
import org.ihtsdo.tk.db.DbDependency;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * The Interface TerminologyStoreDI.
 */
public interface TerminologyStoreDI extends TerminologyDI {

    /**
     * Adds a term change listener. Add this to receive notification for term
     * change events.
     *
     * @param termChangeListener the term change listener
     * @deprecated not in TK3 -- use TerminolgoyDI
     */
    @Deprecated
    void addTermChangeListener(TermChangeListener termChangeListener);

    /**
     * Suspend term change notifications.
     *
     * @deprecated not in TK3 -- use TerminolgoyDI
     */
    @Deprecated
    void suspendChangeNotifications();

    /**
     * Resume term change notifications.
     *
     * @deprecated not in TK3 -- use TerminologyDI
     */
    @Deprecated
    void resumeChangeNotifications();

    /**
     * Iterates over a set of concept data and processes it according to the
     * given
     * <code>processor</code>. Concepts are iterated in parallel
     * (multi-threaded) and the processor must be thread safe.
     *
     * @param processor the concept processor
     * @throws Exception indicates an exception has occurred
     * @see ProcessUnfetchedConceptDataBI
     */
    void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception;

    /**
     * Iterates over a set of concept data and processes it according to the
     * given
     * <code>processor</code>. Concepts are iterated in sequence
     * (single-threaded).
     *
     * @param processor the concept processor
     * @throws Exception indicates an exception has occurred
     * @see ProcessUnfetchedConceptDataBI
     *
     */
    void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception;

    /**
     * Iterates over a set of stamp data in sequence and processes it according
     * to the given
     * <code>processor</code>. Stamps are iterated in sequence
     * (single-threaded).
     *
     * @param processor the concept processor
     * @throws Exception indicates an exception has occurred
     * @see ProcessUnfetchedConceptDataBI
     *
     */
    void iterateStampDataInSequence(ProcessStampDataBI processor) throws Exception;

    /**
     * Removes the specified
     * <code>termChangeListener</code>.
     *
     * @param termChangeListener the term change listener
     */
    void removeTermChangeListener(TermChangeListener termChangeListener);

    /**
     * Creates a new position based on the given
     * <code>path</code> and
     * <code>time</code>.
     *
     * @param path the path to use for the position
     * @param time the time to use for the position
     * @return the position specified by the path and time
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated not in TK3 -- use TerminologyDI
     */
    @Deprecated
    PositionBI newPosition(PathBI path, long time) throws IOException;

    /**
     * Checks if the specified
     * <code>dependencies</code> are satisfied.
     *
     * @param dependencies the dependencies in question
     * @return <code>true</code>, if the dependencies are satisfied
     * @deprecated not in TK3 -- use TerminologyDI
     */
    @Deprecated
    boolean satisfiesDependencies(Collection<DbDependency> dependencies);

    /**
     * Can be used to determine if the metadata used conforms to Release Format
     * 1 (RF1) or Release Format 2 (RF2) standards. The database is checked
     * once, upon opening, to see what type of metadata is used, and that value
     * determines what this method returns.
     *
     * @return <code>true</code>, if the database uses RF2 metadata
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated not in TK3
     */
    @Deprecated
    boolean usesRf2Metadata() throws IOException;

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets a component chronicle for the given
     * <code>uuids</code>.
     *
     * @param uuids the uuids of the desired component
     * @return the component chronicle associated with the uuids
     * @throws IOException signals that an I/O exception has occurred
     */
    ComponentChronicleBI<?> getComponent(Collection<UUID> uuids) throws IOException;

    /**
     * Gets a component chronicle for the given
     * <code>componentContainer</code>.
     *
     * @param componentContainer the component container that contains the
     * desired component
     * @return the component chronicle contained by the component container
     * @throws IOException signals that an I/O exception has occurred
     */
    ComponentChronicleBI<?> getComponent(ComponentContainerBI componentContainer) throws IOException;

    /**
     * Gets a component chronicle associated with the given
     * <code>nid</code>.
     *
     * @param nid the nid of the desired component
     * @return the component chronicle specified by the nid
     * @throws IOException signals that an I/O exception has occurred
     */
    ComponentChronicleBI<?> getComponent(int nid) throws IOException;

    /**
     * Gets a component chronicle associated with the given
     * <code>uuids</code>.
     *
     * @param uuids the uuids of the desired component
     * @return the component chronicle associated with the uuids
     * @throws IOException signals that an I/O exception has occurred
     */
    ComponentChronicleBI<?> getComponent(UUID... uuids) throws IOException;

    /**
     * Gets a component version based on the
     * <code>viewCoordinate</code> for the component associated with the given
     * <code>uuids</code>.
     *
     * @param viewCoordinate the view coordinate specifying which version should
     * be returned
     * @param uuids the uuids of the desired component
     * @return the specified version of the component
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found
     */
    ComponentVersionBI getComponentVersion(ViewCoordinate viewCoordinate, Collection<UUID> uuids)
            throws IOException, ContradictionException;

    /**
     * Gets a component version based on the
     * <code>viewCoordinate</code> for the component associated with the given
     * <code>nid</code>.
     *
     * @param viewCoordinate the view coordinate specifying which version should
     * be returned
     * @param nid the nid of the desired component
     * @return the specified version of the component
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found
     */
    ComponentVersionBI getComponentVersion(ViewCoordinate viewCoordinate, int nid)
            throws IOException, ContradictionException;

    /**
     * Gets a component version based on the
     * <code>ViewCoordinate</code> for the component associated with the given
     * uuid(s).
     *
     * @param viewCoordinate the view coordinate specifying which version should
     * be returned
     * @param uuids the uuids of the desired component
     * @return the specified version of the component
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found
     */
    ComponentVersionBI getComponentVersion(ViewCoordinate viewCoordinate, UUID... uuids)
            throws IOException, ContradictionException;

    /**
     * Gets a concept chronicle associated with the given uuids.
     *
     * @param uuids the uuids of the desired concept
     * @return the concept chronicle associated with the uuids
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptChronicleBI getConcept(Collection<UUID> uuids) throws IOException;

    /**
     * Gets a concept chronicle associated with the given concept container.
     *
     * @param conceptContainer the concept container which contains the concept
     * @return the concept chronicle contained by the concept container
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptChronicleBI getConcept(ConceptContainerBI conceptContainer) throws IOException;

    /**
     * Gets a concept chronicle associated with the given
     * <code>concpetNid</code>.
     *
     * @param conceptNid the nid of the desired concept
     * @return the concept chronicle associated with the nid
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptChronicleBI getConcept(int conceptNid) throws IOException;

    /**
     * Gets a concept chronicle associated with the given
     * <code>uuids</code>.
     *
     * @param uuids the uuids of the desired concept
     * @return the concept chronicle associated with the uuids
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptChronicleBI getConcept(UUID... uuids) throws IOException;

    /**
     * Gets a concept chronicle for given
     * <code>nid</code>.
     *
     * @param nid the nid of a component within a concept
     * @return the concept chronicle which contains the component specified by
     * the nid
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptChronicleBI getConceptForNid(int nid) throws IOException;

    /**
     * Gets a concept nid for the given
     * <code>nid</code>.
     *
     * @param nid the nid of a component within a concept
     * @return the concept nid associated with the concept which contains the
     * component specified by the nid
     * @throws IOException signals that an I/O exception has occurred
     */
    int getConceptNidForNid(int nid) throws IOException;

    /**
     * Gets a concept version based on the given
     * <code>viewCoordinate</code> for the component associated with the given
     * <code>uuids</code>.
     *
     * @param viewCoordinate the view coordinate specifying which version should
     * be returned
     * @param uuids the uuids associated with the desired concept
     * @return the specified version of the concept
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptVersionBI getConceptVersion(ViewCoordinate viewCoordinate, Collection<UUID> uuids) throws IOException;

    /**
     * Gets a concept version based on the given
     * <code>viewCoordinate</code> for the component associated with the given
     * <code>conceptNid</code>.
     *
     * @param viewCoordinate the view coordinate specifying which version should
     * be returned
     * @param conceptNid the concept nid of the desired concept
     * @return the specified version of the concept
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptVersionBI getConceptVersion(ViewCoordinate viewCoordinate, int conceptNid) throws IOException;

    /**
     * Gets the concept version based on the given
     * <code>viewCoordinate</code> for the component associated with the given
     * <code>uuids</code>.
     *
     * @param viewCoordinate the view coordinate specifying which version should
     * be returned
     * @param uuids the uuids of the desired concept
     * @return the specified version of the concept
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptVersionBI getConceptVersion(ViewCoordinate viewCoordinate, UUID... uuids) throws IOException;

    /**
     * Gets the concept versions based on the given
     * <code>viewCoordinate</code> for the component associated with the given
     * <code>conceptNids</code>.
     *
     * @param viewCoordinate the view coordinate specifying which version should
     * be returned
     * @param conceptNids the concept nids associated with the desired concepts
     * @return a map of concept nids to the concept version
     * @throws IOException signals that an I/O exception has occurred
     */
    Map<Integer, ConceptVersionBI> getConceptVersions(ViewCoordinate viewCoordinate, NidBitSetBI conceptNids) throws IOException;

    /**
     * Gets the concepts associated with the given
     * <code>conceptNids</code>.
     *
     * @param conceptNids a set of concept nids of the desired concepts
     * @return the a map of the concept nids to the associated concept
     * @throws IOException signals that an I/O exception has occurred
     */
    Map<Integer, ConceptChronicleBI> getConcepts(NidBitSetBI conceptNids) throws IOException;

    /**
     * Gets a conflict identifier to use for identifying conflicts.
     *
     * @param viewCoordinate the view coordinate specifying which versions are
     * active or inactive
     * @param useCase set to <code>true</code> if the conflict identifier should
     * return the conflicting versions
     * @return a conflict identifier
     */
    ContradictionIdentifierBI getConflictIdentifier(ViewCoordinate viewCoordinate, boolean useCase);

    /**
     * Gets an empty nid set.
     *
     * @return an empty nid set
     * @throws IOException signals that an I/O exception has occurred
     */
    NidBitSetBI getEmptyNidSet() throws IOException;

    /**
     * Gets the latest change set dependencies. Can associate a changeset
     * dependency with an object, such as a task. This method gets a collection
     * of all of the changeset dependencies for the latest changesets to be
     * written and read.
     *
     * @return the latest change set dependencies
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<DbDependency> getLatestChangeSetDependencies() throws IOException;

    /**
     * Gets a generic view coordinate. This is appropriate to use when a
     * <code>viewCoordinate</code> is needed, but no user/preferences have been
     * selected. For example, in a mojo which would run at build time. Views on
     * the Architectonic Auxiliary path.
     *
     * @return a generic view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated not in TK3 -- use TerminologyDI
     */
    @Deprecated
    ViewCoordinate getMetadataViewCoordinate() throws IOException;

    /**
     * Gets a generic edit coordinate. This is appropriate to use when an
     * <code>editCoordinate</code> is needed, but no user/preferences have been
     * selected. For example, in a mojo which would run at build time. Edits on
     * the Architectonic Auxiliary path, with the SNOMED CT core module, and
     * "user" as author.
     *
     * @return a generic edit coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated not in TK3 -- use TerminologyDI
     */
    @Deprecated
    EditCoordinate getMetadataEditCoordinate() throws IOException;

    /**
     * Gets the nid for the component specified by the given
     * <code>uuids</code>.
     *
     * @param uuids the uuids associated with the component with the desired nid
     * @return the nid of the component associated with the given uuids
     * @throws IOException signals that an I/O exception has occurred
     */
    int getNidForUuids(Collection<UUID> uuids) throws IOException;

    /**
     * Gets the nid for the component specified by the given
     * <code>uuids</code>.
     *
     * @param uuids the uuids associated with the component with the desired nid
     * @return the nid of the component associated with the given uuids
     * @throws IOException signals that an I/O exception has occurred
     */
    int getNidForUuids(UUID... uuids) throws IOException;

    /**
     * Gets max stamp nid associated with any component in the read-only
     * database.
     *
     * @return the stamp nid
     * @deprecated not in TK3
     */
    @Deprecated
    int getReadOnlyMaxStamp();

    /**
     * Gets the paths which have the path specified by the given
     * <code>pathNid</code> as an immediate origin.
     *
     * @param pathNid the path nid associated with the desired origin path
     * @return a list of paths which are a child of the specified path
     */
    List<? extends PathBI> getPathChildren(int pathNid);

    /**
     * Gets the nids of the possible children of the concept specified by the
     * <code>conceptNid</code> for the given
     * <code>viewCoordinate</code>.
     *
     * @param conceptNid the concept nid associated with the desired parent
     * concept
     * @param viewCoordinate the view coordinate specifying which version of the
     * concept should be used as a parent
     * @return the nids of the possible child concepts
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one set of children exists
     * for the version specified by the view coordinate
     */
    int[] getPossibleChildren(int conceptNid, ViewCoordinate viewCoordinate) throws IOException, ContradictionException;

    /**
     * Gets the sequence representing the last change in the database. For
     * example, this sequence can be used to determine if the database has
     * changed since a component was drawn, and therefor, if the layout needs to
     * be refreshed.
     *
     * @return the sequence representing the last change in the database
     *
     */
    long getSequence();

    /**
     * Gets a terminology snapshot for the version of the terminology specified
     * by the </code>viewCoordinate</code>. The snapshot returns versions of
     * components rather than chronicles.
     *
     * @param viewCoordinate the view coordinate specifying which version of
     * components and concepts should be used by the methods on *
     * the <code>TerminolgoySnapshotDI</code> interface.
     * @return the terminology snapshot as specified
     */
    TerminologySnapshotDI getSnapshot(ViewCoordinate viewCoordinate);

    /**
     * Gets a terminology builder which uses the given
     * <code>editCoordinate</code> and
     * <code>viewCoordinate</code> to construct blueprints.
     *
     * @param editCoordinate the edit coordinate to use when constructing
     * blueprints
     * @param viewCoordinate the view coordinate to use when constructing
     * blueprints
     * @return the terminology builder
     * @see CreateOrAmendBlueprint
     */
    TerminologyBuilderBI getTerminologyBuilder(EditCoordinate editCoordinate, ViewCoordinate viewCoordinate);

    /**
     * Gets any uncommitted concepts in the database.
     *
     * @return any uncommitted concepts in the database
     */
    Collection<? extends ConceptChronicleBI> getUncommittedConcepts();

    /**
     * Gets the primordial uuid for the concept specified by the given
     * <code>nid</code>. The primordial uuid is the uuid associated with the
     * first version of a component. More than one uuid can be associated with a
     * component, but each component will have only one primordial uuid.
     *
     * @param nid the nid of the concept for which to find the primordial uuid
     * @return the primordial UUID if known. The IUnknown UUID
     * (00000000-0000-0000-C000-000000000046) if not known.
     * @throws IOException signals that an I/O exception has occurred
     */
    UUID getUuidPrimordialForNid(int nid) throws IOException;

    /**
     * Gets the uuids of the concept specified by the given
     * <code>nid</code>.
     *
     * @param nid the nid of the concept for which to find the uuids
     * @return the uuids of the concept specified by the nid
     * @throws IOException signals that an I/O exception has occurred
     */
    List<UUID> getUuidsForNid(int nid) throws IOException;

    /**
     * Checks to see if the database contains the path specified by the given
     * <code>nid</code>.
     *
     * @param nid the nid associated with the path in question
     * @return <code>true</code>, if the database has the desired path
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean hasPath(int nid) throws IOException;

    /**
     * Checks to see if the database has any uncommitted changes.
     *
     * @return <code>true</code>, if uncommitted changes are found
     */
    boolean hasUncommittedChanges();

    /**
     * Checks to see if the database contains the specified
     * <code>uuid</code>. Other methods which get a uuid for nid will create the
     * uuid in the database. This method can be used to check for the existence
     * of a uuid before trying to use it.
     *
     * @param uuid the uuid to check for
     * @return <code>true</code>, if the database contains the desired uuid
     */
    boolean hasUuid(UUID uuid);

    /**
     * Checks to see if the database contains the specified
     * <code>uuids</code>. Other methods which get a uuid for nid will create
     * the uuid in the database. This method can be used to check for the
     * existence of a list of uuids before trying to use them.
     *
     * @param uuids the uuids to check for
     * @return <code>true</code>, if the database contains the desired uuids
     */
    boolean hasUuid(List<UUID> uuids);

    /**
     * Used to delete an uncommitted change to a relationship specified by the
     * given
     * <code>relationshipVersion</code>.
     *
     * @param relationshipVersion the uncommitted relationship version to forget
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated not in TK3 -- use TerminologySnapshotDI
     */
    @Deprecated
    void forget(RelationshipVersionBI relationshipVersion) throws IOException;

    /**
     * Used to delete an uncommitted change to a description specified by the
     * given
     * <code>descriptionVersion</code>.
     *
     * @param descriptionVersion the uncommitted description version to forget
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated not in TK3 -- use TerminologySnapshotDI
     */
    @Deprecated
    void forget(DescriptionVersionBI descriptionVersion) throws IOException;

    /**
     * Used to delete an uncommitted change to a refex specified by the given
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex which has the uncommitted changes to
     * forget
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated not in TK3 -- use TerminologySnapshotDI
     */
    @Deprecated
    void forget(RefexChronicleBI refexChronicle) throws IOException;

    /**
     * Used to delete an uncommitted change to the concept attributes specified
     * by the given
     * <code>conceptAttributeVersion</code>.
     *
     * @param conceptAttributeVersion the uncommitted concept attribute version
     * to forget
     * @return <code>true</code>, if successful
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated not in TK3 -- use TerminologySnapshotDI
     */
    @Deprecated
    boolean forget(ConceptAttributeVersionBI conceptAttributeVersion) throws IOException;

    /**
     * Used to delete any uncommitted changes to the concept specified by the
     * given
     * <code>conceptChronicle</code>.
     *
     * @param conceptChronicle the concept which has the uncommitted changes to
     * forget
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated not in TK3 -- use TerminologySnapshotDI
     */
    @Deprecated
    void forget(ConceptChronicleBI conceptChronicle) throws IOException;

    /**
     * Gets the path nid specified by the given
     * <code>stampNid</code>.
     *
     * @param stampNid the stamp nid
     * @return the path nid of the given stmap nid
     * @see StampBI
     * @deprecated not in TK3
     */
    @Deprecated
    int getPathNidForStampNid(int stampNid);

    /**
     * Gets the author nid specified by the given
     * <code>stampNid</code>.
     *
     * @param stampNid the stamp nid
     * @return the author nid of the given stamp nid
     * @see StampBI
     * @deprecated not in TK3 -- use TerminolgoyDI
     */
    @Deprecated
    int getAuthorNidForStampNid(int stampNid);

    /**
     * Gets the status nid specified by the given
     * <code>stampNid</code>.
     *
     * @param stampNid the stamp nid
     * @return the status nid of the given stamp nid
     * @see StampBI
     * @deprecated not in TK3 -- use TerminolgoyDI
     */
    @Deprecated
    int getStatusNidForStampNid(int stampNid);

    /**
     * Gets the module nid specified by the given
     * <code>stampNid</code>.
     *
     * @param stampNid the stamp nid
     * @return the module nid specified by the given stamp nid
     * @see StampBI
     * @deprecated not in TK3 -- use TerminolgoyDI
     */
    @Deprecated
    int getModuleNidForStampNid(int stampNid);

    /**
     * Gets the time specified by the given
     * <code>stampNid</code>.
     *
     * @param stampNid the stamp nid
     * @return the time specified by the given stamp nid
     * @see StampBI
     * @deprecated not in TK3 -- use TerminolgoyDI
     */
    @Deprecated
    long getTimeForStampNid(int stampNid);

    /**
     * Adds a vetoable property change listener as specified by the
     * <code>vetoableChangeListener</code> for the given
     * <code>conceptEvent</code>. Only CONCEPT_EVENT.PRE_COMMIT is a vetoable
     * change.
     *
     * @param conceptEvent the concept event the listener is registered for
     * @param vetoableChangeListener the vetoable change listener
     * @deprecated not in TK3 -- use TerminolgoyDI
     */
    @Deprecated
    void addVetoablePropertyChangeListener(CONCEPT_EVENT conceptEvent, VetoableChangeListener vetoableChangeListener);

    /**
     * Adds a property change listener as specified by the
     * <code>propertyChangeListener</code> for the given
     * <code>conceptEvent</code>.
     *
     * @param conceptEvent the concept event the listener is registered for
     * @param propertyChangeListener the property change listener
     * @deprecated not in TK3 -- use TerminolgoyDI
     */
    @Deprecated
    void addPropertyChangeListener(CONCEPT_EVENT conceptEvent, PropertyChangeListener propertyChangeListener);

    /**
     * Checks to see of the component specified by the
     * <code>componentNid</code> is a member of the refex collection specified
     * by the
     * <code>refexNid</code>.
     *
     * @param refexNid the nid associated with the refex collection in question
     * @param componentNid the component nid associated with the component in
     * question
     * @return <code>true</code>, if the specified component is a member of the
     * specified refex
     */
    boolean hasExtension(int refexNid, int componentNid);

    /**
     * The Enum CONCEPT_EVENT lists the possible types of concept events.
     */
    public enum CONCEPT_EVENT {

        /**
         * The event occurred before a commit. This is a vetoable event.
         */
        PRE_COMMIT,
        /**
         * The event occurred after a commit.
         */
        POST_COMMIT,
        /**
         * The event occurred when a change was added as uncommitted.
         */
        ADD_UNCOMMITTED,
        /**
         * The event occurred after a sucessful commit.
         */
        POST_SUCESSFUL_COMMIT;
    }

    /**
     * Notifies the property change listeners that the component specified by
     * the
     * <code>nid</code> has changed. This is called during addUncommitted,
     * commit, and cancel. Call if a component is changed and will, somehow, not
     * use any of the previous methods listed.
     *
     * @param nid the nid associated with the changed component
     * @deprecated not in TK3
     */
    @Deprecated
    void touchComponent(int nid);

    /**
     * Creates a change notification event that the component associated with
     * the given
     * <code>nid</code> has changed.
     *
     * @param nid the nid associated with the changed component
     * @deprecated not in TK3
     */
    @Deprecated
    void touchComponentAlert(int nid);

    /**
     * Creates a change notification event that there is a template available
     * for the component associated with the given
     * <code>nid</code>.
     *
     * @param nid the nid associated with component for the template
     * @deprecated not in TK3
     */
    @Deprecated
    void touchComponentTemplate(int nid);

    /**
     * Notifies the property change listeners that the components specified by
     * <code>conceptNids</code> have changed. This is called during
     * addUncommitted, commit, and cancel. Call if components are changed and
     * will, somehow, not use any of the previous methods listed.
     *
     * @param conceptNids the concept nids associated with the changed component
     * @deprecated not in TK3
     */
    @Deprecated
    void touchComponents(Collection<Integer> conceptNids);

    /**
     * Notifies the property change listeners that the referenced component in a
     * refex specified by
     * <code>nid</code> has changed. This is called during addUncommitted,
     * commit, and cancel. Call if the referenced component is changed and will,
     * somehow, not use any of the previous methods listed.
     *
     * @param nid the nid associated with the referenced component
     * @deprecated not in TK3
     */
    @Deprecated
    void touchRefexRC(int nid);

    /**
     * Notifies the property change listeners that the relationship origin
     * concept specified by
     * <code>nid</code> has changed. This is called during addUncommitted,
     * commit, and cancel. Call if the concept is changed and will, somehow, not
     * use any of the previous methods listed.
     *
     * @param nid the nid associated with the relationship origin concept
     * @deprecated not in TK3
     */
    @Deprecated
    void touchRelOrigin(int nid);

    /**
     * Notifies the property change listeners that the relationship target
     * concept specified by
     * <code>nid</code> has changed. This is called during addUncommitted,
     * commit, and cancel. Call if the concept is changed and will, somehow, not
     * use any of the previous methods listed.
     *
     * @param nid the nid associated with the relationship target concept
     * @deprecated not in TK3
     */
    @Deprecated
    void touchRelTarget(int nid);

    /**
     * Gets the stamp nid associated with the specified
     * <code>version</code>.
     *
     * @param version the version with the desired stamp nid
     * @return the stamp nid
     * @throws IOException signals that an I/O exception has occurred
     * @see StampBI
     */
    int getStampNid(TkRevision version) throws IOException;

    /**
     * Checks if the concept specified by the
     * <code>childNid</code> is a kind of the concept specified by the
     * <code>parentNid</code>. Is kind of considers all possible children, not
     * just the direct children of the parent concept.
     *
     * @param childNid the nid associated with the child concept
     * @param parentNid the nid associated with the parent concept
     * @param viewCoordinate the viewCoordinate specifying which version of the
     * concepts in question to use
     * @return <code>true</code>, if the child concept is kind of the parent
     * concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a concept is
     * found for the given view coordinate
     */
    boolean isKindOf(int childNid, int parentNid, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException;
    
    /**
     * Checks if the concept specified by the
     * <code>childNid</code> was ever a kind of the concept specified by the
     * <code>parentNid</code>. Is kind of considers all possible children, not
     * just the direct children of the parent concept.
     *
     * @param childNid the nid associated with the child concept
     * @param parentNid the nid associated with the parent concept
     * @param viewCoordinate the viewCoordinate specifying which version of the
     * concepts in question to use. Will get converted to a view coordinate with all status values.
     * @return <code>true</code>, if the child concept is kind of the parent
     * concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a concept is
     * found for the given view coordinate
     */
    boolean wasEverKindOf(int childNid, int parentNid, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException;

    /**
     * Checks if the concept specified by the
     * <code>childNid</code> is a child of the concept specified by the
     * <code>parentNid</code>. Is child of only considers direct children of the
     * parent concept.
     *
     * @param childNid the nid associated with the child concept
     * @param parentNid the nid associated with the parent concept
     * @param viewCoordinate the viewCoordinate specifying which version of the
     * concepts in question to use
     * @return <code>true</code>, if the child concept is child of the parent
     * concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a concept is
     * found for the given view coordinate
     */
    boolean isChildOf(int childNid, int parentNid, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException;

    /**
     * Gets the nids of the ancestor concepts for the concept associated with
     * the given
     * <code>childNid</code>.
     *
     * @param childNid the nid associated with the child concept
     * @param viewCoordinate the viewCoordinate specifying which version of the
     * concepts in question to use
     * @return the nids of ancestor concepts
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a concept is
     * found for the given view coordinate
     */
    Set<Integer> getAncestors(int childNid, ViewCoordinate viewCoordinate) throws IOException, ContradictionException;
    
    /**
     * Gets the nids of the immediate children for the concept associated with
     * the given
     * <code>parentNid</code>.
     *
     * @param parentNid the nid associated with the parent concept
     * @param viewCoordinate the viewCoordinate specifying which version of the
     * concepts in question to use
     * @return the nids of ancestor concepts
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a concept is
     * found for the given view coordinate
     * @throws ValidationException if unable to validate the "Is a" concept
     */
    Set<Integer> getChildren(int parentNid, ViewCoordinate vc) throws ValidationException, IOException, ContradictionException;
    
    /**
     * Gets the nids of all the descendants including the parent for the concept associated with
     * the given
     * <code>parentNid</code>.
     *
     * @param parentNid the nid associated with the parent concept
     * @param viewCoordinate the viewCoordinate specifying which version of the
     * concepts in question to use
     * @return the nids of ancestor concepts
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a concept is
     * found for the given view coordinate
     * @throws ValidationException if unable to validate the "Is a" concept
     */
    NidBitSetBI getKindOf(int parentNid, ViewCoordinate vc) throws ValidationException, IOException, ContradictionException;

    /**
     * Gets the nids of incoming relationships source concepts.
     *
     * In the relationship A is a B, the concept A has a target relationship to
     * the concept B, and the concept B has a source relationship to the concept
     * A.
     *
     * @param conceptNid the nid of the concept in question
     * @param relTypes the nids of types of relationship to consider
     * @return the nids of the incoming relationships source concepts
     * @throws IOException signals that an I/O exception has occurred
     */
    int[] getIncomingRelationshipsSourceNids(int conceptNid, NidSetBI relTypes) throws IOException;

    /**
     * Regenerates workflow history lucene index. The workflow/lucene directory
     * must not exist for the index to be regenerated.
     *
     * @param viewCoordinate the view coordinate to use for finding workflow
     * history members
     * @return <code>true</code> when the index has been regenerated
     * @throws Exception indicates an exception has occurred
     */
    boolean regenerateWfHxLuceneIndex(ViewCoordinate viewCoordinate) throws Exception;
    
    /**
     * Promotes terminology from the path specified by the source view coordinate to the path specified by the target view coordinate.
     * Uses the metadata from the source edit coordinate to write to the target path. Use this method if the origin 
     * of the source path is the same as the target path. For example, Path B has an origin of Path A, and the desired promotion is from Path B to Path A.
     * @param sourceViewCoordinate the <code>ViewCoordinate</code> representing the source view position
     * @param sourceEditCoordinate the <code>EditCoordinate</code> representing the editing metadata to use in writing the promotion
     * @param targetViewCoordinate the <code>ViewCoordinate</code> representing the target view position
     * @return a <code>TerminoloyPromoterBI</code> object based on the specified coordinates
     */
    TerminologyPromoterBI getTerminologyPromoter(ViewCoordinate sourceViewCoordinate, EditCoordinate sourceEditCoordinate,
            ViewCoordinate targetViewCoordinate);
    /**
     * Promotes terminology from the path specified by the source view coordinate to the path specified by the target path nid.
     * Uses the metadata from the source edit coordinate to write to the target path. Use this method if the origin of the source path
     * is different than the target path. For example, Path B has an origin of Path A, and the desired promotion is from Path B to Path C.
     * @param sourceViewCoordinate the <code>ViewCoordinate</code> representing the source view position
     * @param sourceEditCoordinate the <code>EditCoordinate</code> representing the editing metadata to use in writing the promotion
     * @param targetPath the <code>nid</code> representing the target path
     * @param originPosition the <code>PositionBI</code> representing the origin position of the source path
     * @return a <code>TerminoloyPromoterBI</code> object based on the specified coordinates
     */
    TerminologyPromoterBI getTerminologyPromoter(ViewCoordinate sourceViewCoordinate, EditCoordinate sourceEditCoordinate,
            int targetPath, PositionBI originPosition);
    /**
     * Can call to wait until all datachecks are finished running.
     */
    public void waitTillDatachecksFinished();
}
