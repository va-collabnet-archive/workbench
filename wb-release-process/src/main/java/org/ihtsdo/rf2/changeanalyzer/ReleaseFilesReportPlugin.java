package org.ihtsdo.rf2.changeanalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.changeanalyzer.data.Rf2AssociationRefsetRow;
import org.ihtsdo.rf2.changeanalyzer.data.Rf2AttributeValueRefsetRow;
import org.ihtsdo.rf2.changeanalyzer.data.Rf2DescriptionRow;
import org.ihtsdo.rf2.changeanalyzer.data.Rf2RelationshipRow;
import org.ihtsdo.rf2.changeanalyzer.file.Rf2AssociationRefsetFile;
import org.ihtsdo.rf2.changeanalyzer.file.Rf2AttributeValueRefsetFile;
import org.ihtsdo.rf2.changeanalyzer.file.Rf2ConceptFile;
import org.ihtsdo.rf2.changeanalyzer.file.Rf2DescriptionFile;
import org.ihtsdo.rf2.changeanalyzer.file.Rf2RelationshipFile;
import org.ihtsdo.rf2.changeanalyzer.model.ChangeSummary;
import org.ihtsdo.rf2.changeanalyzer.model.Concept;
import org.ihtsdo.rf2.changeanalyzer.model.Description;
import org.ihtsdo.rf2.changeanalyzer.model.FileChangeReport;
import org.ihtsdo.rf2.changeanalyzer.model.Relationship;
import org.ihtsdo.rf2.changeanalyzer.model.RetiredConcept;

import com.google.gson.Gson;

/**
 * The <code>ReleaseFilesReportPlugin</code> performs different analysis over
 * SNOMED RF2 Release files<br>
 * Reporting all the differences.
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Vahram
 * @goal report-differences
 * @phase install
 */
public class ReleaseFilesReportPlugin extends AbstractMojo {

	private static final String SUMMARY_FILE = "diff-index.json";

	private static final String REACTIVATED_CONCEPTS_REPORT = "reactivated_concepts.json";

	private static final String DEFINED_CONCEPTS_REPORT = "defined_concepts.json";

	public static final String NEW_CONCEPTS_FILE = "reporte_new_concepts.json";

	public static final String NEW_RELATIONSHIPS_FILE = "new_relationships.json";

	public static final String RETIRED_CONCEPT_REASON_FILE = "retired_concept_reason.json";

	public static final String NEW_DESCRIPTIONS_FILE = "new_descriptions.json";

	public static final String OLD_CONCEPTS_NEW_DESCRIPTIONS_FILE = "old_concepts_new_descriptions.json";

	public static final String OLD_CONCEPTS_NEW_RELATIONSHIPS_FILE = "old_concepts_new_relationships.json";

	public static final String NEW_INACTIVE_CONCEPTS_FILE = "new_inactive_concepts.json";

	public static final String REL_GROUP_CHANGED_FILE = "rel_group_changed_relationships.json";

	private static final Logger logger = Logger.getLogger(ReleaseFilesReportPlugin.class);

	private static final String RETIRED_DESCRIPTIONS_FILE = "retiredDesc.json";

	private static final String REACTIVATED_DESCRIPTIONS_FILE = "reactDesc.json";

	private static final String PRIMITIVE_CONCEPTS_REPORT = "primitive_concepts.json";

    private String sep = System.getProperty("line.separator");
    
    private Gson gson = new Gson();
	/**
	 * Location of the directory of report files
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the directory of the release folder.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File inputDirectory;

	/**
	 * Start date for the reports.
	 * 
	 * @parameter
	 * @required
	 */
	private String startDate;

	/**
	 * End date for the reports.
	 * 
	 * @parameter
	 * @required
	 */
	private String endDate;

	/**
	 * Release date. in case there is more than one release in the release
	 * folder.
	 * 
	 * @parameter
	 */
	private String releaseDate;

	private ChangeSummary changeSummary;

	public enum ReleaseFileType {
		DESCRIPTION, CONCEPT, RELATIONSHIP, ATTRIBUTE_VALUE_REFSET, ASSOCIATION_REFSET
	}

