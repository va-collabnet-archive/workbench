/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class ProjectPermissionsAPI.
 */
public class ProjectPermissionsAPI {

    /**
     * The config.
     */
    private I_ConfigAceFrame config;

    /**
     * Instantiates a new project permissions api.
     *
     * @param config the config
     */
    public ProjectPermissionsAPI(I_ConfigAceFrame config) {
        super();
        this.config = config;
    }

    /**
     * Gets the config.
     *
     * @return the config
     */
    public I_ConfigAceFrame getConfig() {
        return config;
    }

    /**
     * Sets the config.
     *
     * @param config the new config
     */
    public void setConfig(I_ConfigAceFrame config) {
        this.config = config;
    }

    /**
     * Adds the permission.
     *
     * @param user the user
     * @param permission the permission
     * @param domain the domain
     * @throws Exception the exception
     */
    public void addPermission(I_GetConceptData user, I_GetConceptData permission, I_GetConceptData domain) throws Exception {
        I_TermFactory tf = Terms.get();

        I_RelVersioned relVersioned = tf.newRelationship(UUID.randomUUID(), user, permission, domain,
                tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
                tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
                tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

        tf.addUncommittedNoChecks(user);
        permission.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
    }

    /**
     * Removes the permission.
     *
     * @param user the user
     * @param permission the permission
     * @param domain the domain
     * @throws Exception the exception
     */
    public void removePermission(I_GetConceptData user, I_GetConceptData permission, I_GetConceptData domain) throws Exception {
        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(permission.getConceptNid());

        List<? extends I_RelTuple> relationships = user.getSourceRelTuples(
                config.getAllowedStatus(),
                allowedTypes,
                config.getViewPositionSetReadOnly(),
                Precedence.TIME,
                config.getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            removePermission(rel);
        }

    }

    /**
     * Removes the permission.
     *
     * @param relationship the relationship
     * @throws Exception the exception
     */
    public void removePermission(I_RelTuple relationship) throws Exception {
        I_TermFactory tf = Terms.get();
        I_GetConceptData concept = tf.getConcept(relationship.getC1Id());

        for (PathBI editPath : config.getEditingPathSet()) {
            I_RelPart newPart = (I_RelPart) relationship.getMutablePart().makeAnalog(
                    ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
                    Long.MAX_VALUE,
                    config.getDbConfig().getUserConcept().getNid(),
                    config.getEditCoordinate().getModuleNid(),
                    editPath.getConceptNid());
            relationship.getFixedPart().addVersion(newPart);
            tf.addUncommittedNoChecks(concept);
            concept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
        }

    }

    /**
     * Check permission for hierarchy.
     *
     * @param user the user
     * @param target the target
     * @param permission the permission
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public boolean checkPermissionForHierarchy(I_GetConceptData user, I_GetConceptData target,
            I_GetConceptData permission) throws IOException, TerminologyException, ContradictionException {
        boolean permisionGranted = false;
        I_TermFactory tf = Terms.get();

        I_IntSet isaType = tf.newIntSet();
        isaType.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

        I_IntSet allowedTypes = tf.newIntSet();
        allowedTypes.add(permission.getConceptNid());

        for (I_GetConceptData parent : permission.getSourceRelTargets(
                config.getAllowedStatus(),
                allowedTypes, config.getViewPositionSetReadOnly(),
                Precedence.TIME, config.getConflictResolutionStrategy())) {
            allowedTypes.add(parent.getConceptNid());
        }

        List<? extends I_RelTuple> relationships = user.getSourceRelTuples(
                config.getAllowedStatus(),
                allowedTypes, config.getViewPositionSetReadOnly(),
                Precedence.TIME, config.getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            I_GetConceptData relC2 = tf.getConcept(rel.getC2Id());
            if (relC2.isParentOfOrEqualTo(target, config.getAllowedStatus(),
                    config.getDestRelTypes(), config.getViewPositionSetReadOnly(),
                    config.getPrecedence(), config.getConflictResolutionStrategy())) {
                permisionGranted = true;
            }
        }

        return permisionGranted;
    }

    /**
     * Check permission for project.
     *
     * @param user the user
     * @param target the target
     * @param permission the permission
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public boolean checkPermissionForProject(I_GetConceptData user, I_GetConceptData target,
            I_GetConceptData permission) throws IOException, TerminologyException {
        boolean permisionGranted = false;
        long latestVersion = Long.MIN_VALUE;

        I_IntSet isaType = Terms.get().newIntSet();
        isaType.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(permission.getConceptNid());

        for (I_GetConceptData parent : permission.getSourceRelTargets(
                config.getAllowedStatus(),
                isaType,
                config.getViewPositionSetReadOnly(),
                Precedence.TIME,
                config.getConflictResolutionStrategy())) {
            allowedTypes.add(parent.getConceptNid());
        }

        I_RelTuple lastestTuple = null;
        List<? extends I_RelTuple> relationships = user.getSourceRelTuples(
                config.getAllowedStatus(),
                allowedTypes,
                config.getViewPositionSetReadOnly(),
                Precedence.TIME,
                config.getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            if (rel.getC2Id() == target.getConceptNid()) {
                if (rel.getTime() > latestVersion) {
                    latestVersion = rel.getTime();
                    lastestTuple = rel;
                }
            }
        }

        if (lastestTuple == null) {
            for (I_GetConceptData parent : target.getSourceRelTargets(
                    config.getAllowedStatus(),
                    isaType,
                    config.getViewPositionSetReadOnly(),
                    Precedence.TIME,
                    config.getConflictResolutionStrategy())) {
                relationships = user.getSourceRelTuples(
                        config.getAllowedStatus(),
                        allowedTypes,
                        config.getViewPositionSetReadOnly(),
                        Precedence.TIME,
                        config.getConflictResolutionStrategy());
                for (I_RelTuple rel : relationships) {
                    if (rel.getC2Id() == parent.getConceptNid()) {
                        if (rel.getTime() > latestVersion) {
                            latestVersion = rel.getTime();
                            lastestTuple = rel;
                        }
                    }
                }
                if (lastestTuple == null) {
                    for (I_GetConceptData parent2 : parent.getSourceRelTargets(
                            config.getAllowedStatus(),
                            isaType,
                            config.getViewPositionSetReadOnly(),
                            Precedence.TIME,
                            config.getConflictResolutionStrategy())) {
                        relationships = user.getSourceRelTuples(
                                config.getAllowedStatus(),
                                allowedTypes,
                                config.getViewPositionSetReadOnly(),
                                Precedence.TIME,
                                config.getConflictResolutionStrategy());
                        for (I_RelTuple rel : relationships) {
                            if (rel.getC2Id() == parent2.getConceptNid()) {
                                if (rel.getTime() > latestVersion) {
                                    latestVersion = rel.getTime();
                                    lastestTuple = rel;
                                }
                            }
                        }
                        if (lastestTuple == null) {
                            for (I_GetConceptData parent3 : parent2.getSourceRelTargets(
                                    config.getAllowedStatus(),
                                    isaType,
                                    config.getViewPositionSetReadOnly(),
                                    Precedence.TIME,
                                    config.getConflictResolutionStrategy())) {
                                relationships = user.getSourceRelTuples(
                                        config.getAllowedStatus(),
                                        allowedTypes,
                                        config.getViewPositionSetReadOnly(),
                                        Precedence.TIME,
                                        config.getConflictResolutionStrategy());
                                for (I_RelTuple rel : relationships) {
                                    if (rel.getC2Id() == parent3.getConceptNid()) {
                                        if (rel.getTime() > latestVersion) {
                                            latestVersion = rel.getTime();
                                            lastestTuple = rel;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (lastestTuple != null) {
            if (isActive(lastestTuple.getStatusNid())) {
                permisionGranted = true;
            }
        }

        return permisionGranted;
    }


    /**
     * Check permission for project.
     *
     * @param user the user
     * @param target the target
     * @param permission the permission
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public boolean getPermissionForUser(I_GetConceptData user, I_GetConceptData permission) throws IOException, TerminologyException {
        boolean permisionGranted = false;
        long latestVersion = Long.MIN_VALUE;

        I_IntSet isaType = Terms.get().newIntSet();
        isaType.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(permission.getConceptNid());

        for (I_GetConceptData parent : permission.getSourceRelTargets(
                config.getAllowedStatus(),
                isaType,
                config.getViewPositionSetReadOnly(),
                Precedence.TIME,
                config.getConflictResolutionStrategy())) {
            allowedTypes.add(parent.getConceptNid());
        }

        I_RelTuple lastestTuple = null;
        List<? extends I_RelTuple> relationships = user.getSourceRelTuples(
                config.getAllowedStatus(),
                allowedTypes,
                config.getViewPositionSetReadOnly(),
                Precedence.TIME,
                config.getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            if (rel.getTime() > latestVersion) {
                latestVersion = rel.getTime();
                lastestTuple = rel;
            }
        }

        if (lastestTuple != null) {
            if (isActive(lastestTuple.getStatusNid())) {
                permisionGranted = true;
            }
        }

        return permisionGranted;
    }

    /**
     * Gets the users for role.
     *
     * @param role the role
     * @param project the project
     * @return the users for role
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public Set<I_GetConceptData> getUsersForRole(I_GetConceptData role, I_GetConceptData project) throws IOException, TerminologyException {

        Set<I_GetConceptData> returnUsers = new HashSet<I_GetConceptData>();
        Set<I_GetConceptData> allUsers = getUsers();

        for (I_GetConceptData user : allUsers) {
            if (checkPermissionForProject(user, project, role)) {
                returnUsers.add(user);
            }
        }

        return returnUsers;
    }

    /**
     * Gets the descendants.
     *
     * @param descendants the descendants
     * @param concept the concept
     *
     * @return the descendants
     */
    public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
        try {
            I_TermFactory termFactory = Terms.get();
            // TODO add config as parameter
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            I_IntSet allowedDestRelTypes = termFactory.newIntSet();
            allowedDestRelTypes.add(Snomed.IS_A.getLenient().getNid());
            allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
            Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
            childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(),
                    config.getConflictResolutionStrategy()));
            descendants.addAll(childrenSet);
            for (I_GetConceptData loopConcept : childrenSet) {
                descendants = getDescendants(descendants, loopConcept);
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return descendants;
    }

