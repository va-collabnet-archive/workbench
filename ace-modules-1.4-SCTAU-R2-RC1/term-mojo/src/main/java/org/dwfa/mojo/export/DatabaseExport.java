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
package org.dwfa.mojo.export;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.process.I_ProcessQueue;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.task.commit.validator.ValidationException;
import org.dwfa.ace.task.path.PromoteToPath;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.dto.ComponentDto;
import org.dwfa.maven.sctid.UuidSctidMapDb;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.Rf2ModuleDescriptor;
import org.dwfa.mojo.export.file.AceOutputHandler;
import org.dwfa.mojo.export.file.Rf1OutputHandler;
import org.dwfa.mojo.export.file.Rf2OutputHandler;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.AceDateFormat;
import org.dwfa.vodb.types.ThinConPart;


/**
 * Exports the components for the database
 *
 * @goal database-export
 */
public class DatabaseExport extends AbstractMojo implements I_ProcessConcepts {

    /**
     * Class logger.
     */
    private Logger logger = Logger.getLogger(DatabaseExport.class.getName());

    /**
     * Get the exportable data from a concept
     */
    private ExportSpecification exportSpecification;

    /**
     * The release position...
     *
     * @parameter
     * @required
     */
    private PositionDescriptor releasePosition;

    /**
     * Positions for export
     *
     * @parameter
     * @required
     */
    private PositionDescriptor[] positionsForExport;

    /**
     * Positions to exclude from export
     *
     * @parameter
     */
    private PositionDescriptor[] excludedPositions;

    /**
     * Origins for export
     *
     * @parameter
     */
    private PositionDescriptor[] originsForExport;

    /**
     * Positions mapped to release modules.
     *
     * @parameter
     */
    private Rf2ModuleDescriptor[] moduleDescriptors;

    /**
     * Included hierarchy
     *
     * @parameter
     * @required
     */
    private ConceptDescriptor[]  inclusions;

    /**
     * Excluded hierarchy
     *
     * @parameter
     */
    private ConceptDescriptor[]  exclusions;

    /**
     * The default namespace for this export
     *
     * @parameter
     * @required
     */
    private String defaultNamespace;

    /**
     * The default namespace for this export
     *
     * @parameter
     * @required
     */
    private String defaultProject;

    /**
     * Directory for export files.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File exportDirectory;

    /**
     * URL used to connect to the UUID-SCTID database
     *
     * @parameter
     * @required
     */
    private String uuidSctidDbConnectionUrl;

    /**
     * UUID-SCTID database driver fully qualified class name
     *
     * @parameter
     * @required
     */
    private String uuidSctidDbDriver;

    /**
     * UUID-SCTID database user to optionally authenticate to the database
     *
     * @parameter
     */
    private String uuidSctidDbUsername;

    /**
     * UUID-SCTID database user's password optionally used to authenticate to the database
     *
     * @parameter
     */
    private String uuidSctidDbPassword;

    /**
     * The number of threads to use.
     *
     * @parameter
     */
    private int numberOfThreads = 1;

    /**
     * Export file output handler for RF2
     */
    private ExportOutputHandler rf2OutputHandler;

    /**
     * Export file output handler for RF1
     */
    private ExportOutputHandler rf1OutputHandler;

    /**
     * Export file output handler for Ace
     */
    private AceOutputHandler aceOutputHandler;;

    /** Da factory */
    private I_TermFactory termFactory;

    /**
     * Processed concept count.
     */
    private int processedConceptCount = 0;

    /**
     * Thread pool for processing concepts.
     */
    private I_ProcessQueue workQueue;

    /**
     * The current Batch of concepts to process.
     */
    private List<I_GetConceptData> currentBatch = new ArrayList<I_GetConceptData>();

    /**
     * Promotes to concepts for calculating promotion origins.
     */
    private I_GetConceptData promotesToConcept;

    /**
     * Tuple part to use for exporting new refset content namely the ADRS.
     */
    private ThinConPart releasePart;

    /**
     * TODO need to mock this out.
     * For testing
     */
    private boolean testing = false;

