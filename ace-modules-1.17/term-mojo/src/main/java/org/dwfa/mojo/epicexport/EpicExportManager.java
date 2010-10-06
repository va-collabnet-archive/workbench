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
package org.dwfa.mojo.epicexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.dwfa.mojo.epicexport.kp.EpicLoadFileFactory;

/**
 * Class used to manage a list of export writers, and export builders. Used to
 * determine
 * where to place an export. If a particular writer or builder is not present,
 * will create a new instance and place it on the list.
 * 
 * @author Steven Neiner
 * @parameter baseDir - The location of the exported files
 * @parameter exportFactory - The factory to produce new writers and builders
 */
public class EpicExportManager {
    // private List<EpicExportWriter> writers;
    // private List<AbstractEpicLoadFileBuilder> builders;
    private String baseDir;
    private EpicLoadFileFactory exportFactory;
    private HashMap<String, I_EpicExportRecordWriter> writers = new HashMap<String, I_EpicExportRecordWriter>();
    private HashMap<String, I_EpicLoadFileBuilder> builders = new HashMap<String, I_EpicLoadFileBuilder>();
    public static final String EPIC_MASTERFILE_NAME_EDG_CLINICAL = "edgclinical";
    public static final String EPIC_MASTERFILE_NAME_EDG_BILLING = "edgbilling";

    public EpicExportManager(String baseDir, EpicLoadFileFactory exportFactory) {
        this.baseDir = baseDir;
        if (!this.baseDir.endsWith("/")) {
            this.baseDir = this.baseDir.concat("/");
        }
        this.exportFactory = exportFactory;

    }

    /**
     * Looks for an existing load file writer that handles the named record
     * type,
     * and creates a new writer if not present.
     * 
     * @param writerName - The name of the writer to look for
     * @return The writer that handles the requested record type
     * @throws IOException
     */
    public I_EpicExportRecordWriter getWriter(String writerName) throws IOException {
        I_EpicExportRecordWriter ret = null;
        ret = writers.get(writerName);
        if (ret == null) {
            StringBuffer filename = new StringBuffer(this.baseDir);
            filename.append(writerName);
            filename.append(".txt");
            System.out.println("Creating " + filename.toString());
            File fw = new File(filename.toString());
            fw.mkdirs();
            if (fw.exists())
                fw.delete();
            ret = exportFactory.getWriter(fw);
            writers.put(writerName, ret);
        }
        return ret;
    }

    public I_EpicLoadFileBuilder getLoadFileBuilder(String masterfile) throws Exception {
        I_EpicLoadFileBuilder ret = builders.get(masterfile);
        if (ret == null) {
            ret = this.exportFactory.getLoadFileBuilder(masterfile, this);
            if (ret != null)
                builders.put(masterfile, ret);
        }
        return ret;
    }

    public void close() throws IOException {

        for (Iterator<I_EpicExportRecordWriter> i = writers.values().iterator(); i.hasNext();) {
            I_EpicExportRecordWriter w = i.next();
            System.out.println(w.getSummary());
            w.close();
        }
    }

    public boolean isEqualOrBothNull(String c1, String c2) {
        boolean ret = false;
        if (c1 == null && c2 == null)
            ret = true;
        else if (c1 != null && c2 != null)
            ret = c1.compareToIgnoreCase(c2) == 0;
        return ret;
    }
}
