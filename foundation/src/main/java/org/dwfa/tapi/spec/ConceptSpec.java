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
package org.dwfa.tapi.spec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_RelateConceptsLocally;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;

public class ConceptSpec {

    private UUID[] uuids;

    private String description;

    private RelSpec[] relSpecs;

    I_ConceptualizeLocally local;

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

    public I_ConceptualizeLocally localize() {
        try {
            if (local == null) {
                local = LocalFixedConcept.get(Arrays.asList(uuids));
            }

            validateDescription(local);
            validateRelationships(local);
            return local;
        } catch (NoMappingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TerminologyException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateRelationships(I_ConceptualizeLocally local) throws IOException, TerminologyException {
        if (relSpecs == null || relSpecs.length == 0) {
            return true;
        }

        for (RelSpec relSpec : relSpecs) {
            I_ConceptualizeLocally relType = relSpec.getRelType().localize();
            I_ConceptualizeLocally destination = relSpec.getDestination().localize();
            boolean foundDestination = false;
            boolean foundType = false;
            List<I_ConceptualizeLocally> destinationsOfType = new ArrayList<I_ConceptualizeLocally>();

            for (I_RelateConceptsLocally rel : local.getSourceRels()) {
                if (rel.getRelType().equals(relType)) {
                    foundType = true;
                    destinationsOfType.add(rel.getC2());
                    if (rel.getC2().equals(destination)) {
                        foundDestination = true;
                        break;
                    }
                }
            }
            if (foundDestination == false) {
                boolean foundTransitively = false;
                if (foundType == true && relSpec.isTransitive()) {
                    for (I_ConceptualizeLocally destinationOfType : destinationsOfType) {
                        if (validateRelationships(destinationOfType)) {
                            foundTransitively = true;
                            break;
                        }
                    }
                }
                if (foundTransitively == false) {
                    throw new RuntimeException("No matching rel: " + relSpec + " found for: " + local);
                }
            }
        }
        return true;
    }

    private void validateDescription(I_ConceptualizeLocally local) throws IOException, TerminologyException {
        boolean found = false;
        for (I_DescribeConceptLocally desc : local.getDescriptions()) {
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

}
