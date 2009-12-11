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
package org.dwfa.mojo.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_ProcessDescriptions;
import org.dwfa.ace.api.I_ProcessRelationships;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguageScoped;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * This MoJo exports refset content from an ACE Berkeley Database. The format for export is described 
 * <a href="https://mgr.cubit.aceworkspace.net/pbl/cubitci/pub/ace-mojo/site/dataimport.html"> here. </a>  
 * 
 * TODO - this class needs to be reworked.
 * 
 * @goal exportRefSets
 * @author Tore Fjellheim
 */
public class ExportRefSet extends AbstractMojo implements I_ProcessConcepts, I_ProcessDescriptions, I_ProcessRelationships  {

	public static final int LANGUAGE = 0;
	public static final int INTEGER = 1;
	public static final int MEASUREMENT = 2;
	public static final int CONCEPT_INTEGER = 3;
	public static final int CONCEPT = 4;
	public static final int BOOLEAN = 5;
	public static final int STRING = 6;
	public static final int SCOPED = 7;

	/**
	 * @parameter
	 * @required
	 */
	File refsetFile = null;

	/**
	 * @parameter
	 * @required
	 */
	File memberFile = null;

	/**
	 * @parameter
	 */
	boolean append = false;

	/*
	 * */
	boolean all = true;
	/**
	 * @parameter 
	 */
	boolean concepts = false;
	/**
	 * @parameter 
	 */
	boolean descriptions = false;
	/**
	 * @parameter 
	 */
	boolean relationships = false;

	/**
	 * Export specification that dictates which concepts are exported and which are not. Only reference sets
	 * whose identifying concept is exported will be exported. Only members relating to components that will 
	 * be exported will in turn be exported.
	 * <p>
	 * For example if you have a reference set identified by concept A, and members B, C and D. If the export spec
	 * does not include exporting concept A then none of the reference set will be exported. However if the
	 * export spec does include A, but not C then the reference set will be exported except it will only have
	 * members B and D - C will be omitted.
	 * @parameter
	 */
	ExportSpecification[] exportSpecifications;

	I_TermFactory termFactory = null;

	BufferedWriter refsetWriter = null;
	BufferedWriter memberWriter = null;	

	UUID fsn_uuid = null;
	UUID pft_uuid = null;

	Set<I_ThinExtByRefVersioned> concept_extensions = new HashSet<I_ThinExtByRefVersioned>();
	Set<I_ThinExtByRefVersioned> desc_extensions = new HashSet<I_ThinExtByRefVersioned>();
	Set<I_ThinExtByRefVersioned> rel_extensions = new HashSet<I_ThinExtByRefVersioned>();

