package org.dwfa.util.io;

import java.awt.FileDialog;
import java.awt.Frame;
import java.beans.ExceptionListener;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
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
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

/**
 * From the book "Java Cookbook, 2nd Edition. Some simple file IO primitives
 * reimplemented in Java. All methods are static since there is no state.
 */
public class FileIO {

    public static void copyFile(File in, File out) throws IOException {
        FileChannel sourceChannel = new FileInputStream(in).getChannel();
        FileChannel destinationChannel = new FileOutputStream(out).getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        sourceChannel.close();
        destinationChannel.close();
    }

    public static class FileAndObject {
        private Object obj;

        private File file;

        public FileAndObject(Object obj, File file) {
            super();
            this.obj = obj;
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public Object getObj() {
            return obj;
        }

    }

    /** Copy a file from one filename to another */
    public static void copyFile(String inName, String outName) throws FileNotFoundException, IOException {
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(inName));
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outName));
        copyFile(is, os, true);
    }

    /** Copy a file from an opened InputStream to an opened OutputStream */
    public static void copyFile(InputStream is, OutputStream os, boolean close) throws IOException {
        int b; // the byte read from the file
        while ((b = is.read()) != -1) {
            os.write(b);
        }
        is.close();
        if (close)
            os.close();
    }

    /** Copy a file from an opened Reader to an opened Writer */
    public static void copyFile(Reader is, Writer os, boolean close) throws IOException {
        int b; // the byte read from the file
        while ((b = is.read()) != -1) {
            os.write(b);
        }
        is.close();
        if (close)
            os.close();
    }

    /** Copy a file from a filename to a PrintWriter. */
    public static void copyFile(String inName, PrintWriter pw, boolean close) throws FileNotFoundException, IOException {
        BufferedReader ir = new BufferedReader(new FileReader(inName));
        copyFile(ir, pw, close);
    }

    /** Open a file and read the first line from it. */
    public static String readLine(String inName) throws FileNotFoundException, IOException {
        BufferedReader is = new BufferedReader(new FileReader(inName));
        String line = null;
        line = is.readLine();
        is.close();
        return line;
    }

    /** The size of blocking to use */
    protected static final int BLKSIZ = 8192;

    /**
     * Copy a data file from one filename to another, alternate method. As the
     * name suggests, use my own buffer instead of letting the BufferedReader
     * allocate and use the buffer.
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

    /** Read the entire content of a Reader into a String */
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

    /** Read the content of a Stream into a String */
    public static String inputStreamToString(InputStream is) throws IOException {
        return readerToString(new InputStreamReader(is));
    }

    public static FileAndObject getObjFromFilesystem(Frame parent, String title, String startDir,
        FilenameFilter fileFilter) throws IOException, ClassNotFoundException {
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

    public static File writeObjToFilesystem(Frame parent, String title, String startDir, String defaultFile, Object obj)
            throws IOException {
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

    public static File writeObjXmlToFilesystem(Frame parent, String title, String startDir, String defaultFile,
        Object obj, Collection<PersistenceDelegateSpec> delegates, Object owner) throws IOException {
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
     * Removes the /../ sections, by substuting the higher named directory, and
     * then returns the first file in the file system that matches the
     * optionally included regular expression.
     * 
     * @param s
     * @return
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

    private static String normalizeFileString(File f) {
        // return f.getAbsolutePath().replace("/", " ").replace("-", " ");
        String path = f.getAbsolutePath();
        path = path.replace("[\\", "[@");
        path = path.replace('\\', '/');
        path = path.replace("[@", "[\\");
        return path;
    }

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

}