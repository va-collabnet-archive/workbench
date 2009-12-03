package org.dwfa.mojo.epicexport.kp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.dwfa.mojo.epicexport.EpicExportManager;
import org.dwfa.mojo.epicexport.EpicExportWriter;
import org.dwfa.mojo.epicexport.I_EpicExportRecordWriter;
import org.dwfa.mojo.epicexport.I_EpicLoadFileBuilder;

public class EpicLoadFileFactory {

	public EpicExportManager getExportManager(String baseDir) {
		return new EpicExportManager(baseDir, this);
	}
	
	public I_EpicLoadFileBuilder getLoadFileBuilder(String masterfile, EpicExportManager em) throws Exception {
		I_EpicLoadFileBuilder ret;
		if (masterfile.equals(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL))
			ret = new EpicExportBuilderEDGClinical(this, em);
		else if (masterfile.equals(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING))
			ret = new EpicExportBuilderEDGBilling(this, em);
		else
			throw new Exception("Unhandled masterfile: " + masterfile);
		return ret;
	}
	
	public I_EpicExportRecordWriter getWriter(File fw) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(fw));
		EpicExportWriter ret = new EpicExportWriter(bw, fw.getName());
		return ret;
	}
	
}