    /**
     * Iterate concepts.
     *
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        logger.info("Start exporting concepts");

        try {
            promotesToConcept = getTermFactory().getConcept(ConceptConstants.PROMOTES_TO.localize().getNid());

            System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DRIVER, uuidSctidDbDriver);
            System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DATABASE_CONNECTION_URL, uuidSctidDbConnectionUrl);
            if (uuidSctidDbUsername != null) {
                System.setProperty(UuidSctidMapDb.SCT_ID_MAP_USER, uuidSctidDbUsername);
            }
            if (uuidSctidDbPassword != null) {
                System.setProperty(UuidSctidMapDb.SCT_ID_MAP_PASSWORD, uuidSctidDbPassword);
            }

            workQueue = LocalVersionedTerminology.get().newProcessQueue(numberOfThreads);

            Map<UUID, Map<UUID, Date>> releasePathDateMap = getReleasePathDateMap();

            List<Position> positions = new ArrayList<Position>();
            setTestOriginPositions(releasePathDateMap, positions);

            for (PositionDescriptor positionDescriptor : positionsForExport) {
                positions.add(new Position(positionDescriptor));
            }

            rf2OutputHandler = new Rf2OutputHandler(
                new File(exportDirectory.getAbsolutePath() + File.separatorChar + "rf2" + File.separatorChar), releasePathDateMap);
            rf1OutputHandler = new Rf1OutputHandler(
                new File(exportDirectory.getAbsolutePath() + File.separatorChar + "rf1" + File.separatorChar), releasePathDateMap);
            aceOutputHandler = new AceOutputHandler(
                new File(exportDirectory.getAbsolutePath() + File.separatorChar + "ace" + File.separatorChar), releasePathDateMap);

            List<I_GetConceptData> inclusionRoots = new ArrayList<I_GetConceptData>();
            for (ConceptDescriptor conceptDescriptor : inclusions) {
                inclusionRoots.add(conceptDescriptor.getVerifiedConcept());
            }

            List<I_GetConceptData> exclusionRoots = new ArrayList<I_GetConceptData>();
            if (exclusions != null) {
                for (ConceptDescriptor conceptDescriptor : exclusions) {
                    exclusionRoots.add(conceptDescriptor.getVerifiedConcept());
                }
            }

            releasePart = new ThinConPart();
            releasePart.setPathId(releasePosition.getPosition().getPath().getConceptId());
            releasePart.setStatusId(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            releasePart.setVersion(releasePosition.getPosition().getVersion());

            exportSpecification = new ExportSpecification(positions, inclusionRoots, exclusionRoots, NAMESPACE.fromString(defaultNamespace), PROJECT.valueOf(defaultProject));
            exportSpecification.setReleasePart(releasePart);
        } catch (IOException e) {
            throw new MojoExecutionException("Execute error: ", e);
        } catch (SQLException e) {
            throw new MojoExecutionException("Execute error: ", e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Execute error: ", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Execute error: ", e);
        }

        try {
            getTermFactory().iterateConcepts(this);

            workQueue.execute(new RunnableProcessConcept(currentBatch));
            workQueue.awaitCompletion();

            if (!testing) {
                ((Rf2OutputHandler) rf2OutputHandler).closeFiles();
                ((Rf1OutputHandler) rf1OutputHandler).closeFiles();
                ((AceOutputHandler) aceOutputHandler).closeFiles();
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Iterate error: ", e);
        }

        logger.info("Finished exporting concepts");
    }

    /**
     * Adds to the <code>positions</code> List all the paths that have been
     * promoted to test.
     *
     * Adds release position Map to the <code>releasePathDateMap</code> Map for
     * each test path so the correct module id and time stamp are export for the
     * test paths.
     *
     * @param releasePathDateMap Map of UUID Date maps.
     * @param positions list of export Positions
     * @throws Exception
     * @throws TerminologyException
     * @throws IOException
     * @throws ParseException
     */
    private void setTestOriginPositions(Map<UUID, Map<UUID, Date>> releasePathDateMap, List<Position> positions)
            throws Exception, TerminologyException, IOException, ParseException {
        if (originsForExport != null) {
            Set<I_Position> positionOrigins = new HashSet<I_Position>();
            for (PositionDescriptor positionDescriptor : originsForExport) {
                positionOrigins.addAll(
                    PromoteToPath.getPositionsToCopy(positionDescriptor.getPath().getVerifiedConcept(), promotesToConcept, getTermFactory()));
            }

            for (I_Position iPosition : positionOrigins) {
                UUID pathUuid = termFactory.getUids(iPosition.getPath().getConceptId()).iterator().next();
                if (!isExcludedPath(pathUuid)) {
                    Date timePoint = new Date();
                    timePoint.setTime(iPosition.getTime());
                    Position position = new Position(getTermFactory().getConcept(iPosition.getPath().getConceptId()),
                        timePoint);
                    position.setLastest(true);
                    positions.add(position);

                    if (!releasePosition.getPath().getUuid().equals(pathUuid.toString())) {
                        Map<UUID, Date> mappedModuleDate;
                        if (releasePathDateMap.containsKey(pathUuid)) {
                            mappedModuleDate = releasePathDateMap.get(pathUuid);
                        } else {
                            mappedModuleDate = new HashMap<UUID, Date>(1);
                            releasePathDateMap.put(pathUuid, mappedModuleDate);
                        }
                        mappedModuleDate.put(UUID.fromString(releasePosition.getPath().getUuid()),
                            AceDateFormat.getVersionHelperDateFormat().parse(releasePosition.getTimeString()));
                    }
                }
            }
        }
    }

