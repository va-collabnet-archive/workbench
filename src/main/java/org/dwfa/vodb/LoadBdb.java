package org.dwfa.vodb;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.edit.AddImage;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.vodb.ProcessAceFormatSources.FORMAT;
import org.dwfa.vodb.types.Path;

public class LoadBdb {
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

   public static void loadFromDirectory(File dataDir) throws Exception {
      ProcessAceFormatSourcesBerkeley loadConstants = null;
      timer = new Stopwatch();
      timer.start();
      loadConstants = new ProcessAceFormatSourcesBerkeley((VodbEnv) LocalVersionedTerminology.get());
      AceLog.getAppLog().info("Starting to process " + dataDir);
      loadConstants.executeFromDir(dataDir);
      Path.writeBasePaths((VodbEnv) LocalVersionedTerminology.get());
      try {
         AddImage.addStockImage((VodbEnv) LocalVersionedTerminology.get());
      } catch (NoMappingException e) {
         AceLog.getAppLog().info(e.getLocalizedMessage());
      }
      AceLog.getAppLog().info("Finished loading " + dataDir + ". Elapsed time: "
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
      ((VodbEnv) LocalVersionedTerminology.get()).createLuceneDescriptionIndex();
      //AceConfig.monitor.setProgressInfoUpper("Starting cleanup.");
      printElapsedTime();
      //((VodbEnv) LocalVersionedTerminology.get()).close();
      //printElapsedTime();
      
   }
	public static void loadFromSingleJar(String jarFile, String dataPrefix) throws Exception {
		ProcessAceFormatSourcesBerkeley loadConstants = null;
		timer = new Stopwatch();
		timer.start();
		loadConstants = new ProcessAceFormatSourcesBerkeley((VodbEnv) LocalVersionedTerminology.get());
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
		((VodbEnv) LocalVersionedTerminology.get()).createLuceneDescriptionIndex();
		//AceConfig.monitor.setProgressInfoUpper("Starting cleanup.");
		printElapsedTime();
		//((VodbEnv) LocalVersionedTerminology.get()).close();
		//printElapsedTime();
	}

	public static void main(String[] args) throws Exception {
		ProcessAceFormatSourcesBerkeley loadConstants = null;
		timer = new Stopwatch();
		timer.start();
		loadConstants = new ProcessAceFormatSourcesBerkeley((VodbEnv) LocalVersionedTerminology.get());
		AceLog.getAppLog().info("Starting to process AceAuxillary: " + Arrays.asList(args));
        
        Set<String> argSet = new HashSet<String>(Arrays.asList(args));
        argSet.remove(args[0]);
        String processed = null;
        
        for (String arg: args) {
            if (arg.contains("cement")) {
                processed = arg;
                AceLog.getAppLog().info("Processing constants in: " + arg);
                loadConstants.execute(new JarFile(arg), "org/jehri/cement/", FORMAT.SNOMED);
                break;
            }
        }
        argSet.remove(processed);
		AceLog.getAppLog().info("Finished loading constants. Elapsed time: "
				+ timer.getElapsedTime());
		Path.writeBasePaths((VodbEnv) LocalVersionedTerminology.get());
		AddImage.addStockImage((VodbEnv) LocalVersionedTerminology.get());
		int[] releaseDates = loadConstants.getReleaseDates();
        for (String arg: argSet) {
            ProcessSnomedBerkeley loadSnomed = new ProcessSnomedBerkeley(
                                                                        (VodbEnv) LocalVersionedTerminology.get(), loadConstants.getConstantToIntMap(),
                                                                        releaseDates[0]);
                                                                AceLog.getAppLog().info("Starting to process SNOMED: " + arg);
                                                                loadSnomed.execute(new JarFile(arg));
                                                                AceLog.getAppLog().info("Finished loading terminologies. Elapsed time: "
                                                                        + timer.getElapsedTime());
        }
	}

}
