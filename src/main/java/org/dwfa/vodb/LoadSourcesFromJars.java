package org.dwfa.vodb;

import java.io.File;
import java.util.Arrays;
import java.util.jar.JarFile;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.edit.AddImage;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.vodb.types.Path;

public class LoadSourcesFromJars {
	private static Stopwatch timer;

	public static void main(String[] args) throws Exception {
		AceConfig.vodb = new VodbEnv();
		ProcessConstantsBerkeley loadConstants = null;
		timer = new Stopwatch();
		timer.start();
		AceConfig.vodb.setup(new File(args[0]), false, 600000000L);
		loadConstants = new ProcessConstantsBerkeley(AceConfig.vodb);
		AceLog.getLog().info("Starting to process AceAuxillary: " + Arrays.asList(args));
		loadConstants.execute(new JarFile(args[1]));
		AceLog.getLog().info("Finished loading constants. Elapsed time: "
				+ timer.getElapsedTime());
		Path.writeBasePaths(AceConfig.vodb);
		AddImage.addStockImage(AceConfig.vodb);
		int[] releaseDates = loadConstants.getReleaseDates();
		if (args.length > 2) {
			ProcessSnomedBerkeley loadSnomed = new ProcessSnomedBerkeley(
					AceConfig.vodb, loadConstants.getConstantToIntMap(),
					releaseDates[0]);
			AceLog.getLog().info("Starting to process SNOMED.");
			loadSnomed.execute(new JarFile(args[2]));
			AceLog.getLog().info("Finished loading terminologies. Elapsed time: "
					+ timer.getElapsedTime());
		}
	}

}