	public static void main(String[] args) {
		ReleaseFilesReportPlugin relplugin = new ReleaseFilesReportPlugin();
		try {
			relplugin.inputDirectory = new File("Full");
			relplugin.endDate = "20130731";
			relplugin.startDate = "20120131";
			relplugin.releaseDate = "20130131";
			relplugin.outputDirectory = new File(".");
			relplugin.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (!outputDirectory.exists()) {
				outputDirectory.mkdirs();
			}
			changeSummary=new ChangeSummary();
			logger.info("Loading descriptinos");
			Rf2DescriptionFile rf2DescFile = new Rf2DescriptionFile(getFilePath(ReleaseFileType.DESCRIPTION), startDate);
			logger.info("Loading concepts");
			Rf2ConceptFile conceptFile = new Rf2ConceptFile(getFilePath(ReleaseFileType.CONCEPT), startDate);
			logger.info("Loading attribute value refset");
			Rf2AttributeValueRefsetFile attrValue = new Rf2AttributeValueRefsetFile(getFilePath(ReleaseFileType.ATTRIBUTE_VALUE_REFSET));
			logger.info("Loading association value refset");
			Rf2AssociationRefsetFile associationFile = new Rf2AssociationRefsetFile(getFilePath(ReleaseFileType.ASSOCIATION_REFSET));
			logger.info("Loading relationships");
			Rf2RelationshipFile relFile = new Rf2RelationshipFile(getFilePath(ReleaseFileType.RELATIONSHIP), startDate);

			ArrayList<Long> newcomponents = generateNewConceptsReport(rf2DescFile, conceptFile);

			generateNewRelationshipsReport(rf2DescFile, relFile);

			generateOldConceptsNewRelationships(rf2DescFile, relFile, newcomponents);
			//
			generateRelGroupChangedRelationships(rf2DescFile, relFile, startDate, endDate);
			relFile.releasePreciousMemory();

			generatingRetiredConceptReasons(rf2DescFile, conceptFile, attrValue, associationFile);

			generateNewDescriptionsReport(rf2DescFile, newcomponents);

			generatingExistingConceptsNewDescriptions(rf2DescFile, newcomponents);

			generatingDefinedConceptsReport(rf2DescFile, conceptFile);

			generatingPrimitiveConceptsReport(rf2DescFile, conceptFile);
			reactivatedConceptsReport(rf2DescFile, conceptFile);
			generatingInactiveConcepts(rf2DescFile, conceptFile);
			conceptFile.releasePreciousMemory();

			generateReactivatedDescriptionsReport(rf2DescFile, newcomponents);
			generateRetiredDescriptionsReport(rf2DescFile, newcomponents);
			
			saveSummary();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void saveSummary() throws IOException {
		
		changeSummary.setTitle("Changes report");
		Date now=new Date();
		changeSummary.setExecutionTime(now.toString());
		changeSummary.setFrom(startDate);
		changeSummary.setTo(endDate);
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		fos = new FileOutputStream(new File(outputDirectory, SUMMARY_FILE));
		logger.info("Generating diff-index.json");
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);	
		
		bw.append(gson.toJson(changeSummary).toString());
		bw.append(sep);
		
		bw.close();
		System.gc();
		
	}

	private String getFilePath(ReleaseFileType descriptions) {
		String result = "";
		switch (descriptions) {
		case DESCRIPTION:
			result = getFilePathRecursive(inputDirectory, "description");
			break;
		case CONCEPT:
			result = getFilePathRecursive(inputDirectory, "concept");
			break;
		case RELATIONSHIP:
			result = getFilePathRecursive(inputDirectory, "relationship");
			break;
		case ASSOCIATION_REFSET:
			result = getFilePathRecursive(inputDirectory, "associationreference");
			break;
		case ATTRIBUTE_VALUE_REFSET:
			result = getFilePathRecursive(inputDirectory, "attributevalue");
			break;
		default:
			break;
		}
		return result;
	}

	public String getFilePathRecursive(File folder, String namePart) {
		String result = "";
		if (folder.isDirectory()) {
			File[] files = folder.listFiles();
			int i = 0;
			while (i < files.length && result.equals("")) {
				result = getFilePathRecursive(files[i], namePart);
				i++;
			}
		} else {
			if (folder.getName().toLowerCase().contains(namePart)) {
				if (releaseDate != null && !releaseDate.equals("") && folder.getName().contains(releaseDate)) {
					result = folder.getPath();
				} else if (releaseDate == null || releaseDate.equals("")) {
					result = folder.getPath();
				}
			}
		}
		return result;
	}

	private void generateRelGroupChangedRelationships(Rf2DescriptionFile rf2DescFile, Rf2RelationshipFile relFile, String startDate, String endDate) throws Exception {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		ArrayList<Long> newRels = relFile.getRelGroupChanged(startDate, endDate);
		fos = new FileOutputStream(new File(outputDirectory, REL_GROUP_CHANGED_FILE));
		logger.info("Generating rel_group_changed_relationships.json");
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		int count=0;
		for (Long long1 : newRels) {
			Rf2RelationshipRow row = relFile.getById(long1, startDate);
			Relationship rel=new Relationship(row.getId().toString() , String.valueOf(row.getActive()) , rf2DescFile.getFsn(row.getSourceId()),row.getSourceId().toString(), rf2DescFile.getFsn(row.getDestinationId()) ,
					 rf2DescFile.getFsn(row.getTypeId()) , rf2DescFile.getFsn(row.getCharacteristicTypeId()));
				bw.append(gson.toJson(rel).toString());
				rel=null;
				bw.append(sep);
				count++;
		}
		bw.close();

		addFileChangeReport(REL_GROUP_CHANGED_FILE,count,"Relationships group number changed");
		
	}

	private void generatingInactiveConcepts(Rf2DescriptionFile rf2DescFile, Rf2ConceptFile conceptFile) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		fos = new FileOutputStream(new File(outputDirectory, NEW_INACTIVE_CONCEPTS_FILE));
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		ArrayList<Long> newInactive = conceptFile.getNewInactiveComponentIds(startDate);
		generateConceptReport(rf2DescFile, conceptFile, bw, newInactive);
		addFileChangeReport(NEW_INACTIVE_CONCEPTS_FILE,newInactive.size(),"New inactive concepts");
	}

