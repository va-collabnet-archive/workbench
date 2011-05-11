	 package org.ihtsdo.tk.contradiction;

import java.util.Collection;

import org.ihtsdo.tk.api.ComponentVersionBI;

public class FoundContradictionVersions {
	ContradictionResult result;
	Collection<? extends ComponentVersionBI> versions;
	
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
}
