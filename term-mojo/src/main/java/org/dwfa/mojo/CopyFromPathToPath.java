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
package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LineageHelper;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.path.CopyPathToPath;
import org.dwfa.cement.ArchitectonicAuxiliary;

import java.util.Arrays;
import java.util.Date;

/**
 * Copies from all the specified paths and their children to the new path. Note
 * that this
 * mojo will only copy content that is explicitly on the origin paths, not
 * inherited from
 * a parent path.
 *
 * @goal copy-from-path-to-path
 */
public class CopyFromPathToPath extends AbstractMojo {

    /**
     * Paths to copy the data from
     *
     * @parameter
     * @required
     */
    protected ConceptDescriptor[] fromPaths;

    /**
     * Path to copy the data to
     *
     * @parameter
     * @required
     */
    protected ConceptDescriptor toPath;

    /**
     * This status will be used to change all content to if set, otherwise the
     * status of the
     * components on the origin path will be used
     *
     * @parameter
     */
    protected ConceptDescriptor status = null;

    /**
     * The release time to stamp all copies with, otherwise NOW will be used
     *
     * @parameter
     */
    protected Date releaseTime = null;

    /**
     * Indicate if all history or only the latest state of the objects should be
     * copied - defaults to false
     *
     * @parameter
     */
    protected boolean copyOnlyLatestState = false;

    /**
     * Indicates whether to read all parts of the object and copy any found, or
     * only the very latest part in time sequence across all paths
     *
     * @parameter
     */
    protected boolean readLatestPartOnly = false;

    /**
     * Indicates whether to include child paths
     *
     * @parameter
     */
    protected boolean includeChildPaths = false;

    /**
     * Indicates whether to require a a concept to have attributes
     *
     * @parameter
     */
    protected boolean validate = true;

    /**
     * Indicates whether to use the SNOMED "IS A" relationship when searching
     * for relationships. Set this to false
     * if a hierarchy does not include SNOMED data.
     *
     * @parameter default-value="true"
     * @required
     */
    protected boolean includeSnomedIsA;

    private I_TermFactory tf;

    public void execute() throws MojoExecutionException, MojoFailureException {

        tf = LocalVersionedTerminology.get();
        try {

            CopyPathToPath copyProcessor = new CopyPathToPath();
            LineageHelper lineageHelper = getLineageHelper();

            for (ConceptDescriptor fromPath : fromPaths) {
                I_GetConceptData srcConcept = fromPath.getVerifiedConcept();
                I_Path srcPath = tf.getPath(srcConcept.getUids());
                copyProcessor.addSourcePosition(tf.newPosition(srcPath, Integer.MAX_VALUE));
                if (includeChildPaths) {
                    for (I_GetConceptData childConcept : lineageHelper.getAllDescendants(srcConcept)) {
                        srcPath = tf.getPath(childConcept.getUids());
                        copyProcessor.addSourcePosition(tf.newPosition(srcPath, Integer.MAX_VALUE));
                    }
                }
            }

            int toPathId = toPath.getVerifiedConcept().getConceptId();
            copyProcessor.setTargetPathId(toPathId);

            if (status != null) {
                copyProcessor.setStatusId(status.getVerifiedConcept().getConceptId());
            }

            if (releaseTime != null) {
                copyProcessor.setVersionTime(tf.convertToThinVersion(releaseTime.getTime()));
            } else {
                copyProcessor.setVersionTime(tf.convertToThinVersion(System.currentTimeMillis()));
            }

            copyProcessor.setCopyOnlyLatestState(copyOnlyLatestState);
            copyProcessor.setReadLatestPartOnly(readLatestPartOnly);
            copyProcessor.setValidate(validate);

            getLog().info("Starting to iterate concept attributes to copy from " + fromPaths + " to " + toPath);
            tf.iterateConcepts(copyProcessor);

            String duplicateVersionError = "";
            if (copyProcessor.hasDuplicateVersionDescriptions()) {
                duplicateVersionError += "One or more descriptions were found with multiple versions on the same path and with the same timestamp. ";
            }
            if (copyProcessor.hasDuplicateVersionRelationships()) {
                duplicateVersionError += "One or more relationships were found with multiple versions on the same path and with the same timestamp. ";
            }
            if (!duplicateVersionError.isEmpty()) {
                throw new MojoExecutionException(duplicateVersionError);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed copying from paths " + Arrays.toString(fromPaths) + " to path " + toPath, e);
        }
    }

    private LineageHelper getLineageHelper() {
        if (includeSnomedIsA) {
            return new LineageHelper();
        } else {
            return new LineageHelper() {
                @Override
                protected I_IntSet getIsARelTypes() throws Exception {
                    I_IntSet relTypes = termFactory.newIntSet();
                    relTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
                    setIsARelTypes(relTypes);
                    return relTypes;
                }
            };
        }
    }
}
