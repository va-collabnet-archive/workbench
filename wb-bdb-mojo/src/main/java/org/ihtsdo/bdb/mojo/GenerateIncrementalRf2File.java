/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.bdb.mojo;

import java.io.File;
import java.util.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.util.id.Type5UuidFactory;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.country.COUNTRY_CODE;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.helper.export.Rf2Export;
import org.ihtsdo.helper.refex.Rf2RefexComputer;
import org.ihtsdo.helper.rf2.Rf2File;
import org.ihtsdo.helper.rf2.Rf2File.ReleaseType;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.helper.transform.UuidSnomedMapHandler;
import org.ihtsdo.helper.transform.UuidToSctIdMapper;
import org.ihtsdo.helper.transform.UuidToSctIdWriter;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * Goal which generates an incremental Rf2 file. If first release, use a delta
 * release type with start date of the time to start including data and an end
 * date of latest.
 *
 * @goal generate-irf2-file
 *
 * @phase process-sources
 */
public class GenerateIncrementalRf2File extends AbstractMojo  {

    /**
     * ConceptSpec for the view paths to base the export on.
     *
     * @parameter
     */
    private ConceptSpec[] pathsToExport;
    /**
     * ConceptSpec for the view paths to base the export on.
     *
     * @parameter
     */
    private String[] exportPathFsns;
    /**
     * Concept for the view path used for export.
     *
     * @parameter
     */
    private ConceptSpec viewPathConceptSpec;
    /**
     * Text of view path concept's FSN, to be used when only the FSN is known,
     * and the path concept was generated with the proper type 5 UUID algorithm
     * using the Type5UuidFactory.PATH_ID_FROM_FS_DESC namespace.
     *
     * @parameter
     */
    private String viewPathConceptSpecFsn;
    /**
     * Start date for inclusion in the RF2 files, in yyyy-MM-dd-HH.mm.ss format.
     *
     * @parameter
     */
    private String startDate;
    /**
     * End date for inclusion in the RF2 files, in yyyy-MM-dd-HH.mm.ss format.
     * 
     * @parameter
     */
    private String endDate;
    /**
     * Effective date, in yyyy-MM-dd-HH.mm.ss format.
     *
     * @parameter @required
     */
    private String effectiveDate;
    /**
     * Project ID namespace, as SCT id
     *
     * @parameter @required
     */
    private String namespace;
    /**
     * Project moduleFsn, as fsn
     *
     * @parameter
     */
    private String moduleFsn;
    /**
     * Project moduleConcept
     *
     * @parameter
     */
    private ConceptDescriptor moduleConcept;
    /**
     * country code
     *
     * @parameter @required
     */
    private String countryCode;
    /**
     * Refsets to exclude
     *
     * @parameter
     */
    private List<ConceptDescriptor> refsetsToExclude;
    /**
     * output directory.
     *
     * @parameter expression="${project.build.directory}/classes" @required
     */
    private File output;
    /**
     * source directory.
     *
     * @parameter expression="${basedir}/src" @required
     */
    private File sourceDir;
    /**
     * Directory of the berkeley database to export from.
     *
     * @parameter
     * expression="${project.build.directory}/generated-resources/berkeley-db"
     * @required
     */
    private File berkeleyDir;
    /**
     * Set to true to make private version of alternate identifiers file.
     *
     * @parameter 
     * default-value="false"
     */
    private boolean makePrivateAltIdsFile;
    /**
     * Simple refset parent concept.
     *
     * @parameter
     */
    private ConceptDescriptor refsetParentConceptSpec;
    /**
     * Set to true to make initial sct to uuid mapping files.
     *
     * @parameter 
     * default-value="false"
     */
    private boolean makeInitialMappingFiles;
    /**
     * Effective date of previous release.
     * @parameter
     */
    private String previousReleaseDate;
    /**
     * RF2 release file type.
     * @parameter @required
     */
    private ReleaseType releaseType;
    /**
     * Set to true to create rf2 release refsets.
     *
     * @parameter 
     * default-value="false"
     */
    private boolean makeRf2Refsets;
    
    private IntSet stampsToWrite = new IntSet();
    private IntSet pathIds;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Bdb.setup(berkeleyDir.getAbsolutePath());
            pathIds = new IntSet();
            if (pathsToExport != null) {
                for (ConceptSpec spec : pathsToExport) {
                    pathIds.add(spec.getLenient().getNid());
                }
            }

