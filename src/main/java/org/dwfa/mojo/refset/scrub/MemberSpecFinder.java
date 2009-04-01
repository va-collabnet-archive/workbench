package org.dwfa.mojo.refset.scrub;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.RefsetUtilities;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.ConceptDescriptor;

/**
 * Finds concept extensions that match a particular criteria.<br>
 * This implementation will sweep through the member refsets looking for:
 * <li>  Specification extensions
 * <li>  Retirement path conflicts (extensions that are not properly retired on all paths)  
 */
public class MemberSpecFinder implements ConceptExtFinder {

	/**
	 * The name of the file to generate containing a list of all the qualifing concept extensions 
	 * that are found.
	 * @parameter
	 */
	public String reportFile;
	
	/**
	 * Specifies the valid extension concept values that are permitted. Extensions not of this type
	 * will be returned by the {@link #iterator()} 
	 * @parameter
	 */
	public ConceptDescriptor[] validTypeConcepts;	

	
	protected I_TermFactory termFactory;
	
	protected RefsetHelper refsetHelper;
	
	protected PrintWriter reportWriter;
	
	private List<Integer> validTypeIds;
	
	
	public MemberSpecFinder() throws Exception {
		termFactory = LocalVersionedTerminology.get();
		if (termFactory == null) { 
			throw new RuntimeException("The LocalVersionedTerminology is not available. Please check the database.");
		}
		refsetHelper = new RefsetHelper(termFactory);
	}
		
	
	/**
	 * Find any concept extension that has a current version part which does NOT have an 
	 * valid concept value/type.
	 */
	public Iterator<I_ThinExtByRefVersioned> iterator() {
		try {
			reportWriter = new PrintWriter(reportFile);
			ArrayList<I_ThinExtByRefVersioned> candidates = new ArrayList<I_ThinExtByRefVersioned>(); 
			
			for (Integer refsetId : refsetHelper.getSpecificationRefsets()) {
				
				int memberRefsetId = refsetHelper.getMemberSetConcept(refsetId).getConceptId();
				I_GetConceptData memberSet = refsetHelper.getConcept(memberRefsetId);
				String memberRefsetName = memberSet.getInitialText();
				System.out.println("\nProcessing spec refset: " + memberRefsetName);
				
				final int CURRENT_STATUS_ID = 
					termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());
				
				List<I_ThinExtByRefVersioned> refsetMembers = termFactory.getRefsetExtensionMembers(memberRefsetId);
				for (I_ThinExtByRefVersioned member : refsetMembers) {
					List<? extends I_ThinExtByRefPart> versions = member.getVersions();
					for (I_ThinExtByRefPart version : versions) {
						if (version.getStatus() == CURRENT_STATUS_ID) {
							if (version instanceof I_ThinExtByRefPartConcept) {
								int inclusionType = ((I_ThinExtByRefPartConcept)version).getConceptId();
								if (!isValidType(inclusionType)) {
									candidates.add(member);
									logCandidate(memberRefsetName, member);
									break;
								}
							}
						}
					}
				}
			}
			System.out.println("Found " + candidates.size() + " candidate extensions.");			
			return candidates.iterator();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			reportWriter.flush();
			reportWriter.close();
		}
	}
	
	private void logCandidate(String refsetName, I_ThinExtByRefVersioned candidate) throws Exception {
		String conceptDesc = termFactory.getConcept(candidate.getComponentId()).getInitialText();
		
		// First index the version parts so we can print back in chronological order
		TreeMap<Long, PartDescription> partIndex = new TreeMap<Long, PartDescription>();  
		for (I_ThinExtByRefPart part : candidate.getVersions()) {
			if (part instanceof I_ThinExtByRefPartConcept) {
				PartDescription partDesc = new PartDescription();
				int inclusionType = ((I_ThinExtByRefPartConcept)part).getConceptId();	
				partDesc.typeDesc = termFactory.getConcept(inclusionType).getInitialText();
				partDesc.statusDesc = termFactory.getConcept(part.getStatus()).getInitialText();
				partDesc.pathDesc = termFactory.getConcept(part.getPathId()).getInitialText();
				Long version = termFactory.convertToThickVersion(part.getVersion());
				partIndex.put(version, partDesc);
			}
		}
		
		System.out.println("\tFound candidate: " + conceptDesc);
		SimpleDateFormat dateFmt = new SimpleDateFormat("d MMM yyyy HH:mm:ss z");
		for (Long version : partIndex.keySet()) {
			PartDescription partDesc = partIndex.get(version);			 
			String dateStr = dateFmt.format(new Date(version));
			System.out.println("\t\t" + partDesc.typeDesc + "," + partDesc.statusDesc + "," + 
					partDesc.pathDesc + "," + dateStr);
			reportWriter.println(refsetName + "\t" + conceptDesc + "\t" + partDesc.typeDesc + "\t" + 
					partDesc.statusDesc + "\t" + partDesc.pathDesc + "\t" + dateStr);			
		}
		reportWriter.println();
	}
	
	private boolean isValidType(int inclusionType) throws Exception {
		if (validTypeIds == null) {
			validTypeIds = new ArrayList<Integer>();
			for (ConceptDescriptor conceptDesc : validTypeConcepts) {
				validTypeIds.add(conceptDesc.getVerifiedConcept().getId().getNativeId());
			}			
		}
		return validTypeIds.contains(Integer.valueOf(inclusionType));
	}
	
	/**
	 * Utilises the {@link RefsetUtilities} class by injecting the db
	 */
	private class RefsetHelper extends RefsetUtilities {
		public RefsetHelper(I_TermFactory termFactory) {
			super.termFactory = termFactory;
		}
	}
	
	private class PartDescription {
		String typeDesc;
		String statusDesc;
		String pathDesc;
	}
}
