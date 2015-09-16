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
package gov.va.export.uscrs;

import gov.va.export.uscrs.USCRSRequestHandler;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.Terms;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 *
 * @goal export-USCRS
 *
 * @phase process-sources
 */
public class USCRSExportMojo extends AbstractMojo {
    /**
     * output directory.
     *
     * @parameter 
     * @required
     */
    private File output;

    /**
     * Project ID namespace, as SCT id
     *
     * @parameter
     * @required
     */
    private String namespace;

    /**
     * Date String (mm/dd/yyyy)
     *
     * @parameter
     */
    private String previousReleaseDate;

    /**
     * Text of view path concept's FSN, to be used when only the FSN is known,
     * and the path concept was generated with the proper type 5 UUID algorithm
     * using the Type5UuidFactory.PATH_ID_FROM_FS_DESC namespace.
     *
     * @parameter
     */
    private String viewPathConceptSpecFsn;
    
    /**
     * Directory of the berkeley database to export from.
     *
     * @parameter
     * expression="${project.build.directory}/generated-resources/berkeley-db"
     * @required
     */
    private File berkeleyDir;

    /**
     * To execute on a subset of concepts.
     *
     * @parameter
     */
	private Set<Integer> nidsToInvestigate;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Bdb.setup(berkeleyDir.getAbsolutePath());
            ViewCoordinate vc = initializeVc();

            USCRSRequestHandler handler = null;
            int namespaceId = parseNamespace(namespace);

            if (previousReleaseDate != null) {
            	long previousExportTime = parsePrevExportDate(previousReleaseDate);
            	handler = new USCRSRequestHandler(vc, output, namespaceId, previousExportTime);
            } else {
            	handler = new USCRSRequestHandler(vc, output, namespaceId);
            }
            
            if (handler.failedSetupStatus()) {
            	throw new Exception("Failed setup of USCRS Request Handler");
            }

            if (!nidsToInvestigate.isEmpty()) {
            	handler.setConceptsToInvestigate(nidsToInvestigate);
            }
            
        	Terms.get().iterateConcepts(handler);
        	handler.completeProcess();
        } catch (Exception e) {
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

	private ViewCoordinate initializeVc() throws Exception {
        int viewPathNid;
       
        if (viewPathConceptSpecFsn != null) {
            UUID pathUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, viewPathConceptSpecFsn);
            viewPathNid = Ts.get().getNidForUuids(pathUuid);
        } else {
            throw new MojoExecutionException("No view path specified.");
        }

        
        ViewCoordinate vc = new ViewCoordinate(Ts.get().getMetadataViewCoordinate());
        vc.getIsaTypeNids().add(Snomed.IS_A.getLenient().getConceptNid());
        PathBI path = Ts.get().getPath(viewPathNid);
        
        PositionBI position = Ts.get().newPosition(path, System.currentTimeMillis());
        vc.setPositionSet(new PositionSet(position));
        
        NidSetBI allowedStatusNids = new NidSet();
        allowedStatusNids.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
        allowedStatusNids.add(SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid());
        vc.setAllowedStatusNids(allowedStatusNids);
        return vc;
	}

	private long parsePrevExportDate(String date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

		return sdf.parse(date).getTime();
	}

	private int parseNamespace(String ns) throws Exception {
		int namespaceId = -1;
		
		try {
			namespaceId = Integer.parseInt (ns);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Must specify an integer for the namespace.  Value entered: " + ns);
		}
		
		if (namespaceId <= 0) {
			throw new Exception("Namespace must be a positive integer.  Value entered: " + ns);
		}

		return namespaceId;
	}

}
