/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.mojo.db;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.rf2.UuidUuidRemapper;
import org.ihtsdo.mojo.db.BinaryChangeSetResolveIds.SctIdResolution;

/**
 * Read all binary change set under a specified directory hierarchy create a map of when the SCT IDs
 * were used with respective enclosing concepts keep only the latest use of SCT IDs which were used
 * for more than one enclosing concept write out a change set file with.<br>
 * 
 * For FILTER_DESCRIPTION_SCTIDS setting only the descriptions are processed.  SCTIDs not on the extension path are discarded.
 *
 * @goal bcs-resolve-sctids
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class BinaryChangeSetResolveIdsMojo extends AbstractMojo {

    private static final String FILE_SEPARATOR = File.separator;
    /**
     * The change set directory
     *
     * @parameter default-value= "${project.build.directory}/changesets/"
     */
    String changeSetDir;
    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}/generated-artifact"
     * @required
     */
    private String genArtifactDir;
    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String buildDir;
    /**
     * Location of the build directory. KEEP_ALL_SCTID, KEEP_NO_ECCS_SCTID, KEEP_LAST_CURRENT_USE, FILTER_DESCRIPTION_SCTIDS
     *
     * @parameter default-value= "KEEP_NO_ECCS_SCTID"
     * @required
     */
    private String resolutionApproach;
    private SctIdResolution resolution;
    /**
     * @parameter @required
     */
    private String extensionPathUuidStr;
    /**
     * Directory used for intermediate serialized sct/uuid mapping cache
     *
     * @parameter default-value = "id-cache"
     */
    private String idCacheDir = "";
    /**
     * Primordial UUIDs of eConcepts to be skip ... i.e. not included in the database load.
     *
     * @parameter
     */
    private String[] skipUuids;
    /**
     * Primordial UUIDs of non-annotated members to be skip ... i.e. not included in the database load.
     *
     * @parameter
     */
    private String[] skipMemberUuids;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("resolving change set ids in: " + changeSetDir);

        if (resolutionApproach.equalsIgnoreCase("KEEP_ALL_SCTID")) {
            resolution = SctIdResolution.KEEP_ALL_SCTID;
        } else if (resolutionApproach.equalsIgnoreCase("KEEP_NO_ECCS_SCTID")) {
            resolution = SctIdResolution.KEEP_NO_ECCS_SCTID;
        } else if (resolutionApproach.equalsIgnoreCase("KEEP_LAST_CURRENT_USE_SCTID")) {
            resolution = SctIdResolution.KEEP_LAST_CURRENT_USE;
        } else if (resolutionApproach.equalsIgnoreCase("FILTER_DESCRIPTION_SCTIDS")) {
            // only process descriptions
            resolution = SctIdResolution.FILTER_DESCRIPTION_SCTIDS;
        } else {
            throw new MojoFailureException("BinaryChangeSetResolveIdsMojo invalid ");
        }

        try {
            BinaryChangeSetResolveIds rcsi = new BinaryChangeSetResolveIds(changeSetDir, 
                    genArtifactDir, 
                    resolution, 
                    true, // log "eccs_in.txt"
                    true, // log "eccs_out.txt"
                    extensionPathUuidStr);

            // Import remap cache which has been 
            String cachePath = buildDir + FILE_SEPARATOR + idCacheDir + FILE_SEPARATOR;            
            String idCacheFName = cachePath + "uuidRemapCache.ser";
            // handle RF1 computed UUIDs which were reassigned in RF2
            File file = new File(idCacheFName);
            if (file.exists()) {
                UuidUuidRemapper uuidRemapCache = new UuidUuidRemapper(idCacheFName);
                rcsi.setSctPrimorialUuidRemapper(uuidRemapCache);
            }
            
            if (skipUuids != null) {
                HashSet<UUID> skipUuidSet = new HashSet<>();
                for (String str : skipUuids) {
                    skipUuidSet.add(UUID.fromString(str));
                }
                rcsi.setSkipUuidSet(skipUuidSet);
            }

            // skip non-annotated refset members
            if (skipMemberUuids != null) {
                HashSet<UUID> skipMemberUuidSet = new HashSet<>();
                for (String str : skipMemberUuids) {
                    skipMemberUuidSet.add(UUID.fromString(str));
                }
                rcsi.setSkipMemberUuidSet(skipMemberUuidSet);
            }

            rcsi.processFiles();
        } catch (IOException | TerminologyException ex) {
            throw new MojoExecutionException("BinaryChangeSetResolveIdsMojo \n", ex);
        }
    }

    public String getChangeSetDir() {
        return changeSetDir;
    }

    public void setChangeSetDir(String changeSetDir) {
        this.changeSetDir = changeSetDir;
    }

    public String getTargetDirectory() {
        return genArtifactDir;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.genArtifactDir = targetDirectory;
    }
}
