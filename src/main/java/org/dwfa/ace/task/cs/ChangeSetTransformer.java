package org.dwfa.ace.task.cs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

public interface ChangeSetTransformer {

	void createXmlCopy(Logger logger, File changeset) throws IOException, FileNotFoundException, ClassNotFoundException;

	String getOutputSuffix();

	void setOutputSuffix(String outputSuffix);
}
