package org.dwfa.ace.task.svn;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.io.FileIO;

@BeanList(specs = { @Spec(directory = "tasks/ace/svn", type = BeanType.TASK_BEAN) })
public class AddSubversionEntryFromDirectory extends AddSubversionEntry {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private transient TaskFailedException ex = null;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion != dataVersion) {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
		ex = null;
	}

    @Override
	protected void addUserInfo(I_EncodeBusinessProcess process, final I_ConfigAceFrame config)
			throws TaskFailedException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					try {
					JFileChooser chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setDialogTitle("Select subversion working directory:");
					chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
					int returnVal = chooser.showDialog(new JFrame(),
							"Select Directory");
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File svnDirectory = chooser.getSelectedFile();
						String workingCopy = FileIO.getRelativePath(svnDirectory);
						SubversionData svd = new SubversionData(null, workingCopy);
						config.svnCompleteRepoInfo(svd);
						AddSubversionEntryFromDirectory.this.setWorkingCopy(workingCopy);
						AddSubversionEntryFromDirectory.this.setRepoUrl(svd.getRepositoryUrlStr());
						AddSubversionEntryFromDirectory.this.setKeyName(workingCopy);
					} else {
						ex = new TaskFailedException("User canceled operation");
					}
				} catch (TaskFailedException e) {
					ex = e;
				}
				}
			});
		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		}
		if (ex != null) {
			throw ex;
		}
	}
}
