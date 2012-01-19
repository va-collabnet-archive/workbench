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
import org.dwfa.util.id.Type5UuidFactory;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.country.COUNTRY_CODE;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.helper.export.Rf2Export;
import org.ihtsdo.helper.rf2.Rf2File;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.helper.transform.UuidToSctIdMapper;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * Goal which generates an incremental Rf2 file.
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
     * Start date for inclusion in the RF2 files, in yyyy-MM-dd-HH.mm.ss format.
     *
     * @parameter @required
     */
    private String startDate;
    /**
     * End date for inclusion in the RF2 files, in yyyy-MM-dd-HH.mm.ss format.
     *
     * @parameter @required
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
     * Project module, as fsn
     *
     * @parameter @required
     */
    private String module;
    /**
     * country code
     *
     * @parameter @required
     */
    private COUNTRY_CODE countryCode;
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
     * Directory of the berkeley database to export from.
     *
     * @parameter
     * expression="${project.build.directory}/generated-resources/berkeley-db"
     * @required
     */
    private File berkeleyDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Bdb.setup(berkeleyDir.getAbsolutePath());
            IntSet pathIds = new IntSet();
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
            UUID moduleId = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, module);

            int viewPathNid;
            if (viewPathConceptSpec != null) {
                viewPathNid = viewPathConceptSpec.getLenient().getNid();
            } else if (viewPathConceptSpecFsn != null) {
                UUID pathUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, viewPathConceptSpecFsn);
                viewPathNid = Ts.get().getNidForUuids(pathUuid);
            } else {
                throw new MojoExecutionException("No view path specified.");
            }
            IntSet sapsToWrite = Bdb.getSapDb().getSpecifiedSapNids(pathIds,
                    TimeHelper.getFileDateFormat().parse(startDate).getTime(),
                    TimeHelper.getTimeFromString(endDate, TimeHelper.getFileDateFormat()));

            ViewCoordinate vc = new ViewCoordinate(Ts.get().getMetadataVC());


            PathBI path = Ts.get().getPath(viewPathNid);
            PositionBI position = Ts.get().newPosition(path,
                    TimeHelper.getTimeFromString(endDate, TimeHelper.getFileDateFormat()));
            vc.setPositionSet(new PositionSet(position));
            getLog().info("Criterion matches " + sapsToWrite.size() + " sapNids: " + sapsToWrite);
            if (sapsToWrite.size() > 0) {
                NidBitSetBI allConcepts = Ts.get().getAllConceptNids();
                Rf2Export exporter = new Rf2Export(output,
                        Rf2File.ReleaseType.DELTA,
                        LANG_CODE.EN,
                        countryCode,
                        namespace,
                        moduleId.toString(),
                        new Date(TimeHelper.getTimeFromString(effectiveDate,
                        TimeHelper.getFileDateFormat())),
                        sapsToWrite.getAsSet(),
                        vc.getVcWithAllStatusValues(),
                        excludedRefsetIds.getAsSet(),
                        allConcepts);
                Ts.get().iterateConceptDataInSequence(exporter);
                exporter.close();
                UuidToSctIdMapper mapper = new UuidToSctIdMapper(namespace, moduleId.toString(), output);
                mapper.map();
                mapper.close();
            }



        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
