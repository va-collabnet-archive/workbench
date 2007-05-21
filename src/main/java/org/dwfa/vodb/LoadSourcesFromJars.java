package org.dwfa.vodb;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.jar.JarFile;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.edit.AddImage;
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

	public static void loadFromSingleJar(String dbDir, String jarFile, String dataPrefix) throws Exception {
		AceConfig.vodb = new VodbEnv();
		ProcessConstantsBerkeley loadConstants = null;
		timer = new Stopwatch();
		timer.start();
		AceConfig.vodb.setup(new File(dbDir), false, 600000000L);
		loadConstants = new ProcessConstantsBerkeley(AceConfig.vodb);
		AceLog.getAppLog().info("Starting to process " + jarFile + ": " + dataPrefix);
		loadConstants.execute(new JarFile(jarFile), dataPrefix, FORMAT.ACE);
		Path.writeBasePaths(AceConfig.vodb);
		AddImage.addStockImage(AceConfig.vodb);
		AceLog.getAppLog().info("Finished loading " + jarFile + ". Elapsed time: "
				+ timer.getElapsedTime());
		printElapsedTime();
		AceLog.getAppLog().info("Creating concept->desc map.");
		AceConfig.vodb.getConceptDescMap();
		//Update the history records for the relationships...
		printElapsedTime();

		//monitor.setProgressInfoUpper("Starting c1RelMap.");
		AceConfig.vodb.createC1RelMap();
		printElapsedTime();
		//monitor.setProgressInfoUpper("Starting c2RelMap.");
		AceConfig.vodb.createC2RelMap();
		printElapsedTime();
		//monitor.setProgressInfoUpper("Starting createIdMaps.");
		AceConfig.vodb.createIdMaps();
		printElapsedTime();
		//monitor.setProgressInfoUpper("Starting createConceptImageMap.");
		AceConfig.vodb.createConceptImageMap();
		//monitor.setProgressInfoUpper("Starting populateTimeBranchDb().");
		AceConfig.vodb.populateTimeBranchDb();
		printElapsedTime();
		//AceConfig.monitor.setProgressInfoUpper("Starting makeLuceneIndex().");
		AceConfig.vodb.makeLuceneIndex();
		//AceConfig.monitor.setProgressInfoUpper("Starting cleanup.");
		printElapsedTime();
		AceConfig.vodb.close();
		printElapsedTime();
	}

	public static void main(String[] args) throws Exception {
		AceConfig.vodb = new VodbEnv();
		ProcessConstantsBerkeley loadConstants = null;
		timer = new Stopwatch();
		timer.start();
		AceConfig.vodb.setup(new File(args[0]), false, 600000000L);
		loadConstants = new ProcessConstantsBerkeley(AceConfig.vodb);
		AceLog.getAppLog().info("Starting to process AceAuxillary: " + Arrays.asList(args));
		loadConstants.execute(new JarFile(args[1]), "org/jehri/cement/", FORMAT.SNOMED);
		AceLog.getAppLog().info("Finished loading constants. Elapsed time: "
				+ timer.getElapsedTime());
		Path.writeBasePaths(AceConfig.vodb);
		AddImage.addStockImage(AceConfig.vodb);
		int[] releaseDates = loadConstants.getReleaseDates();
		if (args.length > 2) {
			ProcessSnomedBerkeley loadSnomed = new ProcessSnomedBerkeley(
					AceConfig.vodb, loadConstants.getConstantToIntMap(),
					releaseDates[0]);
			AceLog.getAppLog().info("Starting to process SNOMED.");
			loadSnomed.execute(new JarFile(args[2]));
			AceLog.getAppLog().info("Finished loading terminologies. Elapsed time: "
					+ timer.getElapsedTime());
		}
	}

}
