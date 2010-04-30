package org.dwfa.ace.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dwfa.ace.refset.ConceptRefsetInclusionDetails;
import org.dwfa.tapi.TerminologyException;

public interface I_HelpCaculateMemberRefsets {

	public void run();

	public void IncludeConcept(int componentId, int refsetId, int parentId)
			throws TerminologyException, IOException;

	public void ExcludeConcept(int componentId, int refsetId, int parentId)
			throws TerminologyException, IOException;

	public void addToExistingRefsetMembers(
			ConceptRefsetInclusionDetails conceptDetails, Integer refset);

	public void addToExistingParentMembers(
			ConceptRefsetInclusionDetails conceptDetails, Integer refset);

	public void addToRefsetMembers(
			ConceptRefsetInclusionDetails conceptDetails, Integer refset);

	public void addToRefsetExclusion(
			ConceptRefsetInclusionDetails conceptDetails, Integer refset);

	public File getOutputDirectory();

	public void setOutputDirectory(File outputDirectory);

	public boolean isValidateOnly();

	public void setValidateOnly(boolean validateOnly);

	public List<Integer> getAllowedRefsets();

	public void setAllowedRefsets(List<Integer> allowedRefsets);

	public boolean isMarkParents();

	public void setMarkParents(boolean markParents);

	public File getReportFile();

	public void setReportFile(File reportFile);

	public int getCommitSize();

	public void setCommitSize(int commitSize);

	public boolean getUseNonTxInterface();

	public void setUseNonTxInterface(boolean useNonTxInterface);

	public File getChangeSetOutputDirectory();

	public void setChangeSetOutputDirectory(File changeSetOutputDirectory);

	public boolean isAdditionalLogging();

	public void setAdditionalLogging(boolean additionalLogging);

}