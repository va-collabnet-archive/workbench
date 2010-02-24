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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.dto.ComponentDto;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.export.file.Rf2OutputHandler;


/**
 * Exports the components for the database
 *
 * @goal database-export
 */
public class DatabaseExport extends AbstractMojo implements I_ProcessConcepts {

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
     * @required
     */
    private ConceptDescriptor[]  exclusions;

    /**
     * Directory for export files.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File exportDirectory;

    /**
     * Directory for the SCT id database
     *
     *
     * @parameter
     * @required
     */
    private File SctIdDbDirectory;

    /** Export file output handler for RF2 */
    ExportOutputHandler rf2OutputHandler;

    /** Da factory */
    private I_TermFactory termFactory;

    /**
     * Iterate concepts.
     *
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            rf2OutputHandler = new Rf2OutputHandler(exportDirectory, SctIdDbDirectory);

            List<Position> positions = new ArrayList<Position>();
            for (PositionDescriptor positionDescriptor : positionsForExport) {
                positions.add(new Position(positionDescriptor));
            }
            List<I_GetConceptData> inclusionRoots = new ArrayList<I_GetConceptData>();
            for (ConceptDescriptor conceptDescriptor : inclusions) {
                inclusionRoots.add(conceptDescriptor.getVerifiedConcept());
            }
            List<I_GetConceptData> exclusionRoots = new ArrayList<I_GetConceptData>();
            for (ConceptDescriptor conceptDescriptor : exclusions) {
                exclusionRoots.add(conceptDescriptor.getVerifiedConcept());
            }

            exportSpecification = new ExportSpecification(positions, inclusionRoots, exclusionRoots);
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
        } catch (Exception e) {
            throw new MojoExecutionException("Iterate error: ", e);
        }
    }

    /**
     * Process a concept.
     *
     * @see org.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.I_GetConceptData)
     */
    @Override
    public void processConcept(I_GetConceptData concept) throws Exception {
        ComponentDto componentDto = exportSpecification.getDataForExport(concept);
        if(componentDto != null){
            rf2OutputHandler.export(componentDto);
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
