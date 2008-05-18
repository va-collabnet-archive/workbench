package org.dwfa.ace.task.cmrscs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/cmrscs", type = BeanType.TASK_BEAN) })
public class ImportCmrscs extends AbstractTask {

    private String rootDirStr = "profiles";

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(rootDirStr);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
                                                                 ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            rootDirStr = (String) in.readObject();
         } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
    throws TaskFailedException {

        LocalVersionedTerminology.get().suspendChangeSetWriters();

        importAllChangeSets(worker.getLogger());

        LocalVersionedTerminology.get().resumeChangeSetWriters();

        return Condition.CONTINUE;
    }

    @SuppressWarnings("unchecked")
	public void importAllChangeSets(Logger logger) throws TaskFailedException {
        try {
        	
            File rootFile = new File(rootDirStr);
            List<File> changeSetFiles = new ArrayList<File>();
            addAllChangeSetFiles(rootFile, changeSetFiles);
            TreeSet<I_ReadChangeSet> readerSet = getSortedReaderSet();
            for (File csf : changeSetFiles) {
                I_ReadChangeSet csr = LocalVersionedTerminology.get()
                .newBinaryChangeSetReader(csf);
                
                readerSet.add(csr);

                logger.info("Adding reader: " + csf.getAbsolutePath());
            }
            while (readerSet.size() > 0) {
                readNext(readerSet);
            }

            LocalVersionedTerminology.get().commit();

        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (ClassNotFoundException e) {
            throw new TaskFailedException(e);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

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
        I_ReadChangeSet first = readerSet.first();
        readerSet.remove(first);
        I_ReadChangeSet next = null;
        if (readerSet.size() > 0) {
            next = readerSet.first();
        }

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


    }

    public static void addAllChangeSetFiles(File rootFile, List<File> changeSetFiles) {
        File[] children = rootFile.listFiles(new FileFilter() {

            public boolean accept(File child) {
                if (child.isHidden() || child.getName().startsWith(".")) {
                    return false;
                }
                if (child.isDirectory()) {
                    return true;
                }
                return child.getName().endsWith(".cmrscs");
            }
        });
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    addAllChangeSetFiles(child, changeSetFiles);
                } else {
                    changeSetFiles.add(child);
                }
            }
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
        throws TaskFailedException {
        // Nothing to do.

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * @return Returns the message.
     */
    public String getRootDirStr() {
        return rootDirStr;
    }

    /**
     * @param message
     *            The message to set.
     */
    public void setRootDirStr(String rootDirStr) {
        this.rootDirStr = rootDirStr;
    }
}