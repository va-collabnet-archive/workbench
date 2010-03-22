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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.commit.validator.ValidationException;
import org.dwfa.dto.ComponentDto;
import org.dwfa.maven.sctid.UuidSctidMapDb;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.export.file.AceOutputHandler;
import org.dwfa.mojo.export.file.Rf1OutputHandler;
import org.dwfa.mojo.export.file.Rf2OutputHandler;


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
     * Positions for export
     *
     * @parameter
     * @required
     */
    private PositionDescriptor[] positionsForExport;

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
    String uuidSctidDbConnectionUrl;

    /**
     * UUID-SCTID database driver fully qualified class name
     *
     * @parameter
     * @required
     */
    String uuidSctidDbDriver;

    /**
     * UUID-SCTID database user to optionally authenticate to the database
     *
     * @parameter
     */
    String uuidSctidDbUsername;

    /**
     * UUID-SCTID database user's password optionally used to authenticate to the database
     *
     * @parameter
     */
    String uuidSctidDbPassword;

    /**
     * Export file output handler for RF2
     */
    ExportOutputHandler rf2OutputHandler;

    /**
     * Export file output handler for RF1
     */
    ExportOutputHandler rf1OutputHandler;

    /**
     * Export file output handler for Ace
     */
    AceOutputHandler aceOutputHandler;;

    /** Da factory */
    private I_TermFactory termFactory;

    /**
     * Processed concept count.
     */
    private int processedConceptCount = 0;

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
            System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DRIVER, uuidSctidDbDriver);
            System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DATABASE_CONNECTION_URL, uuidSctidDbConnectionUrl);
            if (uuidSctidDbUsername != null) {
                System.setProperty(UuidSctidMapDb.SCT_ID_MAP_USER, uuidSctidDbUsername);
            }
            if (uuidSctidDbPassword != null) {
                System.setProperty(UuidSctidMapDb.SCT_ID_MAP_PASSWORD, uuidSctidDbPassword);
            }

            rf2OutputHandler = new Rf2OutputHandler(
                new File(exportDirectory.getAbsolutePath() + File.separatorChar + "rf2" + File.separatorChar));
            rf1OutputHandler = new Rf1OutputHandler(
                new File(exportDirectory.getAbsolutePath() + File.separatorChar + "rf1" + File.separatorChar));
            aceOutputHandler = new AceOutputHandler(
                new File(exportDirectory.getAbsolutePath() + File.separatorChar + "ace" + File.separatorChar));

            List<Position> positions = new ArrayList<Position>();
            for (PositionDescriptor positionDescriptor : positionsForExport) {
                positions.add(new Position(positionDescriptor));
            }
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

            exportSpecification = new ExportSpecification(positions, inclusionRoots, exclusionRoots, NAMESPACE.fromString(defaultNamespace));
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
     * Process a concept.
     *
     * @see org.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.I_GetConceptData)
     */
    @Override
    public void processConcept(I_GetConceptData concept) throws Exception {
        processedConceptCount++;
        if(processedConceptCount % 10000 == 0){
            logger.info("Concepts processed: " + processedConceptCount);
        }

        ComponentDto componentDto = exportSpecification.getDataForExport(concept);
        if(componentDto != null){
            try{
                rf2OutputHandler.export(componentDto);
                rf1OutputHandler.export(componentDto);
                aceOutputHandler.export(componentDto);
            } catch (ValidationException ve) {
                logger.severe(ve.getMessage());
            }
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
}