    /**
     * Gets the roles for user.
     *
     * @param user the user
     * @param project the project
     * @return the roles for user
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public Set<I_GetConceptData> getRolesForUser(I_GetConceptData user) throws IOException, TerminologyException {

        Set<I_GetConceptData> returnRoles = new HashSet<I_GetConceptData>();
        Set<I_GetConceptData> allRoles = new HashSet<I_GetConceptData>();
        // allRoles = getDescendants(allRoles, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_ROLE.getUids()));

        if (Terms.get().getActiveAceFrameConfig().isVaProject()) {
        	// VA
        	allRoles = getDescendants(allRoles, Terms.get().getConcept(UUID.fromString("824308c5-1bdb-5f32-9558-faa51f650118")));
        } else {
        	// JIF
        	allRoles = getDescendants(allRoles, Terms.get().getConcept(UUID.fromString("f6561a78-a48f-594f-89df-5f77a215d807")));
        }

        for (I_GetConceptData role : allRoles) {
            if (getPermissionForUser(user, role)) {
                returnRoles.add(role);
            }
        }

        return returnRoles;
    }

    /**
     * Gets the permissions for user.
     *
     * @param user the user
     * @return the permissions for user
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public Map<I_GetConceptData, I_GetConceptData> getPermissionsForUser(I_GetConceptData user) throws IOException, TerminologyException {
        I_TermFactory tf = Terms.get();

        Map<I_GetConceptData, I_GetConceptData> returnRoles = new HashMap<I_GetConceptData, I_GetConceptData>();
        Set<I_GetConceptData> allRoles = new HashSet<I_GetConceptData>();
//        allRoles = getDescendants(allRoles, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_ROLE.getUids()));
       
        if (Terms.get().getActiveAceFrameConfig().isVaProject()) {
        	// VA
        	allRoles = getDescendants(allRoles, tf.getConcept(UUID.fromString("824308c5-1bdb-5f32-9558-faa51f650118")));
        } else {
        	// JIF
        	allRoles = getDescendants(allRoles, tf.getConcept(UUID.fromString("f6561a78-a48f-594f-89df-5f77a215d807")));
        }

        Set<Integer> allRolesNid = new HashSet<Integer>();
        for (I_GetConceptData loopRole : allRoles) {
            allRolesNid.add(loopRole.getNid());
        }

        I_IntSet isaType = tf.newIntSet();
        isaType.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());


        List<? extends I_RelTuple> relationships = user.getSourceRelTuples(
                config.getAllowedStatus(),
                null, config.getViewPositionSetReadOnly(),
                Precedence.TIME, config.getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            if (allRolesNid.contains(rel.getTypeNid())) {
                I_GetConceptData role = tf.getConcept(rel.getTypeNid());
                I_GetConceptData hierarchy = tf.getConcept(rel.getC2Id());
                returnRoles.put(role, hierarchy);
            }
        }

        return returnRoles;
    }


    public Map<I_GetConceptData, Set<I_GetConceptData>> getMultiplePermissionsForUser(I_GetConceptData user) throws IOException, TerminologyException {
        I_TermFactory tf = Terms.get();

        Map<I_GetConceptData, Set<I_GetConceptData>> returnRoles = new HashMap<I_GetConceptData, Set<I_GetConceptData>>();
        Set<I_GetConceptData> allRoles = new HashSet<I_GetConceptData>();
//        allRoles = getDescendants(allRoles, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_ROLE.getUids()));
       
        if (Terms.get().getActiveAceFrameConfig().isVaProject()) {
        	// VA
        	allRoles = getDescendants(allRoles, tf.getConcept(UUID.fromString("824308c5-1bdb-5f32-9558-faa51f650118")));
        } else {
        	// JIF
        	allRoles = getDescendants(allRoles, tf.getConcept(UUID.fromString("f6561a78-a48f-594f-89df-5f77a215d807")));
        }

        Set<Integer> allRolesNid = new HashSet<Integer>();
        for (I_GetConceptData loopRole : allRoles) {
            allRolesNid.add(loopRole.getNid());
        }

        I_IntSet isaType = tf.newIntSet();
        isaType.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());


        List<? extends I_RelTuple> relationships = user.getSourceRelTuples(
                config.getAllowedStatus(),
                null, config.getViewPositionSetReadOnly(),
                Precedence.TIME, config.getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            if (allRolesNid.contains(rel.getTypeNid())) {
                I_GetConceptData role = tf.getConcept(rel.getTypeNid());
                I_GetConceptData hierarchy = tf.getConcept(rel.getC2Id());
                
                if (!returnRoles.containsKey(role)) {
                	returnRoles.put(role,  new HashSet<I_GetConceptData>());
                }
                
                returnRoles.get(role).add(hierarchy);
            }
        }

        return returnRoles;
    }
    
    /**
     * Calculates a set of valid users - a user is valid is they are a child of
     * the User concept in the top hierarchy, and have a description of type
     * "user inbox".
     *
     * @return The set of valid users.
     */
    public Set<I_GetConceptData> getUsers() {
        HashSet<I_GetConceptData> validUsers = new HashSet<I_GetConceptData>();
        try {
            I_GetConceptData roleParent =
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.TRANSLATOR_ROLE.getUids());

            I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();

            Set<? extends I_GetConceptData> allRoles = roleParent.getDestRelOrigins(config.getAllowedStatus(),
                    allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME,
                    config.getConflictResolutionStrategy());
            I_GetConceptData descriptionType = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids());
            I_IntSet descAllowedTypes = Terms.get().newIntSet();
            descAllowedTypes.add(descriptionType.getConceptNid());

