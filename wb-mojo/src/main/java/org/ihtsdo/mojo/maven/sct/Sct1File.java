package org.ihtsdo.mojo.maven.sct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Sct1File implements Comparable<Object> {
    private static final String TAB_CHARACTER = "\t";

    File file;
    Date revDate;
    long time;
    
    public Sct1File(File f, Date d) {
        this.file = f;
        this.revDate = d;
        this.time = d.getTime();
    }

    public String toString() {
        return file.getPath();
    }

    @Override
    public int compareTo(Object o) {
        Sct1File tmp = (Sct1File) o;
        return revDate.compareTo(tmp.revDate);
    }

    public static int countFileLines(Sct1File rf1) throws IOException {
        int lineCount = 0;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(rf1.file), "UTF-8"));
            try {
                while (br.readLine() != null) {
                    lineCount++;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new IOException("FAILED: error counting lines in " + rf1.file, ex);
            } finally {
                br.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IOException("FAILED: error open BufferedReader for " + rf1.file, ex);
        }

        // lineCount NOTE: COUNT -1 BECAUSE FIRST LINE SKIPPED
        // lineCount NOTE: REQUIRES THAT LAST LINE IS VALID RECORD
        return lineCount - 1;
    }
    
    private static Date getFileRevDate(File f) throws ParseException {
        int pos;
        Date d1 = null;
        Date d2 = null;
        // Check file name for date yyyyMMdd
        // EXAMPLE: ../net/nhs/uktc/ukde/sct1_relationships_uk_drug_20090401.txt
        pos = f.getName().length() - 12; // "yyyyMMdd.txt"
        String s1 = f.getName().substring(pos, pos + 8);
        // normalize date format
        s1 = s1.substring(0, 4) + "-" + s1.substring(4, 6) + "-" + s1.substring(6);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            d1 = dateFormat.parse(s1);
        } catch (ParseException pe) {
            d1 = null;
        }

        // Check path for date yyyy-MM-dd
        // EXAMPLE: ../org/snomed/2003-01-31
        pos = f.getParent().length() - 10; // "yyyy-MM-dd"
        String s2 = f.getParent().substring(pos);
        try {
            d2 = dateFormat.parse(s2);
        } catch (ParseException pe) {
            d2 = null;
        }

        //
        if ((d1 != null) && (d2 != null)) {
            if (d1.equals(d2)) {
                return d1;
            } else {
                throw new ParseException("FAILED: file name date "
                        + "and directory name date do not agree. ", pos);
            }
        } else if (d1 != null) {
            return d1;
        } else if (d2 != null) {
            return d2;
        } else {
            throw new ParseException("FAILED: date can not be determined"
                    + " from either file name date or directory name date.", pos);
        }
    }

    public static List<Sct1File> getSctFiles(String wDir, String subDir, String rootDir,
            String prefix, String postfix) throws ParseException {

        ArrayList<Sct1File> listOfFiles = new ArrayList<Sct1File>();

        File f1 = new File(new File(wDir, subDir), rootDir);
        ArrayList<File> fv = new ArrayList<File>();
        listFilesRecursive(fv, f1, "sct1_" + prefix, postfix);

        File[] files = new File[0];
        files = fv.toArray(files);
        Arrays.sort(files);

        for (File f2 : files) {
            // ADD Sct1File Entry
            Date revDate = Sct1File.getFileRevDate(f2);
            Sct1File fo = new Sct1File(f2, revDate);
            listOfFiles.add(fo);
        }

        Collections.sort(listOfFiles);

        return listOfFiles;
    }

    private static void listFilesRecursive(ArrayList<File> list, File root, String prefix,
            String postfix) {
        if (root.isFile()) {
            list.add(root);
            return;
        }
        File[] files = root.listFiles();
        Arrays.sort(files);
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName().toUpperCase();

            if (files[i].isFile() && name.endsWith(postfix.toUpperCase())
                    && name.startsWith(prefix.toUpperCase())) {
                list.add(files[i]);
            }
            if (files[i].isDirectory()) {
                listFilesRecursive(list, files[i], prefix, postfix);
            }
        }
    }

    public static SctYDesRecord[] parseDescriptions(Sct1File sct1File) throws IOException {

        int count = countFileLines(sct1File);
        SctYDesRecord[] a = new SctYDesRecord[count];

        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(
                sct1File.file), "UTF-8"));
        int descriptions = 0;

        int DESCRIPTIONID = 0;
        int DESCRIPTIONSTATUS = 1;
        int CONCEPTID = 2;
        int TERM = 3;
        int INITIALCAPITALSTATUS = 4;
        int DESCRIPTIONTYPE = 5;
        int LANGUAGECODE = 6;

        // Header row
        r.readLine();

        while (r.ready()) {
            String[] line = r.readLine().split(TAB_CHARACTER);

            // DESCRIPTIONID
            long descriptionId = Long.parseLong(line[DESCRIPTIONID]);
            // DESCRIPTIONSTATUS
            int status = Integer.parseInt(line[DESCRIPTIONSTATUS]);
            // CONCEPTID
            long conSnoId = Long.parseLong(line[CONCEPTID]);
            // TERM
            String text = line[TERM];
            // INITIALCAPITALSTATUS
            int capStatus = Integer.parseInt(line[INITIALCAPITALSTATUS]);
            // DESCRIPTIONTYPE
            int typeInt = Integer.parseInt(line[DESCRIPTIONTYPE]);
            // LANGUAGECODE
            String lang = line[LANGUAGECODE];

            // Save to sortable array
            a[descriptions] = new SctYDesRecord(descriptionId, status, conSnoId, text, capStatus,
                    typeInt, lang);
            descriptions++;

        }
        Arrays.sort(a);
        
        return a;
    }

}
