package org.ihtsdo.project;

import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.ihtsdo.project.panel.TranslationHelperPanel;

public class RevisionProcessSelector implements I_SelectProcesses {


	@Override
	public boolean select(I_DescribeBusinessProcess process) {
		String sub=process.getSubject();
		if (sub!=null)
			return sub.equals(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW);

		return false;
	}

	@Override
	public boolean select(I_DescribeObject object) {
		I_DescribeBusinessProcess objectBP=(I_DescribeBusinessProcess)object;
		String sub=objectBP.getSubject();
		if (sub!=null)
			return sub.equals(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW);

		return false;
	}

}

