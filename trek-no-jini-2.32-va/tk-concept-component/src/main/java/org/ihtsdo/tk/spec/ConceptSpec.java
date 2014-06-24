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


/**
 * The Class ConceptSpec provides a way of representing a concept in a
 * verifiable and human-readable way. The uuid and text of the spec is check to
 * ensure they both point the same concept. A concept spec can be used to find
 * both the nid and uuid that are associated with the represented concept as
 * well as the concept chronicle and version.
 *
 * <p> A concept should never be represented by only a uuid since this provides
 * no way of verifying the uuid is pointing to the correct concept and no
 * human-readable way of knowing which concept is identified.
 */
public class ConceptSpec implements SpecBI {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    /**
     * Writes the concept spec object, including the data version, description,
     * uuids, and relationship specs.
     *
     * @param out the output stream
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(description);
        out.writeObject(uuids);
        out.writeObject(relSpecs);
    }

    /**
     * Reads a concept spec object, including the data version, description,
     * uuids, and relationship specs.
     *
     * @param in the input stream
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
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
    private UUID[] uuids;
    private String description;
    private RelationshipSpec[] relSpecs;

    /**
     * Added to allow JavaBeans spec use.
     */
    public ConceptSpec() {
        super();
    }

    /**
     * Instantiates a new concept spec based on the given
     * <code>descriptionText</code> and
     * <code>conceptUuidString</code>.
     *
     * @param descriptionText the description text of the concept to represent
     * @param conceptUuidString a String representing the uuid of the concept to
     * represent
     */
    public ConceptSpec(String descriptionText, String conceptUuidString) {
        this(descriptionText, conceptUuidString, new RelationshipSpec[]{});
    }

    /**
     * Instantiates a new concept spec based on the given
     * <code>descriptionText</code>,
     * <code>conceptUuidString</code>, and
     * <code>relationshipSpecs</code>.
     *
     * @param descriptionText the description text of the concept to represent
     * @param conceptUuidString a String representing the uuid of the concept to
     * represent
     * @param relationshipSpecs the relationship specs representing the
     * relationships on the concept
     */
    public ConceptSpec(String descriptionText, String conceptUuidString, RelationshipSpec... relationshipSpecs) {
        this(descriptionText, UUID.fromString(conceptUuidString), relationshipSpecs);
    }

    /**
     * Instantiates a new concept spec based on the given
     * <code>descriptionText</code> and
     * <code>conceptUuid</code>.
     *
     * @param descriptionText the description text of the concept to represent
     * @param conceptUuid the uuid of the concept to represent
     */
    public ConceptSpec(String descriptionText, UUID conceptUuid) {
        this(descriptionText, new UUID[]{conceptUuid}, new RelationshipSpec[]{});
    }

    /**
     * Instantiates a new concept spec based on the given
     * <code>descriptionText</code>,
     * <code>conceptUuid</code>, and
     * <code>relationshipSpecs</code>.
     *
     * @param descriptionText the description text of the concept to represent
     * @param conceptUuid the uuid of the concept to represent
     * @param relationshipSpecs the relationship specs representing the
     * relationships on the concept
     */
    public ConceptSpec(String descriptionText, UUID conceptUuid, RelationshipSpec... relationshipSpecs) {
        this(descriptionText, new UUID[]{conceptUuid}, relationshipSpecs);
    }

    /**
     * Instantiates a new concept spec based on the given
     * <code>descriptionText</code>,
     * <code>conceptUuids</code>, and
     * <code>relationshipSpecs</code>.
     *
     * @param descriptionText the description text of the concept to represent
     * @param conceptUuids the uuids of the concept to represent
     * @param relationshipSpecs the relationship specs representing the
     * relationships on the concept
     */
    public ConceptSpec(String descriptionText, UUID[] conceptUuids, RelationshipSpec... relationshipSpecs) {
        this.uuids = conceptUuids;
        this.description = descriptionText;
        this.relSpecs = relationshipSpecs;
    }

    /**
     * Gets a
     * <code>ConceptChronicleBI</code> representing the concept associated with
     * this concept spec. Checks to see if the database has any of the uuids
     * associated with this concept. Validates the descriptions and
     * relationships. Does not guarantee that the concept returned is active.
     *
     * @return the concept chronicle representing the concept associated with
     * this concept spec
     * @throws ValidationException if the elements of a spec cannot be found, or
     * point to different concepts/components
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
     * Gets a
     * <code>ConceptVersionBI</code> representing the concept associated with
     * this concept spec. Checks to see if the database has any of the uuids
     * associated with this concept. Validates the descriptions and
     * relationships based on the view coordinate.
     *
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @return the concept chronicle representing the concept associated with
     * this concept spec
     * @throws ValidationException if the elements of a spec cannot be found, or
     * point to different concepts/components
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
     * Gets a
     * <code>ConceptVersionBI</code> representing the concept associated with
     * this concept spec.
     *
     * @param viewCoordinate the view coordinate specifying which version are active and inactive
     * @return the concept version
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated Use getStrict or getLienient instead.
     */
    @Deprecated
    public ConceptVersionBI get(ViewCoordinate viewCoordinate) throws IOException {
        return getStrict(viewCoordinate);
    }