    /**
     * Process a concept.
     *
     * @see org.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.I_GetConceptData)
     */
    @Override
    public void processConcept(I_GetConceptData concept) throws Exception {
        currentBatch.add(concept);

        if(currentBatch.size() % 1000 == 0){
            workQueue.execute(new RunnableProcessConcept(currentBatch));
            currentBatch.clear();
        }
    }

    /**
     * @return the termFactory
     */
    private final I_TermFactory getTermFactory() {
        if(termFactory == null){
            termFactory = LocalVersionedTerminology.get();
        }
        return termFactory;
    }

    /**
     * @param termFactory the termFactory to set
     */
    public void setTermFactory(I_TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    /**
     * Gets the list of mapped Paths to release Modules and time stamps.
     *
     * Allows mapping of a path to a release module and time.
     *
     * @return Map of UUID modules, Date time stamp maps.
     *
     * @throws ParseException
     */
    private Map<UUID, Map<UUID, Date>> getReleasePathDateMap() throws ParseException {
        Map<UUID, Map<UUID, Date>> releasePathDateMap = new HashMap<UUID, Map<UUID,Date>>();

        if (moduleDescriptors != null) {
            for (Rf2ModuleDescriptor rf2ModuleDescriptor : moduleDescriptors) {
                Map<UUID, Date> mappedModuleDate;
                UUID pathUuid = UUID.fromString(rf2ModuleDescriptor.getPath().getUuid());

                if (releasePathDateMap.containsKey(pathUuid)) {
                    mappedModuleDate = releasePathDateMap.get(pathUuid);
                } else {
                    mappedModuleDate = new HashMap<UUID, Date>(1);
                    releasePathDateMap.put(pathUuid, mappedModuleDate);
                }

                mappedModuleDate.put(UUID.fromString(rf2ModuleDescriptor.getModule().getUuid()),
                    AceDateFormat.getRf2DateFormat().parse(rf2ModuleDescriptor.getModuleTimeString()));
            }
        }

        return releasePathDateMap;
    }

    /**
     * Is the path UUID in the excluded path list
     *
     * @param uuid UUID
     * @return true if and excluded Path UUID
     */
    private boolean isExcludedPath(UUID uuid) {
        boolean excluded = false;
        for (PositionDescriptor positionDescriptor : excludedPositions) {
            if (positionDescriptor.getPath().getUuid().equals(uuid.toString())) {
                excluded = true;
                break;
            }
        }
        return excluded;
    }

    /**
     * Class to use with Work Queue.
     */
    class RunnableProcessConcept implements Runnable {
        private List<I_GetConceptData> conceptsToProcess = new ArrayList<I_GetConceptData>();

        /**
         * Create a runnable with the list of jobs
         *
         * @param conceptsToProcess List of I_GetConceptData
         */
        RunnableProcessConcept(List<I_GetConceptData> conceptsToProcess) {
            this.conceptsToProcess = new ArrayList<I_GetConceptData>(conceptsToProcess);
        }

        /**
         * Process the list of concepts in the thread.
         */
        @Override
        public void run() {
            for(I_GetConceptData concept : conceptsToProcess){
                processedConceptCount++;
                if(processedConceptCount % 10000 == 0){
                    logger.info("Concepts processed: " + processedConceptCount);
                }

                ComponentDto componentDto;
                try {
                    componentDto = exportSpecification.getDataForExport(concept);
                    if(componentDto != null){
                        try{
                            rf2OutputHandler.export(componentDto);
                            rf1OutputHandler.export(componentDto);
                            aceOutputHandler.export(componentDto);
                        } catch (ValidationException ve) {
                            logger.severe(ve.getMessage());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }

    }
}
