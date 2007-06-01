package org.dwfa.vodb;

import java.util.Arrays;
import java.util.Date;
import java.util.jar.JarFile;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.edit.AddImage;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.vodb.ProcessConstants.FORMAT;
import org.dwfa.vodb.types.Path;

public class LoadSourcesFromJars {
	private static Stopwatch timer;
	
	private static void printElapsedTime() {
		Date end = new Date();
		long elapsed = timer.getElapsedTime();
		elapsed = elapsed / 1000;
		AceLog.getAppLog().info("Elapsed sec: " + elapsed);
		elapsed = elapsed / 60;
		AceLog.getAppLog().info("Elapsed min: " + elapsed);
		AceLog.getAppLog().info(end.toString());
	}

	public static void loadFromSingleJar(String jarFile, String dataPrefix) throws Exception {
		ProcessConstantsBerkeley loadConstants = null;
		timer = new Stopwatch();
		timer.start();
		loadConstants = new ProcessConstantsBerkeley((VodbEnv) LocalVersionedTerminology.get());
		AceLog.getAppLog().info("Starting to process " + jarFile + ": " + dataPrefix);
		loadConstants.execute(new JarFile(jarFile), dataPrefix, FORMAT.ACE);
		Path.writeBasePaths((VodbEnv) LocalVersionedTerminology.get());
		AddImage.addStockImage((VodbEnv) LocalVersionedTerminology.get());
		AceLog.getAppLog().info("Finished loading " + jarFile + ". Elapsed time: "
				+ timer.getElapsedTime());
		printElapsedTime();
		AceLog.getAppLog().info("Creating concept->desc map.");
		((VodbEnv) LocalVersionedTerminology.get()).getConceptDescMap();
		//Update the history records for the relationships...
		printElapsedTime();

		//monitor.setProgressInfoUpper("Starting c1RelMap.");
		((VodbEnv) LocalVersionedTerminology.get()).createC1RelMap();
		printElapsedTime();
		//monitor.setProgressInfoUpper("Starting c2RelMap.");
		((VodbEnv) LocalVersionedTerminology.get()).createC2RelMap();
		printElapsedTime();
		//monitor.setProgressInfoUpper("Starting createIdMaps.");
		((VodbEnv) LocalVersionedTerminology.get()).createIdMaps();
		printElapsedTime();
		//monitor.setProgressInfoUpper("Starting createConceptImageMap.");
		((VodbEnv) LocalVersionedTerminology.get()).createConceptImageMap();
		//monitor.setProgressInfoUpper("Starting populateTimeBranchDb().");
		((VodbEnv) LocalVersionedTerminology.get()).populateTimeBranchDb();
		printElapsedTime();
		//AceConfig.monitor.setProgressInfoUpper("Starting makeLuceneIndex().");
		((VodbEnv) LocalVersionedTerminology.get()).makeLuceneDescriptionIndex();
		//AceConfig.monitor.setProgressInfoUpper("Starting cleanup.");
		printElapsedTime();
		((VodbEnv) LocalVersionedTerminology.get()).close();
		printElapsedTime();
	}

	public static void main(String[] args) throws Exception {
		ProcessConstantsBerkeley loadConstants = null;
		timer = new Stopwatch();
		timer.start();
		loadConstants = new ProcessConstantsBerkeley((VodbEnv) LocalVersionedTerminology.get());
		AceLog.getAppLog().info("Starting to process AceAuxillary: " + Arrays.asList(args));
		loadConstants.execute(new JarFile(args[1]), "org/jehri/cement/", FORMAT.SNOMED);
		AceLog.getAppLog().info("Finished loading constants. Elapsed time: "
				+ timer.getElapsedTime());
		Path.writeBasePaths((VodbEnv) LocalVersionedTerminology.get());
		AddImage.addStockImage((VodbEnv) LocalVersionedTerminology.get());
		int[] releaseDates = loadConstants.getReleaseDates();
		if (args.length > 2) {
			ProcessSnomedBerkeley loadSnomed = new ProcessSnomedBerkeley(
					(VodbEnv) LocalVersionedTerminology.get(), loadConstants.getConstantToIntMap(),
					releaseDates[0]);
			AceLog.getAppLog().info("Starting to process SNOMED.");
			loadSnomed.execute(new JarFile(args[2]));
			AceLog.getAppLog().info("Finished loading terminologies. Elapsed time: "
					+ timer.getElapsedTime());
		}
	}

}
