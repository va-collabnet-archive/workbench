package org.dwfa.mojo;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * Goal which checks if components match certain criteria.
 * e.g. have no parents and have child count > 20.
 * @goal vodb-match-criteria
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbMatchCriteria extends AbstractMojo {

    /**
     * List of branch which will included in search.
     * @parameter
     * @required
     */
    private String[] branches;


    /**
     * Whether to find all components with no parents.
     * @parameter
     */
    private boolean findNoParents = true;

    /**
     * Whether to find all components with child count > 20.
     * @parameter
     */
    private boolean findChildCount = true;

    private class FindComponents implements I_ProcessConcepts {

        I_TermFactory termFactory;
        Set<I_Position> origins = new HashSet<I_Position>();

        public void processConcept(I_GetConceptData concept) throws Exception {
             termFactory = LocalVersionedTerminology.get();

            // get origins
            I_Path architectonicPath = termFactory.getPath(ArchitectonicAuxiliary.
                                Concept.ARCHITECTONIC_BRANCH.getUids());

            I_Position latestOnArchitectonicPath = termFactory.newPosition(architectonicPath,
                                                    Integer.MAX_VALUE);

            origins.add(latestOnArchitectonicPath);

            LinkedList<Set<I_Position>> branchPositions =
                    new LinkedList<Set<I_Position>>();
            // get all the concepts/paths/positions for the branches to be compared
            for (String branchName : branches) {
                I_GetConceptData currentConcept = getConceptFromString(
                        branchName);
                I_Path currentPath = termFactory.newPath(origins, currentConcept);
                I_Position currentPosition = termFactory.newPosition(
                        currentPath, Integer.MAX_VALUE);
                Set<I_Position> positions = new HashSet<I_Position>();
                positions.add(currentPosition);
                branchPositions.add(positions);
            }

            // get latest IS-A relationships
            I_IntSet isARel = termFactory.newIntSet();
            isARel.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.
                    IS_A_REL.getUids()).getConceptId());

            if (findNoParents) {
                List<I_RelTuple> results = concept.getSourceRelTuples(
                        null, isARel, null, false);
                if (results.size() == 0) {
                    getLog().info("Found an orphaned concept: " + concept);
                }
                if (results.size() > 1) {
                    getLog().info("Found a concept with more than 1 parent: " + concept);
                }
            }

            if (findChildCount) {
                List<I_RelTuple> results = concept.getDestRelTuples(
                        null, isARel, null, false);
                if (results.size() > 20) {
                    getLog().info("Concept: " + concept + " has > 20 children.");
                }
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
            } else if (conceptString.equals("AMT_SOURCE_DATA")) {
                return termFactory.getConcept(ArchitectonicAuxiliary.Concept.
                        AMT_SOURCE_DATA.getUids());
            } else {
                throw new Exception("Don't recognise specified branch: "
                        + conceptString);
            }
        }

    }

    public void execute() throws MojoExecutionException, MojoFailureException {
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            FindComponents find = new FindComponents();
            try {
                termFactory.iterateConcepts(find);
            } catch (Exception e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
    }

}
