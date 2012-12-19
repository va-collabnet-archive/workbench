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
package org.ihtsdo.tk.spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class ConceptSpec implements SpecBI {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(description);
        out.writeObject(uuids);
        out.writeObject(relSpecs);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            description = in.readUTF();
            uuids = (UUID[]) in.readObject();
            relSpecs = (RelationshipSpec[]) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }
    private UUID[] uuids;
    private String description;
    private RelationshipSpec[] relSpecs;

    /**
     * added to allow JavaBeans spec use.
     */
    public ConceptSpec() {
        super();
    }

    public ConceptSpec(String descriptionText, String conceptUuid) {
        this(descriptionText, conceptUuid, new RelationshipSpec[]{});
    }

    public ConceptSpec(String descriptionText, String conceptUuid, RelationshipSpec... relationshipSpecs) {
        this(descriptionText, UUID.fromString(conceptUuid), relationshipSpecs);
    }

    public ConceptSpec(String descriptionText, UUID conceptUuid) {
        this(descriptionText, new UUID[]{conceptUuid}, new RelationshipSpec[]{});
    }

    public ConceptSpec(String descriptionText, UUID conceptUuid, RelationshipSpec... relationshipSpecs) {
        this(descriptionText, new UUID[]{conceptUuid}, relationshipSpecs);
    }

    public ConceptSpec(String descriptionText, UUID[] conceptUuids, RelationshipSpec... relationshipSpecs) {
        this.uuids = conceptUuids;
        this.description = descriptionText;
        this.relSpecs = relationshipSpecs;
    }

    public ConceptChronicleBI getLenient() throws ValidationException, IOException {
        try {
            boolean found = false;
            for (UUID uuid : uuids) {
                if (Ts.get().hasUuid(uuid)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
               throw new ValidationException("No matching ids in db: " + this.toString());
            }
            ConceptChronicleBI local = Ts.get().getConcept(uuids);
            validateDescription(local);
            validateRelationships(local);
            return local;
        } catch (ContradictionException e) {
            throw new ValidationException(e);
        }
    }

    public ConceptVersionBI getStrict(ViewCoordinate viewCoordinate) throws ValidationException, IOException {
        try {
            boolean found = false;
            for (UUID uuid : uuids) {
                if (Ts.get().hasUuid(uuid)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
               throw new ValidationException("No matching ids in db: " + this.toString());
            }

            ConceptVersionBI local = Ts.get().getConceptVersion(viewCoordinate, uuids);
            validateDescription(local, viewCoordinate);
            validateRelationships(local, viewCoordinate);
            return local;
        } catch (ContradictionException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * 
     * @param viewCoordinate
     * @return
     * @throws IOException
     * @deprecated Use getStrict or getLienient instead. 
     */
    @Deprecated
    public ConceptVersionBI get(ViewCoordinate viewCoordinate) throws IOException {
        return getStrict(viewCoordinate);
    }

    private void validateRelationships(ConceptVersionBI conceptVersion, ViewCoordinate viewCoordinate) throws IOException {
        if (relSpecs == null || relSpecs.length == 0) {
            return;
        }

        next:
        for (RelationshipSpec relSpec : relSpecs) {

            ConceptVersionBI relType = relSpec.getRelationshipTypeSpec().getStrict(viewCoordinate);
            ConceptVersionBI destination = relSpec.getTargetSpec().getStrict(viewCoordinate);
            NidSetBI typeNids = new NidSet();
            typeNids.add(relType.getNid());

            for (ConceptVersionBI dest : conceptVersion.getRelationshipsSourceTargetConcepts(typeNids)) {
                if (dest.equals(destination)) {
                    continue next;
                }
            }
            throw new ValidationException("No match for RelSpec: " + relSpec);
        }
    }

    private void validateRelationships(ConceptChronicleBI conceptChronicle) throws IOException {
        if (relSpecs == null || relSpecs.length == 0) {
            return;
        }

        next:
        for (RelationshipSpec relSpec : relSpecs) {

            ConceptChronicleBI relType = relSpec.getRelationshipTypeSpec().getLenient();
            ConceptChronicleBI destination = relSpec.getTargetSpec().getLenient();
            NidSetBI typeNids = new NidSet();
            typeNids.add(relType.getNid());

            for (RelationshipChronicleBI rel : conceptChronicle.getRelationshipsOutgoing()) {
                for (RelationshipVersionBI rv : rel.getVersions()) {
                    if (rv.getTypeNid() == relType.getNid()
                            && rv.getTargetNid() == destination.getNid()) {
                        continue next;
                    }
                }
            }
            throw new ValidationException("No match for RelSpec: " + relSpec);
        }
    }

    private void validateDescription(ConceptChronicleBI conceptChronicle) throws IOException, ContradictionException {
        boolean found = false;
        for (DescriptionChronicleBI desc : conceptChronicle.getDescriptions()) {
            for (DescriptionVersionBI descv : desc.getVersions()) {
                if (descv.getText().equals(description)) {
                    found = true;
                    break;
                }
            }
        }
        if (found == false) {

            throw new ValidationException("No description matching: '"
                    + description + "' found for:\n"
                    + conceptChronicle);
        }
    }

    private void validateDescription(ConceptVersionBI conceptVersion, ViewCoordinate viewCoordinate) throws IOException, ContradictionException {
        boolean found = false;
        for (DescriptionVersionBI desc : conceptVersion.getDescriptionsActive()) {
            if (desc.getText().equals(description)) {
                found = true;
                break;
            }
        }
        if (found == false) {

            throw new ValidationException("No description matching: '"
                    + description + "' found for:\n"
                    + conceptVersion);
        }
    }

    /**
     * added as an alternative way to get the uuids as strings rather than UUID
     * objects
     * this was done to help with Maven making use of this class
     */
    public String[] getUuidsAsString() {
        String[] returnVal = new String[uuids.length];
        int i = 0;
        for (UUID uuid : uuids) {
            returnVal[i++] = uuid.toString();
        }
        return returnVal;
    }

    /**
     * Added primarily for Maven so that using a String type configuration in
     * a POM file the UUIDs array could be set.
     * This allows the ConceptSpec class to be embedded into a object to be
     * configured
     * by Maven POM configuration. Note that the ConceptDescriptor class also
     * exists
     * for a similar purpose, however it exists in a dependent project and
     * cannot
     * be used in this project.
     */
    public void setUuidsAsString(String[] uuids) {
        this.uuids = new UUID[uuids.length];
        int i = 0;
        for (String uuid : uuids) {
            this.uuids[i++] = UUID.fromString(uuid);
        }
    }

    public UUID[] getUuids() {
        return uuids;
    }

    public void setUuids(UUID[] uuids) {
        this.uuids = uuids;
    }

    public void setUuidStrings(String[] uuidStrings) {
        this.uuids = new UUID[uuidStrings.length];
        for (int i = 0; i < uuidStrings.length; i++) {
            this.uuids[i] = UUID.fromString(uuidStrings[i]);
        }
    }

    public String[] getUuidStrings() {
        String[] results = new String[uuids.length];
        for (int i = 0; i < uuids.length; i++) {
            results[i] = uuids[i].toString();
        }
        return results;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RelationshipSpec[] getRelationshipSpecs() {
        return relSpecs;
    }

    public void setRelationshipSpecs(RelationshipSpec[] relationshipSpecs) {
        this.relSpecs = relationshipSpecs;
    }

    @Override
    public String toString() {
        return "ConceptSpec{" + description + "; " + Arrays.asList(uuids)
                + "}";
    }
}
