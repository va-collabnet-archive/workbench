package org.ihtsdo.file.validation.api;

import java.util.List;

import org.ihtsdo.file.validation.model.File;
import org.ihtsdo.file.validation.model.TestError;

public interface FileValidationAPI {
	
	public List<File> getFiles();
	public List<TestError> testFile(File file);

}
