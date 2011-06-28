package org.ihtsdo.bdb.mojo;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.util.id.Type5UuidFactory;

public class ConceptDescriptor {

    private String pathFsDesc;
    private String uuid;
    private String description;

    public ConceptDescriptor() {
    }

    public ConceptDescriptor(String uuid, String description) {
        setUuid(uuid);
        setDescription(description);
    }

    public static boolean verify(I_GetConceptData concept, String description) throws Exception {
        // check that the description parameter corresponds to one of the
        // concept's descriptions
        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        List<? extends I_DescriptionTuple> descriptionTuples = concept.getDescriptionTuples(null, null, null,
                config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_DescriptionTuple tuple : descriptionTuples) {
            if (description.toLowerCase().trim().equals(tuple.getText().toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    public I_GetConceptData getVerifiedConcept() throws Exception {

        if (uuid == null && pathFsDesc != null) {
            uuid = Type5UuidFactory.get(
                    Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathFsDesc).toString();
        }
        if (uuid == null) {
            throw new Exception("UUID parameter must be specified.");
        } else if (description == null) {
            throw new Exception("Description parameter must be specified.");
        }

        I_TermFactory termFactory = Terms.get();
        List<UUID> uuidList = new LinkedList<UUID>();
        uuidList.add(UUID.fromString(uuid));
        I_GetConceptData concept = null;
        try {
            concept = termFactory.getConcept(uuidList);
            if (verify(concept, description)) {
                return concept;
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage() + " : " + description + " " + uuidList);
        }
        if (concept != null) {
            throw new Exception("Failed to find matching description: " + description + " " + uuidList
                    + " in concept: " + concept);
        }
        throw new Exception("Failed to find matching description: " + description + " " + uuidList);
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return description + " (" + uuid + ")";
    }

    public String getUuid() {
        return uuid;
    }

    public final void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    public String getPathFsDesc() {
        return pathFsDesc;
    }

    public void setPathFsDesc(String pathFsDesc) {
        this.pathFsDesc = pathFsDesc;
    }
}
