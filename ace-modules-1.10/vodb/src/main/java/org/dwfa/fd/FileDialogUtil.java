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
package org.dwfa.fd;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;

import org.dwfa.bpa.process.TaskFailedException;

public class FileDialogUtil {

    public static File getNewFile(String frameTitle, File defaultFile, Frame parentFrame) throws TaskFailedException {
        // Create a file dialog box to prompt for a new file to display
        FileDialog f = new FileDialog(parentFrame, frameTitle, FileDialog.SAVE);
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

    public static File getExistingFile(String frameTitle, FilenameFilter filter, File defaultDirectory,
            Frame parentFrame) throws TaskFailedException {
        // Create a file dialog box to prompt for a new file to display
        FileDialog f = new FileDialog(parentFrame, frameTitle, FileDialog.LOAD);
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
