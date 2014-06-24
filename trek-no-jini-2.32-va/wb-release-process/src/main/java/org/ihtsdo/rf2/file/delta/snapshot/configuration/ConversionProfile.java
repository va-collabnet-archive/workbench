package org.ihtsdo.rf2.file.delta.snapshot.configuration;

import java.util.List;

import org.ihtsdo.rf2.file.delta.snapshot.tasks.AbstractTask;

public class ConversionProfile {
	
	private RF2InputConfiguration rf2Input;
	private RF1ControlFilesConfiguration rf1ControlFiles;
	private DatabaseConnection databaseConnection;
	private List<AbstractTask> chainTasksPreferences;

}
