package org.dwfa.mojo.refset.scrub;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.ConceptDescriptor;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This implementation will take each candidate concept extensions and ensure that 
 * <li> It contains on valid concept extension values (ie no specification types)
 * <li> All retirements are applied to all paths (to ensure no path conflicts) where the latest state is retired.  
 */
public class MemberSpecScrubber implements ConceptExtHandler {

	/**
	 * Specifies the valid concept extension concept values (refset membership types)
	 * @parameter
	 */
	protected ConceptDescriptor[] validTypeConcepts;

	protected I_TermFactory termFactory;

	private int currentStatusId;
	private int retiredStatusId;
	
	public MemberSpecScrubber() throws Exception {
		termFactory = LocalVersionedTerminology.get();        			
		currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());
		retiredStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());

	}
	
	public void process(ConceptExtFinder conceptExtensions) {
		try {
			Iterator<I_ThinExtByRefVersioned> iterator = conceptExtensions.iterator();
			while  (iterator.hasNext()) {
				processExtension(iterator.next());
			}
			termFactory.commit();
		} catch (Exception e) {
			throw new RuntimeException("Unable to complete the scrub.", e);
		}
	}
	
	private void processExtension(I_ThinExtByRefVersioned conceptExtension) throws Exception {
		
		// Get all the "current" parts
		ArrayList<I_ThinExtByRefPartConcept> subjects = new ArrayList<I_ThinExtByRefPartConcept>();
		for (I_ThinExtByRefPart part : conceptExtension.getVersions()) {
			if (part instanceof I_ThinExtByRefPartConcept) {
				if (part.getStatus() == currentStatusId) {
					subjects.add((I_ThinExtByRefPartConcept)part);
				}
			}
		}
		
		// Exclude all the matching "retired" parts (must have a later version and the same path)
		ArrayList<I_ThinExtByRefPartConcept> retiredSubjects = new ArrayList<I_ThinExtByRefPartConcept>();
		for (I_ThinExtByRefPart part : conceptExtension.getVersions()) {
			if (part instanceof I_ThinExtByRefPartConcept) {
				for (I_ThinExtByRefPartConcept subjectPart : subjects) {
					if (part.getStatus() == retiredStatusId) {				
						if ((subjectPart.getConceptId() == ((I_ThinExtByRefPartConcept)part).getConceptId()) &&
								(subjectPart.getPathId() == part.getPathId()) &&
								(subjectPart.getVersion() <= part.getVersion())) {
							retiredSubjects.add(subjectPart);
						}
					}
					if (part.getStatus() == currentStatusId) {
						// Watch out for a newer current part (an un-retirement)
						if ((subjectPart.getConceptId() == ((I_ThinExtByRefPartConcept)part).getConceptId()) &&
								(subjectPart.getPathId() == part.getPathId()) &&
								(subjectPart.getVersion() > part.getVersion())) {
							retiredSubjects.remove(subjectPart);
						}
					}
				}
			}
		}
		subjects.removeAll(retiredSubjects);
		
		// Retire remaining current parts on the same path
		for (I_ThinExtByRefPartConcept subjectPart : subjects) {
			
			I_ThinExtByRefPartConcept newPart = (I_ThinExtByRefPartConcept)subjectPart.duplicatePart();
			newPart.setStatus(retiredStatusId);
			newPart.setVersion(Integer.MAX_VALUE);
			conceptExtension.addVersion(newPart);
			termFactory.addUncommitted(conceptExtension);
		}
	}
}
