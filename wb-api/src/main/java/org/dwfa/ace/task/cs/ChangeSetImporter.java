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
package org.dwfa.ace.task.cs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_ValidateChangeSetChanges;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.ihtsdo.time.TimeUtil;

public abstract class ChangeSetImporter implements ActionListener {

    private boolean continueImport = true;

    public void actionPerformed(ActionEvent arg0) {
        continueImport = false;
    }

    public void importAllChangeSets(Logger logger, String validators, String rootDirStr, boolean validateChangeSets,
            String suffix) throws TaskFailedException {
        importAllChangeSets(logger, validators, rootDirStr, validateChangeSets, suffix, null);
    }

    @SuppressWarnings("unchecked")
    public void importAllChangeSets(Logger logger, String validators, String rootDirStr, boolean validateChangeSets,
            String suffix, String prefix) throws TaskFailedException {
    	logger.info("importAllChangeSets called rootDirStr = "+rootDirStr +" suffix = "+suffix+" prefix = "+prefix);
        try {
            long start = System.currentTimeMillis();
            I_TermFactory tf = Terms.get();
            I_ShowActivity activity = tf.newActivityPanel(true, tf.getActiveAceFrameConfig(), 
                "Importing " + suffix + " change sets. ", false);
            activity.setIndeterminate(true);
            activity.addRefreshActionListener(this);
            String[] validatorArray = new String[] {};

            if (validators != null && validators != "") {
                validatorArray = validators.split("'");
            }

            File rootFile = new File(rootDirStr);
            List<File> changeSetFiles = new ArrayList<File>();
            addAllChangeSetFiles(rootFile, changeSetFiles, suffix, prefix);
            TreeSet<I_ReadChangeSet> readerSet = getSortedReaderSet();
            for (File csf : changeSetFiles) {
                I_ReadChangeSet csr = getChangeSetReader(csf);
                if (validateChangeSets == true && validatorArray.length > 0) {
                    for (String validator : validatorArray) {
                        Class<I_ValidateChangeSetChanges> validatorClass = (Class<I_ValidateChangeSetChanges>) Class.forName(validator);
                        csr.getValidators().add(validatorClass.newInstance());
                    }
                }
                readerSet.add(csr);
                logger.info("Adding reader: " + csf.getAbsolutePath() + "\nThis has nextCommitTime() of : "
                    + csr.nextCommitTime() + " (" + new Date(csr.nextCommitTime()) + ")");
            }

            int max = avaibleBytes(readerSet);
            activity.setMaximum(max);
            activity.setValue(0);
            activity.setIndeterminate(false);
            while (readerSet.size() > 0 && continueImport) {
                activity.setValue(max - avaibleBytes(readerSet));
                activity.setProgressInfoLower(readerSet.first().getChangeSetFile().getName());
                readNext(readerSet);
            }
            Terms.get().commit();
            activity.setIndeterminate(false);
            long elapsed = System.currentTimeMillis() - start;
            String elapsedString = TimeUtil.getElapsedTimeString(elapsed);
            activity.setProgressInfoLower(elapsedString);
            activity.complete();
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public int avaibleBytes(TreeSet<I_ReadChangeSet> readerSet) throws FileNotFoundException, IOException,
            ClassNotFoundException {
        int available = 0;
        for (I_ReadChangeSet reader : readerSet) {
            available = available + reader.availableBytes();
        }
        return available;
    }

    public abstract I_ReadChangeSet getChangeSetReader(File csf);

    public static TreeSet<I_ReadChangeSet> getSortedReaderSet() {
        TreeSet<I_ReadChangeSet> readerSet = new TreeSet<I_ReadChangeSet>(new Comparator<I_ReadChangeSet>() {

            public int compare(I_ReadChangeSet r1, I_ReadChangeSet r2) {
                try {
                    if (r1.nextCommitTime() == r2.nextCommitTime()) {
                        return r1.getChangeSetFile().toURL().toString().compareTo(
                            r2.getChangeSetFile().toURL().toString());
                    }
                    if (r1.nextCommitTime() > r2.nextCommitTime()) {
                        return 1;
                    }
                    return -1;
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (ClassNotFoundException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
                return -1;
            }
        });
        return readerSet;
    }

    public static void readNext(TreeSet<I_ReadChangeSet> readerSet) throws IOException, ClassNotFoundException {
        if (readerSet.size() == 0) {
            return;
        }
        I_TermFactory tf = Terms.get();
        if (tf.getTransactional()) {
            tf.startTransaction();
        }
        I_ReadChangeSet first = readerSet.first();
        readerSet.remove(first);
        if (AceLog.getEditLog().isLoggable(Level.INFO)) {
            AceLog.getEditLog().info(
                "\n--------------------------\nNow reading change set: " + first.getChangeSetFile().getName() + "; "
                    + new Date(first.nextCommitTime()) + "; available bytes: " + first.availableBytes() + " ("
                    + readerSet.size() + " readers left)" + "\n--------------------------\n ");
        }
        Long nextCommitTime = null;
        for (I_ReadChangeSet reader : readerSet) {
            if (reader.nextCommitTime() > first.nextCommitTime()) {
                nextCommitTime = reader.nextCommitTime();
                break;
            }
        }

        if (nextCommitTime == null) {
            first.readUntil(Long.MAX_VALUE);
        } else {
            first.readUntil(nextCommitTime);
        }
        if (first.nextCommitTime() == Long.MAX_VALUE) {
            AceLog.getEditLog().info(
                "\nFinished reader: " + first.getChangeSetFile().getName() + " (" + readerSet.size()
                    + " readers left)\n");

            // don't add back since it is complete.
        } else {
            if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                AceLog.getEditLog().fine(
                    "Adding back reader: " + first.getChangeSetFile().getName() + "\nThis has nextCommitTime() of : "
                        + first.nextCommitTime() + " (" + new Date(first.nextCommitTime()) + ")");
            }
            readerSet.add(first);
        }
        if (tf.getTransactional()) {
            tf.commitTransaction();
        }
    }

    public static void addAllChangeSetFiles(File rootFile, List<File> changeSetFiles, final String suffix) {
        addAllChangeSetFiles(rootFile, changeSetFiles, suffix, null);
    }

    public static void addAllChangeSetFiles(File rootFile, List<File> changeSetFiles, final String suffix,
            final String prefix) {
        File[] children = rootFile.listFiles(new FileFilter() {

            public boolean accept(File child) {
                if (child.isHidden() || child.getName().startsWith(".")) {
                    return false;
                }
                if (child.isDirectory()) {
                    return true;
                }
                if (prefix != null && prefix.length() > 1) {
                    return child.getName().endsWith(suffix) && child.getName().startsWith(prefix);
                } else {
                    return child.getName().endsWith(suffix);
                }
            }
        });
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    addAllChangeSetFiles(child, changeSetFiles, suffix, prefix);
                } else {
                    changeSetFiles.add(child);
                }
            }
        }
    }

}
