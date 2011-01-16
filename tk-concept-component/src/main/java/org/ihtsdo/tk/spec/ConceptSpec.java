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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

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
            relSpecs = (RelSpec[]) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    private UUID[] uuids;

    private String description;

    private RelSpec[] relSpecs;

    /**
     * added to allow JavaBeans spec use.
     */
    public ConceptSpec() {
        super();
    }

    public ConceptSpec(String description, String uuid) {
        this(description, uuid, new RelSpec[] {});
    }

    public ConceptSpec(String description, String uuid, RelSpec... relSpecs) {
        this(description, UUID.fromString(uuid), relSpecs);
    }

    public ConceptSpec(String description, UUID uuid) {
        this(description, new UUID[] { uuid }, new RelSpec[] {});
    }

    public ConceptSpec(String description, UUID uuid, RelSpec... relSpecs) {
        this(description, new UUID[] { uuid }, relSpecs);
    }

    public ConceptSpec(String description, UUID[] uuids, RelSpec... relSpecs) {
        this.uuids = uuids;
        this.description = description;
        this.relSpecs = relSpecs;
    }

    public ConceptVersionBI get(ViewCoordinate c) throws IOException  {
            try {
				ConceptVersionBI local = Ts.get().getConceptVersion(c, uuids);
				validateDescription(local, c);
				validateRelationships(local, c);
				return local;
			} catch (ContraditionException e) {
				throw new ValidationException(e);
			}
    }

    private boolean validateRelationships(ConceptVersionBI local, ViewCoordinate c) throws IOException {
        if (relSpecs == null || relSpecs.length == 0) {
            return true;
        }

        for (RelSpec relSpec : relSpecs) {
        	ConceptVersionBI relType = relSpec.getRelTypeSpec().get(c);
        	ConceptVersionBI destination = relSpec.getDestinationSpec().get(c);
            List<ConceptVersionBI> destinationsOfType = new ArrayList<ConceptVersionBI>();
            NidSetBI typeNids = new NidSet();
            typeNids.add(relType.getNid());
            
            for (ConceptVersionBI dest : local.getRelsOutgoingDestinations(typeNids)) {
                destinationsOfType.add(dest);
                if (dest.equals(destination)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void validateDescription(ConceptVersionBI local, ViewCoordinate c) throws IOException, ContraditionException {
        boolean found = false;
        for (DescriptionVersionBI desc : local.getDescsActive()) {
            if (desc.getText().equals(description)) {
                found = true;
                break;
            }
        }
        if (found == false) {
            throw new RuntimeException("No description matching: " + description + " found for: " + local);
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
    
    public void setUuidStrs(String[] uuidStrs) {
        this.uuids = new UUID[uuidStrs.length];
        for (int i = 0; i < uuidStrs.length; i++) {
            this.uuids[i] = UUID.fromString(uuidStrs[i]);
        }
    }

    public String[] getUuidStrs() {
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

    public RelSpec[] getRelSpecs() {
        return relSpecs;
    }

    public void setRelSpecs(RelSpec[] relSpecs) {
        this.relSpecs = relSpecs;
    }

    @Override
    public String toString() {
        return "ConceptSpec{" + description + "; " + Arrays.asList(uuids) +
                "}";
    }
    
    
}
