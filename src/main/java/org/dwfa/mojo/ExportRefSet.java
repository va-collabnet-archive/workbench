package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
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


	I_TermFactory termFactory = null;

	BufferedWriter refsetWriter = null;
	BufferedWriter memberWriter = null;	

	String date = "";

	UUID fsn_uuid = null;
	UUID pft_uuid = null;

	Set<I_ThinExtByRefVersioned> concept_extensions = new HashSet<I_ThinExtByRefVersioned>();
	Set<I_ThinExtByRefVersioned> desc_extensions = new HashSet<I_ThinExtByRefVersioned>();
	Set<I_ThinExtByRefVersioned> rel_extensions = new HashSet<I_ThinExtByRefVersioned>();

	HashMap<Integer,List<I_ThinExtByRefVersioned>> members = new HashMap<Integer,List<I_ThinExtByRefVersioned>>();

	public void execute() throws MojoExecutionException, MojoFailureException {
		setDate();

		termFactory = LocalVersionedTerminology.get(); 
		try {
			
			try {
                if (MojoUtil.alreadyRun(getLog(), "ExportRefSet")) {
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
				writeRefsetFile(ext,"concept");
			}
			for (I_ThinExtByRefVersioned ext: desc_extensions) {
				writeRefsetFile(ext,"description");
			}
			for (I_ThinExtByRefVersioned ext: rel_extensions) {
				writeRefsetFile(ext,"relationship");
			}

		} catch (IOException e) {
			throw new MojoExecutionException("output file not available for writing");
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoExecutionException("Local Versioned Terminology Failed to iterate over identifiers");
		}
	}


	int idCounter = 0;
	public void processId(I_IdVersioned id, Set<I_ThinExtByRefVersioned> extensions) throws Exception {
		idCounter++;			
		if (idCounter%10000==0) {
			getLog().info("Processed: " + idCounter + " identifiers");
		}
		
		if (idCounter==1) {
			System.out.println("Adding extension to " + termFactory.getConcept(id.getNativeId()));
            List<I_GetExtensionData> extensions_test =
                termFactory.getExtensionsForComponent(id.getNativeId());
            System.out.println(extensions_test.size()); // ZERO

            I_ThinExtByRefVersioned extension =
                termFactory.newExtension(id.getNativeId(), id.getNativeId(), id.getNativeId(),
                		id.getNativeId());
            	
            I_ThinExtByRefPartConcept conceptExtension = termFactory.newConceptExtensionPart();

            conceptExtension.setPathId(id.getNativeId());
            conceptExtension.setStatus(id.getNativeId());
            conceptExtension.setVersion(Integer.MAX_VALUE);
            conceptExtension.setConceptId(id.getNativeId());            

            extension.addVersion(conceptExtension);
            termFactory.addUncommitted(termFactory.getConcept(id.getNativeId()));
            System.out.println(termFactory.getUncommitted());
            extensions_test =
                termFactory.getExtensionsForComponent(id.getNativeId());
            
            System.out.println(extensions_test.size()); // ZERO 
		}
		

		int nativeId = id.getTuples().get(0).getNativeId();



		List<I_GetExtensionData> extensionData = termFactory.getExtensionsForComponent(nativeId);
		
	
		
		
		for (I_GetExtensionData data : extensionData) {
			I_ThinExtByRefVersioned ext = data.getExtension();
			UniversalAceExtByRefBean bean = data.getUniversalAceBean();
			getLog().debug("Exporting UUID to refset:" + bean.getComponentUid());

			List<? extends I_ThinExtByRefPart> versions = ext.getVersions();

			for (I_ThinExtByRefPart version : versions) {
				boolean found = false;
				for (I_ThinExtByRefVersioned previous : extensions) {
					if (previous.getRefsetId()==ext.getRefsetId()) {
						found = true;
					}
				}
				if (!found) {
					extensions.add(ext);
				}

				/*
				if (version instanceof I_ThinExtByRefPartBoolean) {
					System.out.println("boolean");
				} else  if (version instanceof I_ThinExtByRefPartConcept) {					
					if (RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getUids().contains(termFactory.getConcept(((I_ThinExtByRefPartConcept) version).getConceptId()).getUids().iterator().next())) {
						System.out.println("include linage");
						this.addConceptInLinage(nativeId);

					}
					if (RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().contains(termFactory.getConcept(((I_ThinExtByRefPartConcept) version).getConceptId()).getUids().iterator().next())) {
						System.out.println("include individual");

					}
					if (RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids().contains(termFactory.getConcept(((I_ThinExtByRefPartConcept) version).getConceptId()).getUids().iterator().next())) {
						System.out.println("exclude invidivual");

					}
					if (RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getUids().contains(termFactory.getConcept(((I_ThinExtByRefPartConcept) version).getConceptId()).getUids().iterator().next())) {
						System.out.println("exclude linage");

					}
				} else  if (version instanceof I_ThinExtByRefPartInteger) {
					System.out.println("integer");					
				} else  if (version instanceof I_ThinExtByRefPartLanguage) {
					System.out.println("language");					
				} else  if (version instanceof I_ThinExtByRefPartMeasurement) {
					System.out.println("measurement");					
				} else  if (version instanceof I_ThinExtByRefPartString) {
					System.out.println("string");
				} else  if (version instanceof I_ThinExtByRefTuple) {
					System.out.println("tuple");
				} 
				 */

				List<I_ThinExtByRefVersioned> ext_members = members.get(ext.getRefsetId());
				if (ext_members == null) {
					ext_members = new ArrayList<I_ThinExtByRefVersioned>();
					members.put(ext.getRefsetId(),ext_members);
				}
				ext_members.add(ext);

			}		

		}


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

	public void writeRefsetFile(I_ThinExtByRefVersioned ext, String memberType) throws IOException, TerminologyException {
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
		File newrefsetFile = new File(refsetFile.getAbsolutePath().replace(".txt", fsn + "_" +memberType + ".txt"));
		refsetWriter = new BufferedWriter(new FileWriter(newrefsetFile,append));

		refsetWriter.write("Refset uuid\tEffectiveTime\tStatus\tName\tShortName\tRefSetType\t");
		refsetWriter.newLine();
		// Id SCTID 
		refsetWriter.write(termFactory.getUids(ext.getRefsetId()).iterator().next().toString());
		refsetWriter.write("\t");
		//EffectiveTime String[14]
		refsetWriter.write(date);
		refsetWriter.write("\t");
		// Status Enum Status of the RefSet record
		int status = termFactory.getConcept(ext.getRefsetId()).getConceptAttributes().getTuples().get(0).getConceptStatus();
		refsetWriter.write(termFactory.getConcept(status).toString());
		refsetWriter.write("\t");
		// Name String[255] Name of the RefSet
		refsetWriter.write(fsn);				
		refsetWriter.write("\t");
		// ShortName String[20] Short name for the RefSet
		refsetWriter.write(pft);
		refsetWriter.write("\t");
		refsetWriter.write(termFactory.getConcept(ext.getTypeId()).toString());
		refsetWriter.write("\t");
		refsetWriter.newLine();
		refsetWriter.close();

		writeMembers(ext.getRefsetId(), fsn, termFactory.getConcept(ext.getTypeId()),memberType);

	}

	public void writeMembers(int refsetId, String fsn, I_GetConceptData type,String memberType) throws IOException, TerminologyException {

		List<I_ThinExtByRefVersioned> ext_members = members.get(refsetId);

		File newmemberFile = new File(memberFile.getAbsolutePath().replace(".txt", fsn + "_" +memberType + ".txt"));
		memberWriter = new BufferedWriter(new FileWriter(newmemberFile,append));

		int extensiontype = -1;
		getLog().info("Writing member...");
		if (RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Language Extension");
			extensiontype = LANGUAGE;
		}
		if (RefsetAuxiliary.Concept.INT_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Int Extension");
			extensiontype = INTEGER;
		}
		if (RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Measurement Extension");
			extensiontype = MEASUREMENT;
		}
		if (RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Concept Extensions");
			extensiontype = CONCEPT;
		}
		if (RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Concept-Int Extension");
			extensiontype = CONCEPT_INTEGER;
		}
		if (RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Boolean Extension");
			extensiontype = BOOLEAN;
		}
		if (RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting Scoped Language Extension");
			extensiontype = SCOPED;
		}
		if (RefsetAuxiliary.Concept.STRING_EXTENSION.getUids().contains(type.getUids().iterator().next())) {
			getLog().info("Exporting String Extension");
			extensiontype = STRING;
		}

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
			memberWriter.write(termFactory.getUids(ext.getComponentId()).iterator().next().toString());
			memberWriter.write("\t");				
			//ComponentId
			memberWriter.write(termFactory.getUids(ext.getComponentId()).iterator().next().toString());
			memberWriter.write("\t");				
			//EffectiveTime
			memberWriter.write(date);
			memberWriter.write("\t");				
			//path
			memberWriter.write(termFactory.getUids(version.getPathId()).iterator().next().toString());
		}

		public void setDate() {
			Calendar cal = Calendar.getInstance();
			date = cal.get(Calendar.YEAR) + "-" + 
			addLeadingZero(cal.get(Calendar.MONTH)) + "-" + 
			addLeadingZero(cal.get(Calendar.DATE)) + " " + 
			addLeadingZero(cal.get(Calendar.HOUR_OF_DAY)) + ":" + 
			addLeadingZero(cal.get(Calendar.MINUTE)) + ":00";

		}

		public String addLeadingZero(int i) {
			if (i < 10) {
				return "0"+i;
			}
			return ""+i;
		}


		public void processConcept(I_GetConceptData concept) throws Exception {
			this.processId(concept.getId(),concept_extensions);		
		}


		public void processDescription(I_DescriptionVersioned versionedDesc)
		throws Exception {
			this.processId(termFactory.getId(versionedDesc.getDescId()),desc_extensions);

		}


		public void processRelationship(I_RelVersioned versionedRel)
		throws Exception {
			this.processId(termFactory.getId(versionedRel.getRelId()),rel_extensions);
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
