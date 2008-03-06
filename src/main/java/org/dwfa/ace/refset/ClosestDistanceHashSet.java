package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.HashSet;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.tapi.TerminologyException;

public class ClosestDistanceHashSet<T> extends HashSet<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean add(T o) {

		if (o instanceof ConceptRefsetInclusionDetails) {
			ConceptRefsetInclusionDetails newConcept = (ConceptRefsetInclusionDetails) o;
			ConceptRefsetInclusionDetails oldConcept = (ConceptRefsetInclusionDetails) get(newConcept.getConceptId());

			if (oldConcept==null) {
				return super.add(o);
			} else {
				if (oldConcept.getDistance() > newConcept.getDistance()) {
					remove(o);
					/*
					try {
						if (LocalVersionedTerminology.get()!=null) {
							System.out.println("Found concept with closer distance : " + LocalVersionedTerminology.get().getConcept(oldConcept.getConceptId()));
							System.out.println("further specification was in : " + LocalVersionedTerminology.get().getConcept(oldConcept.getInclusionReasonId()));
							System.out.println("closer specification was in : " + LocalVersionedTerminology.get().getConcept(newConcept.getInclusionReasonId()));
						}
						
					} catch (TerminologyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					*/
					return super.add(o);
				}
			}
		} else {
			return super.add(o);
		}

		return true;

	}

	public ConceptRefsetInclusionDetails get(int key) {
		ConceptRefsetInclusionDetails crid = null;

		for (Object o : this) {
			if (o instanceof ConceptRefsetInclusionDetails) {
				ConceptRefsetInclusionDetails concept = (ConceptRefsetInclusionDetails) o;
				if (concept.getConceptId()==key) {
					crid = concept;
				}
			}
		}
		return crid;
	}

}
