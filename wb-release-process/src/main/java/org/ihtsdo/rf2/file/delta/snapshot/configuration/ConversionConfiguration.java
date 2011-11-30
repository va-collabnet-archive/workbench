package org.ihtsdo.rf2.file.delta.snapshot.configuration;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.ihtsdo.rf2.file.delta.snapshot.tasks.AbstractTask;

public class ConversionConfiguration {
	
	private List<ConversionProfile> profiles;
	private TreeSet<AbstractTask> tasksChain;
	private Logger logger;

}
