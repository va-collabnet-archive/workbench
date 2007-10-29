package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * Converts specified description types to tall-man capitalisation.
 * Generates a report and workflow based on modified concepts.
 * @author Christine Hill
 *
 */

/**
 *
 * @goal vodb-execute-tallman
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbExecuteTallMan extends AbstractMojo {

    /**
     * Branch to which the latest component of specified branch will be
     * copied to.
     * @parameter
     */
    private ConceptDescriptor editingBranchDescriptor;

    /**
     * Branch which will be copied to new branch.
     * @parameter
     */
    private ConceptDescriptor viewingBranchDescriptor;

    /**
     * Description types to check.
     * @parameter
     */
    private ConceptDescriptor[] descriptionsToCheck;

    /**
     * Contains list of words in their target capitalisation.
     * @parameter
     * @required
     */
    private File inputFile;

    /**
     * Location to write output.
     * @parameter
     * @required
     */
    private File workflowOutputFile;

    /**
     * Location to write details.
     *
     * @parameter
     * @required
     */
    private File reportOutputFile;

    private HashSet<List<UUID>> modifiedUuids;

    private HashSet<String> modifiedDescriptions;

    public void execute() throws MojoExecutionException, MojoFailureException {

        I_TermFactory termFactory = LocalVersionedTerminology.get();
        BufferedReader in;
        try {

            modifiedUuids = new HashSet<List<UUID>>();
            modifiedDescriptions = new HashSet<String>();

            // initialise output directories/files
            workflowOutputFile.getParentFile().mkdirs();
            reportOutputFile.getParentFile().mkdirs();

            BufferedWriter workflowWriter = new BufferedWriter(
                    new FileWriter(workflowOutputFile));
            BufferedWriter reportWriter = new BufferedWriter(
                    new FileWriter(reportOutputFile));

            // read in tall man words from input file
            in = new BufferedReader(new FileReader(inputFile));
            HashSet<String> tallManWords = new HashSet<String>();
            String currentLine = in.readLine();
            while (currentLine != null) {
                tallManWords.add(currentLine);
                currentLine = in.readLine();
            }

            // execute tall man plugin
            TallManWriter tallManWriter = new TallManWriter(tallManWords);

            // iterate over each concept
            termFactory.iterateConcepts(tallManWriter);

            String message = "Tallman plugin modified "
                            + tallManWriter.getNumberModifiedComponents()
                            + " descriptions.";
            getLog().info(message);

            // write uuids of modified concepts to workflow file
            for (List<UUID> uuid : modifiedUuids) {
                workflowWriter.write(uuid.toString());
                workflowWriter.newLine();
            }

            // write report of modified concepts for review
            for (String description : modifiedDescriptions) {
                reportWriter.write(description);
                reportWriter.newLine();
            }

            // release IO resources
            in.close();
            workflowWriter.flush();
            workflowWriter.close();
            reportWriter.flush();
            reportWriter.close();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private class TallManWriter implements I_ProcessConcepts {
        private I_TermFactory termFactory;
        private I_GetConceptData viewingBranch;
        private I_GetConceptData editingBranch;
        private int numberModifiedComponents;
        private HashSet<String> tallManWords;
        private HashSet<I_GetConceptData> descriptionTypes;

        public TallManWriter(HashSet<String> tallManWords) throws Exception {
            descriptionTypes = new HashSet<I_GetConceptData>();
            this.tallManWords = tallManWords;
            termFactory = LocalVersionedTerminology.get();
            viewingBranch = viewingBranchDescriptor.getVerifiedConcept();
            editingBranch = editingBranchDescriptor.getVerifiedConcept();
            for (ConceptDescriptor description : descriptionsToCheck) {
                descriptionTypes.add(description.getVerifiedConcept());
            }
            numberModifiedComponents = 0;
        }

        public void processConcept(I_GetConceptData concept) throws Exception {

            List<UUID> uuids = concept.getUids();

            // create list of IDs of description types to check (e.g. preferred term)
            I_IntSet descriptionTypesToCheck = termFactory.newIntSet();
            for (I_GetConceptData descriptionConcept : descriptionTypes) {
                descriptionTypesToCheck.add(descriptionConcept.getConceptId());
            }

            int currentUnreviewedId = termFactory.getConcept(
                    ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids()).getConceptId();

            // get origins
            I_Path originPath = termFactory.getPath(ArchitectonicAuxiliary.
                    Concept.ARCHITECTONIC_BRANCH.getUids());

            I_Position originPosition = termFactory.newPosition(originPath,
                                                    Integer.MAX_VALUE);

            Set<I_Position> origins = new HashSet<I_Position>();
            origins.add(originPosition);

            // get the copy-to concept/path
            I_Path copyToPath = termFactory.newPath(origins,
                    editingBranch);

            // get concept/path/position of the branch being copied
            I_Path oldPath = termFactory.newPath(origins,
                        viewingBranch);
            I_Position oldPosition = termFactory.newPosition(oldPath,
                    Integer.MAX_VALUE);
            Set<I_Position> positionsToCheck = new HashSet<I_Position>();
            positionsToCheck.add(oldPosition);

            // get latest descriptions
            List<I_DescriptionTuple> descriptionTuples =
                concept.getDescriptionTuples(null, descriptionTypesToCheck, positionsToCheck);

            // copy latest descriptions to new path/version
            for (I_DescriptionTuple tuple: descriptionTuples) {

                String currentDescription = tuple.getText();
                tuple.getConceptId();

                // find descriptions that contain the tall man word
                // (ignoring case)
                if (descriptionContainsTallManWordIgnoresCase(
                        tallManWords, currentDescription)) {

                    String updatedCurrentDescription = replaceAllTallManWords(
                            tallManWords, currentDescription, uuids);

                    if (currentDescription.equals(updatedCurrentDescription)) {
                        // nothing changed
                    } else {
                        // add to report
                        numberModifiedComponents++;
                        modifiedUuids.add(uuids);

                        // update the description with the tall man alternative
                        I_DescriptionPart newPart = tuple.duplicatePart();
                        newPart.setText(updatedCurrentDescription);
                        newPart.setStatusId(currentUnreviewedId);
                        newPart.setVersion(Integer.MAX_VALUE);
                        newPart.setPathId(copyToPath.getConceptId());
                        tuple.getDescVersioned().addVersion(newPart);
                        termFactory.addUncommitted(concept);
                    }
                }
            }
        }

        /**
         * Searches a string for multiple tall man words to replace.
         * @param tallManWords
         * @param stringToReplace
         * @param currentUuid
         * @return
         */
        public String replaceAllTallManWords(HashSet<String> tallManWords,
                String stringToReplace, List<UUID> currentUuid) {

            String result = stringToReplace;
            for (String tallManWord: tallManWords) {
                result = replaceSingleTallManWord(tallManWord, stringToReplace);
            }

            modifiedDescriptions.add(stringToReplace + " CONVERTED TO: " + result + "\t " + currentUuid);
            return result;
        }

        /**
         * Searches a string for a single tall man word to replace.
         * @param tallmanWord
         * @param stringToReplace
         * @return
         */
        public String replaceSingleTallManWord(String tallmanWord, String stringToReplace) {

            // need create a search string which includes both the upper and
            // lower case of each character in the tall man word (regular expression
            // matching)
            String searchString = "";
            for (int x = 0; x < tallmanWord.length(); x++) {
                char c = tallmanWord.charAt(x);

                // convert to opposite case (i.e. if the current character is
                // upper case, convert to lower case. if lower case, conver to
                // upper case.
                char oppositeC = '.';
                if (c <= 'Z') {
                    oppositeC = (char)(c + 32);
                } else {
                    oppositeC = (char)(c - 32);
                }
                searchString = searchString + "[" + c + oppositeC + "]";
            }
            return stringToReplace.replaceAll(searchString, tallmanWord);
        }

        /**
         * Checks if a description contains a tall man word.
         * @param tallManWords
         * @param stringToCheck
         * @return
         */
        public boolean descriptionContainsTallManWord(HashSet<String> tallManWords,
                String stringToCheck) {
            for (String word: tallManWords) {
                if (stringToCheck.contains(word)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Checks if a description contains a tall man word, ignoring case differences.
         * @param tallManWords
         * @param stringToCheck
         * @return
         */
        public boolean descriptionContainsTallManWordIgnoresCase(HashSet<String> tallManWords,
                String stringToCheck) {
            stringToCheck = stringToCheck.toLowerCase();
            for (String word: tallManWords) {
                word = word.toLowerCase();
                if (stringToCheck.contains(word)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Gets the number of modified components.
         * @return
         */
        public int getNumberModifiedComponents() {
            return numberModifiedComponents;
        }

        /**
         * Sets the number of modified components.
         * @param numberModifiedComponents
         */
        public void setNumberModifiedComponents(int numberModifiedComponents) {
            this.numberModifiedComponents = numberModifiedComponents;
        }
    }
}
