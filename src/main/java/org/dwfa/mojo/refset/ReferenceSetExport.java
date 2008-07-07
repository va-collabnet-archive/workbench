package org.dwfa.mojo.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * This mojo exports reference sets from an ACE database
 * 
 * @goal refset-export
 * @author Dion McMurtrie
 */
public class ReferenceSetExport extends AbstractMojo implements I_ProcessConcepts {

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
	 * @required
	 */
	ExportSpecification[] exportSpecifications;
	
	/**
	 * Defines the directory to which the reference sets are exported
	 * 
	 * @parameter
	 * @required
	 */
	File refsetOutputDirectory;

	private I_TermFactory tf = LocalVersionedTerminology.get();

	private I_IntSet allowedStatuses;

	private Set<I_Position> positions;

	private HashMap<Integer, BufferedWriter> writerMap = new HashMap<Integer, BufferedWriter>();

	private HashMap<Integer, RefsetType> refsetTypeMap = new HashMap<Integer, RefsetType>();
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			allowedStatuses = tf.newIntSet();
			positions = new HashSet<I_Position>();
			for (ExportSpecification spec : exportSpecifications) {
				for (PositionDescriptor pd : spec.getPositionsForExport()) {
					positions.add(pd.getPosition());
				}
				for (ConceptDescriptor status : spec.getStatusValuesForExport()) {
					allowedStatuses.add(status.getVerifiedConcept().getConceptId());
				}
			}
			
			refsetOutputDirectory.mkdirs();
			
			tf.iterateConcepts(this);
			
			for (BufferedWriter writer : writerMap.values()) {
				writer.close();
			}
			
		} catch (Exception e) {
			throw new MojoExecutionException("exporting reference sets failed for specification " + exportSpecifications, e);
		}
	}

	
	public void processConcept(I_GetConceptData concept) throws Exception {
		if (testSpecification(concept)) {
			exportRefsets(concept.getConceptId());
			
			for (I_RelVersioned rel : concept.getSourceRels()) {
				processRelationship(rel);
			}
			
			for (I_DescriptionVersioned desc : concept.getDescriptions()) {
				processDescription(desc);
			}
		}
	}
	
	

	private void processDescription(I_DescriptionVersioned versionedDesc)
			throws Exception {
		for (I_DescriptionPart part : versionedDesc.getVersions()) {
			if (testSpecification(part.getTypeId())
					&& allowedStatuses.contains(part.getStatusId())
					&& checkPath(part.getPathId())) {
				exportRefsets(versionedDesc.getDescId());
			}
			
		}
	}


	private void processRelationship(I_RelVersioned versionedRel)
			throws Exception {
		if (testSpecification(versionedRel.getC2Id())) {
			for (I_RelPart part : versionedRel.getVersions()) {
				if (testSpecification(part.getCharacteristicId())
						&& testSpecification(part.getPathId())
						&& testSpecification(part.getRefinabilityId())
						&& testSpecification(part.getRelTypeId())
						&& allowedStatuses.contains(part.getStatusId())
						&& checkPath(part.getPathId())) {
					
					// found a valid version of this relationship for export
					// therefore export its extensions
					exportRefsets(versionedRel.getRelId());
				}
			}
		}
	}


	private void exportRefsets(int componentId) throws TerminologyException, Exception {
		List<I_ThinExtByRefVersioned> extensions = tf.getAllExtensionsForComponent(componentId);
		for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
			if (testSpecification(thinExtByRefVersioned.getRefsetId())) {
				for (I_ThinExtByRefTuple thinExtByRefTuple : thinExtByRefVersioned.getTuples(allowedStatuses, positions, false)) {
					export(thinExtByRefTuple);
				}
			}
		}
	}

	private void export(I_ThinExtByRefTuple thinExtByRefTuple) throws IOException, TerminologyException, InstantiationException, IllegalAccessException {
		int refsetId = thinExtByRefTuple.getRefsetId();
		
		RefsetType refsetType = refsetTypeMap.get(refsetId);
		if (refsetType == null) {
			refsetType = RefsetType.findByExtension(thinExtByRefTuple);
			refsetTypeMap.put(refsetId, refsetType);
		}
		
		BufferedWriter refsetWriter = writerMap.get(refsetId);
		if (refsetWriter == null) {
			//must not have written to this file yet
			String refsetName = tf.getConcept(refsetId).getInitialText();
			refsetName = refsetName.replace("/", "-");
			refsetName = refsetName.replace("'", "_");
			
			refsetWriter = new BufferedWriter(new FileWriter(new File(refsetOutputDirectory, refsetName + refsetType.getFileExtension())));
			writerMap.put(refsetId, refsetWriter);
			refsetWriter.write(refsetType.getRefsetHandler().getHeaderLine());
			refsetWriter.newLine();
		}
		
		//note that we are assuming that the type of refset member will be the same as previous for this file type
		//if not we'll get a class cast exception, as we probably should
		refsetWriter.write(refsetType.getRefsetHandler().formatRefsetLine(tf, thinExtByRefTuple));
		refsetWriter.newLine();
	}

	private boolean testSpecification(I_GetConceptData concept) throws Exception {
		for (ExportSpecification spec : exportSpecifications) {
			if (spec.test(concept)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean testSpecification(int id) throws TerminologyException, IOException, Exception {
		return testSpecification(tf.getConcept(id));
	}


	private boolean checkPath(int pathId) {
		for (I_Position position : positions) {
			if (position.getPath().getConceptId() == pathId) {
				return true;
			}
		}
		return false;
	}


}
