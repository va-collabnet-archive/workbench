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

// TODO: Auto-generated Javadoc
/**
 * The Class ConceptSpec.
 */
public class ConceptSpec implements SpecBI {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    /** The Constant dataVersion. */
    private static final int dataVersion = 1;

    /**
     * Write object.
     *
     * @param out the out
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(description);
        out.writeObject(uuids);
        out.writeObject(relSpecs);
    }

    /**
     * Read object.
     *
     * @param in the in
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException the class not found exception
     */
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
    
    /** The uuids. */
    private UUID[] uuids;
    
    /** The description. */
    private String description;
    
    /** The rel specs. */
    private RelationshipSpec[] relSpecs;

    /**
     * added to allow JavaBeans spec use.
     */
    public ConceptSpec() {
        super();
    }

    /**
     * Instantiates a new concept spec.
     *
     * @param descriptionText the description text
     * @param conceptUuid the concept uuid
     */
    public ConceptSpec(String descriptionText, String conceptUuid) {
        this(descriptionText, conceptUuid, new RelationshipSpec[]{});
    }

    /**
     * Instantiates a new concept spec.
     *
     * @param descriptionText the description text
     * @param conceptUuid the concept uuid
     * @param relationshipSpecs the relationship specs
     */
    public ConceptSpec(String descriptionText, String conceptUuid, RelationshipSpec... relationshipSpecs) {
        this(descriptionText, UUID.fromString(conceptUuid), relationshipSpecs);
    }

    /**
     * Instantiates a new concept spec.
     *
     * @param descriptionText the description text
     * @param conceptUuid the concept uuid
     */
    public ConceptSpec(String descriptionText, UUID conceptUuid) {
        this(descriptionText, new UUID[]{conceptUuid}, new RelationshipSpec[]{});
    }

    /**
     * Instantiates a new concept spec.
     *
     * @param descriptionText the description text
     * @param conceptUuid the concept uuid
     * @param relationshipSpecs the relationship specs
     */
    public ConceptSpec(String descriptionText, UUID conceptUuid, RelationshipSpec... relationshipSpecs) {
        this(descriptionText, new UUID[]{conceptUuid}, relationshipSpecs);
    }

    /**
     * Instantiates a new concept spec.
     *
     * @param descriptionText the description text
     * @param conceptUuids the concept uuids
     * @param relationshipSpecs the relationship specs
     */
    public ConceptSpec(String descriptionText, UUID[] conceptUuids, RelationshipSpec... relationshipSpecs) {
        this.uuids = conceptUuids;
        this.description = descriptionText;
        this.relSpecs = relationshipSpecs;
    }

    /**
     * Gets the lenient.
     *
     * @return the lenient
     * @throws ValidationException the validation exception
     * @throws IOException signals that an I/O exception has occurred
     */
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

    /**
     * Gets the strict.
     *
     * @param viewCoordinate the view coordinate
     * @return the strict
     * @throws ValidationException the validation exception
     * @throws IOException signals that an I/O exception has occurred
     */
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
     * Gets the.
     *
     * @param viewCoordinate the view coordinate
     * @return the concept version bi
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated Use getStrict or getLienient instead.
     */
    @Deprecated
    public ConceptVersionBI get(ViewCoordinate viewCoordinate) throws IOException {
        return getStrict(viewCoordinate);
    }

    /**
     * Validate relationships.
     *
     * @param conceptVersion the concept version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     */
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

    /**
     * Validate relationships.
     *
     * @param conceptChronicle the concept chronicle
     * @throws IOException signals that an I/O exception has occurred
     */
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

    /**
     * Validate description.
     *
     * @param conceptChronicle the concept chronicle
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException the contradiction exception
     */
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

    /**
     * Validate description.
     *
     * @param conceptVersion the concept version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException the contradiction exception
     */
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
     * this was done to help with Maven making use of this class.
     *
     * @return the uuids as string
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
     *
     * @param uuids the new uuids as string
     */
    public void setUuidsAsString(String[] uuids) {
        this.uuids = new UUID[uuids.length];
        int i = 0;
        for (String uuid : uuids) {
            this.uuids[i++] = UUID.fromString(uuid);
        }
    }

    /**
     * Gets the uuids.
     *
     * @return the uuids
     */
    public UUID[] getUuids() {
        return uuids;
    }

    /**
     * Sets the uuids.
     *
     * @param uuids the new uuids
     */
    public void setUuids(UUID[] uuids) {
        this.uuids = uuids;
    }

    /**
     * Sets the uuid strings.
     *
     * @param uuidStrings the new uuid strings
     */
    public void setUuidStrings(String[] uuidStrings) {
        this.uuids = new UUID[uuidStrings.length];
        for (int i = 0; i < uuidStrings.length; i++) {
            this.uuids[i] = UUID.fromString(uuidStrings[i]);
        }
    }

    /**
     * Gets the uuid strings.
     *
     * @return the uuid strings
     */
    public String[] getUuidStrings() {
        String[] results = new String[uuids.length];
        for (int i = 0; i < uuids.length; i++) {
            results[i] = uuids[i].toString();
        }
        return results;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the relationship specs.
     *
     * @return the relationship specs
     */
    public RelationshipSpec[] getRelationshipSpecs() {
        return relSpecs;
    }

    /**
     * Sets the relationship specs.
     *
     * @param relationshipSpecs the new relationship specs
     */
    public void setRelationshipSpecs(RelationshipSpec[] relationshipSpecs) {
        this.relSpecs = relationshipSpecs;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ConceptSpec{" + description + "; " + Arrays.asList(uuids)
                + "}";
    }
}
