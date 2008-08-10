package org.dwfa.ace.task.cs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_ValidateChangeSetChanges;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;

public abstract class ChangeSetImporter implements ActionListener {

    private boolean continueImport = true;

    public void actionPerformed(ActionEvent arg0) {
        continueImport = false;
    }

    @SuppressWarnings("unchecked")
    public void importAllChangeSets(Logger logger, String validators, String rootDirStr, boolean validateChangeSets, String suffix) throws TaskFailedException {
        try {
            I_TermFactory tf = LocalVersionedTerminology.get();
            I_ShowActivity activity = tf.newActivityPanel();
            activity.setProgressInfoUpper("Importing " + suffix + " change sets. ");
            activity.setIndeterminate(true);
            activity.addActionListener(this);
            String[] validatorArray = new String[]{};

            if (validators != null && validators != "") {
                validatorArray = validators.split("'");
            }

            File rootFile = new File(rootDirStr);
            List<File> changeSetFiles = new ArrayList<File>();
            addAllChangeSetFiles(rootFile, changeSetFiles, suffix);
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
                logger.info("Adding reader: " + csf.getAbsolutePath());
                logger.info("This has nextCommitTime() of : " + csr.nextCommitTime());
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
            LocalVersionedTerminology.get().commit();
            activity.setIndeterminate(false);
            activity.complete();
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (ClassNotFoundException e) {
            throw new TaskFailedException(e);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public int avaibleBytes(TreeSet<I_ReadChangeSet> readerSet) throws FileNotFoundException, IOException, ClassNotFoundException {
        int available = 0;
        for (I_ReadChangeSet reader: readerSet) {
            available = available  + reader.availableBytes();
        }
         return available;
    }

    public abstract I_ReadChangeSet getChangeSetReader(File csf);

    public static TreeSet<I_ReadChangeSet> getSortedReaderSet() {
        TreeSet<I_ReadChangeSet> readerSet = new TreeSet<I_ReadChangeSet>(new Comparator<I_ReadChangeSet>() {

            public int compare(I_ReadChangeSet r1, I_ReadChangeSet r2) {
                try {
                    if (r1.nextCommitTime() == r2.nextCommitTime()) {
                        return 0;
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
        I_TermFactory tf = LocalVersionedTerminology.get();
        if (tf.getTransactional()) {
            tf.startTransaction();
        }
        I_ReadChangeSet first = readerSet.first();
        readerSet.remove(first);
        AceLog.getEditLog().info("Now reading change set: " + first.getChangeSetFile().getName());
        I_ReadChangeSet next = null;
        if (readerSet.size() > 0) {
            next = readerSet.first();
        }

        System.out.println("LONG.MAX_VALUE = " + Long.MAX_VALUE);

        if (next == null) {
            first.readUntil(Long.MAX_VALUE);
        } else {
            first.readUntil(next.nextCommitTime());
        }
        if (first.nextCommitTime() == Long.MAX_VALUE) {
            //don't add back since it is complete.
        } else {
            readerSet.add(first);
        }
        if (tf.getTransactional()) {
            tf.commitTransaction();
        }
    }

    public static void addAllChangeSetFiles(File rootFile, List<File> changeSetFiles, final String suffix) {
        File[] children = rootFile.listFiles(new FileFilter() {

            public boolean accept(File child) {
                if (child.isHidden() || child.getName().startsWith(".")) {
                    return false;
                }
                if (child.isDirectory()) {
                    return true;
                }
                return child.getName().endsWith(suffix);
            }
        });
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    addAllChangeSetFiles(child, changeSetFiles, suffix);
                } else {
                    changeSetFiles.add(child);
                }
            }
        }
    }

}
