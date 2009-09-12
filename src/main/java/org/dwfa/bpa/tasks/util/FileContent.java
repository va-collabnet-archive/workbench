package org.dwfa.bpa.tasks.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;

/**
 * Class to store file name and content, to be stored as an attachment.
 */
public class FileContent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String filename;
    private byte[] contents;

    /**
     * Create a new FileContent object.
     * 
     * @param file The file to read in.
     */
    public FileContent(File file) {
        try {
            filename = file.getName();

            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int nextByte = fis.read();
            while (nextByte != -1) {
                baos.write(nextByte);
                nextByte = fis.read();
            }
            // set content
            contents = baos.toByteArray();

            baos.close();
            fis.close();

        } catch (Exception e) {
            // if an error occurs, empty the file contents
            contents = new byte[0];
        }
    }

    /**
     * Returns the filename.
     * 
     * @return The filename.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Returns the content of the file as an array of bytes.
     * 
     * @return The content of the file as an array of bytes.
     */
    public byte[] getContents() {
        return contents;
    }
}
