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
package org.ihtsdo.mojo.mojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

/**
 * Converts specified description types to tall-man capitalisation.
 * Generates a report and workflow based on modified concepts.
 * 
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
     * 
     * @parameter
     */
    private ConceptDescriptor editingBranchDescriptor;

    /**
     * Branch which will be copied to new branch.
     * 
     * @parameter
     */
    private ConceptDescriptor viewingBranchDescriptor;

    /**
     * Description types to check.
     * 
     * @parameter
     */
    private ConceptDescriptor[] descriptionsToCheck;

    /**
     * Contains list of words in their target capitalisation.
     * 
     * @parameter
     * @required
     */
    private File inputFile;

    /**
     * Location to write output.
     * 
     * @parameter
     * @required
     */
    private File workflowOutputFile;

    /**
     * Location to modified concepts report.
     * 
     * @parameter
     * @required
     */
    private File modifiedConceptsOutputFile;

    /**
     * Location to unmodified concepts report.
     * i.e. where the tall man word existed (ignoring capitilisation)
     * but didn't need to be modified due to already containing correct
     * capitilisation.
     * 
     * @parameter
     * @required
     */
    private File unmodifiedConceptsOutputFile;

    /**
     * Report on tall man words which weren't found at all in any concept's
     * description.
     * 
     * @parameter
     * @required
     */
    private File unusedTallManWordsOutputFile;

    private HashSet<List<UUID>> modifiedUuids;

    private HashSet<String> modifiedDescriptions;

    private HashSet<String> unmodifiedDescriptions;

    private HashSet<String> foundTallManWords;

    public void execute() throws MojoExecutionException, MojoFailureException {

        I_TermFactory termFactory = LocalVersionedTerminology.get();
        BufferedReader in;
        try {
            foundTallManWords = new HashSet<String>();
            modifiedUuids = new HashSet<List<UUID>>();
            modifiedDescriptions = new HashSet<String>();
            unmodifiedDescriptions = new HashSet<String>();

            // initialise output directories/files
            workflowOutputFile.getParentFile().mkdirs();
            modifiedConceptsOutputFile.getParentFile().mkdirs();

            BufferedWriter workflowWriter = new BufferedWriter(new FileWriter(workflowOutputFile));
            BufferedWriter modifiedConceptsWriter = new BufferedWriter(new FileWriter(modifiedConceptsOutputFile));
            BufferedWriter unmodifiedConceptsWriter = new BufferedWriter(new FileWriter(unmodifiedConceptsOutputFile));
            BufferedWriter unusedTallManWordsWriter = new BufferedWriter(new FileWriter(unusedTallManWordsOutputFile));

            // read in tall man words from input file
            in = new BufferedReader(new FileReader(inputFile));
            HashSet<String> tallManWords = new HashSet<String>();
            String currentLine = in.readLine();
            while (currentLine != null) {
                currentLine = currentLine.replaceAll("\"", "");
                currentLine = currentLine.trim();
                tallManWords.add(currentLine);
                currentLine = in.readLine();
            }

            // execute tall man plugin
            TallManWriter tallManWriter = new TallManWriter(tallManWords);

            // iterate over each concept
            termFactory.iterateConcepts(tallManWriter);

            String message = "Tallman plugin modified " + tallManWriter.getNumberModifiedDescriptions()
                + " descriptions.";
            getLog().info(message);

            // write uuids of modified concepts to workflow file
            for (List<UUID> uuidList : modifiedUuids) {
                for (UUID uuid : uuidList) {
                    workflowWriter.write(uuid.toString());
                    workflowWriter.newLine();
                }
            }

            // write report of modified concepts for review
            for (String description : modifiedDescriptions) {
                modifiedConceptsWriter.write(description);
                modifiedConceptsWriter.newLine();
            }

            // write a report of tall man words that were NOT used
            // to update any descriptions
            HashSet<String> unusedTallManWords = new HashSet<String>();
            for (String tallManWord : tallManWords) {
                if (!foundTallManWords.contains(tallManWord)) {
                    unusedTallManWords.add(tallManWord);
                }
            }
            for (String word : unusedTallManWords) {
                unusedTallManWordsWriter.write(word);
                unusedTallManWordsWriter.newLine();
            }

            // write a report for descriptions which contained a
            // tall man word (ignoring caps) but didn't need to be
            // modified as were already correct
            for (String word : unmodifiedDescriptions) {
                unmodifiedConceptsWriter.write(word);
                unmodifiedConceptsWriter.newLine();
            }

            // release IO resources
            in.close();
            workflowWriter.flush();
            workflowWriter.close();
            modifiedConceptsWriter.flush();
            modifiedConceptsWriter.close();
            unusedTallManWordsWriter.flush();
            unusedTallManWordsWriter.close();
            unmodifiedConceptsWriter.flush();
            unmodifiedConceptsWriter.close();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private class TallManWriter implements I_ProcessConcepts {
        private I_TermFactory termFactory;
        private I_GetConceptData viewingBranch;
        private I_GetConceptData editingBranch;
        private int numberModifiedDescriptions;
        private HashSet<String> tallManWords;
        private HashSet<I_GetConceptData> descriptionTypes;

        public TallManWriter(HashSet<String> tallManWords) throws Exception {
            descriptionTypes = new HashSet<I_GetConceptData>();
            this.tallManWords = tallManWords;
            termFactory = Terms.get();
            viewingBranch = viewingBranchDescriptor.getVerifiedConcept();
            editingBranch = editingBranchDescriptor.getVerifiedConcept();
            for (ConceptDescriptor description : descriptionsToCheck) {
                descriptionTypes.add(description.getVerifiedConcept());
            }
            numberModifiedDescriptions = 0;
        }

        public void processConcept(I_GetConceptData concept) throws Exception {

            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            List<UUID> uuids = concept.getUids();

            // create list of IDs of description types to check (e.g. preferred
            // term)
            I_IntSet descriptionTypesToCheck = termFactory.newIntSet();
            for (I_GetConceptData descriptionConcept : descriptionTypes) {
                descriptionTypesToCheck.add(descriptionConcept.getConceptNid());
            }

            int currentUnreviewedId = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids()).getConceptNid();

            // get origins
            PathBI originPath = termFactory.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());

            PositionBI originPosition = termFactory.newPosition(originPath, Integer.MAX_VALUE);

            Set<PositionBI> origins = new HashSet<PositionBI>();
            origins.add(originPosition);

            // get the copy-to concept/path
            PathBI copyToPath = termFactory.getPath(editingBranch.getUids());

            // get concept/path/position of the branch being copied
            PathBI oldPath = termFactory.getPath(viewingBranch.getUids());

            PositionBI oldPosition = termFactory.newPosition(oldPath, Integer.MAX_VALUE);
            Set<PositionBI> positionsToCheck = new HashSet<PositionBI>();
            positionsToCheck.add(oldPosition);

            // get latest descriptions
            List<? extends I_DescriptionTuple> descriptionTuples = concept.getDescriptionTuples(null, descriptionTypesToCheck,
                new PositionSetReadOnly(positionsToCheck), 
                config.getPrecedence(), config.getConflictResolutionStrategy());

            // copy latest descriptions to new path/version
            for (I_DescriptionTuple tuple : descriptionTuples) {

                String currentDescription = tuple.getText();
                tuple.getConceptNid();

                // find descriptions that contain the tall man word
                // (ignoring case)
                if (descriptionContainsTallManWordIgnoresCase(tallManWords, currentDescription)) {

                    String updatedCurrentDescription = replaceAllTallManWords(tallManWords, currentDescription, uuids);

                    if (currentDescription.equals(updatedCurrentDescription)) {
                        // nothing changed
                        unmodifiedDescriptions.add(currentDescription + "\t " + uuids);
                    } else {
                        // add to report
                        numberModifiedDescriptions++;
                        modifiedUuids.add(uuids);

                        // update the description with the tall man alternative
                        I_DescriptionPart newPart = (I_DescriptionPart) tuple.makeAnalog(currentUnreviewedId, copyToPath.getConceptNid(), Long.MAX_VALUE);
                        newPart.setText(updatedCurrentDescription);
                        tuple.getDescVersioned().addVersion(newPart);
                        // termFactory.addUncommitted(concept);

                        // update the current concept with
                        // get latest concept attributes
                        List<? extends I_ConceptAttributeTuple> conceptAttributeTuples = concept.getConceptAttributeTuples(null,
                            new PositionSetReadOnly(positionsToCheck),
                            config.getPrecedence(), config.getConflictResolutionStrategy());
                        // copy latest attributes and set status to unreviewed
                        for (I_ConceptAttributeTuple attribute : conceptAttributeTuples) {
                            I_ConceptAttributePart newAttributePart = (I_ConceptAttributePart) attribute.makeAnalog(currentUnreviewedId, copyToPath.getConceptNid(), Long.MAX_VALUE);
                            concept.getConceptAttributes().addVersion(newAttributePart);
                        }
                        termFactory.addUncommitted(concept);
                    }
                }
            }
        }

        /**
         * Searches a string for multiple tall man words to replace.
         * 
         * @param tallManWords
         * @param stringToReplace
         * @param currentUuid
         * @return
         */
        public String replaceAllTallManWords(HashSet<String> tallManWords, String stringToReplace,
                List<UUID> currentUuid) {

            String result = stringToReplace;
            for (String tallManWord : tallManWords) {
                result = replaceSingleTallManWord(tallManWord, result);
            }

            if (!result.equals(stringToReplace)) {
                modifiedDescriptions.add(stringToReplace + " CONVERTED TO: " + result + "\t " + currentUuid);
            }
            return result;
        }

        /**
         * Searches a string for a single tall man word to replace.
         * 
         * @param tallmanWord
         * @param stringToReplace
         * @return
         */
        public String replaceSingleTallManWord(String tallmanWord, String stringToReplace) {

            // create a search string
            String searchString = "(\\b)";

            for (int x = 0; x < tallmanWord.length(); x++) {
                char c = tallmanWord.charAt(x);

                // create group for first character so we can reference this
                // later
                if (x == 0) {
                    searchString = searchString + "(";
                }

                searchString = searchString + c;

                // end group for first character
                if (x == 0) {
                    searchString = searchString + ")";
                }
            }
            searchString = searchString + "(\\b)";

            // compile with case insensitive so that all combinations of case
            // will be matched
            Pattern pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(stringToReplace);

            StringBuffer result = new StringBuffer();
            while (matcher.find()) {

                char c = matcher.group(2).charAt(0);

                if (Character.isUpperCase(c)) {
                    // need to conserve upper case in replacement string
                    tallmanWord = c + tallmanWord.substring(1, tallmanWord.length());
                }

                // replace the match with the tall man word, but include the
                // word boundary we found (otherwise it would drop spaces,
                // brackets etc)
                matcher.appendReplacement(result, matcher.group(1) + tallmanWord + matcher.group(3));
            }
            matcher.appendTail(result);

            return result.toString();
        }

        /**
         * Checks if a description contains a tall man word, ignoring case
         * differences.
         * 
         * @param tallManWords
         * @param stringToCheck
         * @return
         */
        public boolean descriptionContainsTallManWordIgnoresCase(HashSet<String> tallManWords, String stringToCheck) {
            boolean result = false;
            stringToCheck = stringToCheck.toLowerCase();
            for (String word : tallManWords) {
                if (stringToCheck.contains(word.toLowerCase())) {
                    foundTallManWords.add(word);
                    result = true;
                }
            }
            return result;
        }

        /**
         * Gets the number of modified descriptions.
         * 
         * @return
         */
        public int getNumberModifiedDescriptions() {
            return numberModifiedDescriptions;
        }

        /**
         * Sets the number of modified descriptions.
         * 
         * @param numberModifiedDescriptions
         */
        public void setNumberModifiedDescriptions(int numberModifiedDescriptions) {
            this.numberModifiedDescriptions = numberModifiedDescriptions;
        }
    }
}
