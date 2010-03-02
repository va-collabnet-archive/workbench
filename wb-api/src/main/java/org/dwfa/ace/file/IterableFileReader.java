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
package org.dwfa.ace.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Provides a mechanism to iteratively progress through a file line by line
 * whereby various
 * implementing subclasses will produce a strongly typed output from each line.
 * <p>
 * Effectively each line of the source file will be converted to an object of
 * type T and the objects will be streamed from the file as this instance is
 * iterated.
 * <p>
 * <b>NOTE</b> If a line of the file cannot be processed a RuntimeException may
 * be raised by the implementation as {@link Iterator#next()} defines no checked
 * exceptions.
 * 
 * @param <T> The defined type output to be produced by a concrete
 *            implementation
 */
public abstract class IterableFileReader<T> implements Iterable<T> {

    protected File sourceFile;

    protected boolean hasHeader;

    protected String headerLine;

    private FileReaderIterator<T> fileReaderIterator;

    /**
     * Indicates if transactions should be used in the import
     */
    protected boolean transactional;

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public boolean getHasHeader() {
        return hasHeader;
    }

    /**
     * @param hasHeader If true the first line of the file will not be iterated.
     * @see #getHeader()
     */
    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    /**
     * If it exists, the header line will be available after iteration
     * commences.
     * 
     * @return The header line (if available) otherwise null.
     */
    public String getHeader() {
        return this.headerLine;
    }

    public long getSize() throws IOException {
        return getFileReaderIterator().getSize();
    }

    public Iterator<T> iterator() {
        try {
            return getFileReaderIterator();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private FileReaderIterator<T> getFileReaderIterator() throws IOException {
        if (fileReaderIterator == null) {
            fileReaderIterator = new FileReaderIterator<T>(sourceFile);
        }

        return fileReaderIterator;
    }

    /**
     * To be implemented by subclass.
     * 
     * @param line The next line in the file.
     * @return A actual object. Must never return null.
     *         If the line cannot be processed a runtime exception should be
     *         raised.
     * @throws RuntimeException if processing fails
     */
    protected abstract T processLine(String line);

    /**
     * The iterator instance for the reader
     */
    public class FileReaderIterator<E> implements Iterator<T> {

        protected String currentLine;

        private long size;

        protected BufferedReader reader;

        protected FileReaderIterator(File sourceFile) throws IOException {
            setSize(sourceFile);
            reader = new BufferedReader(new FileReader(sourceFile));

            if (hasHeader) {
                headerLine = reader.readLine();
            }

            currentLine = getNextLine();
        }

        protected String getNextLine() {
            try {
                String nextLine = reader.readLine();
                if (nextLine == null) {
                    reader.close();
                }
                return nextLine;
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        public boolean hasNext() {
            return (currentLine != null);
        }

        public T next() {
            String result = currentLine;
            currentLine = getNextLine();
            return processLine(result);
        }

        public void remove() {
        }

        private void setSize(File sourceFile) throws IOException {
            try {
                size = 0;
                reader = new BufferedReader(new FileReader(sourceFile));

                for (; reader.readLine() != null; size++) {
                }
            } finally {
                reader.close();
            }
        }

        public long getSize() {
            return size;
        }
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }
}
