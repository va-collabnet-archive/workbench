package org.ihtsdo.file.validation.api;

import java.io.File;
import java.util.List;

import org.ihtsdo.file.validation.model.ReleaseFile;
import org.ihtsdo.file.validation.model.TestError;

public interface FileValidationAPI {
	
	public List<ReleaseFile> getFiles();
	public List<TestError> testFile(ReleaseFile releaseFile, File fileToTest);

}
