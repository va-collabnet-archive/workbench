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

import java.io.File;
import java.util.logging.Level;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.edit.AddImage;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.vodb.process.ProcessAceFormatSourcesBerkeley;
import org.dwfa.vodb.process.ProcessSnomedBerkeley;
import org.dwfa.vodb.types.Path;

public class LoadSources {

    private static Stopwatch timer;

    public static void main(String[] args) {
        LocalVersionedTerminology.set(new VodbEnv());
        ProcessAceFormatSourcesBerkeley loadConstants = null;
        try {
            timer = new Stopwatch();
            timer.start();
            ((VodbEnv)Terms.get()).setup(new File(args[0]), false, 600000000L);
            loadConstants = new ProcessAceFormatSourcesBerkeley((VodbEnv) Terms.get());
            AceLog.getAppLog().info("Starting to process AceAuxillary.");
            loadConstants.executeSnomed(new File(args[1]));
            AceLog.getAppLog().info("Finished loading constants. Elapsed time: " + timer.getElapsedTime());
            Path.writeBasePaths((VodbEnv) Terms.get());
            AddImage.addStockImages((VodbEnv) Terms.get());
            I_IntSet releaseDates = loadConstants.getReleaseDates();
            if (args.length > 2) {
                ProcessSnomedBerkeley loadSnomed = new ProcessSnomedBerkeley((VodbEnv) Terms.get(),
                    releaseDates.getSetValues()[0]);
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