	HashMap<Integer,List<I_ThinExtByRefVersioned>> members = new HashMap<Integer,List<I_ThinExtByRefVersioned>>();

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {

		termFactory = LocalVersionedTerminology.get(); 
		try {
			
			try {
                if (MojoUtil.alreadyRun(getLog(), "ExportRefSet", this.getClass(), targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
			
			fsn_uuid = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids().iterator().next();
			pft_uuid = ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids().iterator().next();

			refsetFile.getParentFile().mkdirs();
			memberFile.getParentFile().mkdirs();

			if (all || concepts) {				
				getLog().info("Iterating over concepts");
				termFactory.iterateConcepts(this);
			}	
			if (all || descriptions) {				
				getLog().info("Iterating over descriptions");
				termFactory.iterateDescriptions(this);
			}
			if (all || relationships) {				
				getLog().info("Iterating over relationships");
				termFactory.iterateRelationships(this);
			}
			getLog().info("Iteration complete...writing refsets");
			for (I_ThinExtByRefVersioned ext: concept_extensions) {
				writeRefsetFile(ext);
			}
			for (I_ThinExtByRefVersioned ext: desc_extensions) {
				writeRefsetFile(ext);
			}
			for (I_ThinExtByRefVersioned ext: rel_extensions) {
				writeRefsetFile(ext);
			}

		} catch (IOException e) {
			throw new MojoExecutionException("output file not available for writing", e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoExecutionException("Local Versioned Terminology Failed to iterate over identifiers", e);
		}
	}


	int idCounter = 0;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public void processId(I_IdVersioned id, Set<I_ThinExtByRefVersioned> extensions) throws Exception {
		idCounter++;			
		if (idCounter%10000==0) {
			getLog().info("Processed: " + idCounter + " identifiers");
		}

		int nativeId = id.getNativeId();

		List<I_GetExtensionData> extensionData = termFactory.getExtensionsForComponent(nativeId);
		
		for (I_GetExtensionData data : extensionData) {
			I_ThinExtByRefVersioned ext = data.getExtension();
			
			I_GetConceptData refsetConcept = termFactory.getConcept(ext.getRefsetId());
			if (testExportSpecification(refsetConcept)) {
				if (getLog().isDebugEnabled()) {
					UniversalAceExtByRefBean bean = data.getUniversalAceBean();
					getLog().debug("Exporting UUID to refset:" + bean.getComponentUid());
				}
	
				for (I_ThinExtByRefPart version : ext.getVersions()) {
					boolean found = false;
					for (I_ThinExtByRefVersioned previous : extensions) {
						if (previous.getRefsetId()==ext.getRefsetId()) {
							found = true;
							break;
						}
					}
					if (!found) {
						extensions.add(ext);
					}
	
					List<I_ThinExtByRefVersioned> ext_members = members.get(ext.getRefsetId());
					if (ext_members == null) {
						ext_members = new ArrayList<I_ThinExtByRefVersioned>();
						members.put(ext.getRefsetId(),ext_members);
					}
					ext_members.add(ext);
				}
			}
		}
	}

	private boolean testExportSpecification(I_GetConceptData concept) throws Exception {
		if (exportSpecifications == null || exportSpecifications.length == 0) {
			return true;
		}
		
		for (ExportSpecification spec : exportSpecifications) {
			if (spec.test(concept)) {
				return true;
			}
		}
		return false;
	}

	/*
	public void addConceptInLinage(int conceptId) throws TerminologyException, IOException {
		I_GetConceptData c = termFactory.getConcept(conceptId);
		List<I_RelVersioned> drels = c.getDestRels();
		for (I_RelVersioned rel : drels) {
			rel.getC1Id();
		}
	}
	 */

	private void writeRefsetFile(I_ThinExtByRefVersioned ext) throws IOException, TerminologyException {
		String fsn = null;
		String pft = null;

		List<I_DescriptionVersioned> descriptions = termFactory.getConcept(ext.getRefsetId()).getDescriptions();
		for (I_DescriptionVersioned desc : descriptions) {
			if (termFactory.getUids(desc.getTuples().get(0).getTypeId()).contains(fsn_uuid)) {
				fsn = desc.getTuples().get(0).getText();
			}					
			if (termFactory.getUids(desc.getTuples().get(0).getTypeId()).contains(pft_uuid)) {
				pft = desc.getTuples().get(0).getText();
			}					
		}

		getLog().info("Writing refset...");

		fsn = fsn.replace("/", "-");
		fsn = fsn.replace("'", "_");

		File newrefsetFile = new File(refsetFile.getAbsolutePath().replace(".txt", fsn + ".txt"));
		newrefsetFile.getParentFile().mkdirs();
		if (!newrefsetFile.exists()) {
			newrefsetFile.createNewFile();
		}
		refsetWriter = new BufferedWriter(new FileWriter(newrefsetFile,append));

		refsetWriter.write("Refset uuid\tEffectiveTime\tStatus\tName\tShortName\tRefSetType\t");
		refsetWriter.newLine();
		
		for (I_ThinExtByRefPart version : ext.getVersions()) {
			// Id SCTID
			refsetWriter.write(termFactory.getUids(ext.getRefsetId())
					.iterator().next().toString());
			refsetWriter.write("\t");
			// EffectiveTime String[14]
			long datetime = termFactory.convertToThickVersion(version
					.getVersion());
			refsetWriter.write(getFormattedDate(datetime));
			refsetWriter.write("\t");
			// Status Enum Status of the RefSet record
			int status = termFactory.getConcept(ext.getRefsetId())
					.getConceptAttributes().getTuples().get(0)
					.getConceptStatus();
			refsetWriter.write(termFactory.getConcept(status).toString());
			refsetWriter.write("\t");
			// Name String[255] Name of the RefSet
			refsetWriter.write(fsn);
			refsetWriter.write("\t");
			// ShortName String[20] Short name for the RefSet
			refsetWriter.write(pft);
			refsetWriter.write("\t");
			refsetWriter.write(termFactory.getConcept(ext.getTypeId())
					.toString());
			refsetWriter.write("\t");
			refsetWriter.newLine();
		}
		refsetWriter.close();

		writeMembers(ext.getRefsetId(), fsn, termFactory.getConcept(ext.getTypeId()));

	}

	public void writeMembers(int refsetId, String fsn, I_GetConceptData type) throws IOException, TerminologyException {

		List<I_ThinExtByRefVersioned> ext_members = members.get(refsetId);

		int extensiontype = -1;
		String fileextension = "";

		getLog().info("Writing member...");

		if (RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Language Extension");
			extensiontype = LANGUAGE;
			fileextension  = "language.refset";		
		}
		if (RefsetAuxiliary.Concept.INT_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Int Extension");
			extensiontype = INTEGER;
			fileextension  = "integer.refset";		
		}
		if (RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Measurement Extension");
			extensiontype = MEASUREMENT;
			fileextension  = "measurement.refset";		
		}
		if (RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Concept Extensions");
			extensiontype = CONCEPT;
			fileextension  = "concept.refset";		
		}
		if (RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Concept-Int Extension");
			extensiontype = CONCEPT_INTEGER;
			fileextension  = "conint.refset";		
		}
		if (RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Boolean Extension");
			extensiontype = BOOLEAN;
			fileextension  = "boolean.refset";		
		}
		if (RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Scoped Language Extension");
			extensiontype = SCOPED;
			fileextension  = "scoped.refset";		
		}
		if (RefsetAuxiliary.Concept.STRING_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting String Extension");
			extensiontype = STRING;
			fileextension  = "string.refset";		
		}

		fsn = fsn.replace("/", "-");
		
		File newmemberFile = new File(memberFile.getAbsolutePath().replace(".txt", fsn + "." + fileextension));
		memberWriter = new BufferedWriter(new FileWriter(newmemberFile,append));
		
		memberWriter.write("refset uuid\tmember uuid\tstatus uuid\tcomponent uuid\teffective date\tpath");
		switch (extensiontype) {
		case LANGUAGE:							
			memberWriter.write("\tacceptability extension value\tcorrectness extension value\tsynonymy value");
			break;
		case BOOLEAN:
			memberWriter.write("\tboolean extension value");
			break;
		case CONCEPT:
			memberWriter.write("\tconcept extension value");
			break;
		case CONCEPT_INTEGER:
			memberWriter.write("\tconcept extension value\tinteger extension value");
			break;
		case INTEGER:
			memberWriter.write("\tinteger extension value");
			break;
		case MEASUREMENT:
			memberWriter.write("\tmeasurement extension value\tunit of measure extension value\t");
			break;
		case SCOPED:
			memberWriter.write("\tpriority\tscope\ttag");
			break;
		case STRING:
			memberWriter.write("\tstring extension value");
			break;
		}

		memberWriter.newLine();

		for (I_ThinExtByRefVersioned ext : ext_members) {
			List<? extends I_ThinExtByRefPart> versions = ext.getVersions();

			/*
			 * Probably should be able to filter on versions here based on status
			 * values etc.
			 * */

			for (I_ThinExtByRefPart version : versions) {					
				writeStandard(ext,version);					

				switch (extensiontype) {
				case LANGUAGE:							
					memberWriter.write("\t");
					//Acceptability
					memberWriter.write(termFactory.getUids(((I_ThinExtByRefPartLanguage) version).getAcceptabilityId()).iterator().next().toString());
					memberWriter.write("\t");				
					//Correctness
					memberWriter.write(termFactory.getUids(((I_ThinExtByRefPartLanguage) version).getCorrectnessId()).iterator().next().toString());
					memberWriter.write("\t");				
					//DegreeOfSynonymy
					memberWriter.write(termFactory.getUids(((I_ThinExtByRefPartLanguage) version).getDegreeOfSynonymyId()).iterator().next().toString());
					memberWriter.newLine();
					break;
				case BOOLEAN:
					memberWriter.write("\t");
					//BOOLEAN
					memberWriter.write(new Boolean(((I_ThinExtByRefPartBoolean) version).getValue()).toString());
					memberWriter.newLine();
					break;
				case CONCEPT:
					memberWriter.write("\t");
					//CONCEPT
					memberWriter.write(termFactory.getUids(((I_ThinExtByRefPartConcept) version).getConceptId()).iterator().next().toString());
					memberWriter.newLine();
					break;
				case CONCEPT_INTEGER:
					memberWriter.write("\t");
					//CONCEPT
					memberWriter.write(termFactory.getUids(((I_ThinExtByRefPartConceptInt) version).getConceptId()).iterator().next().toString());
					memberWriter.write("\t");				
					//INTEGER
					memberWriter.write(new Integer(((I_ThinExtByRefPartConceptInt) version).getIntValue()).toString());
					memberWriter.newLine();
					break;
				case INTEGER:
					memberWriter.write("\t");
					//INTEGER
					memberWriter.write(new Integer(((I_ThinExtByRefPartInteger) version).getValue()).toString());
					memberWriter.newLine();
					break;
				case MEASUREMENT:
					memberWriter.write("\t");
					//MEASUREMENT
					memberWriter.write(new Double(((I_ThinExtByRefPartMeasurement) version).getMeasurementValue()).toString());
					memberWriter.write("\t");				
					//UNIT OF MEAUSURE
					memberWriter.write(termFactory.getUids(((I_ThinExtByRefPartMeasurement) version).getUnitsOfMeasureId()).iterator().next().toString());
					memberWriter.newLine();
					break;
				case SCOPED:
					memberWriter.write("\t");
					//Acceptability
					memberWriter.write(termFactory.getUids(((I_ThinExtByRefPartLanguageScoped) version).getAcceptabilityId()).iterator().next().toString());
					memberWriter.write("\t");				
					//Correctness
					memberWriter.write(termFactory.getUids(((I_ThinExtByRefPartLanguageScoped) version).getCorrectnessId()).iterator().next().toString());
					memberWriter.write("\t");				
					//DegreeOfSynonymy
					memberWriter.write(termFactory.getUids(((I_ThinExtByRefPartLanguageScoped) version).getDegreeOfSynonymyId()).iterator().next().toString());
					memberWriter.write("\t");
					//PRIORITY
					memberWriter.write(new Integer(((I_ThinExtByRefPartLanguageScoped) version).getPriority()).toString());
					memberWriter.write("\t");				
					//SCOPE
					memberWriter.write(termFactory.getUids(((I_ThinExtByRefPartLanguageScoped) version).getScopeId()).iterator().next().toString());
					memberWriter.write("\t");				
					//TAG
					memberWriter.write(termFactory.getUids(((I_ThinExtByRefPartLanguageScoped) version).getTagId()).iterator().next().toString());
					
					memberWriter.newLine();
					break;
				case STRING:
					memberWriter.write("\t");
					//MEASUREMENT
					memberWriter.write(((I_ThinExtByRefPartString) version).getStringValue().toString());
					memberWriter.newLine();
					break;
				}

			}

			}
			memberWriter.close();
		}

		public void writeStandard(I_ThinExtByRefVersioned ext, I_ThinExtByRefPart version) throws IOException, TerminologyException {
			//Id
			memberWriter.write(termFactory.getUids(ext.getRefsetId()).iterator().next().toString());
			memberWriter.write("\t");				
			//memberId
			memberWriter.write(termFactory.getUids(ext.getMemberId()).iterator().next().toString());
			memberWriter.write("\t");				
			//Status
			memberWriter.write(termFactory.getUids(version.getStatus()).iterator().next().toString());
			memberWriter.write("\t");				
			//ComponentId
			memberWriter.write(termFactory.getUids(ext.getComponentId()).iterator().next().toString());
			memberWriter.write("\t");				
			//EffectiveTime
			long datetime = termFactory.convertToThickVersion(version.getVersion());
			memberWriter.write(getFormattedDate(datetime));
			memberWriter.write("\t");				
			//path
			memberWriter.write(termFactory.getUids(version.getPathId()).iterator().next().toString());
		}

		private String getFormattedDate(long datetime) {
			return dateFormat.format(new Date(datetime));
		}


		public String addLeadingZero(int i) {
			if (i < 10) {
				return "0"+i;
			}
			return ""+i;
		}


		public void processConcept(I_GetConceptData concept) throws Exception {
			if (testExportSpecification(concept)) {
				this.processId(concept.getId(),concept_extensions);
			} else {
				getLog().debug("Suppressing export of concept " + concept + " refsets");
			}
			
		}


		public void processDescription(I_DescriptionVersioned versionedDesc)
		throws Exception {
			if (testExportSpecification(termFactory.getConcept(versionedDesc.getConceptId()))) {
				this.processId(termFactory.getId(versionedDesc.getDescId()),desc_extensions);
			} else {
				getLog().debug("Suppressing export of description " + versionedDesc + " refsets");
			}
		}


		public void processRelationship(I_RelVersioned versionedRel)
		throws Exception {
			if (testExportSpecification(termFactory.getConcept(versionedRel.getC1Id()))
							&& testExportSpecification(termFactory.getConcept(versionedRel.getC2Id()))) {
				
				this.processId(termFactory.getId(versionedRel.getRelId()),rel_extensions);
			} else {
				getLog().debug("Suppressing export of relationship " + versionedRel + " refsets");
			}
		}

		public boolean isConcepts() {
			return concepts;
		}

		public void setConcepts(boolean concepts) {
			this.concepts = concepts;
			all = false;
		}

		public boolean isDescriptions() {
			return descriptions;
		}

		public void setDescriptions(boolean descriptions) {
			this.descriptions = descriptions;
			all = false;
		}

		public boolean isRelationships() {
			return relationships;
		}

		public void setRelationships(boolean relationships) {
			this.relationships = relationships;
			all = false;
		}


	}
