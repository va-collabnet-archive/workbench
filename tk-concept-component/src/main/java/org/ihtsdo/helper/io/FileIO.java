/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.helper.io;

import java.awt.FileDialog;
import java.awt.Frame;
import java.beans.ExceptionListener;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * From the book "Java Cookbook, 2nd Edition". Some simple file IO primitives
 * reimplemented in Java. All methods are static since there is no state.
 */
public class FileIO {

    /*
     * Comment by Tore
     * I changed the copy File method because on windows platforms the nio
     * transferTo method
     * cannot handle > 64MB files.
     * please see: http://www.rgagnon.com/javadetails/java-0064.html
     */
    /**
     * Copies a file from one location to another.
     *
     * @param in the input file
     * @param out the output file
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void copyFile(File in, File out) throws IOException {
        FileChannel sourceChannel = new FileInputStream(in).getChannel();
        FileChannel destinationChannel = new FileOutputStream(out).getChannel();
        // magic number for Windows, 64Mb - 32Kb)
        int maxCount = (64 * 1024 * 1024) - (32 * 1024);
        long size = sourceChannel.size();
        long position = 0;
        while (position < size) {
            position += sourceChannel.transferTo(position, maxCount, destinationChannel);
        }
        sourceChannel.close();
        destinationChannel.close();
    }

    /**
     * Copies all the files in the directory.
     *
     * @param from the directory to copy from
     * @param to the directory to copy to
     * @param copyInvisibles set to <code>true</code> to copy hidden files
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void recursiveCopy(File from, File to, boolean copyInvisibles) throws IOException {
        if (from.isDirectory()) {
            to.mkdirs();
            for (File f : from.listFiles()) {
                if (f.isHidden() == false || ((copyInvisibles == true) && (f.getName().endsWith(".DS_Store") == false))) {
                    File childTo = new File(to, f.getName());
                    recursiveCopy(f, childTo, copyInvisibles);
                }
            }
        } else {
            copyFile(from, to);
        }
    }

    /**
     * Deletes all the files in the directory.
     *
     * @param from the directory to delete all the files from
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void recursiveDelete(File from) throws IOException {
        if (from.isDirectory()) {
            for (File f : from.listFiles()) {
                recursiveDelete(f);
            }
        }
        from.delete();
    }

    /**
     * The Class FileAndObject associates a
     * <code>File</code> with an
     * <code>Object</code>.
     */
    public static class FileAndObject {

        private Object obj;
        private File file;

        /**
         * Instantiates a new file and object.
         *
         * @param obj the object to associate
         * @param file the file to associate
         */
        public FileAndObject(Object obj, File file) {
            super();
            this.obj = obj;
            this.file = file;
        }

        /**
         * Gets the file associated with this file and object.
         *
         * @return the associated file
         */
        public File getFile() {
            return file;
        }

        /**
         * Gets the object associated with this file and object.
         *
         * @return the associated object
         */
        public Object getObj() {
            return obj;
        }
    }

