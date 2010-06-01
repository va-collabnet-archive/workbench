/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.ace.task.util;

import static junit.framework.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Performs a diff against a 2 text files. First checks for file length equality followed by content equality.
 */
public final class TextFileDiffer {

    private final String lineSeparator;

    public TextFileDiffer() {
        lineSeparator = System.getProperty("line.separator");
    }

    public void compare(final File actualFile, final File expectedFile) throws Exception {
        compareLineCounts(actualFile, expectedFile);
        compareContents(actualFile, expectedFile);
    }

    private void compareLineCounts(final File actualFile, final File expectedFile) throws Exception {
        int actualLineCount = getLineCount(actualFile);
        int expectedLineCount = getLineCount(expectedFile);

        String message = new StringBuffer().append("Comparing ").
                append(lineSeparator).
                append("actual file -> " + actualFile).
                append(lineSeparator).
                append("with expected file -> " + expectedFile).
                append(".").
                append(lineSeparator).
                append("Actual line count is different to expected line count.").
                toString();
        assertEquals(message, actualLineCount, expectedLineCount);
    }

    private void compareContents(final File actualFile, final File expectedFile) throws Exception {
        BufferedReader actualReader = null;
        BufferedReader expectedReader = null;
        try {
            actualReader = new BufferedReader(new FileReader(actualFile));
            expectedReader = new BufferedReader(new FileReader(expectedFile));

            String expectedLine;
            String actualLine;
            int lineCount = 0;

            while ((expectedLine = expectedReader.readLine()) != null) {
                lineCount++;
                if (!expectedLine.equals(actualLine = actualReader.readLine())) {
                    String message =
                            new StringBuilder().append("Comparing ").
                            append(lineSeparator).
                            append("actual file -> " + actualFile).
                            append(lineSeparator).
                            append("with expected file -> " + expectedFile).
                            append(".").
                            append(lineSeparator).
                            append("Line ").append(lineCount).append(" is different.").
                            append(lineSeparator).
                            append("Expected -> ").
                            append(expectedLine).
                            append(lineSeparator).
                            append("Actual -> ").append(actualLine).
                            toString();
                    throw new AssertionError(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            close(actualReader);
            close(expectedReader);
        }
    }

    private int getLineCount(final File file) throws Exception {
        BufferedReader reader = null;
        int count = 0;

        try {
            reader = new BufferedReader(new FileReader(file));
            while (reader.readLine() != null) {
                count++;
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            close(reader);
        }
    }

    private void close(final BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                //do nothing.
            }
        }
    }
}
