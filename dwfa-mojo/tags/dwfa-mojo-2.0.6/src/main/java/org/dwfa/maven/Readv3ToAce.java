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
package org.dwfa.maven;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * Convert readv3 files to a set of <a
 * href='https://mgr.cubit.aceworkspace.net/apps/dwfa/ace-mojo/dataimport.html'>ACE
 * formatted</a> files.
 * 
 * @goal readv3-to-ace
 * @phase process-resources
 */

public class Readv3ToAce extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the readv3 data directory to read from.
	 * 
	 * @parameter expression="${project.build.directory}/generated-resources/net/nhs/uktc/v3"
	 * @required
	 */
	private File readv3dir;

	/**
	 * Location of the ace data directory to write to.
	 * 
	 * @parameter expression="${project.build.directory}/classes/ace"
	 * @required
	 */
	private File aceDir;

	/**
	 * The effective date to associate with all changes.
	 * 
	 * @parameter
	 * @required
	 */
	private String effectiveDate;

	/**
	 * The path name to associate with all changes.
	 * 
	 * @parameter
	 * @required
	 */
	private String pathFsDesc;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			getLog().info("readv3 dir: " + readv3dir);
			getLog().info("ACE dir: " + aceDir);
			aceDir.mkdirs();

			BufferedReader descriptionFileReader = new BufferedReader(
					new FileReader(new File(readv3dir, "Descrip.v3")));

			BufferedReader termsFileReader = new BufferedReader(new FileReader(
					new File(readv3dir, "Terms.v3")));

			BufferedReader conceptFileReader = new BufferedReader(
					new FileReader(new File(readv3dir, "Concept.v3")));

			BufferedReader relationshipFileReader = new BufferedReader(
					new FileReader(new File(readv3dir, "V3hier.v3")));

			Writer concepts = new BufferedWriter(new FileWriter(new File(
					aceDir, "concepts.txt")));

			Writer descriptions = new BufferedWriter(new FileWriter(new File(
					aceDir, "descriptions.txt")));

			Writer relationships = new BufferedWriter(new FileWriter(new File(
					aceDir, "relationships.txt")));

			/*
			 * Writer ids = new BufferedWriter(new FileWriter(new File(aceDir,
			 * "ids.txt")));
			 */

			UUID pathUUID = Type5UuidFactory.get(
					Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathFsDesc);

			Map<String, String> termidReadCodeMap = new HashMap<String, String>();

			Map<String, String> termidDescTypeMap = new HashMap<String, String>();

			setupDescriptionIndex(descriptionFileReader, termidReadCodeMap,
					termidDescTypeMap);

			/* Description create */
			while (termsFileReader.ready()) {
				int termidIndex = 0;
				int termStatusIndex = 1;
				int term30Index = 2;
				int term60Index = 3;
				int term198Index = 4;

				String line = termsFileReader.readLine();
				if (line != null && line.length() > 5) {

					String[] termparts = line.split("\\|");

					String descriptionType = termidDescTypeMap
							.get(termparts[termidIndex]);

					ArchitectonicAuxiliary.Concept descriptionTypeConcept = null;

					if (descriptionType != null) {

						if (descriptionType.equals("P")) {

							descriptionTypeConcept = ArchitectonicAuxiliary.Concept.READ_30_DESC;

						} else if (descriptionType.equals("S")) {
							descriptionTypeConcept = ArchitectonicAuxiliary.Concept.READ_SYN_30_DESC;

						}

						writeDescription(descriptions, pathUUID,
								termidReadCodeMap, termidIndex,
								termStatusIndex, termparts[term30Index],
								termparts,
								Type5UuidFactory.READV3_TERM30_DESC_ID,
								descriptionTypeConcept);
						if (termparts.length > term60Index) {
							writeDescription(descriptions, pathUUID,
									termidReadCodeMap, termidIndex,
									termStatusIndex, termparts[term60Index],
									termparts,
									Type5UuidFactory.READV3_TERM60_DESC_ID,
									descriptionTypeConcept);
						}
						if (termparts.length > term198Index) {
							writeDescription(descriptions, pathUUID,
									termidReadCodeMap, termidIndex,
									termStatusIndex, termparts[term198Index],
									termparts,
									Type5UuidFactory.READV3_TERM198_DESC_ID,
									descriptionTypeConcept);
						}
					}
				}

				/* Concept Create */

				while (conceptFileReader.ready()) {
					int readcodeIndex = 0;
					int conceptStatusIndex = 1;
					int linguisticIndex = 2;
					int subjectTypeIndex = 3;

					String line2 = conceptFileReader.readLine();
					if (line2 != null && line2.length() > 5) {

						String[] conceptparts = line2.split("\\|");

						concepts.append(Type5UuidFactory.get(
								Type5UuidFactory.READV3_CONCEPT_ID,
								conceptparts[readcodeIndex])
								.toString());
						concepts.append("\t");

						if (conceptparts[conceptStatusIndex].equals("O")) {

							concepts
									.append(ArchitectonicAuxiliary.Concept.OPTIONAL
											.getUids().iterator().next()
											.toString());

						} else if (conceptparts[conceptStatusIndex].equals("C")) {

							concepts
									.append(ArchitectonicAuxiliary.Concept.CURRENT
											.getUids().iterator().next()
											.toString());

						} else if (conceptparts[conceptStatusIndex].equals("R")) {

							concepts
									.append(ArchitectonicAuxiliary.Concept.RETIRED
											.getUids().iterator().next()
											.toString());

						} else if (conceptparts[conceptStatusIndex].equals("E")) {

							concepts
									.append(ArchitectonicAuxiliary.Concept.EXTINCT
											.getUids().iterator().next()
											.toString());
						} else {

							throw new Exception("Cant handle :"
									+ conceptparts[conceptStatusIndex]);

						}

						concepts.append("\t");

						concepts.append("1");
						concepts.append("\t");

						concepts.append(effectiveDate);
						concepts.append("\t");

						concepts.append(pathUUID.toString());
						concepts.append("\n");

					}

				}
			}

			/* Relationship Create */
			while (relationshipFileReader.ready()) {

				int childReadcodeIndex = 0;
				int parentReadcodeIndex = 1;
				int childReadcodeOrder = 2;

				String line3 = relationshipFileReader.readLine();
				if (line3 != null && line3.length() > 5) {

					String[] relationshipParts = line3.split("\\|");

					relationships.append(Type5UuidFactory.get(Type5UuidFactory.READV3_REL_ID,
							relationshipParts[childReadcodeIndex]+relationshipParts[parentReadcodeIndex]).toString());
					relationships.append("\t");

					relationships.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString());
					relationships.append("\t");

					relationships.append(Type5UuidFactory.get(Type5UuidFactory.READV3_CONCEPT_ID,
							relationshipParts[childReadcodeIndex]).toString());
					relationships.append("\t");

					relationships
							.append(ArchitectonicAuxiliary.Concept.IS_A_REL
									.getUids().iterator().next().toString());
					relationships.append("\t");

					relationships
							.append(Type5UuidFactory.get(Type5UuidFactory.READV3_CONCEPT_ID,
									relationshipParts[parentReadcodeIndex]).toString());
					relationships.append("\t");

					relationships
							.append(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP
									.getUids().iterator().next().toString());
					relationships.append("\t");
					// refine
					relationships
							.append(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
									.getUids().iterator().next().toString());
					relationships.append("\t");
					// group
					relationships.append("0");
					relationships.append("\t");

					relationships.append(effectiveDate);
					relationships.append("\t");

					relationships.append(pathUUID.toString());
					relationships.append("\n");

				}

			}

			/*
			 * if (line.length() > 0) { processRow(concepts, descriptions,
			 * relationships, ids, pathUUID, line); }
			 */

			/*
			 * writeRoot(concepts, descriptions, relationships, ids, pathUUID);
			 * 
			 * while (r.ready()) { String line = r.readLine(); if (line.length() >
			 * 0) { processRow(concepts, descriptions, relationships, ids,
			 * pathUUID, line); } }
			 */
			descriptionFileReader.close();

			descriptions.close();
			concepts.close();
			relationships.close();

			/* ids.close(); */

		} catch (FileNotFoundException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

	}

	private void writeDescription(Writer descriptions, UUID pathUUID,
			Map<String, String> termidReadCodeMap, int termidIndex,
			int termStatusIndex, String term, String[] termparts,
			UUID genPrefix,
			ArchitectonicAuxiliary.Concept descriptionTypeConcept)
			throws Exception {

		if (term == null || term.length() < 2) {

			return;
		}
		descriptions.append(Type5UuidFactory.get(genPrefix,
				termparts[termidIndex] + term).toString());
		descriptions.append("\t");

		if (termparts[termStatusIndex].equals("O")) {

			descriptions.append(ArchitectonicAuxiliary.Concept.RETIRED
					.getUids().iterator().next().toString());

		} else if (termparts[termStatusIndex].equals("C")) {

			descriptions.append(ArchitectonicAuxiliary.Concept.CURRENT
					.getUids().iterator().next().toString());

		} else {

			throw new Exception("Cant handle :" + termparts[termStatusIndex]);

		}

		descriptions.append("\t");

		descriptions.append(Type5UuidFactory.get(
				Type5UuidFactory.READV3_CONCEPT_ID,
				termidReadCodeMap.get(termparts[termidIndex])).toString());
		descriptions.append("\t");

		descriptions.append(term);
		descriptions.append("\t");

		descriptions.append("1");
		descriptions.append("\t");

		descriptions.append(descriptionTypeConcept.getUids().iterator().next()
				.toString());

		descriptions.append("\t");

		descriptions.append("en-gb");
		descriptions.append("\t");

		descriptions.append(effectiveDate);
		descriptions.append("\t");

		descriptions.append(pathUUID.toString());
		descriptions.append("\n");
	}

	private void setupDescriptionIndex(BufferedReader descriptionFileReader,
			Map<String, String> termidReadCodeMap,
			Map<String, String> termidDescTypeMap) throws IOException {

		while (descriptionFileReader.ready()) {
			int readcodeIndex = 0;
			int termidIndex = 1;
			int descTypeIndex = 2;

			String line = descriptionFileReader.readLine();
			if (line != null && line.length() > 5) {
				String[] descparts = line.split("\\|");

				termidReadCodeMap.put(descparts[termidIndex],
						descparts[readcodeIndex]);
				termidDescTypeMap.put(descparts[termidIndex],
						descparts[descTypeIndex]);

			}

		}
	}

	private void processRow(Writer concepts, Writer descriptions,
			Writer relationships, Writer ids, UUID pathUUID, String line)
			throws IOException, NoSuchAlgorithmException,
			UnsupportedEncodingException {

		String id = line.substring(0, 8).trim();
		String desc = line.substring(8).trim();

		ids.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, id)
				.toString()); // concept id
		ids.append("\t");
		ids.append(ArchitectonicAuxiliary.Concept.UNSPECIFIED_STRING.getUids()
				.iterator().next().toString()); // source
		ids.append("\t");
		ids.append(id); // source id
		ids.append("\t");
		ids.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator()
				.next().toString()); // status
		ids.append("\t");
		ids.append(effectiveDate);
		ids.append("\t");
		ids.append(pathUUID.toString()); // path id
		ids.append("\n");

		concepts.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID,
				id).toString()); // concept id
		concepts.append("\t");
		concepts.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
				.iterator().next().toString()); // status
		concepts.append("\t");
		concepts.append("1"); // primitive
		concepts.append("\t");
		concepts.append(effectiveDate);
		concepts.append("\t");
		concepts.append(pathUUID.toString()); // path id
		concepts.append("\n");

		descriptions.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_DESC_ID,
				id).toString());
		descriptions.append("\t");
		descriptions.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
				.iterator().next().toString());
		descriptions.append("\t");
		descriptions.append(Type5UuidFactory.get(
				Type5UuidFactory.OPCS_CONCEPT_ID, id).toString());
		descriptions.append("\t");
		descriptions.append(desc);
		descriptions.append("\t");
		descriptions.append("1");
		descriptions.append("\t");
		descriptions
				.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids().iterator().next().toString()); // status
		descriptions.append("\t");
		descriptions.append("en"); // primitive
		descriptions.append("\t");
		descriptions.append(effectiveDate);
		descriptions.append("\t");
		descriptions.append(pathUUID.toString()); // path id
		descriptions.append("\n");

		String parentId = "opcs";
		if (id.contains(".")) {
			parentId = id.substring(0, 3);
		}

		relationships.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_REL_ID,
				id + parentId).toString());
		relationships.append("\t");
		relationships.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
				.iterator().next().toString());
		relationships.append("\t");
		relationships.append(Type5UuidFactory.get(
				Type5UuidFactory.OPCS_CONCEPT_ID, id).toString());
		relationships.append("\t");
		relationships.append(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()
				.iterator().next().toString());
		relationships.append("\t");
		relationships.append(Type5UuidFactory.get(
				Type5UuidFactory.OPCS_CONCEPT_ID, parentId).toString());
		relationships.append("\t");
		relationships
				.append(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
						.getUids().iterator().next().toString());
		relationships.append("\t");
		relationships.append(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
				.getUids().iterator().next().toString());
		relationships.append("\t");
		relationships.append("0");
		relationships.append("\t");
		relationships.append(effectiveDate);
		relationships.append("\t");
		relationships.append(pathUUID.toString()); // path id
		relationships.append("\n");
	}

	private void writeRoot(Writer concepts, Writer descriptions,
			Writer relationships, Writer ids, UUID pathUUID)
			throws IOException, NoSuchAlgorithmException,
			UnsupportedEncodingException {
		String id = "opcs";
		String desc = "OPCS Concept";

		ids.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, id)
				.toString()); // concept id
		ids.append("\t");
		ids.append(ArchitectonicAuxiliary.Concept.UNSPECIFIED_STRING.getUids()
				.iterator().next().toString()); // source
		ids.append("\t");
		ids.append(id); // source id
		ids.append("\t");
		ids.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator()
				.next().toString()); // status
		ids.append("\t");
		ids.append(effectiveDate);
		ids.append("\t");
		ids.append(pathUUID.toString()); // path id
		ids.append("\n");

		concepts.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID,
				id).toString()); // concept id
		concepts.append("\t");
		concepts.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
				.iterator().next().toString()); // status
		concepts.append("\t");
		concepts.append("1"); // primitive
		concepts.append("\t");
		concepts.append(effectiveDate);
		concepts.append("\t");
		concepts.append(pathUUID.toString()); // path id
		concepts.append("\n");

		descriptions.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_DESC_ID,
				id).toString());
		descriptions.append("\t");
		descriptions.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
				.iterator().next().toString());
		descriptions.append("\t");
		descriptions.append(Type5UuidFactory.get(
				Type5UuidFactory.OPCS_CONCEPT_ID, id).toString());
		descriptions.append("\t");
		descriptions.append(desc);
		descriptions.append("\t");
		descriptions.append("1");
		descriptions.append("\t");
		descriptions
				.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids().iterator().next().toString()); // status
		descriptions.append("\t");
		descriptions.append("en"); // primitive
		descriptions.append("\t");
		descriptions.append(effectiveDate);
		descriptions.append("\t");
		descriptions.append(pathUUID.toString()); // path id
		descriptions.append("\n");

	}
}
