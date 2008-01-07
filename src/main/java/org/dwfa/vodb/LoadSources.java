package org.dwfa.vodb;

import java.io.File;
import java.util.logging.Level;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.edit.AddImage;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.vodb.types.Path;

public class LoadSources {

	private static Stopwatch timer;
	public static void main(String[] args) { 
		LocalVersionedTerminology.set(new VodbEnv());
		ProcessAceFormatSourcesBerkeley loadConstants = null;
		try {
			timer = new Stopwatch();
			timer.start();
			AceConfig.getVodb().setup(new File(args[0]), false, 600000000L);
			loadConstants = new ProcessAceFormatSourcesBerkeley(AceConfig.getVodb());
			AceLog.getAppLog().info("Starting to process AceAuxillary.");
			loadConstants.execute(new File(args[1]));
			AceLog.getAppLog().info("Finished loading constants. Elapsed time: " + timer.getElapsedTime());
			Path.writeBasePaths(AceConfig.getVodb());
			AddImage.addStockImages(AceConfig.getVodb());
			int[] releaseDates = loadConstants.getReleaseDates();
			if (args.length > 2) {
				ProcessSnomedBerkeley loadSnomed = new ProcessSnomedBerkeley(AceConfig.getVodb(), releaseDates[0]);
				AceLog.getAppLog().info("(2) Starting to process SNOMED: " + args[2]);
				loadSnomed.execute(new File(args[2]));
				AceLog.getAppLog().info("Finished loading terminologies. Elapsed time: " + timer.getElapsedTime());
			} 
			
		} catch (Exception e) {
			if (loadConstants != null) {
				loadConstants.getLog().log(Level.SEVERE, e.toString(), e);
			} else {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
	}

}
