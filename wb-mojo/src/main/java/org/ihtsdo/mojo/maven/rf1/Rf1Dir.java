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
package org.ihtsdo.mojo.maven.rf1;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.maven.plugin.MojoFailureException;

public class Rf1Dir {
    private String directoryName;
    private String fileNameContains;

    public static final String SUBSETMEMBER_ID_NAMESPACE_UUID_TYPE1 = "b7d13800-e38d-11df-bccf-0800200c9a66";
    public static final String SUBSETREFSET_ID_NAMESPACE_UUID_TYPE1 = "d0b3c9c0-e395-11df-bccf-0800200c9a66";
    public static final String SUBSETPATH_ID_NAMESPACE_UUID_TYPE1 = "e1cff9e0-e395-11df-bccf-0800200c9a66";

    public static final String HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1 = "22928260-08d8-11e0-81e0-0800200c9a66";

    private static final String FILE_SEPARATOR = File.separator;

    public Rf1Dir() {
        this.directoryName = "";
    }

    public Rf1Dir(String name, String containsStr) {
        this.directoryName = name.replace("/", FILE_SEPARATOR);
        this.fileNameContains = containsStr;
    }

    public String getDirName() {
        return directoryName;
    }

    public String getFNameContains() {
        return fileNameContains;
    }

    public void setDirName(String dirName) {
        this.directoryName = dirName;
    }

    public void setFNameContains(String s) {
        this.fileNameContains = s.replace("/", FILE_SEPARATOR);
    }

    public static List<RF1File> getRf1FileList(String wDir, String subDir, Rf1Dir[] inDirs,
            ArrayList<String> filter, Date dateStart, Date dateStop) throws MojoFailureException {
        
        List<List<RF1File>> dirList = getRf1Files(wDir, subDir, inDirs, filter, dateStart, dateStop);
        
        // SORT FILES INTO DATE ORDER
        List<RF1File> fileList = new ArrayList<RF1File>();
        for (List<RF1File> a : dirList)
            for (RF1File b : a)
                fileList.add(b);
        Collections.sort(fileList);
        
        return fileList;
    }
    
    public static List<List<RF1File>> getRf1Files(String wDir, String subDir, Rf1Dir[] inDirs,
            ArrayList<String> filter, Date dateStart, Date dateStop) throws MojoFailureException {

        List<List<RF1File>> listOfDirs = new ArrayList<List<RF1File>>();
        for (Rf1Dir sctDir : inDirs) {
            ArrayList<RF1File> listOfFiles = new ArrayList<RF1File>();

            File f1 = new File(new File(wDir, subDir), sctDir.getDirName());
            ArrayList<File> fv = new ArrayList<File>();

            for (int i = 0; i < filter.size(); i++)
                filter.set(i, filter.get(i).toUpperCase());
            listFilesRecursive(fv, f1, filter);

            File[] files = new File[0];
            files = fv.toArray(files);
            Arrays.sort(files);

            for (File f2 : files) {
                // ADD RF1File Entry
                String revDate = getFileRevDate(f2);
                if (inDateRange(revDate, dateStart, dateStop)) {
                    RF1File fo = new RF1File(f2, wDir, subDir, revDate, sctDir);
                    listOfFiles.add(fo);
                }
            }

            listOfDirs.add(listOfFiles);
        }
        return listOfDirs;
    }

    private static boolean inDateRange(String revDateStr, Date dateStart, Date dateStop)
            throws MojoFailureException {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date revDate;
        try {
            revDate = formatter.parse(revDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new MojoFailureException(
                    "SimpleDateFormat yyyy-MM-dd HH:mm:ss parse error: revDateStr");
        }

        if (dateStart != null && revDate.compareTo(dateStart) < 0)
            return false; // precedes start date

        if (dateStop != null && revDate.compareTo(dateStop) > 0)
            return false; // after end date

        return true;
    }

    private static String getFileRevDate(File f) throws MojoFailureException {
        int pos;
        // Check file name for date yyyyMMdd
        pos = f.getName().length() - 12; // "yyyyMMdd.txt"
        String s1 = f.getName().substring(pos, pos + 8);
        // normalize date format
        s1 = s1.substring(0, 4) + "-" + s1.substring(4, 6) + "-" + s1.substring(6);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(s1);
        } catch (ParseException pe) {
            s1 = null;
        }

        if (s1 != null) {
            return s1 + " 00:00:00";
        } else {
            throw new MojoFailureException("FAILED: date can not be determined"
                    + " from either file");
        }
    }

    private static void listFilesRecursive(ArrayList<File> list, File root, ArrayList<String> filter) {
        if (root.isFile()) {
            list.add(root);
            return;
        }
        File[] files = root.listFiles();
        Arrays.sort(files);
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName().toUpperCase();

            boolean filterOK = true;
            for (String s : filter)
                if (name.contains(s) == false)
                    filterOK = false;

            if (files[i].isFile() && filterOK) {
                list.add(files[i]);
            }
            if (files[i].isDirectory()) {
                listFilesRecursive(list, files[i], filter);
            }
        }
    }

}
