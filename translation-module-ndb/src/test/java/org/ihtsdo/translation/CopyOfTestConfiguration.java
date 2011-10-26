/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.translation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;

/**
 * The Class TestContextualizedDescriptions.
 */
public class CopyOfTestConfiguration extends TestCase {

	Logger logger = Logger.getLogger(CopyOfTestConfiguration.class);
	
	/** The vodb directory. */
	File vodbDirectory;

	/** The read only. */
	boolean readOnly = false;

	/** The cache size. */
	Long cacheSize = Long.getLong("600000000");

	/** The db setup config. */
	DatabaseSetupConfig dbSetupConfig;

	/** The config. */
	I_ConfigAceFrame config;

	/** The tf. */
	I_TermFactory tf;

	/** The new project concept. */
	I_GetConceptData newProjectConcept;

	/** The allowed statuses with retired. */
	I_IntSet allowedStatusesWithRetired;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		System.out.println("Deleting test fixture");
		deleteDirectory(new File("berkeley-db"));
		System.out.println("Creating test fixture");
		//copyDirectory(new File("/workspaces/Translation Module/project-management-ndb/berkeley-db"), new File("berkeley-db"));
		vodbDirectory = new File("/workspaces/Translation Module/project-management-ndb/berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = Terms.get();
		config = getTestConfig();
	}

	/**
	 * Test state full.
	 */
	public void testStateFull() {
		try {
			logger.debug("CONFIG: " + config);
			I_IntSet allowedDestRelTypes = tf.newIntSet();
			I_IntSet allowedStatus = tf.newIntSet();
			logger.debug("Configuration done");
			allowedDestRelTypes = config.getDestRelTypes();
			// allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			allowedStatus = config.getAllowedStatus();
			I_GetConceptData snomedRoot = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			logger.debug("VIEW POSITION: " + config.getViewPositionSetReadOnly());
			List<? extends I_RelTuple> children = snomedRoot.getDestRelTuples(allowedStatus, allowedDestRelTypes, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(),
					config.getConflictResolutionStrategy(), config.getClassifierConcept().getNid(), 
					RelAssertionType.INFERRED);
			logger.debug("CHILDREN SIZE: " + children.size());
			for (I_RelTuple tuple : children) {
				I_GetConceptData concept = tf.getConcept(tuple.getConceptNid());
				logger.debug(concept);
			}
		} catch (TerminologyException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private I_ConfigAceFrame getTestConfig() {
		I_ConfigAceFrame config = null;
		try {
			config = tf.newAceFrameConfig();
			PathBI path = tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")});
			PathBI editPath = tf.getPath(new UUID[] {UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")});
			DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
			PositionBI position = tf.newPosition(path, Long.MAX_VALUE);
			config.addViewPosition(position);
			config.addEditingPath(editPath);
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			config.getDestRelTypes().add(tf.getConcept(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")).getConceptNid());
			config.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.localize().getNid());
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());

			config.setPrecedence(Precedence.TIME);
			tf.setActiveAceFrameConfig(config);
			//tf.setupIsaCache(config.getViewCoordinate().getIsaCoordinate()).getLatch().await();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return config;
	}

	// If targetLocation does not exist, it will be created.
	public void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}
}
