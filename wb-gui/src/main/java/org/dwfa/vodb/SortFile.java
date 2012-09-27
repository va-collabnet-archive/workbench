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
package org.dwfa.vodb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class SortFile {

    public static interface I_ReportDups<T> extends Comparator<T> {
        public void reportDups(boolean report);
    }

    /**
     * Field order:
     * concept uuid
     * status uuid
     * primitive
     * effective data
     * path uuid
     * 
     * @author kec
     * 
     */
    public static class ConceptComparator implements I_ReportDups<String> {
        Writer dupWriter;
        boolean report = true;

        public void reportDups(boolean report) {
            this.report = report;
        }

        public ConceptComparator(File file) throws IOException {
            super();
            this.dupWriter = new BufferedWriter(new FileWriter(file));
        }

        public void close() throws IOException {
            this.dupWriter.close();
        }

        public int compare(String o1, String o2) {
            int comparison = o1.compareTo(o2);
            if (comparison == 0 && report) {
                try {
                    dupWriter.write("Error: duplicate records: \n" + o1 + "\n" + o2 + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return comparison;
        }
    }

    /**
     * Field order:
     * relationship uuid 0
     * status uuid 1
     * concept1 uuid 2
     * relationship type uuid
     * concept2 uuid
     * characteristic type uuid
     * refinability uuid
     * relationship group
     * effective date
     * path uuid
     * 
     * @author kec
     * 
     */
    public static class DescriptionComparator implements I_ReportDups<String> {
        Writer dupWriter;
        boolean report = true;

        public void reportDups(boolean report) {
            this.report = report;
        }

        public DescriptionComparator(File file) throws IOException {
            super();
            this.dupWriter = new BufferedWriter(new FileWriter(file));
        }

        public void close() throws IOException {
            this.dupWriter.close();
        }

        public int compare(String o1, String o2) {
            String[] split1 = o1.split("\t");
            String[] split2 = o2.split("\t");

            int comparison = split1[2].compareTo(split2[2]);
            if (comparison != 0) {
                return comparison;
            }
            comparison = o1.compareTo(o2);
            if (comparison == 0 && report) {
                try {
                    dupWriter.write("Error: duplicate records: \n" + o1 + "\n" + o2 + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return o1.compareTo(o2);
        }
    }

    public static class RelationshipComparator implements I_ReportDups<String> {
        Writer dupWriter;
        boolean report = true;

        public void reportDups(boolean report) {
            this.report = report;
        }

        public RelationshipComparator(File file) throws IOException {
            super();
            this.dupWriter = new BufferedWriter(new FileWriter(file));
        }

        public void close() throws IOException {
            this.dupWriter.close();
        }

        public int compare(String o1, String o2) {
            String[] split1 = o1.split("\t");
            String[] split2 = o2.split("\t");

            int comparison = split1[2].compareTo(split2[2]);
            if (comparison != 0) {
                return comparison;
            }
            comparison = o1.compareTo(o2);
            if (comparison == 0 && report) {
                try {
                    dupWriter.write("Error: duplicate records: \n" + o1 + "\n" + o2 + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return o1.compareTo(o2);
        }

    }

    public static class LineSorterComparator implements Comparator<LineSorter> {
        Comparator<String> comparator;

        public LineSorterComparator(Comparator<String> comparator) {
            super();
            this.comparator = comparator;
        }

        public int compare(LineSorter o1, LineSorter o2) {
            return comparator.compare(o1.line, o2.line);
        }

    }

    public static class LineSorter implements Comparator<LineSorter> {
        LineNumberReader reader;
        Comparator<String> comparator;
        String line;

        public LineSorter(File f, Comparator<String> comparator) throws FileNotFoundException {
            super();
            this.reader = new LineNumberReader(new FileReader(f));
            this.comparator = comparator;
        }

        public int compare(LineSorter o1, LineSorter o2) {
            return comparator.compare(o1.line, o2.line);
        }

        public boolean hasNext() throws IOException {
            if (reader.ready()) {
                line = reader.readLine();
                return true;
            }
            reader.close();
            return false;
        }

        public String getLine() {
            return line;
        }
    }

    public static void sortFile(File inputFile, File outputFile, I_ReportDups<String> comparator, int maxLinesInMemory)
            throws IOException {
        List<File> tempFiles = new ArrayList<File>();

        SortedSet<String> sortedConceptLines = new TreeSet<String>(comparator);
        LineNumberReader in = new LineNumberReader(new FileReader(inputFile));
        while (in.ready()) {
            sortedConceptLines.add(in.readLine());
            if (in.getLineNumber() % maxLinesInMemory == 0) {
                File tempFile = new File(UUID.randomUUID().toString());
                tempFiles.add(tempFile);
                Writer w = new BufferedWriter(new FileWriter(tempFile));
                for (String line : sortedConceptLines) {
                    w.write(line);
                    w.write('\n');
                }
                w.close();
                sortedConceptLines.clear();
            }
        }
        in.close();
        if (tempFiles.size() == 0) {
            Writer w = new BufferedWriter(new FileWriter(outputFile));
            for (String line : sortedConceptLines) {
                w.write(line);
                w.write('\n');
            }
            w.close();
            sortedConceptLines.clear();
        } else {
            TreeSet<LineSorter> lineSorterTree = new TreeSet<LineSorter>(new LineSorterComparator(comparator));
            for (File f : tempFiles) {
                LineSorter s = new LineSorter(f, comparator);
                if (s.hasNext()) {
                    lineSorterTree.add(s);
                }
            }
            Writer w = new BufferedWriter(new FileWriter(outputFile));
            try {
                while (true) {
                    LineSorter s = lineSorterTree.first();
                    comparator.reportDups(false);
                    lineSorterTree.remove(s);
                    comparator.reportDups(true);
                    w.write(s.line);
                    w.write('\n');
                    if (s.hasNext()) {
                        lineSorterTree.add(s);
                    }
                }
            } catch (NoSuchElementException ex) {
                // done...
            }
            w.close();
            for (File f : tempFiles) {
                f.delete();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("Sorting concepts");
        File inputFile = new File("/Users/kec/temp/smallSnomedBdb/target/generated-resources/concepts.txt");
        File sortedFile = new File("/Users/kec/temp/smallSnomedBdb/target/generated-resources/concepts-sorted.txt");
        File dupFile = new File("/Users/kec/temp/smallSnomedBdb/target/generated-resources/concepts-sorted-dups.txt");
        ConceptComparator cCompare = new ConceptComparator(dupFile);
        sortFile(inputFile, sortedFile, cCompare, 600000);
        cCompare.close();
        System.out.println("Sorting descriptions");

        inputFile = new File("/Users/kec/temp/smallSnomedBdb/target/generated-resources/descriptions.txt");
        sortedFile = new File("/Users/kec/temp/smallSnomedBdb/target/generated-resources/descriptions-sorted.txt");
        dupFile = new File("/Users/kec/temp/smallSnomedBdb/target/generated-resources/descriptions-sorted-dups.txt");
        DescriptionComparator dCompare = new DescriptionComparator(dupFile);
        sortFile(inputFile, sortedFile, dCompare, 600000);
        dCompare.close();
        System.out.println("Sorting relationships");

        inputFile = new File("/Users/kec/temp/smallSnomedBdb/target/generated-resources/relationships.txt");
        sortedFile = new File("/Users/kec/temp/smallSnomedBdb/target/generated-resources/relationships-sorted.txt");
        dupFile = new File("/Users/kec/temp/smallSnomedBdb/target/generated-resources/relationships-sorted-dups.txt");
        RelationshipComparator rCompare = new RelationshipComparator(dupFile);
        sortFile(inputFile, sortedFile, rCompare, 600000);
        rCompare.close();
        System.out.println("Complete in " + (System.currentTimeMillis() - startTime));

    }
}