    /**
     * Validates the relationships associated with this concept spec based on
     * the given
     * <code>conceptVersion</code> and
     * <code>viewCoordinate</code>. Ensures that the concept has the specified
     * active relationships.
     *
     * @param conceptVersion the concept version representing the concept to
     * validate
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
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

            for (ConceptVersionBI dest : conceptVersion.getRelationshipsOutgoingTargetConcepts(typeNids)) {
                if (dest.equals(destination)) {
                    continue next;
                }
            }
            throw new ValidationException("No match for RelSpec: " + relSpec);
        }
    }

    /**
     * Validates the relationships associated with this concept spec based on
     * the given
     * <code>conceptChronicle</code>. Ensures that the concept has the specified
     * relationships. Does not guarantee the relationships are active.
     *
     * @param conceptChronicle the concept version representing the concept to
     * validate
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
     * Validates the descriptions associated with this concept spec based on the
     * given
     * <code>conceptChronicle</code>. Ensures that the concept has the specified
     * descriptions. Does not guarantee the descriptions are active.
     *
     * @param conceptChronicle the concept version representing the concept to
     * validate
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * specified view coordinate
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
     * Validates the descriptions associated with this concept spec based on the
     * given
     * <code>conceptVersion</code> and
     * <code>viewCoordinate</code>. Ensures that the concept has the specified
     * active descriptions.
     *
     * @param conceptVersion the concept version representing the concept to
     * validate
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
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
     * Added as an alternative way to get the uuids as strings rather than UUID
     * objects this was done to help with Maven making use of this class.
     *
     * @return an array of strings representing the uuids
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
     * Added primarily for Maven so that using a String type configuration in a
     * POM file the UUIDs array could be set. This allows the ConceptSpec class
     * to be embedded into a object to be configured by Maven POM configuration.
     * Note that the ConceptDescriptor class also exists for a similar purpose,
     * however it exists in a dependent project and cannot be used in this
     * project.
     *
     * @param uuids an array of strings representing the uuids
     */
    public void setUuidsAsString(String[] uuids) {
        this.uuids = new UUID[uuids.length];
        int i = 0;
        for (String uuid : uuids) {
            this.uuids[i++] = UUID.fromString(uuid);
        }
    }

    /**
     * Gets the uuids associated with this concept spec. No validation is
     * performed.
     *
     * @return the uuids associated with this concept spec
     */
    public UUID[] getUuids() {
        return uuids;
    }

    /**
     * Sets the uuids associated with this concept spec.
     *
     * @param uuids the uuids associated with this concept spec
     */
    public void setUuids(UUID[] uuids) {
        this.uuids = uuids;
    }

    /**
     * ets the uuids associated with this concept spec from a string
     * representing the uuid.
     *
     * @param uuidStrings an array of strings representing the uuids
     */
    public void setUuidStrings(String[] uuidStrings) {
        this.uuids = new UUID[uuidStrings.length];
        for (int i = 0; i < uuidStrings.length; i++) {
            this.uuids[i] = UUID.fromString(uuidStrings[i]);
        }
    }

    /**
     * Gets an array of strings representing the uuids.
     *
     * @return an array of strings representing the uuids
     */
    public String[] getUuidStrings() {
        String[] results = new String[uuids.length];
        for (int i = 0; i < uuids.length; i++) {
            results[i] = uuids[i].toString();
        }
        return results;
    }

    /**
     * Gets the description associated with this concept spec.
     *
     * @return the description associated with this concept spec
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description associated with this concept spec.
     *
     * @param description the description associated with this concept spec
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the relationship specs associated with this concept spec.
     *
     * @return the relationship specs associated with this concept spec
     */
    public RelationshipSpec[] getRelationshipSpecs() {
        return relSpecs;
    }

    /**
     * Sets the relationship specs associated with this concept spec.
     *
     * @param relationshipSpecs the new relationship specs associated with this
     * concept spec
     */
    public void setRelationshipSpecs(RelationshipSpec[] relationshipSpecs) {
        this.relSpecs = relationshipSpecs;
    }

    /**
     * Returns a String representation of this concept spec object. Includes the
     * associated description and uuids.
     *
     * @return a string representing this concept spec
     */
    @Override
    public String toString() {
        return "ConceptSpec{" + description + "; " + Arrays.asList(uuids)
                + "}";
    }
}