    /**
     * Copies a file from one filename to another.
     *
     * @param inName the input name
     * @param outName the output name
     * @throws FileNotFoundException if a specified file was not found
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void copyFile(String inName, String outName) throws FileNotFoundException, IOException {
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(inName));
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outName));
        copyFile(is, os, true);
    }

    /**
     * Copies a file from an opened input stream to a file location.
     *
     * @param is the input stream
     * @param outFile the output file
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void copyFile(InputStream is, File outFile) throws IOException {
        outFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(outFile);
        copyFile(is, fos, true);
    }

    /**
     * Copy a file from an opened InputStream to an opened OutputStream.
     *
     * @param is the input stream
     * @param os the output sream
     * @param close set to <code>true</code> to close the output stream
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void copyFile(InputStream is, OutputStream os, boolean close) throws IOException {
        byte[] buffer = new byte[4096];
        int length;
        int bytesWritten = 0;
        while (is.available() > 0) {
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
                bytesWritten = bytesWritten + length;
            }
        }
        is.close();
        if (close) {
            os.close();
        }
    }

    /**
     * Copy a file from an opened Reader to an opened Writer.
     *
     * @param is the file reader
     * @param os the output writer
     * @param close set to <code>true</code> to close the writer
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void copyFile(Reader is, Writer os, boolean close) throws IOException {
        int b; // the byte read from the file
        while ((b = is.read()) != -1) {
            os.write(b);
        }
        is.close();
        if (close) {
            os.close();
        }
    }

    /**
     * Copy a file from a filename to a PrintWriter.
     *
     * @param inName the filename
     * @param pw the print writer
     * @param close set to <code>true</code> to close the print writer
     * @throws FileNotFoundException if a specified file was not found
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void copyFile(String inName, PrintWriter pw, boolean close) throws FileNotFoundException, IOException {
        BufferedReader ir = new BufferedReader(new FileReader(inName));
        copyFile(ir, pw, close);
    }

    /**
     * Open a file and read the first line from it.
     *
     * @param inName the input file name
     * @return a string representing the first line of the file
     * @throws FileNotFoundException if a specified file was not found
     * @throws IOException signals that an I/O exception has occurred
     */
    public static String readLine(String inName) throws FileNotFoundException, IOException {
        BufferedReader is = new BufferedReader(new FileReader(inName));
        String line = null;
        line = is.readLine();
        is.close();
        return line;
    }
    /**
     * The size of blocking to use.
     */
    protected static final int BLKSIZ = 8192;

    /**
     * Copy a data file from one filename to another, alternate method. As the
     * name suggests, use my own buffer instead of letting the BufferedReader
     * allocate and use the buffer.
     *
     * @param inName the input file name
     * @param outName the output file name
     * @throws FileNotFoundException if a specified file was not found
     * @throws IOException signals that an I/O exception has occurred
     */
    public void copyFileBuffered(String inName, String outName) throws FileNotFoundException, IOException {
        InputStream is = new FileInputStream(inName);
        OutputStream os = new FileOutputStream(outName);
        int count = 0; // the byte count
        byte[] b = new byte[BLKSIZ]; // the bytes read from the file
        while ((count = is.read(b)) != -1) {
            os.write(b, 0, count);
        }
        is.close();
        os.close();
    }

    /**
     * Read the entire content of a Reader into a String.
     *
     * @param is the input reader
     * @return the content of the reader represented as a string
     * @throws IOException signals that an I/O exception has occurred
     */
    public static String readerToString(Reader is) throws IOException {
        StringBuffer sb = new StringBuffer();
        char[] b = new char[BLKSIZ];
        int n;

        // Read a block. If it gets any chars, append them.
        while ((n = is.read(b)) > 0) {
            sb.append(b, 0, n);
        }

        // Only construct the String object once, here.
        return sb.toString();
    }

    /**
     * Read the content of a Stream into a String.
     *
     * @param is the input stream
     * @return the content of the input stream represented as a string
     * @throws IOException signals that an I/O exception has occurred
     */
    public static String inputStreamToString(InputStream is) throws IOException {
        return readerToString(new InputStreamReader(is));
    }

    /**
     * The Class FileAndObjectResult associates a
     * <code>FileAndObject</code> with a result.
     */
    public static class FileAndObjectResult {

        private FileAndObject returnValue;
        private Exception ex;

        /**
         * Gets the file and object.
         *
         * @return the file and object
         */
        public FileAndObject getReturnValue() {
            return returnValue;
        }

        /**
         * Sets the file and object to return.
         *
         * @param returnValue the file and object to return
         */
        public void setReturnValue(FileAndObject returnValue) {
            this.returnValue = returnValue;
        }

        /**
         * Gets the exception associated with this file and object.
         *
         * @return the associated exception
         */
        public Exception getEx() {
            return ex;
        }

        /**
         * Sets the exception associated with this file and object.
         *
         * @param ex the exception to be associated
         */
        public void setEx(Exception ex) {
            this.ex = ex;
        }
    }