            if (exportPathFsns != null) {
                for (String exportPathFsn : exportPathFsns) {
                    pathIds.add(Ts.get().getNidForUuids(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                            exportPathFsn)));
                }
            }

            IntSet excludedRefsetIds = new IntSet();
            if (refsetsToExclude != null) {
                for (ConceptDescriptor cd : refsetsToExclude) {
                    ConceptSpec spec = new ConceptSpec(cd.getDescription(), UUID.fromString(cd.getUuid()));
                    int validatedNid = spec.getLenient().getNid();
                    excludedRefsetIds.add(validatedNid);
                }
            }
            UUID moduleId = null;
            if(moduleConcept != null){
                moduleId = UUID.fromString(moduleConcept.getUuid());
            }else if(moduleFsn != null){
                moduleId = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, moduleFsn);
            }else{
                throw new MojoExecutionException("No module specified.");
            }

            int viewPathNid;
            if (viewPathConceptSpec != null) {
                viewPathNid = viewPathConceptSpec.getLenient().getNid();
            } else if (viewPathConceptSpecFsn != null) {
                UUID pathUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, viewPathConceptSpecFsn);
                viewPathNid = Ts.get().getNidForUuids(pathUuid);
            } else {
                throw new MojoExecutionException("No view path specified.");
            }
            
            //filter start and end dates by release version
            if (releaseType.equals(ReleaseType.DELTA)) {
                if(startDate == null || endDate == null){
                    throw new MojoExecutionException("A Delta Release requires both a start and end date to be specified.");
                }
                //use specified start and end dates
            } else if (releaseType.equals(ReleaseType.FULL)) {
                startDate = "bot";
                endDate = "latest";
            } else if (releaseType.equals(ReleaseType.SNAPSHOT)) {
                startDate = "bot";
                endDate = "latest";
            }
            
            ViewCoordinate vc = new ViewCoordinate(Ts.get().getMetadataViewCoordinate());
            vc.getIsaTypeNids().add(Snomed.IS_A.getLenient().getConceptNid());
            PathBI path = Ts.get().getPath(viewPathNid);
            PositionBI position = Ts.get().newPosition(path,
                    TimeHelper.getTimeFromString(endDate, TimeHelper.getFileDateFormat()));
            vc.setPositionSet(new PositionSet(position));
            stampsToWrite = Bdb.getSapDb().getSpecifiedSapNids(pathIds,
                    TimeHelper.getTimeFromString(startDate, TimeHelper.getFileDateFormat()),
                    TimeHelper.getTimeFromString(endDate, TimeHelper.getFileDateFormat()));
            File refsetCs = new File(output.getParentFile(), "changesets");
            refsetCs.mkdir();
            if (makeRf2Refsets) {
                Rf2RefexComputer rf2RefexComputer = new Rf2RefexComputer(vc, Ts.get().getMetadataEditCoordinate(),
                        refsetCs, stampsToWrite.getAsSet());
                rf2RefexComputer.setup();
                Ts.get().iterateConceptDataInSequence(rf2RefexComputer);
                rf2RefexComputer.cleanup();

                Set<Integer> newStampNids = rf2RefexComputer.getNewStampNids();
                for (int stamp : newStampNids) {
                    stampsToWrite.add(stamp);
                }
            }

            IntSet sapsToRemove = new IntSet();
            if(previousReleaseDate != null){
                sapsToRemove = Bdb.getSapDb().getSpecifiedSapNids(pathIds,
                    TimeHelper.getTimeFromString(previousReleaseDate, TimeHelper.getFileDateFormat()),
                    TimeHelper.getTimeFromString("latest", TimeHelper.getFileDateFormat()));
            }
            getLog().info("Release type: " + releaseType);  
            getLog().info("Criterion matches " + stampsToWrite.size() + " sapNids: " + stampsToWrite);
            for(int stamp : stampsToWrite.getAsSet()){
                long timeForStampNid = Ts.get().getTimeForStampNid(stamp);
                String time = TimeHelper.formatDate(timeForStampNid);
                System.out.println("#### stamp: " + stamp + " Time: " + time);
            }
            NidBitSetBI allConcepts = Ts.get().getAllConceptNids();
            if (makeInitialMappingFiles) {
                UuidToSctIdMapper mapper = new UuidToSctIdMapper(allConcepts, namespace, output);
                Ts.get().iterateConceptDataInSequence(mapper);
                mapper.close();
            }
            UuidSnomedMapHandler handler = new UuidSnomedMapHandler(output, output);
            handler.setNamespace(namespace);
            Integer refsetParentConceptNid = 0;
            if (refsetParentConceptSpec != null) {
                refsetParentConceptNid = Ts.get().getNidForUuids(UUID.fromString(refsetParentConceptSpec.getUuid()));
            }
            
             Rf2Export exporter = new Rf2Export(output,
                    releaseType,
                    LANG_CODE.EN,
                    COUNTRY_CODE.valueOf(countryCode),
                    namespace,
                    moduleId.toString(),
                    new Date(TimeHelper.getTimeFromString(effectiveDate,
                    TimeHelper.getAltFileDateFormat())),
                    stampsToWrite.getAsSet(),
                    vc,
                    excludedRefsetIds.getAsSet(),
                    allConcepts,
                    makePrivateAltIdsFile,
                    refsetParentConceptNid,
                    new Date(TimeHelper.getTimeFromString(this.previousReleaseDate,
                        TimeHelper.getFileDateFormat())),
                    sapsToRemove.getAsSet());
            Ts.get().iterateConceptDataInSequence(exporter);
            exporter.writeOneTimeFiles();
            exporter.close();
            UuidToSctIdWriter writer = new UuidToSctIdWriter(namespace, moduleId.toString(),
                    output, handler, releaseType, COUNTRY_CODE.valueOf(countryCode),
                    new Date(TimeHelper.getTimeFromString(effectiveDate,
                    TimeHelper.getAltFileDateFormat())));
            writer.write();
            writer.close();
            handler.writeMaps();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