	private void generateConceptReport(Rf2DescriptionFile rf2DescFile, Rf2ConceptFile conceptFile, BufferedWriter bw, ArrayList<Long> newInactive) throws IOException {
		for (Long long1 : newInactive) {
			String fsn = rf2DescFile.getFsn(long1);
			Pattern p = Pattern.compile("\\((.*?)\\)", Pattern.DOTALL);
			String semanticTag = "";
			if (fsn != null) {
				Matcher matcher = p.matcher(fsn);
				while (matcher.find()) {
					semanticTag = matcher.group(1);
				}
			}
			Concept concept=new Concept(long1.toString() ,rf2DescFile.getFsn(conceptFile.getDefinitionStatusId(long1)) , fsn , semanticTag);
			bw.append(gson.toJson(concept).toString());
			concept=null;
			bw.append(sep);
		}
		bw.close();
	}

	private void generatingDefinedConceptsReport(Rf2DescriptionFile rf2DescFile, Rf2ConceptFile conceptFile) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		fos = new FileOutputStream(new File(outputDirectory, DEFINED_CONCEPTS_REPORT));
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		ArrayList<Long> newInactive = conceptFile.getDefinedConectps(startDate, endDate);
		generateConceptReport(rf2DescFile, conceptFile, bw, newInactive);

