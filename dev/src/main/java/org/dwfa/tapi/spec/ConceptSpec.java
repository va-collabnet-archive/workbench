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

    public ConceptSpec(String description, String uuid) {
        this(description, uuid, new RelSpec[]{});
    }

    public ConceptSpec(String description, String uuid, RelSpec... relSpecs) {
        this(description, UUID.fromString(uuid), relSpecs);
    }

    public ConceptSpec(String description, UUID uuid) {
        this(description, new UUID[] { uuid }, new RelSpec[]{});
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
            I_ConceptualizeLocally local = LocalFixedConcept.get(Arrays.asList(uuids));
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
                     for (I_ConceptualizeLocally destinationOfType: destinationsOfType) {
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

}
