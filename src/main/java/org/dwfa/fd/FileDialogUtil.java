package org.dwfa.fd;

import java.awt.FileDialog;
import java.io.File;

import javax.swing.JFrame;

import org.dwfa.bpa.process.TaskFailedException;

public class FileDialogUtil {
	public static File getNewFile(String frameTitle) throws TaskFailedException {
		// Create a file dialog box to prompt for a new file to display
		FileDialog f = new FileDialog(new JFrame(), frameTitle,
				FileDialog.SAVE);
		f.setDirectory(System.getProperty("user.dir"));
		f.setVisible(true); // Display dialog and wait for response
		if (f.getFile() != null) {
			File directory = new File(f.getDirectory(), f.getFile());
			f.dispose(); // Get rid of the dialog box
			return directory;
		}
		f.dispose(); // Get rid of the dialog box
		throw new TaskFailedException("User canceled operation. ");
	}
	public static File getExistingFile(String frameTitle) throws TaskFailedException {
		// Create a file dialog box to prompt for a new file to display
		FileDialog f = new FileDialog(new JFrame(), frameTitle,
				FileDialog.LOAD);
		f.setDirectory(System.getProperty("user.dir"));
		f.setVisible(true); // Display dialog and wait for response
		if (f.getFile() != null) {
			File directory = new File(f.getDirectory(), f.getFile());
			f.dispose(); // Get rid of the dialog box
			return directory;
		}
		f.dispose(); // Get rid of the dialog box
		throw new TaskFailedException("User canceled operation. ");
	}

}