		addFileChangeReport(DEFINED_CONCEPTS_REPORT,newInactive.size(),"New fully defined concepts");
		
	}

	private void reactivatedConceptsReport(Rf2DescriptionFile rf2DescFile, Rf2ConceptFile conceptFile) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		fos = new FileOutputStream(new File(outputDirectory, REACTIVATED_CONCEPTS_REPORT));
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		ArrayList<Long> newInactive = conceptFile.getReactivatedCount(startDate, endDate);
		generateConceptReport(rf2DescFile, conceptFile, bw, newInactive);

		addFileChangeReport(REACTIVATED_CONCEPTS_REPORT,newInactive.size(),"Ractivated concepts");
	}

	private void generatingPrimitiveConceptsReport(Rf2DescriptionFile rf2DescFile, Rf2ConceptFile conceptFile) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		fos = new FileOutputStream(new File(outputDirectory, PRIMITIVE_CONCEPTS_REPORT));
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		ArrayList<Long> newInactive = conceptFile.getPrimitivatedConectps(startDate, endDate);
		generateConceptReport(rf2DescFile, conceptFile, bw, newInactive);

		addFileChangeReport(PRIMITIVE_CONCEPTS_REPORT,newInactive.size(),"New primitive concepts");
	}

	private void generateOldConceptsNewRelationships(Rf2DescriptionFile rf2DescFile, Rf2RelationshipFile relFile, ArrayList<Long> newcomponents) throws FileNotFoundException,
			UnsupportedEncodingException, IOException {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		fos = new FileOutputStream(new File(outputDirectory, OLD_CONCEPTS_NEW_RELATIONSHIPS_FILE));
		logger.info("Generating old_concepts_new_relationships.json");
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		ArrayList<Long> existingRels = relFile.getExistingComponentIds(startDate);
		int count=0;
		for (Long long1 : existingRels) {
			ArrayList<Rf2RelationshipRow> rf2RelRows = relFile.getAllRows(startDate, long1);
			for (Rf2RelationshipRow row : rf2RelRows) {
				if (!newcomponents.contains(Long.parseLong(row.getSourceId().toString()))) {
					Relationship rel=new Relationship(row.getId().toString() , String.valueOf(row.getActive()) , rf2DescFile.getFsn(row.getSourceId()),row.getSourceId().toString(), rf2DescFile.getFsn(row.getDestinationId()) ,
							 rf2DescFile.getFsn(row.getTypeId()) , rf2DescFile.getFsn(row.getCharacteristicTypeId()));
						bw.append(gson.toJson(rel).toString());
						rel=null;
						bw.append(sep);
						count++;
				}
			}
		}
		bw.close();

		addFileChangeReport(OLD_CONCEPTS_NEW_RELATIONSHIPS_FILE,count,"New relationships in existing concepts");
		
	}

	private void generatingExistingConceptsNewDescriptions(Rf2DescriptionFile rf2DescFile, ArrayList<Long> newcomponents) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		fos = new FileOutputStream(new File(outputDirectory, OLD_CONCEPTS_NEW_DESCRIPTIONS_FILE));
		logger.info("Generating old_concepts_new_descriptions.json");
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		ArrayList<Long> existingDescriptios = rf2DescFile.getExistingComponentIds(startDate);
		int count=0;
		for (Long long1 : existingDescriptios) {
			ArrayList<Rf2DescriptionRow> rf2DescRows = rf2DescFile.getAllRows(startDate, long1);
			for (Rf2DescriptionRow rf2DescRow : rf2DescRows) {
				if (!newcomponents.contains(rf2DescRow.getConceptId())) {
					Description desc=new Description(long1.toString() , rf2DescRow.getEffectiveTime() , String.valueOf(rf2DescRow.getActive()) , rf2DescRow.getConceptId().toString() ,
							rf2DescRow.getLanguageCode() , rf2DescFile.getFsn(rf2DescRow.getTypeId()) , rf2DescRow.getTerm() ,
						    rf2DescFile.getFsn(rf2DescRow.getCaseSignificanceId()));
					bw.append(gson.toJson(desc).toString());
					desc=null;
					bw.append(sep);
					count++;
				}
			}
		}
		bw.close();

		addFileChangeReport(OLD_CONCEPTS_NEW_DESCRIPTIONS_FILE,count,"New descriptions in existing concepts");
	}

	private void generateNewDescriptionsReport(Rf2DescriptionFile rf2DescFile, ArrayList<Long> newcomponents) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		fos = new FileOutputStream(new File(outputDirectory, NEW_DESCRIPTIONS_FILE));
		logger.info("Generating new_descriptions.json");
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		ArrayList<Long> newDescriptios = rf2DescFile.getNewComponentIds(startDate);
		int count=writeDescriptionsFile(rf2DescFile, newcomponents, bw, newDescriptios);

		addFileChangeReport(NEW_DESCRIPTIONS_FILE,count,"New descriptions");
	}

	private void generateRetiredDescriptionsReport(Rf2DescriptionFile rf2DescFile, ArrayList<Long> newcomponents) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		fos = new FileOutputStream(new File(outputDirectory, RETIRED_DESCRIPTIONS_FILE));
		logger.info("Generating retired_descriptions_" + releaseDate + ".json");
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		ArrayList<Long> retiredDescriptios = rf2DescFile.getRetiredComponents(startDate, endDate);
		int count=writeDescriptionsFile(rf2DescFile, newcomponents, bw, retiredDescriptios);

		addFileChangeReport(RETIRED_DESCRIPTIONS_FILE,count,"Retired descriptions");
	}

	private void generateReactivatedDescriptionsReport(Rf2DescriptionFile rf2DescFile, ArrayList<Long> newcomponents) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		fos = new FileOutputStream(new File(outputDirectory, REACTIVATED_DESCRIPTIONS_FILE));
		logger.info("Generating reactivated_descriptions_" + releaseDate + ".json");
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		ArrayList<Long> retiredDescriptios = rf2DescFile.getReactivatedCount(startDate, endDate);
		int count=writeDescriptionsFile(rf2DescFile, newcomponents, bw, retiredDescriptios);

		addFileChangeReport(REACTIVATED_DESCRIPTIONS_FILE,count,"Reactivated descriptions");
	}

	private int writeDescriptionsFile(Rf2DescriptionFile rf2DescFile, ArrayList<Long> newcomponents, BufferedWriter bw, ArrayList<Long> retiredDescriptions) throws IOException {
		int count=0;
		for (Long long1 : retiredDescriptions) {
			Rf2DescriptionRow rf2DescRow = rf2DescFile.getLastActiveRow(startDate, long1);
			if (!newcomponents.contains(rf2DescRow.getConceptId())) {
				Description desc=new Description(long1.toString() , rf2DescRow.getEffectiveTime() , String.valueOf(rf2DescRow.getActive()) , rf2DescRow.getConceptId().toString() ,
						rf2DescRow.getLanguageCode() , rf2DescFile.getFsn(rf2DescRow.getTypeId()) , rf2DescRow.getTerm() ,
					    rf2DescFile.getFsn(rf2DescRow.getCaseSignificanceId()));
				bw.append(gson.toJson(desc).toString());
				desc=null;
				bw.append(sep);
				count++;
			}
		}
		bw.close();
		return count;
	}

	private void generatingRetiredConceptReasons(Rf2DescriptionFile rf2DescFile, Rf2ConceptFile conceptFile, Rf2AttributeValueRefsetFile attrValue, Rf2AssociationRefsetFile associationFile)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		ArrayList<Long> retiredConcepts = conceptFile.getRetiredComponents(startDate, endDate);
		fos = new FileOutputStream(new File(outputDirectory, RETIRED_CONCEPT_REASON_FILE));
		logger.info("Generating retired_concept_reason.json");
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		int count=0;
		for (Long long1 : retiredConcepts) {
			Rf2AttributeValueRefsetRow refsetRow = attrValue.getRowByReferencedComponentId(long1);
			ArrayList<Rf2AssociationRefsetRow> associationRow = associationFile.getRowByReferencedComponentId(long1);

			String fsn = rf2DescFile.getFsn(long1);
			Pattern p = Pattern.compile("\\((.*?)\\)", Pattern.DOTALL);
			String semanticTag = "";
			if (fsn != null) {
				Matcher matcher = p.matcher(fsn);
				while (matcher.find()) {
					semanticTag = matcher.group(1);
				}
			}
			if (!associationRow.isEmpty()) {
				for (Rf2AssociationRefsetRow rf2AssociationRefsetRow : associationRow) {
					String assValue = rf2AssociationRefsetRow.getTargetComponent();
					if (refsetRow != null) {
						String value = refsetRow.getValueId();
						RetiredConcept concept=new RetiredConcept(long1.toString() ,rf2DescFile.getFsn(conceptFile.getDefinitionStatusId(long1)) , fsn , semanticTag,
								rf2DescFile.getFsn(Long.parseLong(value)),rf2DescFile.getFsn(Long.parseLong(rf2AssociationRefsetRow.getRefsetId())),
								rf2DescFile.getFsn(Long.parseLong(assValue)) ,String.valueOf(conceptFile.isNewComponent(long1, startDate)));
						bw.append(gson.toJson(concept).toString());
						
						concept=null;
					} else {
						RetiredConcept concept=new RetiredConcept(long1.toString() ,rf2DescFile.getFsn(conceptFile.getDefinitionStatusId(long1)) , fsn , semanticTag,
								"no reason",rf2DescFile.getFsn(Long.parseLong(rf2AssociationRefsetRow.getRefsetId())),
								rf2DescFile.getFsn(Long.parseLong(assValue)) ,String.valueOf(conceptFile.isNewComponent(long1, startDate)));
						bw.append(gson.toJson(concept).toString());
						concept=null;
					}
					bw.append(sep);
					count++;
				}
			} else {
				if (refsetRow != null) {
					String value = refsetRow.getValueId();
					RetiredConcept concept=new RetiredConcept(long1.toString() ,rf2DescFile.getFsn(conceptFile.getDefinitionStatusId(long1)) , fsn , semanticTag,
							rf2DescFile.getFsn(Long.parseLong(value)),"no association","-" ,"-");
					bw.append(gson.toJson(concept).toString());
					concept=null;
				} else {
					RetiredConcept concept=new RetiredConcept(long1.toString() ,rf2DescFile.getFsn(conceptFile.getDefinitionStatusId(long1)) , fsn , semanticTag,
							"no reason","no association","-" ,"-");
					bw.append(gson.toJson(concept).toString());
					concept=null;
				}
				bw.append(sep);
				count++;
			}
		}
		bw.close();
		attrValue.releasePreciousMemory();
		associationFile.releasePreciousMemory();

		addFileChangeReport(RETIRED_CONCEPT_REASON_FILE,count,"Retired concept reasons");
		
	}

	private void addFileChangeReport(String fileName, int count, String reportName) {
		FileChangeReport fileChanges= new FileChangeReport();
		fileChanges.setFile(fileName);
		fileChanges.setCount(count);
		fileChanges.setName(reportName);
		List<FileChangeReport>lChanges= changeSummary.getReports();
		if (lChanges==null){
			lChanges=new ArrayList<FileChangeReport>();
		}
		lChanges.add(fileChanges);
		changeSummary.setReports(lChanges);
	}

	private void generateNewRelationshipsReport(Rf2DescriptionFile rf2DescFile, Rf2RelationshipFile relFile) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		FileOutputStream fos;
		OutputStreamWriter osw;
		BufferedWriter bw;
		ArrayList<Long> newRels = relFile.getNewComponentIds(startDate);
		fos = new FileOutputStream(new File(outputDirectory, NEW_RELATIONSHIPS_FILE));
		logger.info("Generating new_relationships.json");
		osw = new OutputStreamWriter(fos, "UTF-8");
		bw = new BufferedWriter(osw);
		for (Long long1 : newRels) {
			Rf2RelationshipRow row = relFile.getById(long1, startDate);
			Relationship rel=new Relationship(row.getId().toString() , String.valueOf(row.getActive()) , rf2DescFile.getFsn(row.getSourceId()),row.getSourceId().toString(), rf2DescFile.getFsn(row.getDestinationId()) ,
				 rf2DescFile.getFsn(row.getTypeId()) , rf2DescFile.getFsn(row.getCharacteristicTypeId()));
			bw.append(gson.toJson(rel).toString());
			rel=null;
			bw.append(sep);
		}
		bw.close();

		addFileChangeReport(NEW_RELATIONSHIPS_FILE,newRels.size(),"New relationships");
	}

	private ArrayList<Long> generateNewConceptsReport(Rf2DescriptionFile rf2DescFile, Rf2ConceptFile conceptFile) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		logger.info("getting new conscpt ids");
		ArrayList<Long> newcomponents = conceptFile.getNewComponentIds(startDate);
		FileOutputStream fos = new FileOutputStream(new File(outputDirectory, NEW_CONCEPTS_FILE));
		logger.info("Generating reporte_new_concepts.json");
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);

		for (Long long1 : newcomponents) {
			String fsn = rf2DescFile.getFsn(long1);
			Pattern p = Pattern.compile("\\((.*?)\\)", Pattern.DOTALL);
			String semanticTag = "";
			if (fsn != null) {
				Matcher matcher = p.matcher(fsn);
				while (matcher.find()) {
					semanticTag = matcher.group(1);
				}
			}
			Concept concept=new Concept(long1.toString() ,rf2DescFile.getFsn(conceptFile.getDefinitionStatusId(long1)) , fsn , semanticTag);
			bw.append(gson.toJson(concept).toString());
			concept=null;
			bw.append(sep);
		}
		bw.close();

		addFileChangeReport(NEW_CONCEPTS_FILE,newcomponents.size(),"New concepts");
		
		return newcomponents;
	}

}
