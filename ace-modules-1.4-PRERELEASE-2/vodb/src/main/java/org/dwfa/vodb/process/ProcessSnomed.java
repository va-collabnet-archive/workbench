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
package org.dwfa.vodb.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessRelationships;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.util.AceDateFormat;
import org.dwfa.vodb.process.ProcessAceFormatSources.FORMAT;
import org.dwfa.vodb.types.IntSet;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public abstract class ProcessSnomed extends ProcessSources {
    int constantDate;

    public ProcessSnomed(int constantDate) throws DatabaseException {
        super(true);
        this.constantDate = constantDate;
    }

    static class MakeRelSet implements I_ProcessRelationships {
        List<Integer> ids = new ArrayList<Integer>();
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

        public MakeRelSet() {
            super();
        }

        public I_IntSet getIntSet() {
            int[] values = new int[ids.size()];
            int index = 0;
            for (Integer id : ids) {
                values[index++] = id;
            }
            Arrays.sort(values);
            return new IntSet(values);
        }

        public void processRelationship(I_RelVersioned rel) throws Exception {
            ids.add(rel.getRelId());
        }
    }

    public abstract void iterateRelationships(MakeRelSet oldRelItr) throws Exception;

    public void execute(File snomedDir) throws Exception {
        MakeRelSet oldRelSet = new MakeRelSet();
        iterateRelationships(oldRelSet);

        DateFormat dateFormat = AceDateFormat.getRf1DirectoryDateFormat();

        for (File releaseDir : snomedDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(".") == false;
            }
        })) {
            getLog().info(releaseDir.getName());
            Date releaseDate = dateFormat.parse(releaseDir.getName());
            addReleaseDate(releaseDate);
            for (File contentFile : releaseDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith("sct");
                }
            })) {
                getLog().info(contentFile.getName());
                FileInputStream fs = new FileInputStream(contentFile);
                InputStreamReader isr = new InputStreamReader(fs, Charset.forName("UTF-8"));

                BufferedReader br = new BufferedReader(isr);
                if (contentFile.getName().startsWith("sct_concepts_")) {
                    readConcepts(br, releaseDate, FORMAT.SNOMED, new CountDownLatch(Integer.MAX_VALUE));
                } else if (contentFile.getName().startsWith("sct_descriptions_")) {
                    readDescriptions(br, releaseDate, FORMAT.SNOMED, new CountDownLatch(Integer.MAX_VALUE));
                } else if (contentFile.getName().startsWith("sct_relationships_")) {
                    readRelationships(br, releaseDate, FORMAT.SNOMED, new CountDownLatch(Integer.MAX_VALUE));
                }
                br.close();
            }
        }

        cleanupSNOMED(oldRelSet.getIntSet());
    }

    boolean processConcepts = true;
    boolean processRels = true;
    boolean processDescriptions = true;

    public void execute(JarFile snomedJar) throws Exception {
        MakeRelSet oldRelSet = new MakeRelSet();
        iterateRelationships(oldRelSet);

        DateFormat dateFormat = AceDateFormat.getRf1DirectoryDateFormat();
        SortedSet<String> entryStringSet = new TreeSet<String>(new Comparator<String>() {

            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }

        });
        Enumeration<JarEntry> jarEnum = snomedJar.entries();
        while (jarEnum.hasMoreElements()) {
            JarEntry je = jarEnum.nextElement();
            entryStringSet.add(je.getName());
        }

        getLog().info(" sorted process set: " + entryStringSet);

        for (String entryName : entryStringSet) {
            JarEntry je = snomedJar.getJarEntry(entryName);
            if (je.getName().startsWith("org/snomed/") && je.getName().endsWith(".txt")) {
                int startIndex = "org/snomed/".length();
                int endIndex = startIndex + "yyyy-MM-dd".length();
                Date releaseDate = dateFormat.parse(je.getName().substring(startIndex, endIndex));
                addReleaseDate(releaseDate);
                getLog().info(" processing: " + je.getName());
                if (processConcepts && je.getName().contains("concepts")) {
                    InputStreamReader isr = new InputStreamReader(snomedJar.getInputStream(je),
                        Charset.forName("UTF-8"));
                    readConcepts(isr, releaseDate, FORMAT.SNOMED, new CountDownLatch(Integer.MAX_VALUE));
                    isr.close();
                } else if (processDescriptions && je.getName().contains("descriptions")) {
                    InputStream is = snomedJar.getInputStream(je);
                    InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
                    readDescriptions(new InputStreamReader(snomedJar.getInputStream(je)), releaseDate, FORMAT.SNOMED,
                        new CountDownLatch(Integer.MAX_VALUE));
                    isr.close();
                } else if (processRels && je.getName().contains("relationships")) {
                    InputStreamReader isr = new InputStreamReader(snomedJar.getInputStream(je),
                        Charset.forName("UTF-8"));
                    readRelationships(new InputStreamReader(snomedJar.getInputStream(je)), releaseDate, FORMAT.SNOMED,
                        new CountDownLatch(Integer.MAX_VALUE));
                    isr.close();
                }
            }
        }
        cleanupSNOMED(oldRelSet.getIntSet());
    }

    protected Object getId(StreamTokenizer st) {
        return new Long(st.sval);
    }

    protected Object getDescType(StreamTokenizer st) {
        return new Integer(st.sval);
    }

    protected Object getStatus(StreamTokenizer st) {
        return new Integer(st.sval);
    }

    protected Object getRefinability(StreamTokenizer st) {
        return new Integer(st.sval);
    }

    protected Object getCharacteristic(StreamTokenizer st) {
        return new Integer(st.sval);
    }

}
