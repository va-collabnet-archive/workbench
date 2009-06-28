package org.dwfa.fd;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFrame;

import org.dwfa.bpa.process.TaskFailedException;

public class FileDialogUtil {
	public static File getNewFile(String frameTitle, File defaultFile) throws TaskFailedException {
		return getNewFile(frameTitle, defaultFile, null);
	}
	
	public static File getNewFile(String frameTitle, File defaultFile, Frame parentFrame) throws TaskFailedException {
		// Create a file dialog box to prompt for a new file to display
		if (parentFrame == null) {
			parentFrame = new JFrame();
		}
		FileDialog f = new FileDialog(parentFrame, frameTitle,
				FileDialog.SAVE);
		if (defaultFile != null) {
			f.setDirectory(defaultFile.getParent());
			f.setFile(defaultFile.getName());
		} else {
			f.setDirectory(System.getProperty("user.dir"));
		}
		f.setVisible(true); // Display dialog and wait for response
		if (f.getFile() != null) {
			File directory = new File(f.getDirectory(), f.getFile());
			f.dispose(); // Get rid of the dialog box
			return directory;
		}
		f.dispose(); // Get rid of the dialog box
		throw new TaskFailedException("User canceled operation. ");
		
	}
	
	public static File getNewFile(String frameTitle) throws TaskFailedException {
		return getNewFile(frameTitle, null);
	}
	
	public static File getExistingFile(String frameTitle) throws TaskFailedException {
		return getExistingFile(frameTitle, null);
	}
	public static File getExistingFile(String frameTitle, FilenameFilter filter) throws TaskFailedException {
		return getExistingFile(frameTitle, filter, null);
	}

	public static File getExistingFile(String frameTitle, FilenameFilter filter, File defaultDirectory) throws TaskFailedException {
		// Create a file dialog box to prompt for a new file to display
		return getExistingFile(frameTitle, filter, defaultDirectory, null);
	}

	public static File getExistingFile(String frameTitle, FilenameFilter filter, File defaultDirectory, Frame parentFrame) throws TaskFailedException {
		// Create a file dialog box to prompt for a new file to display
		FileDialog f = new FileDialog(parentFrame, frameTitle,
				FileDialog.LOAD);
		if (defaultDirectory != null) {
			f.setDirectory(defaultDirectory.getAbsolutePath());
		} else {
			f.setDirectory(System.getProperty("user.dir"));
		}
		if (filter != null) {
			f.setFilenameFilter(filter);
		}
		
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