    /**
     * Gets the file and object from a file system.
     *
     * @param parent the frame to use for presenting the file dialog
     * @param title the title of the file dialog
     * @param startDir the directory to start with for locating files
     * @param fileFilter the filter to apply to the files
     * @return the file and object found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public static FileAndObject getObjFromFilesystem(final Frame parent, final String title, final String startDir,
            final FilenameFilter fileFilter) throws IOException, ClassNotFoundException {
        final FileAndObjectResult returnValue = new FileAndObjectResult();
        if (SwingUtilities.isEventDispatchThread()) {
            returnValue.setReturnValue(getObjFromFilesystemCore(parent, title, startDir, fileFilter));
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            returnValue.setReturnValue(getObjFromFilesystemCore(parent, title, startDir, fileFilter));
                        } catch (FileNotFoundException e) {
                            returnValue.setEx(e);
                        } catch (IOException e) {
                            returnValue.setEx(e);
                        } catch (ClassNotFoundException e) {
                            returnValue.setEx(e);
                        }
                    }
                });
            } catch (InterruptedException e) {
                throw new IOException(returnValue.getEx().getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new IOException(returnValue.getEx().getMessage(), e);
            }
        }
        if (returnValue.getEx() != null) {
            throw new IOException(returnValue.getEx());
        }
        return returnValue.getReturnValue();
    }

    /**
     * Gets the file from the specified location.
     *
     * @param parent the frame to use for presenting the file dialog
     * @param title the title of the file dialog
     * @param startDir the directory to start with for locating files
     * @param fileFilter the filter to apply to the files
     * @return the file found, <code>null</code> if none are found
     */
    public static File getFile(Frame parent, String title, String startDir,
            FilenameFilter fileFilter) {
        if (parent == null) {
            parent = new JFrame();
        }
        FileDialog fd = new FileDialog(parent, title, FileDialog.LOAD);
        fd.setFilenameFilter(fileFilter);
        if (startDir.startsWith("/")) {

            startDir = startDir.replace('/', File.separatorChar);
            fd.setDirectory(startDir);
        } else {
            startDir = startDir.replace('/', File.separatorChar);
            fd.setDirectory(System.getProperty("user.dir") + System.getProperty("file.separator") + startDir);
        }
        fd.setVisible(true); // Display dialog and wait for response
        if (fd.getFile() != null) {
            File f = new File(fd.getDirectory(), fd.getFile());
            if (f.exists()) {
                return f;
            }
            File d = new File(fd.getDirectory());
            for (File child : d.listFiles()) {
                if (child.getName().startsWith(fd.getFile())) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * Gets the file and object from file system core.
     *
     * @param parent the frame to use for presenting the file dialog
     * @param title the title of the file dialog
     * @param startDir the directory to start with for locating files
     * @param fileFilter the filter to apply to the files
     * @return the file and object found
     * @throws FileNotFoundException if a specified file was not found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    private static FileAndObject getObjFromFilesystemCore(Frame parent, String title, String startDir,
            FilenameFilter fileFilter) throws FileNotFoundException, IOException, ClassNotFoundException {
        if (parent == null) {
            parent = new JFrame();
        }
        FileDialog fd = new FileDialog(parent, title, FileDialog.LOAD);
        fd.setFilenameFilter(fileFilter);
        if (startDir.startsWith("/")) {

            startDir = startDir.replace('/', File.separatorChar);
            fd.setDirectory(startDir);
        } else {
            startDir = startDir.replace('/', File.separatorChar);
            fd.setDirectory(System.getProperty("user.dir") + System.getProperty("file.separator") + startDir);
        }
        fd.setVisible(true); // Display dialog and wait for response
        if (fd.getFile() != null) {
            File objFile = new File(fd.getDirectory(), fd.getFile());
            FileInputStream fis = new FileInputStream(objFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object obj = ois.readObject();
            ois.close();
            return new FileAndObject(obj, objFile);
        }
        throw new IOException("User did not select a file");
    }

    /**
     * The Class FileResult associates a
     * <code>Exception</code> with an
     * <code>Exception</code>.
     */
    public static class FileResult {

        
        private File returnValue;
        
        private Exception ex;

        /**
         * Gets the file associated with this file and result.
         *
         * @return the associated file
         */
        public File getReturnValue() {
            return returnValue;
        }

        /**
         * Sets the file to be returned.
         *
         * @param returnValue the file to associate
         */
        public void setReturnValue(File returnValue) {
            this.returnValue = returnValue;
        }

        /**
         * Gets the exception associated with this file and result.
         *
         * @return the associated exception
         */
        public Exception getEx() {
            return ex;
        }

        /**
         * Sets the exception associated with this file and result.
         *
         * @param ex the exception to associate
         */
        public void setEx(Exception ex) {
            this.ex = ex;
        }
    }

    /**
     * Writes an object to file system.
     *
     * @param parent the frame to use for presenting the file dialog
     * @param title the title of the file dialog
     * @param startDir the directory to start with for locating files
     * @param defaultFile the default file for the file dialog
     * @param obj the object to write
     * @return the written file
     * @throws IOException signals that an I/O exception has occurred
     */
    public static File writeObjToFilesystem(final Frame parent, final String title, final String startDir,
            final String defaultFile, final Object obj) throws IOException {
        final FileResult returnValue = new FileResult();
        if (SwingUtilities.isEventDispatchThread()) {
            returnValue.setReturnValue(writeObjeToFilesystemCore(parent, title, startDir, defaultFile, obj));
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            returnValue.setReturnValue(writeObjeToFilesystemCore(parent, title, startDir, defaultFile,
                                    obj));
                        } catch (FileNotFoundException e) {
                            returnValue.setEx(e);
                        } catch (IOException e) {
                            returnValue.setEx(e);
                        }
                    }
                });
            } catch (InterruptedException e) {
                throw new IOException(returnValue.getEx().getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new IOException(returnValue.getEx().getMessage(), e);
            }
        }
        if (returnValue.getEx() != null) {
            throw new IOException(returnValue.getEx());
        }
        return returnValue.getReturnValue();
    }

