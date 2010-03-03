/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.vodb;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.edit.AddImage;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.vodb.process.ProcessAceFormatSources.FORMAT;
import org.dwfa.vodb.process.ProcessAceFormatSourcesBerkeley;
import org.dwfa.vodb.process.ProcessSnomedBerkeley;
import org.dwfa.vodb.types.Path;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

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

    public static void loadFromDirectory(File dataDir, String encoding)
            throws Exception {
        ProcessAceFormatSourcesBerkeley loadConstants = null;
        timer = new Stopwatch();
        timer.start();
        loadConstants =
                new ProcessAceFormatSourcesBerkeley(
                    (VodbEnv) LocalVersionedTerminology.get());
        AceLog.getAppLog().info("Starting to process " + dataDir);
        loadConstants.executeFromDir(dataDir, encoding);
        Path.writeBasePaths((VodbEnv) LocalVersionedTerminology.get());
        try {
            AddImage.addStockImages((VodbEnv) LocalVersionedTerminology.get());
        } catch (NoMappingException e) {
            AceLog.getAppLog().info(e.getLocalizedMessage());
        }
        AceLog.getAppLog().info(
            "Finished loading " + dataDir + ". Elapsed time: "
                + timer.getElapsedTime());
        printElapsedTime();
        // Update the history records for the relationships...
        AceLog.getAppLog().info("Starting populateTimeBranchDb()");
        ((VodbEnv) LocalVersionedTerminology.get()).populatePositions();
        printElapsedTime();
        AceLog.getAppLog().info("Starting createLuceneDescriptionIndex()");
        ((VodbEnv) LocalVersionedTerminology.get())
            .createLuceneDescriptionIndex();
        printElapsedTime();
    }

    public static void loadFromSingleJar(String jarFile, String dataPrefix, final File extractDir)
            throws Exception {
        ProcessAceFormatSourcesBerkeley loadConstants = null;
        timer = new Stopwatch();
        timer.start();
        loadConstants =
                new ProcessAceFormatSourcesBerkeley(
                    (VodbEnv) LocalVersionedTerminology.get());
        AceLog.getAppLog().info(
            "Starting to process " + jarFile + ": " + dataPrefix);
        loadConstants.execute(new File(jarFile), dataPrefix, FORMAT.ACE, extractDir);
        Path.writeBasePaths((VodbEnv) LocalVersionedTerminology.get());
        try {
            AddImage.addStockImages((VodbEnv) LocalVersionedTerminology.get());
        } catch (NoMappingException e) {
            AceLog.getAppLog().info(e.getLocalizedMessage());
        }
        AceLog.getAppLog().info(
            "Finished loading " + jarFile + ". Elapsed time: "
                + timer.getElapsedTime());
        printElapsedTime();
        AceLog.getAppLog().info("Starting populateTimeBranchDb()");
        ((VodbEnv) LocalVersionedTerminology.get()).populatePositions();
        printElapsedTime();
    }

    public static void main(String[] args, final File unjaringDir) throws Exception {
        ProcessAceFormatSourcesBerkeley loadConstants = null;
        timer = new Stopwatch();
        timer.start();
        loadConstants = new ProcessAceFormatSourcesBerkeley((VodbEnv) LocalVersionedTerminology.get());
        AceLog.getAppLog().info("Starting to process AceAuxillary: " + Arrays.asList(args));

        Set<String> argSet = new HashSet<String>(Arrays.asList(args));
        argSet.remove(args[0]);
        String processed = null;

        for (String arg : args) {
            if (arg.contains("cement")) {
                processed = arg;
                AceLog.getAppLog().info("Processing constants in: " + arg);
                loadConstants.execute(new File(arg), "org/jehri/cement/", FORMAT.SNOMED, unjaringDir);
                break;
            }
        }
        argSet.remove(processed);
        AceLog.getAppLog().info("Finished loading constants. Elapsed time: " + timer.getElapsedTime());
        loadConstants.flushIdBuffer();
        Path.writeBasePaths((VodbEnv) LocalVersionedTerminology.get());
        AddImage.addStockImages((VodbEnv) LocalVersionedTerminology.get());
        I_IntSet releaseDates = loadConstants.getReleaseDates();
        for (String arg : argSet) {
            ProcessSnomedBerkeley loadSnomed = new ProcessSnomedBerkeley((VodbEnv) LocalVersionedTerminology.get(),
                                                                         releaseDates.getSetValues()[0]);
            AceLog.getAppLog().info("(1) Starting to process SNOMED: " + arg);
            loadSnomed.execute(new JarFile(arg));
            AceLog.getAppLog().info("Finished loading terminologies. Elapsed time: " + timer.getElapsedTime());
        }
    }
}
