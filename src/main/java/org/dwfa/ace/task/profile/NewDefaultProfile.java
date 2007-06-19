package org.dwfa.ace.task.profile;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.DocumentAuxiliary;
import org.dwfa.cement.HL7;
import org.dwfa.cement.QueueType;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/profile", type = BeanType.TASK_BEAN) })
public class NewDefaultProfile extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String usernamePropName = ProcessAttachmentKeys.USERNAME.getAttachmentKey();
    private String passwordPropName = ProcessAttachmentKeys.PASSWORD.getAttachmentKey();
    private String adminUsernamePropName = ProcessAttachmentKeys.ADMIN_USERNAME.getAttachmentKey();
    private String adminPasswordPropName = ProcessAttachmentKeys.ADMIN_PASSWORD.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(usernamePropName);
        out.writeObject(passwordPropName);
        out.writeObject(adminUsernamePropName);
        out.writeObject(adminPasswordPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            profilePropName = (String) in.readObject();
            usernamePropName = (String) in.readObject();
            passwordPropName = (String) in.readObject();
            adminUsernamePropName = (String) in.readObject();
            adminPasswordPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            String username = (String) process.readProperty(usernamePropName);
            String password = (String) process.readProperty(passwordPropName);
            String adminUsername = (String) process.readProperty(adminUsernamePropName);
            String adminPassword = (String) process.readProperty(adminPasswordPropName);
            I_ConfigAceFrame newProfile = newProfile(username, password, 
                adminUsername, adminPassword);
            process.setProperty(profilePropName, newProfile);
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String address) {
        this.profilePropName = address;
    }

    
    public static I_ConfigAceFrame newProfile(String username, String password, 
        String adminUsername, String adminPassword)
            throws TerminologyException, IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        I_ConfigAceFrame activeConfig = tf.newAceFrameConfig();

        activeConfig.setUsername(username);
        activeConfig.setPassword(password);
        activeConfig.setAdminPassword(adminPassword);
        activeConfig.setAdminUsername(adminUsername);

        I_IntSet statusPopupTypes = tf.newIntSet();
        statusPopupTypes.add(tf.uuidToNative(Concept.ACTIVE.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.CURRENT.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.FLAGGED_FOR_REVIEW.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.LIMITED.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.PENDING_MOVE.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.CONSTANT.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.INACTIVE.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.RETIRED.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.DUPLICATE.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.OUTDATED.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.AMBIGUOUS.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.ERRONEOUS.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.LIMITED.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.INAPPROPRIATE.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.MOVED_ELSEWHERE.getUids()));
        statusPopupTypes.add(tf.uuidToNative(Concept.PENDING_MOVE.getUids()));
        activeConfig.setEditStatusTypePopup(statusPopupTypes);

        I_IntSet descPopupTypes = tf.newIntSet();
        descPopupTypes.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.UNSPECIFIED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.ENTRY_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.XHTML_DEF.getUids()));
        activeConfig.setEditDescTypePopup(descPopupTypes);

        I_IntSet relCharacteristic = tf.newIntSet();
        relCharacteristic.add(tf.uuidToNative(Concept.STATED_RELATIONSHIP.getUids()));
        relCharacteristic.add(tf.uuidToNative(Concept.INFERRED_RELATIONSHIP.getUids()));
        relCharacteristic.add(tf.uuidToNative(Concept.QUALIFIER_CHARACTERISTIC.getUids()));
        relCharacteristic.add(tf.uuidToNative(Concept.HISTORICAL_CHARACTERISTIC.getUids()));
        relCharacteristic.add(tf.uuidToNative(Concept.ADDITIONAL_CHARACTERISTIC.getUids()));
        activeConfig.setEditRelCharacteristicPopup(relCharacteristic);

        I_IntSet relRefinabilty = tf.newIntSet();
        relRefinabilty.add(tf.uuidToNative(Concept.MANDATORY_REFINABILITY.getUids()));
        relRefinabilty.add(tf.uuidToNative(Concept.OPTIONAL_REFINABILITY.getUids()));
        relRefinabilty.add(tf.uuidToNative(Concept.NOT_REFINABLE.getUids()));
        activeConfig.setEditRelRefinabiltyPopup(relRefinabilty);

        I_IntSet relTypes = tf.newIntSet();
        relTypes.add(tf.uuidToNative(Concept.IS_A_REL.getUids()));

        try {
            relTypes.add(tf.uuidToNative(SNOMED.Concept.IS_A.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }
        activeConfig.setEditRelTypePopup(relTypes);

        I_IntSet roots = tf.newIntSet();
        roots.add(tf.uuidToNative(Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids()));
        roots.add(tf.uuidToNative(Concept.STATUS.getUids()));
        roots.add(tf.uuidToNative(Concept.DESCRIPTION_TYPE.getUids()));
        addIfNotNull(roots, SNOMED.Concept.ROOT, tf);
        addIfNotNull(roots, DocumentAuxiliary.Concept.DOCUMENT_AUXILIARY, tf);
        addIfNotNull(roots, RefsetAuxiliary.Concept.REFSET_AUXILIARY, tf);
        addIfNotNull(roots, HL7.Concept.HL7, tf);
        addIfNotNull(roots, QueueType.Concept.QUEUE_TYPE, tf);
        activeConfig.setRoots(roots);

        I_IntSet allowedStatus = tf.newIntSet();
        allowedStatus.add(tf.uuidToNative(Concept.ACTIVE.getUids()));
        allowedStatus.add(tf.uuidToNative(Concept.CURRENT.getUids()));
        allowedStatus.add(tf.uuidToNative(Concept.FLAGGED_FOR_REVIEW.getUids()));
        allowedStatus.add(tf.uuidToNative(Concept.LIMITED.getUids()));
        allowedStatus.add(tf.uuidToNative(Concept.PENDING_MOVE.getUids()));
        allowedStatus.add(tf.uuidToNative(Concept.CONFLICTING.getUids()));
        allowedStatus.add(tf.uuidToNative(Concept.CONSTANT.getUids()));
        allowedStatus.add(tf.uuidToNative(Concept.CONCEPT_RETIRED.getUids()));
        allowedStatus.add(tf.uuidToNative(Concept.FLAGGED_POTENTIAL_DESC_STYLE_ERROR.getUids()));
        allowedStatus.add(tf.uuidToNative(Concept.FLAGGED_POTENTIAL_DUPLICATE.getUids()));
        allowedStatus.add(tf.uuidToNative(Concept.FLAGGED_POTENTIAL_REL_ERROR.getUids()));
        activeConfig.setAllowedStatus(allowedStatus);

        I_IntSet destRelTypes = tf.newIntSet();
        destRelTypes.add(tf.uuidToNative(Concept.IS_A_REL.getUids()));

        try {
            destRelTypes.add(tf.uuidToNative(SNOMED.Concept.IS_A.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }
        activeConfig.setDestRelTypes(destRelTypes);

        I_IntSet sourceRelTypes = tf.newIntSet();
        activeConfig.setSourceRelTypes(sourceRelTypes);

        I_IntSet descTypes = tf.newIntSet();
        descTypes.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        descTypes.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        descTypes.add(tf.uuidToNative(Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));
        descTypes.add(tf.uuidToNative(Concept.DESCRIPTION_TYPE.getUids()));
        descTypes.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        descTypes.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        descTypes.add(tf.uuidToNative(Concept.XHTML_DEF.getUids()));
        activeConfig.setDescTypes(descTypes);

        I_IntSet inferredViewTypes = tf.newIntSet();
        inferredViewTypes.add(tf.uuidToNative(Concept.INFERRED_RELATIONSHIP.getUids()));
        activeConfig.setInferredViewTypes(inferredViewTypes);

        I_IntSet statedViewTypes = tf.newIntSet();
        statedViewTypes.add(tf.uuidToNative(Concept.STATED_RELATIONSHIP.getUids()));
        statedViewTypes.add(tf.uuidToNative(Concept.DEFINING_CHARACTERISTIC.getUids()));
        statedViewTypes.add(tf.uuidToNative(Concept.ADDITIONAL_CHARACTERISTIC.getUids()));
        statedViewTypes.add(tf.uuidToNative(Concept.HISTORICAL_CHARACTERISTIC.getUids()));
        statedViewTypes.add(tf.uuidToNative(Concept.QUALIFIER_CHARACTERISTIC.getUids()));
        activeConfig.setStatedViewTypes(statedViewTypes);

        activeConfig.setDefaultDescriptionType(tf.getConcept(Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));
        activeConfig.setDefaultRelationshipCharacteristic(tf.getConcept(Concept.STATED_RELATIONSHIP.getUids()));
        activeConfig.setDefaultRelationshipRefinability(tf.getConcept(Concept.OPTIONAL_REFINABILITY.getUids()));
        activeConfig.setDefaultRelationshipType(tf.getConcept(Concept.IS_A_REL.getUids()));
        activeConfig.setDefaultStatus(tf.getConcept(Concept.ACTIVE.getUids()));

        I_IntList treeDescPrefList = activeConfig.getTreeDescPreferenceList();
        treeDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        treeDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        treeDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        treeDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

        I_IntList shortLabelDescPrefList = activeConfig.getShortLabelDescPreferenceList();
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

        I_IntList longLabelDescPrefList = activeConfig.getLongLabelDescPreferenceList();
        longLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        longLabelDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        longLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        longLabelDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));

        I_IntList tableDescPrefList = activeConfig.getTableDescPreferenceList();
        tableDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        tableDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        tableDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        tableDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

        activeConfig.setDefaultStatus(tf.getConcept(Concept.CURRENT.getUids()));
        activeConfig.setDefaultDescriptionType(tf.getConcept(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

        activeConfig.setDefaultRelationshipType(tf.getConcept(Concept.IS_A_REL.getUids()));
        activeConfig.setDefaultRelationshipCharacteristic(tf.getConcept(Concept.DEFINING_CHARACTERISTIC.getUids()));
        activeConfig.setDefaultRelationshipRefinability(tf.getConcept(Concept.OPTIONAL_REFINABILITY.getUids()));

        if (activeConfig.getUsername() == null) {
            activeConfig.setChangeSetWriterFileName("nullUser." + UUID.randomUUID().toString() + ".jcs");
        } else {
            activeConfig.setChangeSetWriterFileName(activeConfig.getUsername() + "." + UUID.randomUUID().toString()
                    + ".jcs");
        }
        
        I_Path editPath = tf.getPath(Concept.ARCHITECTONIC_BRANCH.getUids());
        //activeConfig.addEditingPath(editPath);

        I_Position viewPosition = tf.newPosition(editPath, Integer.MAX_VALUE);
        Set<I_Position> viewSet = new HashSet<I_Position>();
        viewSet.add(viewPosition);
        activeConfig.setViewPositions(viewSet);

        return activeConfig;
    }

    private static void addIfNotNull(I_IntSet roots, I_ConceptualizeUniversally concept, I_TermFactory tf)
            throws TerminologyException, IOException {
        try {
            roots.add(tf.uuidToNative(concept.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }
    }

    public String getAdminPasswordPropName() {
        return adminPasswordPropName;
    }

    public void setAdminPasswordPropName(String adminPasswordPropName) {
        this.adminPasswordPropName = adminPasswordPropName;
    }

    public String getAdminUsernamePropName() {
        return adminUsernamePropName;
    }

    public void setAdminUsernamePropName(String adminUsernamePropName) {
        this.adminUsernamePropName = adminUsernamePropName;
    }

    public String getPasswordPropName() {
        return passwordPropName;
    }

    public void setPasswordPropName(String passwordPropName) {
        this.passwordPropName = passwordPropName;
    }

    public String getUsernamePropName() {
        return usernamePropName;
    }

    public void setUsernamePropName(String usernamePropName) {
        this.usernamePropName = usernamePropName;
    }

}
