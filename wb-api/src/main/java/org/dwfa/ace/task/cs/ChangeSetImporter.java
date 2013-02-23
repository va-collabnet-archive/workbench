/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public abstract class ChangeSetImporter implements ActionListener {

    private boolean continueImport = true;
    private static boolean commitAfterImport = false;

    @Override
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
        try {
            long start = System.currentTimeMillis();
            I_TermFactory tf = Terms.get();
            I_ShowActivity activity = tf.newActivityPanel(true, tf.getActiveAceFrameConfig(),
                    "Importing " + suffix + " change sets. ", false);
            activity.setIndeterminate(true);
            activity.addRefreshActionListener(this);
            String[] validatorArray = new String[]{};

            if (validators != null && !"".equals(validators)) {
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
                StringBuilder sb = new StringBuilder();
                sb.append("Adding reader nextCommitTime: ");
                sb.append(csr.nextCommitTime());
                sb.append(", ");
                sb.append((new Date(csr.nextCommitTime())).toString());
                sb.append(", ");
                sb.append(csf.getAbsolutePath());
                AceLog.getEditLog().info(sb.toString());
            }

            List<File> importedFileList = new LinkedList<File>();
            int max = avaibleBytes(readerSet);
            activity.setMaximum(max);
            activity.setValue(0);
            activity.setIndeterminate(false);
            Set<ConceptChronicleBI> annotationIndexes = new HashSet<ConceptChronicleBI>();
            while (readerSet.size() > 0 && continueImport) {
                activity.setValue(max - avaibleBytes(readerSet));
                activity.setProgressInfoLower(readerSet.first().getChangeSetFile().getName());

                File potentialImportCSFile = readerSet.first().getChangeSetFile();
                if (readNext(readerSet, annotationIndexes)) {
                    importedFileList.add(potentialImportCSFile);
                }
            }
            
            for (ConceptChronicleBI annotatedIndex: annotationIndexes) {
                Ts.get().addUncommittedNoChecks(annotatedIndex);
            }
            if (commitAfterImport) {
                Terms.get().commit();
            }

            annotationIndexes.clear();
            
            if (WorkflowHelper.isWorkflowCapabilityAvailable()) {
                createWfHxLuceneIndex(activity, importedFileList, annotationIndexes);
            }
            for (ConceptChronicleBI annotatedIndex: annotationIndexes) {
                Ts.get().addUncommittedNoChecks(annotatedIndex);
            }
            if (commitAfterImport) {
                Terms.get().commit();
            }


            activity.setIndeterminate(false);
            long elapsed = System.currentTimeMillis() - start;
            String elapsedString = TimeUtil.getElapsedTimeString(elapsed);
            activity.setProgressInfoLower(elapsedString);
            activity.complete();
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private void createWfHxLuceneIndex(I_ShowActivity activity, List<File> changeSetFiles, 
            Set<ConceptChronicleBI> indexedAnnotations) throws IOException, ClassNotFoundException {
        if (changeSetFiles.isEmpty()) {
            if (AceLog.getEditLog().isLoggable(Level.INFO)) {
                AceLog.getEditLog().info("Workflow history lucene index already updated with all changes");
            }
        } else {
            TreeSet<I_ReadChangeSet> wfHxReaderSet = getSortedReaderSet();
            for (File csf : changeSetFiles) {
                I_ReadChangeSet wcsr = getChangeSetWfHxReader(csf);
                wfHxReaderSet.add(wcsr);
            }

            I_ReadChangeSet firstFile = wfHxReaderSet.first();

            if (AceLog.getEditLog().isLoggable(Level.INFO)) {
                AceLog.getEditLog().info("Importing for updating workflow history lucene index");
            }

            // Read all changesets
            activity.setValue(0);
            int filesToImport = wfHxReaderSet.size();
            int counter = 0;
            while (wfHxReaderSet.size() > 0 && continueImport) {
                activity.setValue(filesToImport - counter++);
                readNext(wfHxReaderSet, indexedAnnotations);
            }

            if (AceLog.getEditLog().isLoggable(Level.INFO)) {
                AceLog.getEditLog().info("Processing imported change sets to generate index");
            }

            // Send first change set file again to signify that done importing, and time to process Lucene Index
            if (firstFile != null) {
                TreeSet<I_ReadChangeSet> finalizeWfHxLuceneIndexReaderSet = getSortedReaderSet();
                finalizeWfHxLuceneIndexReaderSet.add(firstFile);

                readNext(finalizeWfHxLuceneIndexReaderSet, indexedAnnotations);
            }

            if (AceLog.getEditLog().isLoggable(Level.INFO)) {
                AceLog.getEditLog().info("Update of workflow history lucene index complete");
            }
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

    public abstract I_ReadChangeSet getChangeSetWfHxReader(File csf);

    public static TreeSet<I_ReadChangeSet> getSortedReaderSet() {
        TreeSet<I_ReadChangeSet> readerSet = new TreeSet<I_ReadChangeSet>(new Comparator<I_ReadChangeSet>() {

            @Override
            public int compare(I_ReadChangeSet r1, I_ReadChangeSet r2) {
                try {
                    if (r1.nextCommitTime() == r2.nextCommitTime()) {
                        if (r1.getChangeSetFile() == null && r2.getChangeSetFile() == null) {
                            return 0;
                        }
                        if (r1.getChangeSetFile() == null) {
                            return 1;
                        }
                        if (r2.getChangeSetFile() == null) {
                            return -1;
                        }
                        return r1.getChangeSetFile().toURI().toURL().toString().compareTo(
                                r2.getChangeSetFile().toURI().toURL().toString());
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

    public static boolean readNext(TreeSet<I_ReadChangeSet> readerSet, Set<ConceptChronicleBI> indexedAnnotations) throws IOException, ClassNotFoundException {
        if (readerSet.isEmpty()) {
            return false;
        }
        I_TermFactory tf = Terms.get();
        if (tf.getTransactional()) {
            tf.startTransaction();
        }
        I_ReadChangeSet first = readerSet.first();
        readerSet.remove(first);
        if (AceLog.getEditLog().isLoggable(Level.INFO)) {
            if (first.getChangeSetFile() != null) {
                AceLog.getEditLog().info(
                        "\n--------------------------\nNow reading change set: " + first.getChangeSetFile().getName() + "; "
                        + new Date(first.nextCommitTime()) + "; available bytes: " + first.availableBytes() + " ("
                        + readerSet.size() + " readers left)" + "\n--------------------------\n ");
            }
        }
        Long nextCommitTime = null;
        for (I_ReadChangeSet reader : readerSet) {
            if (reader.nextCommitTime() > first.nextCommitTime()) {
                nextCommitTime = reader.nextCommitTime();
                break;
            }
        }

        if (nextCommitTime == null) {
            first.readUntil(Long.MAX_VALUE, indexedAnnotations);
        } else {
            first.readUntil(nextCommitTime, indexedAnnotations);
        }
        if (first.nextCommitTime() == Long.MAX_VALUE) {
            if (first.getChangeSetFile() != null) {
                AceLog.getEditLog().info(
                    "\nFinished reader: " + first.getChangeSetFile().getName() + " (" + readerSet.size()
                    + " readers left)\n");
            }
            // don't add back since it is complete.
        } else {
            if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                if (first.getChangeSetFile() != null) {
                AceLog.getEditLog().fine(
                        "Adding back reader: " + first.getChangeSetFile().getName() + "\nThis has nextCommitTime() of : "
                        + first.nextCommitTime() + " (" + new Date(first.nextCommitTime()) + ")");
                }
            }
            readerSet.add(first);
        }
        if (tf.getTransactional()) {
            tf.commitTransaction();
        }

        return first.isContentMerged();
    }

    public static void addAllChangeSetFiles(File rootFile, List<File> changeSetFiles, final String suffix) {
        addAllChangeSetFiles(rootFile, changeSetFiles, suffix, null);
    }

    public static void addAllChangeSetFiles(File rootFile, List<File> changeSetFiles, final String suffix,
            final String prefix) {
        File[] children = rootFile.listFiles(new FileFilter() {

            @Override
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
