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
package org.dwfa.maven;

public class InputFileSpec {
    private String inputFile;
    private boolean skipFirstLine = false;
    private Character inputColumnDelimiter = '\t';
    private Character inputCharacterDelimiter = '"';
    private String inputEncoding = "UTF-8";
    private int debugRowStart = 0;
    private int debugRowEnd = 0;

    private I_ReadAndTransform[] columnSpecs;

    public I_ReadAndTransform[] getColumnSpecs() {
        return columnSpecs;
    }

    public void setColumnSpecs(I_ReadAndTransform[] columnSpecs) {
        this.columnSpecs = columnSpecs;
    }

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String fileName) {
        this.inputFile = fileName;
    }

    public Character getInputCharacterDelimiter() {
        return inputCharacterDelimiter;
    }

    public void setInputCharacterDelimiter(Character inputCharacterDelimiter) {
        this.inputCharacterDelimiter = inputCharacterDelimiter;
    }

    public Character getInputColumnDelimiter() {
        return inputColumnDelimiter;
    }

    public void setInputColumnDelimiter(Character inputColumnDelimiter) {
        this.inputColumnDelimiter = inputColumnDelimiter;
    }

    public String getInputEncoding() {
        return inputEncoding;
    }

    public void setInputEncoding(String inputEncoding) {
        this.inputEncoding = inputEncoding;
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("Input file: ");
        b.append(inputFile);
        b.append("\nSkip first line: ");
        b.append(skipFirstLine);
        b.append("\nColumn delimiter: ");
        if (Character.isWhitespace(inputColumnDelimiter)) {
            b.append(whiteSpaceCharToUnicode(inputColumnDelimiter));
        } else {
            b.append(inputColumnDelimiter);
        }
        b.append("\nCharacter delimiter: ");
        if (Character.isWhitespace(inputCharacterDelimiter)) {
            b.append(whiteSpaceCharToUnicode(inputCharacterDelimiter));
        } else {
            b.append(inputCharacterDelimiter);
        }
        b.append("\nEncoding: ");
        b.append(inputEncoding);
        b.append("\nTransforms: \n");
        int col = 1;
        if (columnSpecs != null) {
            for (I_ReadAndTransform t : columnSpecs) {
                b.append("  ");
                b.append(col++);
                b.append(". ");
                b.append(t);
                b.append("\n");
            }
        }
        return b.toString();
    }

    public static String whiteSpaceCharToUnicode(Character ws) {
        switch ((int) ws.charValue()) {
        case 9:
            return "'\\u0009' - HORIZONTAL TABULATION";
        case 10:
            return "'\\u000A' - LINE FEED";
        case 11:
            return "'\\u000B' - VERTICAL TABULATION";
        case 12:
            return "'\u000C' - FORM FEED";
        case 13:
            return "'\\u000D' - CARRIAGE RETURN";
        }
        return Integer.toHexString((int) ws.charValue());
    }

    public boolean isSkipFirstLine() {
        return skipFirstLine;
    }

    public boolean skipFirstLine() {
        return skipFirstLine;
    }

    public void setSkipFirstLine(boolean skipFirstLine) {
        this.skipFirstLine = skipFirstLine;
    }

    public int getDebugRowStart() {
        return debugRowStart;
    }

    public void setDebugRowStart(int debugRowStart) {
        this.debugRowStart = debugRowStart;
    }

    public int getDebugRowEnd() {
        return debugRowEnd;
    }

    public void setDebugRowEnd(int debugRowEnd) {
        this.debugRowEnd = debugRowEnd;
    }

}
