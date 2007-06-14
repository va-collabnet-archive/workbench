package org.dwfa.mojo;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * Goal which copies the latest changes on a component to another branch.
 * @goal vodb-copy-latest
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbCopyLatestComponent extends AbstractMojo {

    /**
     * Branch to which the latest component of specified branch will be
     * copied to.
     * @parameter
     */
    private String branchToCopyTo;

    /**
     * Branch which will be copied to new branch.
     * @parameter
     */
    private String branchToCopy;

    private class CopyLatestComponent implements I_ProcessConcepts {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        public void processConcept(I_GetConceptData concept) throws Exception {

            // get origins
            I_Path originPath = termFactory.getPath(ArchitectonicAuxiliary.
                                Concept.ARCHITECTONIC_BRANCH.getUids());

            I_Position originPosition = termFactory.newPosition(originPath,
                                                    Integer.MAX_VALUE);

            Set<I_Position> origins = new HashSet<I_Position>();
            origins.add(originPosition);

            // get the copy-to concept/path
            I_GetConceptData copyToConcept = getConceptFromString(branchToCopyTo);
            I_Path copyToPath = termFactory.newPath(origins, copyToConcept);

            // get concept/path/position of the branch being copied
            I_GetConceptData oldConcept = getConceptFromString(branchToCopy);
            I_Path oldPath = termFactory.newPath(origins,
                        oldConcept);
            I_Position oldPosition = termFactory.newPosition(oldPath,
                    Integer.MAX_VALUE);
            Set<I_Position> positions = new HashSet<I_Position>();
            positions.add(oldPosition);

            // get latest concept attributes
            List<I_ConceptAttributeTuple> conceptAttributeTuples =
                concept.getConceptAttributeTuples(null, positions);
            // copy latest attributes to new path/version
            for (I_ConceptAttributeTuple tuple: conceptAttributeTuples) {
                I_ConceptAttributePart newPart = tuple.duplicatePart();
                newPart.setVersion(Integer.MAX_VALUE);
                newPart.setPathId(copyToPath.getConceptId());
                concept.getConceptAttributes().addVersion(newPart);
            }

            // get latest descriptions
            List<I_DescriptionTuple> descriptionTuples =
                concept.getDescriptionTuples(null, null, positions);
            // copy latest descriptions to new path/version
            for (I_DescriptionTuple tuple: descriptionTuples) {
                I_DescriptionPart newPart = tuple.duplicatePart();
                newPart.setVersion(Integer.MAX_VALUE);
                newPart.setPathId(copyToPath.getConceptId());
                tuple.getDescVersioned().addVersion(newPart);
            }

            // get latest relationships
            List<I_RelTuple> relationshipTuples =
                concept.getSourceRelTuples(null, null, positions, false);
            // copy latest relationships to new path/version
            for (I_RelTuple tuple: relationshipTuples) {
                I_RelPart newPart = tuple.duplicatePart();
                newPart.setVersion(Integer.MAX_VALUE);
                newPart.setPathId(copyToPath.getConceptId());
                tuple.getRelVersioned().addVersion(newPart);
            }

            termFactory.addUncommitted(concept);
        }

        private I_GetConceptData getConceptFromString(String conceptString)
                throws Exception {
            if (conceptString.equals("TGA_TEST_DATA")) {
                return termFactory.getConcept(ArchitectonicAuxiliary.Concept.
                        TGA_TEST_DATA.getUids());
            } else if (conceptString.equals("TGA_DATA")) {
                return termFactory.getConcept(ArchitectonicAuxiliary.Concept.
                        TGA_DATA.getUids());
            } else {
                throw new Exception("Don't recognise specified branch: "
                        + conceptString);
            }
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        CopyLatestComponent copy = new CopyLatestComponent();
        try {
            termFactory.iterateConcepts(copy);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