    /**
     * Writes an object to file system.
     *
     * @param parent the frame to use for presenting the file dialog
     * @param title the title of the file dialog
     * @param startDir the directory to start with for locating files
     * @param defaultFile the default file for the file dialog
     * @param obj the object to write
     * @return the written file
     * @throws FileNotFoundException if a specified file was not found
     * @throws IOException signals that an I/O exception has occurred
     */
    private static File writeObjeToFilesystemCore(Frame parent, String title, String startDir, String defaultFile,
            Object obj) throws FileNotFoundException, IOException {
        if (parent == null) {
            parent = new JFrame();
        }
        FileDialog fd = new FileDialog(parent, title, FileDialog.SAVE);
        fd.setDirectory(startDir);
        fd.setFile(defaultFile);
        fd.setVisible(true); // Display dialog and wait for response
        if (fd.getFile() != null) {
            File objFile = new File(fd.getDirectory(), fd.getFile());
            FileOutputStream fos = new FileOutputStream(objFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
            return objFile;
        } else {
            throw new IOException("User canceled save operation");
        }
    }

    /**
     * Writes an xml object to a file system.
     *
     * @param parent the frame to use for presenting the file dialog
     * @param title the title of the file dialog
     * @param startDir the directory to start with for locating files
     * @param defaultFile the default file for the file dialog
     * @param obj the object to write
     * @param delegates the delegates associated with the xml object
     * @param owner the owner associated with the xml object
     * @return the written file
     * @throws IOException signals that an I/O exception has occurred
     */
    public static File writeObjXmlToFilesystem(final Frame parent, final String title, final String startDir,
            final String defaultFile, final Object obj, final Collection<PersistenceDelegateSpec> delegates,
            final Object owner) throws IOException {
        final FileResult returnValue = new FileResult();
        if (SwingUtilities.isEventDispatchThread()) {
            returnValue.setReturnValue(writeObjXmlToFilesystemCore(parent, title, startDir, defaultFile, obj,
                    delegates, owner));
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            returnValue.setReturnValue(writeObjXmlToFilesystemCore(parent, title, startDir,
                                    defaultFile, obj, delegates, owner));
                        } catch (FileNotFoundException e) {
                            returnValue.setEx(e);
                        } catch (IOException e) {
                            returnValue.setEx(e);
                        }
                    }
                });
            } catch (InterruptedException e) {
                throw new IOException(returnValue.getEx().getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new IOException(returnValue.getEx().getMessage(), e);
            }
        }
        if (returnValue.getEx() != null) {
            throw new IOException(returnValue.getEx());
        }
        return returnValue.getReturnValue();

    }

    /**
     * Writes an xml object to a file system.
     *
     * @param parent the frame to use for presenting the file dialog
     * @param title the title of the file dialog
     * @param startDir the directory to start with for locating files
     * @param defaultFile the default file for the file dialog
     * @param obj the object to write
     * @param delegates the delegates associated with the xml object
     * @param owner the owner associated with the xml object
     * @return the written file
     * @throws FileNotFoundException if a specified file was not found
     * @throws IOException signals that an I/O exception has occurred
     */
    private static File writeObjXmlToFilesystemCore(Frame parent, String title, String startDir, String defaultFile,
            Object obj, Collection<PersistenceDelegateSpec> delegates, Object owner) throws FileNotFoundException,
            IOException {
        if (parent == null) {
            parent = new JFrame();
        }
        FileDialog fd = new FileDialog(parent, title, FileDialog.SAVE);
        fd.setDirectory(startDir);
        fd.setFile(defaultFile);
        fd.setVisible(true); // Display dialog and wait for response
        if (fd.getFile() != null) {
            File objFile = new File(fd.getDirectory(), fd.getFile());
            FileOutputStream fos = new FileOutputStream(objFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            XMLEncoder encoder = new XMLEncoder(bos);
            encoder.setExceptionListener(new ExceptionListener() {
                @Override
                public void exceptionThrown(Exception exception) {
                    exception.printStackTrace();
                }
            });
            if (delegates != null) {
                for (PersistenceDelegateSpec spec : delegates) {
                    encoder.setPersistenceDelegate(spec.getType(), spec.getPersistenceDelegate());
                }
            }
            if (owner != null) {
                encoder.setOwner(owner);
            }
            encoder.writeObject(obj);
            encoder.close();
            return objFile;
        } else {
            throw new IOException("User canceled save operation");
        }
    }

    /**
     * Accepts a string with regular expressions and possibly /../ portions.
     * Removes the /../ sections, by substituting the higher named directory,
     * and then returns the first file in the file system that matches the
     * optionally included regular expression.
     *
     * @param s the string to normalize
     * @return the found file
     */
    public static File normalizeFileStr(String s) {
        // System.out.println("s" + s);
        int slashDotIndex = s.indexOf("/../");
        while (slashDotIndex >= 0) {
            String part1 = s.substring(0, slashDotIndex);
            // System.out.println("part1a " + part1);
            part1 = part1.substring(0, part1.lastIndexOf(File.separator));
            // System.out.println("part1b " + part1);
            String part2 = s.substring(slashDotIndex + 3);
            // System.out.println("part2 " + part2);
            s = part1 + part2;
            // System.out.println("s " + s);
            slashDotIndex = s.indexOf("/../");
        }
        s = s.replace('/', File.separatorChar);
        File inputFile = new File(s);
        if (inputFile.exists()) {
            return inputFile;
        }
        // Find an ancestor that exists
        File p = inputFile.getParentFile();
        while (p.exists() == false) {
            p = p.getParentFile();
        }
        // Try regular expression matching...

        // System.out.println("Regex: " + normalizeFileString(inputFile));
        Pattern pattern = Pattern.compile(normalizeFileString(inputFile));

        File f = matchPattern(p, pattern);
        return f;
    }

    /**
     * Gets and string representing the normalized path of a file.
     *
     * @param f the file specifying the path
     * @return the string representation of the path
     */
    private static String normalizeFileString(File f) {
        // return f.getAbsolutePath().replace("/", " ").replace("-", " ");
        String path = f.getAbsolutePath();
        path = path.replace("[\\", "[@");
        path = path.replace('\\', '/');
        path = path.replace("[@", "[\\");
        return path;
    }

    /**
     * Returns a file found in the specified directory based on the given <code>pattern</code>.
     *
     * @param p the parent directory
     * @param pattern the pattern representing a regular expression
     * @return a file matching the given pattern, <code>null</code> if none found
     */
    private static File matchPattern(File p, Pattern pattern) {
        for (File f : p.listFiles()) {
            // System.out.println("Current file: " + f.toString());
            // System.out.println("Testing: " + f.getAbsolutePath());
            // System.out.println("Testing normal: " + normalizeFileString(f));
            Matcher m = pattern.matcher(normalizeFileString(f));
            if (m.matches()) {
                return f;
            }
            if (f.isDirectory()) {
                File result = matchPattern(f, pattern);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Returns a String of the relative directory of a file, relative to the
     * <code>user.dir</code> System property.
     *
     * @param f the file to get the relative directory of.
     * @return a String representing the relative directory.
     */
    public static String getRelativePath(File f) {
        File startupDir = new File(System.getProperty("user.dir"));
        String startupDirString = startupDir.getAbsolutePath();
        String fileAbsolutePath = f.getAbsolutePath();
        if (fileAbsolutePath.contains(startupDirString)) {
            return fileAbsolutePath.substring(startupDirString.length() + 1);
        }

        int depth = 1;
        File parent = startupDir.getParentFile();
        while (fileAbsolutePath.contains(parent.getAbsolutePath()) == false) {
            depth++;
            parent = parent.getParentFile();
        }
        StringBuilder relativePath = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            relativePath.append("..").append(File.separator);
        }
        relativePath.append(fileAbsolutePath.substring(parent.getAbsolutePath().length() + 1));
        return relativePath.toString();
    }

    /**
     * Returns a String of the relative directory of a file, relative to the
     * specified <code>directory</code>.
     *
     * @param f the file to get the relative directory of.
     * @param directory the directory
     * @return a String representing the relative directory.
     */
    public static String getPathRelativeToDir(File f, File directory) {
        String startupDirString = directory.getAbsolutePath();
        String fileAbsolutePath = f.getAbsolutePath();
        if (fileAbsolutePath.contains(startupDirString)) {
            return fileAbsolutePath.substring(startupDirString.length() + 1);
        }

        int depth = 1;
        File parent = directory.getParentFile();
        while (fileAbsolutePath.contains(parent.getAbsolutePath()) == false) {
            depth++;
            parent = parent.getParentFile();
        }
        StringBuilder relativePath = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            relativePath.append("..").append(File.separator);
        }
        relativePath.append(fileAbsolutePath.substring(parent.getAbsolutePath().length() + 1));
        return relativePath.toString();
    }

    /**
     * Gets the normalized version of a relative path.
     *
     * @param f the file to use for finding the relative path
     * @return the normalized String representing the relative path
     */
    public static String getNormalizedRelativePath(File f) {
        return getRelativePath(f).replace('\\', '/');
    }

    /**
     * Gets the files within the specified directory. Can filter by the start or end of the file name.
     *
     * @param rootFile the parent directory
     * @param fileList the list to add the found files to
     * @param prefix the prefix of the desired files
     * @param suffix the suffix of the desired file
     * @param excludeHidden set to <code>true</code> to exclude hidden files
     * @return a list of files that were found
     */
    public static List<File> recursiveGetFiles(File rootFile, String prefix, String suffix, boolean excludeHidden) {
        List<File> fileList = new ArrayList<File>();
        recursiveGetFiles(rootFile, fileList, prefix, suffix, excludeHidden);
        return fileList;
    }

    /**
     * Gets the files within the specified directory. Can filter by the start or end of the file name.
     *
     * @param rootFile the parent directory
     * @param fileList the list to add the found files to
     * @param prefix the prefix of the desired files
     * @param suffix the suffix of the desired file
     * @param excludeHidden set to <code>true</code> to exclude hidden files
     */
    private static void recursiveGetFiles(File rootFile, List<File> fileList, final String prefix, final String suffix,
            final boolean excludeHidden) {
        File[] children = rootFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File child) {
                if (excludeHidden) {
                    if (child.isHidden() || child.getName().startsWith(".")) {
                        return false;
                    }
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
                    recursiveGetFiles(child, fileList, prefix, suffix, excludeHidden);
                } else {
                    fileList.add(child);
                }
            }
        }
    }
}