            for (I_GetConceptData user : allRoles) {

                I_DescriptionTuple latestTuple = null;
                long latestVersion = Long.MIN_VALUE;

                List<? extends I_DescriptionTuple> descriptionResults =
                        user.getDescriptionTuples(null, descAllowedTypes, Terms.get()
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(),
                        Precedence.TIME, config.getConflictResolutionStrategy());
                for (I_DescriptionTuple descriptionTuple : descriptionResults) {
                    if (descriptionTuple.getTime() > latestVersion) {
                        latestVersion = descriptionTuple.getTime();
                        latestTuple = descriptionTuple;
                    }
                }
                if (latestTuple != null) {
                    for (int currentStatusId : Terms.get().getActiveAceFrameConfig().getViewCoordinate().getAllowedStatusNids().getSetValues()) {
                        if (latestTuple.getStatusNid() == currentStatusId) {
                            validUsers.add(user);
                        }
                    }
                }
            }

        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return validUsers;
    }

    /**
     * Gets the users inbox addresses.
     *
     * @return the users inbox addresses
     */
    public Set<String> getUsersInboxAddresses() {
        HashSet<String> inboxes = new HashSet<String>();
        try {
            I_GetConceptData userParent =
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());

            I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();

            Set<? extends I_GetConceptData> allUsers = userParent.getDestRelOrigins(config.getAllowedStatus(),
                    allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME,
                    config.getConflictResolutionStrategy());
            I_GetConceptData descriptionType =
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids());
            I_IntSet descAllowedTypes = Terms.get().newIntSet();
            descAllowedTypes.add(descriptionType.getConceptNid());

            for (I_GetConceptData user : allUsers) {

                I_DescriptionTuple latestTuple = null;
                long latestVersion = Long.MIN_VALUE;

                List<? extends I_DescriptionTuple> descriptionResults =
                        user.getDescriptionTuples(null, descAllowedTypes, Terms.get()
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(),
                        Precedence.TIME, config.getConflictResolutionStrategy());
                for (I_DescriptionTuple descriptionTuple : descriptionResults) {
                    if (descriptionTuple.getTime() > latestVersion) {
                        latestVersion = descriptionTuple.getTime();
                        latestTuple = descriptionTuple;
                    }
                }
                if (latestTuple != null) {
                    for (int currentStatusId : Terms.get().getActiveAceFrameConfig().getViewCoordinate().getAllowedStatusNids().getSetValues()) {
                        if (latestTuple.getStatusNid() == currentStatusId) {
                            inboxes.add(latestTuple.getText());
                        }
                    }
                }
            }

        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return inboxes;
    }

    /**
     * Gets the users inbox addresses for role.
     *
     * @param role the role
     * @param project the project
     * @return the users inbox addresses for role
     */
    public Set<String> getUsersInboxAddressesForRole(I_GetConceptData role, I_GetConceptData project) {
        HashSet<String> inboxes = new HashSet<String>();
        try {
            I_GetConceptData userParent =
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());

            I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();

            Set<? extends I_GetConceptData> allUsers = userParent.getDestRelOrigins(config.getAllowedStatus(),
                    allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME,
                    config.getConflictResolutionStrategy());
            I_GetConceptData descriptionType =
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids());
            I_IntSet descAllowedTypes = Terms.get().newIntSet();
            descAllowedTypes.add(descriptionType.getConceptNid());

            for (I_GetConceptData user : allUsers) {
                if (checkPermissionForProject(user, project, role)) {
                    I_DescriptionTuple latestTuple = null;
                    long latestVersion = Long.MIN_VALUE;

                    List<? extends I_DescriptionTuple> descriptionResults =
                            user.getDescriptionTuples(null, descAllowedTypes, Terms.get()
                            .getActiveAceFrameConfig().getViewPositionSetReadOnly(),
                            Precedence.TIME, config.getConflictResolutionStrategy());
                    for (I_DescriptionTuple descriptionTuple : descriptionResults) {
                        if (descriptionTuple.getTime() > latestVersion) {
                            latestVersion = descriptionTuple.getTime();
                            latestTuple = descriptionTuple;
                        }
                    }
                    if (latestTuple != null) {
                        for (int currentStatusId : Terms.get().getActiveAceFrameConfig().getViewCoordinate().getAllowedStatusNids().getSetValues()) {
                            if (latestTuple.getStatusNid() == currentStatusId) {
                                inboxes.add(latestTuple.getText());
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return inboxes;
    }

    /**
     * Checks if is active.
     *
     * @param statusId the status id
     * @return true, if is active
     */
    public boolean isActive(int statusId) {
        List<Integer> activeStatuses = new ArrayList<Integer>();
        try {
            activeStatuses.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
            activeStatuses.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            activeStatuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
            activeStatuses.add(ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid());
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return (activeStatuses.contains(statusId));
    }

    /**
     * Checks if is inactive.
     *
     * @param statusId the status id
     * @return true, if is inactive
     */
    public boolean isInactive(int statusId) {
        List<Integer> inactiveStatuses = new ArrayList<Integer>();
        try {
            inactiveStatuses.add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
            inactiveStatuses.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
            inactiveStatuses.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return (inactiveStatuses.contains(statusId));
    }
}
