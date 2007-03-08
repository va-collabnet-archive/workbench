package org.dwfa.vodb;

import java.io.File;
import java.util.logging.Level;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.edit.AddImage;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.vodb.types.Path;

public class LoadSources {

	private static Stopwatch timer;
	public static void main(String[] args) { 
		AceConfig.vodb = new VodbEnv();
		ProcessConstantsBerkeley loadConstants = null;
		try {
			timer = new Stopwatch();
			timer.start();
			AceConfig.vodb.setup(new File(args[0]), false);
			loadConstants = new ProcessConstantsBerkeley(AceConfig.vodb);
			System.out.println("Starting to process AceAuxillary.");
			loadConstants.execute(new File(args[1]));
			System.out.println("Finished loading constants. Elapsed time: " + timer.getElapsedTime());
			Path.writeBasePaths(AceConfig.vodb);
			AddImage.addStockImage(AceConfig.vodb);
			int[] releaseDates = loadConstants.getReleaseDates();
			if (args.length > 2) {
				ProcessSnomedBerkeley loadSnomed = new ProcessSnomedBerkeley(AceConfig.vodb,
						loadConstants.getConstantToIntMap(), releaseDates[0]);
				System.out.println("Starting to process SNOMED.");
				loadSnomed.execute(new File(args[2]));
				System.out.println("Finished loading terminologies. Elapsed time: " + timer.getElapsedTime());
			} 
			
		} catch (Exception e) {
			if (loadConstants != null) {
				loadConstants.getLog().log(Level.SEVERE, e.toString(), e);
			} else {
				e.printStackTrace();
			}
		}
	}

}
