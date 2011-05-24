	 package org.ihtsdo.tk.contradiction;

import java.util.Collection;

import org.ihtsdo.tk.api.ComponentVersionBI;

public class FoundContradictionVersions {
	private ContradictionResult result;
	private Collection<? extends ComponentVersionBI> versions;
	
	public FoundContradictionVersions(ContradictionResult r, Collection<? extends ComponentVersionBI> v) {
		result = r;
		versions = v;
	}
	
	public ContradictionResult getResult() {
		return result;
	}
	
	public Collection<? extends ComponentVersionBI> getVersions() {
		return versions;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("ContradictionFinder findings: ");
		
		if (versions.size() == 0 && result == ContradictionResult.NONE) {
			buffer.append("\nThere are no versions in conflict");
		} else if (result == ContradictionResult.ERROR || 
				   versions.size() == 0 || 
				   result == ContradictionResult.NONE) {
			buffer.append("\nThere was an odd result that requires investigation for this concept");
		} else {
			buffer.append("\nThe result of the inspection was: ");

			switch (result) {
			case CONTRADICTION:
				buffer.append("Contradcition");
				break;
			case DUPLICATE_EDIT:
				buffer.append("Duplicate edit of a concept");
				break;
			case DUPLICATE_NEW:
				buffer.append("Duplicate newly created concepts");
				break;
			case SINGLE_MODELER_CHANGE:
				buffer.append("Single modeler change");
				break;
			}				

			buffer.append("\nWith the following Versions: ");
			
			int counter = 0;
			for ( ComponentVersionBI v : versions) {
				buffer.append("\n" + counter++ + ": " + v.toString());
			}
		}
		
		return buffer.toString();
	}
}
