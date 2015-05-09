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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.util.id.Type5UuidFactory;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.country.COUNTRY_CODE;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.helper.export.Rf2Export;
import org.ihtsdo.helper.refex.Rf2RefexComputer;
import org.ihtsdo.helper.rf2.Rf2File.ReleaseType;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.helper.transform.UuidSnomedMapHandler;
import org.ihtsdo.helper.transform.UuidToSctIdMapper;
import org.ihtsdo.helper.transform.UuidToSctIdWriter;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.query.helper.release.ReleaseSpecProcessor;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * Goal which generates an incremental Rf2 file. If first release, use a delta
 * release type with start date of the time to start including data and an end
 * date of latest.
 *
 * Documentation of regarding managed service terms of service and release
 * files.<p>
 *
 * 4. MODIFICATIONS TO THE INTERNATIONAL RELEASE<p>
 *
 * 4.1 Subject to clause 2.1.4, the Licensee may not modify any part of the
 * SNOMED CT Core distributed as part of the International Release or as part of
 * a Member's National Release.<p>
 *
 * 4.2 Subject to any express and specific statement to the contrary in the
 * documentation distributed as part of the International Release, the Licensee
 * may not modify any of the documentation (including Specifications) or
 * software (unless provided in source code form) distributed as part of the
 * International Release.<p>
 *
 * 4.3 The Licensee may, by written notice, request the Licensor to modify the
 * SNOMED CT Core. Upon receipt of such written notice, the Licensor shall
 * consult with the Licensee and shall give due consideration as to whether the
 * proposed modification should be made based on the Licensor's editorial
 * guidelines and policies. Following due consideration of the matter, including
 * consideration of any information presented by the Licensee, the Licensor
 * shall inform the Licensee whether the proposed modification shall be made and
 * if the Licensor agrees that the proposed modification should be made, the
 * Licensor shall give a non-binding indication of when, reasonably and in good
 * faith, it anticipates that the proposed modification will be made. If the
 * Licensee would like the content of the proposed modification to be developed
 * more quickly than the Licensor has indicated, the Licensee may itself
 * undertake or procure the undertaking of the development of the content of the
 * proposed modification (outside of any existing Licensor's support services
 * contract). On receipt of the developed content of the proposed modification,
 * the Licensor will then give due consideration as to whether the developed
 * content meets the Licensor's quality assurance, other governance processes,
 * Standards and Regulations. If the developed content meets the Licensor's
 * quality assurance, other governance processes, Standards and Regulations then
 * the Licensor shall incorporate the modification into the SNOMED CT Core
 * according to its schedule which will give due consideration as to when the
 * proposed modification shall be incorporated into the SNOMED CT Core, taking
 * into account other proposals for the modification of the SNOMED CT Core and
 * the work required to include the proposed modification in the SNOMED CT
 * Core.<p>
 *
 * There are guidelines around what can and cannot be done within an extension:
 * <p>
 * - Relationships within the stated view of the International release should
 * not be amended or retired within an extension.
 * <p>
 * - Stated relationships (IS_A or other) from concepts in the International
 * release to concepts in an extension are not allowed (in either an extension
 * or in the International release).
 * <p>
 * - Stated relationships from concepts in an extension to concepts in the
 * International release are allowed.<p>
 *
 * Given the above, adding content to an extension (by adding relationships to
 * the stated view of an extension and then classifying) may result in
 * retirement (within the extension) of redundant relationships in the
 * International release. There is not an issue with this as long as the stated
 * view and the transitive closure associated with the International release is
 * not changed (which it shouldn't be, given the above guidelines)
 * .<p>
 *
 * @goal generate-irf2-file
 *
 * @phase process-sources
 */
public class GenerateIncrementalRf2File extends AbstractMojo {

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
     * Text of edit path concept's FSN, to be used when only the FSN is known,
     * and the path concept was generated with the proper type 5 UUID algorithm
     * using the Type5UuidFactory.PATH_ID_FROM_FS_DESC namespace.
     *
     * @parameter
     * @required
     */
    private String editPathConceptSpecFsn;
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
     * @parameter
     * @required
     */
    private String effectiveDate;
    /**
     * Effective date, in yyyy-MM-dd-HH.mm.ss format.
     *
     * @parameter
     * @required
     */
    private String snomedCoreReleaseDate;
    /**
     * Project ID namespace, as SCT id
     *
     * @parameter
     * @required
     */
    private String namespace;
    /**
     * Project moduleConcept
     *
     * @parameter
     * @required
     */
    private ConceptDescriptor[] moduleConcepts;
    /**
     * country code
     *
     * @parameter
     * @required
     */
    private String countryCode;
    /**
     * country code
     *
     * @parameter default-value="EN"
     */
    private String languageCode;
    /**
     * Refsets to exclude
     *
     * @parameter
     */
    private List<ConceptDescriptor> refsetsToExclude;
    /**
     * output directory.
     *
     * @parameter expression="${project.build.directory}/classes"
     * @required
     */
    private File output;
    /**
     * source directory.
     *
     * @parameter expression="${basedir}/src"
     * @required
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
     * @parameter default-value="false"
     */
    private boolean makePrivateAltIdsFile;
    /**
     * Simple refset parent concept.
     *
     * @parameter
     */
    private ConceptDescriptor refsetParentConceptSpec;
    /**
     * Directory containing SCTID to UUID mapping files.
     *
     * @parameter default-value="${basedir}/src/main/sct-uuid-maps"
     */
    private File mappingFileDir;
    /**
     * Effective date of previous release.
     *
     * @parameter
     */
    private String previousReleaseDate;
    /**
     * RF2 release file type.
     *
     * @parameter
     * @required
     */
    private ReleaseType releaseType;
    /**
     * Set to true to create rf2 release refsets.
     *
     * @parameter default-value="false"
     */
    private boolean makeRf2Refsets;
    /**
     * Taxonomy parent concepts.
     *
     * @parameter @required
     */
    private ConceptDescriptor[] taxonomyParentConcepts;
    
    
    /**
     * Concepts which should not be exported.
     *
     * @parameter @optional
     */
    private ConceptDescriptor[] conceptsToSkip;
    
    private IntSet stampsToWrite = new IntSet();
    private IntSet pathIds;
    ViewCoordinate vc;
    Collection<Integer> taxonomyParentNids;
    /**
     * Concept number refset parent concept.
     *
     * @parameter @required
     */
    private ConceptDescriptor conceptNumberRefsetParentConceptSpec;

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
            HashSet<UUID> moduleUuids = new HashSet<>();
            if (moduleConcepts != null || moduleConcepts.length > 0) {
                for (ConceptDescriptor cd : moduleConcepts) {
                    moduleUuids.add(UUID.fromString(cd.getUuid()));
                }
            } else {
                throw new MojoExecutionException("No module specified.");
            }
            moduleUuids.add(Snomed.UNSPECIFIED_MODULE.getLenient().getPrimUuid()); //classifier written on this module

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
                if (startDate == null || endDate == null) {
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
            taxonomyParentNids = new HashSet<>();
            for (ConceptDescriptor cd : taxonomyParentConcepts) {
                taxonomyParentNids.add(Ts.get().getNidForUuids(UUID.fromString(cd.getUuid())));
            }

            vc = new ViewCoordinate(Ts.get().getMetadataViewCoordinate());
            vc.getIsaTypeNids().add(Snomed.IS_A.getLenient().getConceptNid());
            PathBI path = Ts.get().getPath(viewPathNid);
            PositionBI position = Ts.get().newPosition(path,
                    TimeHelper.getTimeFromString(endDate, TimeHelper.getFileDateFormat()));
            vc.setPositionSet(new PositionSet(position));
            IntSet moduleIds = new IntSet();
            for (UUID uuid : moduleUuids) {
                moduleIds.add(Ts.get().getNidForUuids(uuid));
            }
            File metaDir = new File(output.getParentFile(), "refset-econcept");
            metaDir.mkdir();
//          compute spec refsets
            Integer refsetParentConceptNid = null;
            if (refsetParentConceptSpec != null) {
                refsetParentConceptNid = Ts.get().getNidForUuids(UUID.fromString(refsetParentConceptSpec.getUuid()));
                vc.getPositionSet().getViewPathNidSet();
                EditCoordinate ec = new EditCoordinate(TermAux.USER.getLenient().getConceptNid(),
                        Ts.get().getNidForUuids(UUID.fromString(moduleConcepts[0].getUuid())),
                        viewPathNid);
                ReleaseSpecProcessor refsetSpecComputer = new ReleaseSpecProcessor(ec,
                        vc, ChangeSetPolicy.OFF, refsetParentConceptNid);
                refsetSpecComputer.process();
                if (releaseType == ReleaseType.FULL) {
                    refsetSpecComputer.writeRefsetSpecMetadata(metaDir); //only care about FULL for import
                }
            }
            stampsToWrite = Bdb.getSapDb().getSpecifiedSapNids(null,
                    TimeHelper.getTimeFromString(startDate, TimeHelper.getFileDateFormat()),
                    TimeHelper.getTimeFromString(endDate, TimeHelper.getFileDateFormat()),
                    null, moduleIds, pathIds);

            File refsetCs = new File(output.getParentFile(), "changesets");
            refsetCs.mkdir();
//            get stamps after refset computations
            stampsToWrite = Bdb.getSapDb().getSpecifiedSapNids(null,
                    TimeHelper.getTimeFromString(startDate, TimeHelper.getFileDateFormat()),
                    TimeHelper.getTimeFromString(endDate, TimeHelper.getFileDateFormat()),
                    null, moduleIds, pathIds);

//          write RF2 specific metadata refsets  
            if (makeRf2Refsets) {
                Rf2RefexComputer rf2RefexComputer = new Rf2RefexComputer(vc, Ts.get().getMetadataEditCoordinate(),
                        refsetCs, stampsToWrite.getAsSet(), effectiveDate, snomedCoreReleaseDate);
                rf2RefexComputer.setup();
                Ts.get().iterateConceptDataInSequence(rf2RefexComputer);
                rf2RefexComputer.addModuleDependencyMember();
                rf2RefexComputer.cleanup();

                Set<Integer> newStampNids = rf2RefexComputer.getNewStampNids();
                for (int stamp : newStampNids) {
                    stampsToWrite.add(stamp);
                }
            }

            IntSet sapsToRemove = new IntSet();
            if (previousReleaseDate != null) {
                IntSet allPaths = new IntSet(pathIds.getSetValues());
                int nid = Ts.get().getNidForUuids(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, editPathConceptSpecFsn));
                allPaths.add(nid);
                sapsToRemove = Bdb.getSapDb().getSpecifiedSapNids(allPaths,
                        TimeHelper.getTimeFromString(previousReleaseDate, TimeHelper.getFileDateFormat()),
                        TimeHelper.getTimeFromString("latest", TimeHelper.getFileDateFormat()));
            }
            getLog().info("Release type: " + releaseType);
            getLog().info("Criterion matches " + stampsToWrite.size() + " sapNids: " + stampsToWrite);
            boolean initMapper = true;
            String[] paths = mappingFileDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File file, String string) {
                    return string.endsWith(".txt");
                }
            });
            for (String s : paths) {
                BufferedReader reader = new BufferedReader(new FileReader(mappingFileDir + System.getProperty("file.separator") + s));
                String readLine = reader.readLine();
                if (reader.readLine() != null) {
                    initMapper = false;
                }
                reader.close();
            }
            if (initMapper) {
                UuidToSctIdMapper mapper = new UuidToSctIdMapper(Ts.get().getAllConceptNids(), namespace, mappingFileDir);
                Ts.get().iterateConceptDataInSequence(mapper); //why sequence?
                mapper.close();
            }
            UuidSnomedMapHandler handler = new UuidSnomedMapHandler(mappingFileDir, mappingFileDir);
            handler.setNamespace(namespace);

            if (refsetParentConceptSpec != null) {
                refsetParentConceptNid = Ts.get().getNidForUuids(UUID.fromString(refsetParentConceptSpec.getUuid()));
            }
            Integer conNumRefsetParentConceptNid = null;
            if (conceptNumberRefsetParentConceptSpec != null) {
                conNumRefsetParentConceptNid = Ts.get().getNidForUuids(UUID.fromString(conceptNumberRefsetParentConceptSpec.getUuid()));
            }

            TaxonomyFilter filter = new TaxonomyFilter();
            Ts.get().iterateConceptDataInParallel(filter);
            NidBitSetBI nidsToRelease = filter.getResults();
            Rf2Export exporter = new Rf2Export(output,
                    releaseType,
                    LANG_CODE.valueOf(languageCode),
                    COUNTRY_CODE.valueOf(countryCode),
                    namespace,
                    moduleConcepts[0].getUuid(),
                    new Date(TimeHelper.getTimeFromString(effectiveDate,
                                    TimeHelper.getAltFileDateFormat())),
                    new Date(TimeHelper.getTimeFromString(snomedCoreReleaseDate,
                                    TimeHelper.getAltFileDateFormat())),
                    stampsToWrite.getAsSet(),
                    vc,
                    excludedRefsetIds.getAsSet(),
                    nidsToRelease,
                    makePrivateAltIdsFile,
                    refsetParentConceptNid,
                    new Date(TimeHelper.getTimeFromString(this.previousReleaseDate,
                                    TimeHelper.getFileDateFormat())),
                    sapsToRemove.getAsSet(),
                    taxonomyParentNids,
                    conNumRefsetParentConceptNid);
            Ts.get().iterateConceptDataInSequence(exporter);
            exporter.writeOneTimeFiles();
            exporter.close();
            UuidToSctIdWriter writer = new UuidToSctIdWriter(namespace, moduleConcepts[0].getUuid(),
                    output, handler, releaseType, COUNTRY_CODE.valueOf(countryCode),
                    new Date(TimeHelper.getTimeFromString(effectiveDate,
                                    TimeHelper.getAltFileDateFormat())), vc);
            writer.write();
            writer.close();
            handler.writeMaps();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private class TaxonomyFilter implements ProcessUnfetchedConceptDataBI {

        ConcurrentSkipListSet<Integer> results = new ConcurrentSkipListSet<>();
        
        private HashSet<Integer> conceptNidsToSkip = new HashSet<Integer>();
        
        {
            if (conceptsToSkip != null)
            {
                for (ConceptDescriptor cs : conceptsToSkip)
                {
                    try
                    {
                        int nid = Ts.get().getNidForUuids(UUID.fromString(cs.getUuid()));
                        System.out.println("Will skip " + cs.getUuid() + " " + cs.getDescription() + nid);
                        conceptNidsToSkip.add(nid);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        @Override
        public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
            if (conceptNidsToSkip.contains(conceptNid))
            {
                return;
            }
            for (int parentNid : taxonomyParentNids) {
                if (Ts.get().wasEverKindOf(conceptNid, parentNid, vc)) {
                    results.add(conceptNid);
                }
            }
        }

        @Override
        public NidBitSetBI getNidSet() throws IOException {
            return Ts.get().getAllConceptNids();
        }

        @Override
        public boolean continueWork() {
            return true;
        }

        public NidBitSetBI getResults() throws IOException {
            NidBitSetBI resultSet = Ts.get().getEmptyNidSet();
            for (Integer nid : results) {
                resultSet.setMember(nid);
            }
            return resultSet;
        }
    }
}
