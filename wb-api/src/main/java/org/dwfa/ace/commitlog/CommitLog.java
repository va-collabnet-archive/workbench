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
package org.dwfa.ace.commitlog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.util.io.FileIO;

public class CommitLog implements I_WriteChangeSet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private File changeSetFile;

    private File tempFile;

    private transient OutputStreamWriter tempOut;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
    private static SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");

    public CommitLog(File changeSetFile, File tempFile) {
        super();
        this.changeSetFile = new File(changeSetFile.getParent(), fileDateFormat.format(new Date()) + "."
            + changeSetFile.getName());
        this.tempFile = tempFile;
    }

    public void commit() throws IOException {
        if (tempOut != null) {
            tempOut.flush();
            tempOut.close();
            tempOut = null;
            String canonicalFileString = tempFile.getCanonicalPath();
            if (tempFile.exists()) {
                try {
                    if (changeSetFile.exists()) {
                        changeSetFile.delete();
                    }
                    if (tempFile.renameTo(changeSetFile) == false) {
                        FileIO.copyFile(tempFile.getCanonicalPath(), changeSetFile.getCanonicalPath());
                    }
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(new Exception("FileIO.copyFile failed in CommitLog."));
                }

                tempFile = new File(canonicalFileString);
                tempFile.delete();
            }
        }
    }

    public void open(I_IntSet commitSapNids) throws IOException {
        if (changeSetFile.exists() == false) {
            changeSetFile.getParentFile().mkdirs();
            changeSetFile.createNewFile();
        }
        FileIO.copyFile(changeSetFile.getCanonicalPath(), tempFile.getCanonicalPath());
        tempOut = new OutputStreamWriter(new FileOutputStream(tempFile, true));
    }

    public void writeChanges(I_GetConceptData change, long time) throws IOException {
        tempOut.append(dateFormat.format(new Date(time)));
		tempOut.append("\tconcept\t");
		tempOut.append(change.getInitialText());
		tempOut.append("\t");
		tempOut.append(change.getUids().toString());
		tempOut.append("\n");
    }

    @Override
    public void setPolicy(ChangeSetPolicy policy) {
        // nothing to do, does not honor policy
    }

}
