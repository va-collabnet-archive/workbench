/*
 * Created on Apr 19, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui;

import java.io.File;

class FileWrapper {
    File file;

    /**
     * @param file
     */
    public FileWrapper(File file) {
        super();
        this.file = file;
    }

    /**
     * @return Returns the file.
     */
    public File getFile() {
        return file;
    }

    public String toString() {
        return this.file.getName();
    }
}